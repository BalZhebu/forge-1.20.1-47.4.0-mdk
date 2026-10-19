package com.TovidY.kunluncontinent.entity.icempress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 雪帝 —— 极寒领域的敌对生物。
 *
 * <p><b>不会自然生成</b>：{@link #checkSpawnRules} 只允许刷怪蛋与指令，
 * 同时也<b>没有</b>注册 {@code SpawnPlacements}、没有进任何 biome 的 spawner 列表，
 * 三重保险。</p>
 *
 * <h3>动画</h3>
 * <ul>
 *   <li>{@code move}（2.5s，looping）—— 由 {@link #moveState} 驱动，移动时播；</li>
 *   <li>{@code ack}（1.6s）—— 由 {@link #ackState} 驱动，命中时播一次；</li>
 *   <li>{@code phase2}（2.9s）—— 由 {@link #phase2State} 驱动，二阶段转场时播一次。</li>
 * </ul>
 *
 * <h3>AI</h3>
 * <p><b>没有仇恨时站桩</b>：没有 {@code RandomStrollGoal}，所以它不会自己溜达，
 * 只会转头（{@link RandomLookAroundGoal}）和看玩家。有目标时才由
 * {@link MeleeAttackGoal} 接管移动。</p>
 *
 * <h3>二阶段（血量 &lt; 50%）</h3>
 * <p>触发一次：禁止移动 + 无敌 + 播 {@code phase2}，{@link #PHASE2_DURATION_TICKS}
 * （60 tick = 3 秒）后粒子爆炸、解除无敌。状态写入 NBT，避免读档后重复触发。</p>
 */
public class IceEmpressEntity extends Monster {

    // ── 动画状态（客户端渲染时读取） ──
    public final AnimationState moveState = new AnimationState();
    public final AnimationState ackState = new AnimationState();
    public final AnimationState phase2State = new AnimationState();

    /** 客户端要靠它决定是否播放攻击动画（服务端置位，自动同步）。 */
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(IceEmpressEntity.class, EntityDataSerializers.BOOLEAN);

    /** 二阶段转场中（客户端据此播 {@code phase2}）。 */
    private static final EntityDataAccessor<Boolean> IS_PHASE2 =
            SynchedEntityData.defineId(IceEmpressEntity.class, EntityDataSerializers.BOOLEAN);

    /** ack 动画时长 1.6s = 32 tick。 */
    private static final int ATTACK_ANIM_TICKS = 32;

    /** 二阶段持续 60 tick = 3 秒（phase2 动画 2.9s，正好收尾）。 */
    private static final int PHASE2_DURATION_TICKS = 60;

    /** 二阶段触发阈值：当前血量低于上限的 50%。 */
    private static final float PHASE2_HEALTH_RATIO = 0.5F;

    /** 雪帝有仇恨时的禁飞半径（格）。 */
    public static final double FLIGHT_SUPPRESS_RADIUS = 150.0D;

    /**
     * 服务端存活中的雪帝（用于禁飞判定）。
     *
     * <p>用静态集合而不是每次 {@code getEntitiesOfClass} 扫 150 格：150 格 = 300×300 方块
     * ≈ 19×19 个区块，每 tick 每玩家扫一遍太贵；这里集合里通常只有 0~2 个元素，
     * 逐个比距离就够了。</p>
     *
     * <p>用 <b>WeakHashMap 支撑的 Set</b>：区块卸载走的是 {@code Entity#setRemoved}
     * （final，改不了），这时 {@link #remove(Entity.RemovalReason)} 不会触发，
     * 弱引用能保证没人再引用它时能正常被回收，不会因为区块卸载泄漏。</p>
     */
    private static final Set<IceEmpressEntity> SERVER_BOSSES =
            Collections.newSetFromMap(new WeakHashMap<>());

    private int attackTimer = 0;

    /** 二阶段剩余 tick（>0 表示正在转场）。 */
    private int phase2Timer = 0;

    /** 二阶段是否已经触发过（持久化，避免读档重复触发）。 */
    private boolean phase2Done = false;

    /**
     * 血条名字的翻译键（同时也是「这根血条是谁的」的标记）。
     *
     * <h3>为什么不用实体显示名</h3>
     * <p>血条只能通过 {@code ClientboundBossEventPacket} 发到客户端，客户端还原出来的一律是
     * {@code LerpingBossEvent}，拿不到实体类型，也就 {@code instanceof} 不出来。
     * 于是固定用一个翻译键当标记：客户端渲染器（{@code IceEmpressBossBarRenderer}）
     * 比对 {@code getName()} 的 {@code TranslatableContents} 键即可精确识别。</p>
     *
     * <p>因此血条名字<b>不跟随命名牌</b>（名牌仍会显示玩家起的名字，血条始终是「雪帝」）。
     * 这是有意为之：一旦跟随改名，键就变成了字面量，自定义贴图会失效。</p>
     */
    public static final String BOSS_BAR_KEY = "boss.kunluncontinent.ice_empress";

    /**
     * BOSS 血条（服务端驱动，只在看得见本实体的玩家身上显示）。
     *
     * <p>与 {@code DemonWhaleEntity} 同一套写法：{@link #startSeenByPlayer} /
     * {@link #stopSeenByPlayer} 增删玩家，{@link #tick} 里刷进度。
     * {@code ServerBossEvent#setProgress} 内部会比对旧值，值没变不会发包，
     * 所以每 tick 调一次也不会刷屏。</p>
     *
     * <p>真正画出来的是客户端 {@code render/IceEmpressBossBarRenderer}（自定义贴图），
     * 原版那根由 {@code CustomizeGuiOverlayEvent.BossEventProgress} 取消掉。
     * 这里的颜色 / 遮罩只影响「是否压暗天空」等原版附带效果。</p>
     */
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable(BOSS_BAR_KEY),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.PROGRESS
    ).setDarkenScreen(true);

    public IceEmpressEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                // 速度较慢：僵尸 0.23、玩家行走 0.1，这里 0.19（配合 MeleeAttackGoal 的 1.0 倍率）
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // 二阶段期间禁止追击：转场时它必须站着放技能
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            public boolean canUse() {
                return !isPhase2Active() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isPhase2Active() && super.canContinueToUse();
            }
        });

        // ⚠️ 故意不加 RandomStrollGoal：没有仇恨时站桩不动（用户明确要求）。
        //    RandomLookAroundGoal 只转头、不位移，保留它避免站得像块木头。
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
        this.entityData.define(IS_PHASE2, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            return;
        }

        // 注册到禁飞判定集合（只在真正 tick 时注册，避免 create 后立刻丢弃的实例泄漏）
        if (!SERVER_BOSSES.contains(this)) {
            SERVER_BOSSES.add(this);
        }

        // 血条进度（setProgress 内部比对旧值，未变化不发包）
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.attackTimer > 0 && --this.attackTimer <= 0) {
            // 攻击动画播完，复位同步标记 → 客户端随之停止
            this.entityData.set(IS_ATTACKING, false);
        }

        this.tickPhase2();
    }

    // ==================== 二阶段 ====================

    private void tickPhase2() {
        if (this.phase2Timer > 0) {
            if (--this.phase2Timer <= 0) {
                this.endPhase2();
            } else {
                // 兜底：绕过 hurt 的扣血（比如直接 setHealth(0) 的斩杀类技能）也抬回 50%
                float gate = this.getMaxHealth() * PHASE2_HEALTH_RATIO;
                if (this.getHealth() < gate) {
                    this.setHealth(gate);
                }

                // 站桩：清寻路 + 掐掉水平位移（垂直保留，免得卡在半空）
                this.getNavigation().stop();
                Vec3 motion = this.getDeltaMovement();
                if (motion.x != 0.0D || motion.z != 0.0D) {
                    this.setDeltaMovement(0.0D, motion.y, 0.0D);
                }
            }
            return;
        }

        if (!this.phase2Done && this.getHealth() < this.getMaxHealth() * PHASE2_HEALTH_RATIO) {
            this.startPhase2();
        }
    }

    private void startPhase2() {
        this.phase2Done = true;
        this.phase2Timer = PHASE2_DURATION_TICKS;
        this.entityData.set(IS_PHASE2, true);
        this.setInvulnerable(true);   // 免疫几乎全部伤害（BYPASSES_INVULNERABILITY 除外）

        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        this.playSound(SoundEvents.WITHER_SPAWN, 2.0F, 1.4F);
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 c = this.position();
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    c.x, c.y + this.getBbHeight() * 0.5D, c.z,
                    80, 1.6D, 1.2D, 1.6D, 0.05D);
        }
    }

    private void endPhase2() {
        this.entityData.set(IS_PHASE2, false);
        this.setInvulnerable(false);

        // 粒子爆炸：只用粒子 + 音效，不生成 Explosion 实体（不破坏方块）
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 c = this.position();
            double cy = c.y + this.getBbHeight() * 0.5D;

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, cy, c.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, c.x, cy, c.z, 120, 1.8D, 1.4D, 1.8D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, c.x, cy, c.z, 60, 1.2D, 1.0D, 1.2D, 0.18D);

            // 把近处玩家往外推一下，纯粹是表现，不造成伤害
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0D))) {
                Vec3 dir = player.position().subtract(c);
                if (dir.lengthSqr() < 1.0E-4D) {
                    dir = new Vec3(0.0D, 0.0D, 1.0D);
                }
                dir = dir.normalize();
                player.push(dir.x * 1.4D, 0.55D, dir.z * 1.4D);
                player.hurtMarked = true;
            }
        }

        this.playSound(SoundEvents.GENERIC_EXPLODE, 3.0F, 0.9F);
    }

    /** 二阶段转场中（禁止移动 / 攻击 / 受伤）。 */
    public boolean isPhase2Active() {
        return this.phase2Timer > 0;
    }

    // ==================== 伤害拦截（锁血） ====================

    /**
     * 二阶段期间无敌。
     *
     * <h3>为什么只靠 {@code setInvulnerable(true)} 不够</h3>
     * <p>原版 {@code Entity#isInvulnerableTo} 的判定是</p>
     * <pre>
     *   invulnerable &amp;&amp; !source.is(BYPASSES_INVULNERABILITY) &amp;&amp; <b>!source.isCreativePlayer()</b>
     * </pre>
     * <p><b>创造模式玩家的攻击会直接绕过 invulnerable</b>。玩家在创造模式里测 BOSS 时
     * 一刀就能穿过去 —— 这就是"到了 50% 依旧掉血"的原因。这里把创造模式那条也一起拦掉，
     * 只保留 {@code /kill} 这类 {@code BYPASSES_INVULNERABILITY} 的伤害能生效。</p>
     */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (this.isPhase2Active() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    /**
     * 伤害入口：<b>只负责二阶段无敌</b>。
     *
     * <p>50% 锁血<b>不在这里</b>做 —— 这一层的 {@code amount} 还是原版攻击力（几到几十），
     * 本模组的最终伤害是 {@code CombatEventHandler} 在 {@code LivingHurtEvent} 里用
     * 「物攻 + 原版伤害」重算出来的（几百上千）。在这里判 50% 阈值永远判不到，
     * 这正是之前"到了 50% 还在掉血"的真正原因。锁血见 {@link #actuallyHurt}。</p>
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return super.hurt(source, amount);
        }

        // 二阶段：绝对锁血（连创造模式玩家的攻击也挡，见 isInvulnerableTo 的说明）
        if (this.isPhase2Active() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    /**
     * 50% 锁血：<b>先让伤害打实，扣完血再把血量抬回 50%</b>。
     *
     * <h3>为什么必须在这里</h3>
     * <p>{@code actuallyHurt} 是唯一拿得到"最终伤害"的地方：它内部依次执行
     * {@code LivingHurtEvent}（{@code CombatEventHandler} 在这里把伤害重算成最终值）
     * → 护甲吸收 → {@code LivingDamageEvent} → 最后才 {@code setHealth(getHealth() - f1)}。</p>
     *
     * <p>在 {@code super} 之后检查一次，可以同时满足两个要求：</p>
     * <ul>
     *   <li><b>伤害照常生效</b>：伤害数字、仇恨、击退、吸血、
     *       {@code CombatDamageBackupHandler} 兜底全部按全额走，
     *       不会因为锁血而显示一个被砍过的数字；</li>
     *   <li><b>血量不许跌破 50%</b>：抬回正好 50% 并立刻进入二阶段。</li>
     * </ul>
     *
     * <p>{@code LivingEntity#hurt} 是在调用完 {@code actuallyHurt} <b>之后</b>才判断
     * {@code isDeadOrDying()} 并触发 {@code die()}，所以哪怕这一击是致死量，
     * 也会被抬回 50% 而不是直接死亡 —— 一击秒杀同样跳不过二阶段。</p>
     */
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float before = this.getHealth();
        super.actuallyHurt(source, amount);

        if (this.level().isClientSide || this.phase2Done) {
            return;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;   // /kill、虚空等照常生效
        }

        float gate = this.getMaxHealth() * PHASE2_HEALTH_RATIO;
        if (before > gate && this.getHealth() <= gate) {
            this.setHealth(gate);   // 精确锁在 50%
            this.startPhase2();     // 进入 3 秒无敌转场
        }
    }

    // ==================== 禁飞领域 ====================

    /**
     * 玩家是否处于「有仇恨的雪帝」的禁飞领域内（半径 {@link #FLIGHT_SUPPRESS_RADIUS}）。
     *
     * <p>由 {@code PWPlayerTickEvent#updatePlayerFly} 每 tick 调用。集合通常为空或一两个元素，
     * 只做一次距离平方比较，开销可以忽略；玩家离开范围后这里返回 {@code false}，
     * 原来的飞行判定（精神力 &gt; 5000 可飞）会自动把飞行权还回去。</p>
     *
     * <p>只有<b>有仇恨</b>（{@code getTarget() != null}）才生效 —— 没有目标的雪帝
     * 只是个站桩雕像，不该影响路过的人。</p>
     */
    public static boolean isFlightSuppressedNear(Player player) {
        if (player == null || player.level().isClientSide || SERVER_BOSSES.isEmpty()) {
            return false;
        }
        double maxDistSqr = FLIGHT_SUPPRESS_RADIUS * FLIGHT_SUPPRESS_RADIUS;
        for (IceEmpressEntity boss : SERVER_BOSSES) {
            if (boss.isRemoved() || !boss.isAlive()) continue;
            if (boss.level() != player.level()) continue;
            if (boss.getTarget() == null) continue;
            if (boss.distanceToSqr(player) <= maxDistSqr) {
                return true;
            }
        }
        return false;
    }

    /**
     * 死亡 / 丢弃 / 换维度时从禁飞集合里摘掉。
     *
     * <p>不能覆盖 {@code setRemoved} —— 它在 1.20.1 里是 {@code final}
     * （编译会报"无法覆盖 Entity 中的 setRemoved"）。区块卸载走的就是 {@code setRemoved}，
     * 那条路靠 WeakHashMap 兜底。</p>
     */
    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        SERVER_BOSSES.remove(this);
    }

    // ==================== 动画 ====================

    private void setupAnimationStates() {
        if (this.isMoving()) {
            this.moveState.startIfStopped(this.tickCount);
        } else {
            this.moveState.stop();
        }

        if (this.entityData.get(IS_ATTACKING)) {
            this.ackState.startIfStopped(this.tickCount);
        } else {
            this.ackState.stop();
        }

        if (this.entityData.get(IS_PHASE2)) {
            this.phase2State.startIfStopped(this.tickCount);
        } else {
            this.phase2State.stop();
        }
    }

    private boolean isMoving() {
        return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
    }

    /** 近战命中时触发攻击动画（必须在 super 之前置位，否则同步会晚一 tick）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (!this.level().isClientSide) {
            this.attackTimer = ATTACK_ANIM_TICKS;
            this.entityData.set(IS_ATTACKING, true);
        }
        return super.doHurtTarget(target);
    }

    // ==================== 存档 ====================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Phase2Done", this.phase2Done);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.phase2Done = tag.getBoolean("Phase2Done");
    }

    // ==================== BOSS 血条：与玩家可见性绑定 ====================

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // 注意：这里**故意不**覆盖 setCustomName —— 血条名字要一直保持 BOSS_BAR_KEY，
    // 否则玩家用命名牌改名后客户端的贴图识别会失效。名牌不受影响，仍显示自定义名。

    // ==================== 生成与音效 ====================

    /**
     * 不自然生成。
     *
     * <p>只放行刷怪蛋与 {@code /summon}；自然生成（{@code NATURAL}）与刷怪笼都会被拒绝。
     * 这比只依赖「不注册 SpawnPlacements」更保险 —— 后者在别的 mod 往 biome 里塞刷怪项时会被绕过。</p>
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.MOB_SUMMONED;
    }

    /** 雪帝本身就是极寒生物，不会被细雪冻伤。 */
    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PLAYER_HURT_FREEZE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT_FREEZE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.POWDER_SNOW_STEP, 0.15F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }
}
