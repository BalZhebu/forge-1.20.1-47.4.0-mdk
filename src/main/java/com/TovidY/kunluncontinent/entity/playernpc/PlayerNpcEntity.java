package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2COpenNpcDialogPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public class PlayerNpcEntity extends PathfinderMob implements Merchant {

    private boolean isSparring = false;                  // 是否处于”切磋”状态
    @Nullable private ServerPlayer sparringPlayer;       // 记录正在切磋的玩家
    @Nullable private ServerPlayer tradePlayer;         // 当前交易玩家
    private MerchantOffers offers;                      // 交易配方表
    private boolean traded = false;                      // 是否与玩家完成过交易
    private long spawnTime = 0L;                        // 生成时的服务端 tick 数

    private final PlayerAttributeCapability soulCapability = new PlayerAttributeCapability();

    private static final EntityDataAccessor<Integer> SKIN_INDEX =
            SynchedEntityData.defineId(PlayerNpcEntity.class, EntityDataSerializers.INT);

    public static final int MAX_SKIN_COUNT = 3;

    @Nullable private ServerPlayer dialogPlayer;

    public PlayerNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public PlayerAttributeCapability getSoulCapability() {
        return this.soulCapability;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND, true);
        return super.doHurtTarget(target);
    }

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (NpcSkinRegistry.getSkinCount() > 0) {
            this.setSkinIndex(this.random.nextInt(NpcSkinRegistry.getSkinCount()));
        }
        // 根据维度生成等级，记录生成时间
        int npcLevel;
        if (level instanceof ServerLevel serverLevel) {
            npcLevel = getSpawnLevel(serverLevel, this.random);
            this.spawnTime = serverLevel.getGameTime();
        } else {
            npcLevel = 1 + this.random.nextInt(99);
        }
        NpcSoulGenerator.initNpcSoulData(this.soulCapability, npcLevel);
        this.recalculateNpcStats();
        return data;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (this.isSparring) {
                    player.sendSystemMessage(Component.literal("§c[" + this.getName().getString() + "] §f专注应对，切勿分心！"));
                    return InteractionResult.SUCCESS;
                }
                this.setDialogPlayer(serverPlayer);
                this.getNavigation().stop();
                this.getLookControl().setLookAt(serverPlayer, 180.0F, 180.0F);
                NetworkHandler.INSTANCE.sendTo(
                        new S2COpenNpcDialogPacket(this.getId()),
                        serverPlayer.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * 汇总 NPC 魂环属性并更新至实体 Attributes
     */
    public void recalculateNpcStats() {
        if (this.soulCapability == null) return;

        double totalHp = this.soulCapability.getMaxshengming();
        double totalAtk = this.soulCapability.getGongji();

        var maxHpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);

        if (maxHpAttr != null && totalHp > 0) {
            maxHpAttr.setBaseValue(totalHp);
            this.setHealth((float) totalHp);
        }

        if (attackAttr != null && totalAtk > 0) {
            attackAttr.setBaseValue(totalAtk);
        }
    }

    // ------------------- 切磋核心机制 -------------------

    /**
     * 开启切磋模式
     */
    public void startSparring(ServerPlayer player) {
        this.isSparring = true;
        this.sparringPlayer = player;
        this.setTarget(player);
        this.setLastHurtByMob(player);
        this.getNavigation().moveTo(player, 1.25D);
        player.sendSystemMessage(Component.literal("§c[" + this.getName().getString() + "] §f领教了！看招！"));
    }

    /**
     * 判断伤害来源是否来自切磋的玩家
     */
    private ServerPlayer getAttackingSparringPlayer(DamageSource source) {
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();

        if (attacker instanceof ServerPlayer p && p == this.sparringPlayer) return p;
        if (direct instanceof ServerPlayer p && p == this.sparringPlayer) return p;

        return this.isSparring ? this.sparringPlayer : null;
    }

    /**
     * 拦截致死伤害（玩家攻击 NPC 认输）
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isSparring) {
            ServerPlayer player = getAttackingSparringPlayer(source);
            if (player != null && (this.getHealth() - amount <= 0.5F || amount >= this.getHealth())) {
                this.stopSparringWithDefeat(player);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * 防死兜底（拦截致死伤害）
     */
    @Override
    public void die(DamageSource cause) {
        if (this.isSparring) {
            ServerPlayer player = getAttackingSparringPlayer(cause);
            if (player != null) {
                this.stopSparringWithDefeat(player);
                return;
            }
        }
        super.die(cause);
    }

    /**
     * NPC 战败/认输
     */
    private void stopSparringWithDefeat(ServerPlayer player) {
        this.isSparring = false;

        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.getNavigation().stop();
        this.setDeltaMovement(0, 0, 0);

        this.setHealth(this.getMaxHealth());
        this.clearFire();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        if (player != null) {
            player.sendSystemMessage(Component.literal("§a[" + this.getName().getString() + "] §f甘拜下风！阁下身手不凡，多谢高抬贵手！"));
            // 根据NPC等级掉落钱袋
            this.dropSparringReward(player);
        }

        this.sparringPlayer = null;
    }

    /**
     * 切磋战败奖励：根据NPC等级随机掉落钱袋。
     * 小钱袋保底1~2个；中钱袋概率随等级增长（30级5%，89级45%）；
     * 大钱袋60级以上开始有概率（10%~40%），90级以上必定掉落1~2个。
     */
    private void dropSparringReward(ServerPlayer player) {
        RandomSource rand = this.random;
        int level = this.soulCapability.getDengji();

        // 小钱袋保底1~2个
        int smallCount = 1 + rand.nextInt(2);
        for (int i = 0; i < smallCount; i++) {
            spawnAtLocation(ModItems.SMALL_MONEY_BAG.get());
        }
        // 中钱袋：30级起有概率，5%~45%
        if (level >= 30 && rand.nextInt(100) < Math.min(45, level - 30)) {
            int middleCount = 1 + rand.nextInt(2);
            for (int i = 0; i < middleCount; i++) {
                spawnAtLocation(ModItems.MIDDLE_MONEY_BAG.get());
            }
        }
        // 大钱袋：60级起有概率，90级必掉
        if (level >= 60) {
            int bigChance = level >= 90 ? 100 : Math.min(40, (level - 60));
            if (rand.nextInt(100) < bigChance) {
                int bigCount = level >= 90 ? 1 + rand.nextInt(2) : 1;
                for (int i = 0; i < bigCount; i++) {
                    spawnAtLocation(ModItems.BIG_MONEY_BAG.get());
                }
            }
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);
        if (this.isSparring && victim instanceof ServerPlayer player) {
            this.stopSparringWithVictory(player);
        }
        return result;
    }

    /**
     * NPC 胜利逻辑
     */
    private void stopSparringWithVictory(ServerPlayer player) {
        this.isSparring = false;

        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.getNavigation().stop();
        this.setDeltaMovement(0, 0, 0);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        if (player != null) {
            player.sendSystemMessage(Component.literal("§e[" + this.getName().getString() + "] §f承让了！阁下的功夫还需要多加练习啊！"));
        }

        this.sparringPlayer = null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.dialogPlayer != null) {
                if (!this.dialogPlayer.isAlive() || this.dialogPlayer.isRemoved() || this.distanceToSqr(this.dialogPlayer) > 64.0D) {
                    this.dialogPlayer = null;
                }
            }

            if (this.isSparring && this.sparringPlayer != null) {
                if (this.getTarget() != this.sparringPlayer) {
                    this.setTarget(this.sparringPlayer);
                }

                if (this.sparringPlayer.isDeadOrDying() || this.sparringPlayer.isRemoved()) {
                    this.stopSparringWithVictory(this.sparringPlayer);
                }
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtDialogPlayerGoal(this));
        this.goalSelector.addGoal(2, new NpcSoulSkillGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> this.isSparring()));
    }

    // ------------------- 交易系统 -------------------

    public void openTradeMenu(ServerPlayer player) {
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradePlayer = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        // 开启交易界面，标记已交易，永不消失
        if (player != null) {
            this.traded = true;
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradePlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            int npcLevel = this.soulCapability.getDengji();
            this.offers = NpcTradeCatalog.generate(npcLevel);
        }
        return this.offers;
    }

    @Override public void overrideOffers(MerchantOffers offers) { this.offers = offers; }
    @Override public void notifyTrade(MerchantOffer offer) { offer.increaseUses(); }
    @Override public void notifyTradeUpdated(ItemStack stack) { }
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) { }
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return this.level().isClientSide; }

    // ------------------- NBT 与数据同步 -------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_INDEX, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SkinIndex", this.getSkinIndex());
        compound.putBoolean("IsSparring", this.isSparring);
        compound.put("NpcSoulData", this.soulCapability.serializeNBT());
        compound.putBoolean("Traded", this.traded);
        compound.putLong("SpawnTime", this.spawnTime);
        if (this.offers != null) {
            compound.put("Offers", this.offers.createTag());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("NpcSoulData")) {
            CompoundTag soulTag = compound.getCompound("NpcSoulData");
            this.soulCapability.deserializeNBT(soulTag);

            if (this.soulCapability.getWuhunListsname() != null && !this.soulCapability.getWuhunListsname().isEmpty()) {
                this.soulCapability.setHunhuankuaiguan(0);
            }

            refreshNpcAttributes();
        }

        this.traded = compound.getBoolean("Traded");
        this.spawnTime = compound.getLong("SpawnTime");

        if (compound.contains("Offers")) {
            this.offers = new MerchantOffers(compound.getCompound("Offers"));
        }
    }

    public void refreshNpcAttributes() {
        if (this.soulCapability == null) return;
        double totalHp = this.soulCapability.getMaxshengming();
        double totalAtk = this.soulCapability.getGongji();

        var hpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        var atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);

        if (hpAttr != null && totalHp > 0) {
            hpAttr.setBaseValue(totalHp);
            this.setHealth((float) totalHp);
        }

        if (atkAttr != null && totalAtk > 0) {
            atkAttr.setBaseValue(totalAtk);
        }
    }

    @Nullable
    public ServerPlayer getDialogPlayer() {
        return this.dialogPlayer;
    }

    public void setDialogPlayer(@Nullable ServerPlayer player) {
        this.dialogPlayer = player;
    }

    public boolean isSparring() {
        return this.isSparring;
    }

    @Override
    public Component getName() {
        return NpcSkinRegistry.getName(this.getSkinIndex());
    }

    public int getSkinIndex() {
        return this.entityData.get(SKIN_INDEX);
    }

    public void setSkinIndex(int index) {
        this.entityData.set(SKIN_INDEX, index);
    }

    /**
     * 自然生成检测。
     *
     * <p>主世界（OVERWORLD）：1-40级，5%概率出50-99级
     * <p>下界（NETHER）：    30-70级，5%概率出80-99级
     * <p>末地（END）：       50-99级
     * <p>其他维度：          1-99级（全段随机）
     *
     * <p>生成条件：
     * <ul>
     *   <li>地面方块支持（grass/dirt/stone/sand 等）
     *   <li>昼夜均可生成（无亮度限制）
     *   <li>高度 ≥ 0
     * </ul>
     */

    @Override
    public void checkDespawn() {
        if (this.isSparring() || this.traded || this.isPersistenceRequired()) {
            this.noActionTime = 0;
            return;
        }
        if (this.level().getGameTime() - this.spawnTime < DESPAWN_TICKS) {
            this.noActionTime = 0;
            return;
        }
        super.checkDespawn();
    }

    public static boolean checkSpawnRules(EntityType<PlayerNpcEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type);
    }

    /** 未交易 NPC 的最大存活 tick 数，默认 10 分钟（6000 ticks） */
    public static final long DESPAWN_TICKS = 6000L;

    /**
     * 根据维度与玩家等级区间计算 NPC 出生等级。
     * 低概率事件（稀有高等级）在主世界/下界独立判定。
     */
    public static int getSpawnLevel(ServerLevel level, RandomSource random) {
        int baseMin, baseMax, rareMin, rareChance;
        var dimKey = level.dimension();

        if (dimKey == Level.END) {
            // 末地：50-99 级，无稀有事件
            baseMin = 50; baseMax = 99;
            rareMin = 0;  rareChance = 0;
        } else if (dimKey == Level.NETHER) {
            // 下界：30-70 级，5% 出 80-99 级
            baseMin = 30; baseMax = 70;
            rareMin = 80; rareChance = 5;
        } else if (dimKey == Level.OVERWORLD) {
            // 主世界：1-40 级，5% 出 50-99 级
            baseMin = 1;  baseMax = 40;
            rareMin = 50; rareChance = 5;
        } else {
            // 其他自定义维度：1-99 级，无稀有事件
            baseMin = 1;  baseMax = 99;
            rareMin = 0;  rareChance = 0;
        }
        if (rareChance > 0 && random.nextInt(100) < rareChance) {
            return rareMin + random.nextInt(99 - rareMin + 1);
        }
        return baseMin + random.nextInt(baseMax - baseMin + 1);
    }

}