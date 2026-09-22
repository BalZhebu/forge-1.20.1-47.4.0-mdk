package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.concurrent.atomic.AtomicReference;

public class SnowDemonEntity extends Monster {

    public SnowDemonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ── 动画状态 ──
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();

    // ── 同步数据：客户端要靠它决定播哪个动画 ──
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(SnowDemonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JUMPING =
            SynchedEntityData.defineId(SnowDemonEntity.class, EntityDataSerializers.BOOLEAN);

    /** 普通攻击动画时长（ack = 0.85s → 17 tick） */
    private static final int ATTACK_ANIM_TICKS = 17;
    /** 跳跃技能动画时长（jump = 1.6s → 32 tick） */
    private static final int JUMP_ANIM_TICKS = 32;

    private int attackTimer = 0;
    private int jumpTimer = 0;

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    /** 是否正在播放「跳起砸地」技能动画。刻意不叫 isJumping()，避免和父类潜在的同名方法撞车。 */
    public boolean isJumpAnimPlaying() {
        return this.entityData.get(IS_JUMPING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
        this.entityData.define(IS_JUMPING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 9.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }


    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            // 服务端倒计时：结束时把动画标记复位，客户端随之停止播放
            if (this.attackTimer > 0 && --this.attackTimer <= 0) {
                this.entityData.set(IS_ATTACKING, false);
            }
            if (this.jumpTimer > 0 && --this.jumpTimer <= 0) {
                this.entityData.set(IS_JUMPING, false);
            }
        }
    }

    private void setupAnimationStates() {
        // 移动：走路循环
        if (this.isMoving()) {
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
        }

        // 普通攻击（右拳）
        if (this.isAttacking()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
        } else {
            this.attackAnimationState.stop();
        }

        // 技能：跳起砸地
        if (this.isJumpAnimPlaying()) {
            this.jumpAnimationState.startIfStopped(this.tickCount);
        } else {
            this.jumpAnimationState.stop();
        }
    }

    private boolean isMoving() {
        return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
    }

    /**
     * 近战命中时触发普通攻击动画。
     * 必须放在 super 之前，否则同步标记会晚一 tick。
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (!this.level().isClientSide) {
            this.attackTimer = ATTACK_ANIM_TICKS;
            this.entityData.set(IS_ATTACKING, true);
        }
        return super.doHurtTarget(target);
    }

    /**
     * 播放「跳起砸地」技能动画（1.6s）。
     * 注意：这里只负责动画表现；跳跃位移与落地范围伤害属于技能逻辑，
     * 由外部（自定义 Goal / 指令 / 事件）调用本方法后自行实现。
     */
    public void playJumpAnimation() {
        if (!this.level().isClientSide && this.jumpTimer <= 0) {
            this.jumpTimer = JUMP_ANIM_TICKS;
            this.entityData.set(IS_JUMPING, true);
        }
    }

    public static boolean checkSnowDemonSpawnRules(EntityType<SnowDemonEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBiome(pos).is(ModDimensions.POLAR_ICE_BIOME)) {
            return false;
        }
        int surfaceHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() < surfaceHeight - 2) {
            return false;
        }
        return level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), entityType)
                && level.getRawBrightness(pos, 0) >= 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && this.isAlive()) {
            if (this.getTarget() != null && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                this.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    if (attr.getNianxian() > 5000) {
                        breakEnvironment();
                    }
                });
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);
        if (this.random.nextFloat() < 0.1F) {
            this.spawnAtLocation(ModItems.RINSEI_INGOT.get());
        }
    }

    private void breakEnvironment() {
        AABB aabb = this.getBoundingBox().inflate(0.1D);
        int yMin = Mth.floor(this.getY() + 0.5D);
        int yMax = Mth.floor(aabb.maxY);
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(aabb.minX), yMin, Mth.floor(aabb.minZ),
                Mth.floor(aabb.maxX), yMax, Mth.floor(aabb.maxZ))) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(this.level(), pos) <= 2.0F) {
                this.level().destroyBlock(pos, true, this);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        this.refreshDimensions();
    }
}
