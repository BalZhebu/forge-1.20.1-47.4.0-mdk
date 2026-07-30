package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class PlayerNpcEntity extends PathfinderMob implements Merchant {

    private boolean isSparring = false;         // 是否处于“切磋”状态
    @Nullable private ServerPlayer sparringPlayer; // 记录正在切磋的玩家，防止 lost target
    @Nullable private ServerPlayer tradePlayer;  // 当前交易玩家
    private MerchantOffers offers;               // 交易配方表

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
    @Nullable
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (NpcSkinRegistry.getSkinCount() > 0) {
            this.setSkinIndex(this.random.nextInt(NpcSkinRegistry.getSkinCount()));
        }
        int randomLevel = 1 + this.random.nextInt(99);
        NpcSoulGenerator.initNpcSoulData(this.soulCapability, randomLevel);
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
        PlayerAttributeCapability cap = this.getSoulCapability();
        if (cap == null) return;

        // 1. 直接读取 NpcSoulData 计算好的总生命与总攻击
        double totalHp = cap.getMaxshengming(); // 例如 NBT 里的 1609.2f
        double totalAtk = cap.getGongji();      // 例如 NBT 里的 515.5f

        // 2. 注入 MC 生物属性表
        var maxHpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);

        if (maxHpAttr != null && totalHp > 0) {
            maxHpAttr.setBaseValue(totalHp);
            this.setHealth((float) totalHp); // 保持满血
        }

        if (attackAttr != null && totalAtk > 0) {
            attackAttr.setBaseValue(totalAtk);
        }
    }

    // ------------------- 切磋核心机制优化 -------------------

    /**
     * 开启切磋模式
     */
    public void startSparring(ServerPlayer player) {
        this.isSparring = true;
        this.sparringPlayer = player;
        this.setTarget(player); // 赋予仇恨，主动攻击玩家
        player.sendSystemMessage(Component.literal("§c[" + this.getName().getString() + "] §f领教了！看招！"));
    }

    /**
     * 辅助方法：判断伤害来源是否来自切磋的玩家（包括近战、弓箭、投掷物等）
     */
    private ServerPlayer getAttackingSparringPlayer(DamageSource source) {
        Entity attacker = source.getEntity(); // 真实攻击者（如发射箭矢的玩家）
        Entity direct = source.getDirectEntity(); // 直接伤害实体（如箭矢本身）

        if (attacker instanceof ServerPlayer p && p == this.sparringPlayer) return p;
        if (direct instanceof ServerPlayer p && p == this.sparringPlayer) return p;

        // 如果是切磋中，哪怕来源检测不到（比如摔落/点燃），只要处于切磋状态，也优先返回记录的切磋玩家
        return this.isSparring ? this.sparringPlayer : null;
    }

    /**
     * 第一道防线：拦截致死伤害
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isSparring) {
            ServerPlayer player = getAttackingSparringPlayer(source);
            // 如果受到伤害后生命值将低于 1.0F（致命伤）
            if (player != null && (this.getHealth() - amount <= 0.5F || amount >= this.getHealth())) {
                this.stopSparringWithDefeat(player);
                return false; // 彻底取消该伤害
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * 第二道防线：兜底拦截 die()（防止极高伤害或MOD伤害绕过 hurt）
     */
    @Override
    public void die(DamageSource cause) {
        if (this.isSparring) {
            ServerPlayer player = getAttackingSparringPlayer(cause);
            if (player != null) {
                // 强制阻止死亡流程，恢复满血并触发认输
                this.stopSparringWithDefeat(player);
                return; // 不调用 super.die()，防止实体真正死亡！
            }
        }
        super.die(cause);
    }

    /**
     * NPC 战败/认输时的动作与反馈
     */
    private void stopSparringWithDefeat(ServerPlayer player) {
        this.isSparring = false;

        // 1. 彻底清空仇恨与目标，停止当前寻路和移动
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.getNavigation().stop();
        this.setDeltaMovement(0, 0, 0); // 瞬间停下

        // 2. 恢复满血与状态
        this.setHealth(this.getMaxHealth());
        this.clearFire(); // 清除身上的火

        // 3. 动作与音效反馈
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        // 4. 发送认输台词
        if (player != null) {
            player.sendSystemMessage(Component.literal("§a[" + this.getName().getString() + "] §f甘拜下风！阁下身手不凡，多谢高抬贵手！"));

            // 5. 给予奖励
            ItemStack reward = new ItemStack(Items.DIRT, 1);
            if (!player.getInventory().add(reward)) {
                this.spawnAtLocation(reward);
            }
        }

        this.sparringPlayer = null; // 重置切磋玩家引用
    }

    /**
     * 当 NPC 击杀目标实体时触发（用于检测玩家被 NPC 击败）
     */
    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);
        if (this.isSparring && victim instanceof ServerPlayer player) {
            this.stopSparringWithVictory(player);
        }
        return result;
    }

    /**
     * NPC 击败玩家时的挑衅与复位逻辑
     */
    private void stopSparringWithVictory(ServerPlayer player) {
        this.isSparring = false;

        // 清理仇恨与移动
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.getNavigation().stop();
        this.setDeltaMovement(0, 0, 0);

        // 播放胜利/挑衅音效，发送挑衅台词
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        if (player != null) {
            player.sendSystemMessage(Component.literal("§e[" + this.getName().getString() + "] §f承让了！阁下的功夫还需要多加练习啊！"));
        }

        this.sparringPlayer = null;
    }

    /**
     * 兜底检测：如果玩家在切磋中死亡或离开
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.dialogPlayer != null) {
                if (!this.dialogPlayer.isAlive() || this.dialogPlayer.isRemoved() || this.distanceToSqr(this.dialogPlayer) > 64.0D) {
                    this.dialogPlayer = null;
                }
            }
            if (this.isSparring) {
                if (this.sparringPlayer != null) {
                    if (this.sparringPlayer.isDeadOrDying() || this.sparringPlayer.isRemoved()) {
                        this.stopSparringWithVictory(this.sparringPlayer);
                    }
                } else {
                    this.isSparring = false;
                }
            }
        }
    }

    // ------------------- 基础属性与 AI 配置 -------------------

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtDialogPlayerGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ------------------- 交易系统实现 -------------------

    public void openTradeMenu(ServerPlayer player) {
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradePlayer = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradePlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.DIRT, 1),
                    new ItemStack(Items.GRASS_BLOCK, 1),
                    99, 2, 0.05F
            ));
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

    // ------------------- NBT 数据与皮肤同步 -------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_INDEX, 0);
    }

    // 序列化与反序列化保存 NPC 的魂环数据
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SkinIndex", this.getSkinIndex());
        compound.putBoolean("IsSparring", this.isSparring);
        compound.put("NpcSoulData", this.soulCapability.serializeNBT());
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
    }

    /**
     * 刷新 NPC 属性，将开启武魂后的面板写进原生属性
     */
    public void refreshNpcAttributes() {
        if (this.soulCapability == null) return;
        double totalHp = this.soulCapability.getMaxshengming(); // 1555.3
        double totalAtk = this.soulCapability.getGongji();      // 497.6

        var hpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        var atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);

        if (hpAttr != null && totalHp > 0) {
            hpAttr.setBaseValue(totalHp);
            this.setHealth((float) totalHp); // 强行拉满血量
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
}