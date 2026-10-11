package com.TovidY.kunluncontinent.item.baseskillist;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

public abstract class BaseSkillItem extends Item {
    public BaseSkillItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    public abstract float getBaseCost();
    public abstract float getDamageMultiplier();
    public abstract int getCastTime();
    public abstract int getCooldownTicks();

    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            float playerGongji = ModAttributeAPI.getGongji(player);
            float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;

            this.executeEffect(level, player, powerMultiplier, finalDamage);
        }
    }

    /**
     * NPC 释放技能专属入口（利用 FakePlayer 完美兼容现有 27 个技能类）
     */
    public void executeEffectForNpc(ServerLevel serverLevel, PlayerNpcEntity npc, float powerMultiplier, float finalDamage) {
        var fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
        fakePlayer.setPos(npc.getX(), npc.getY(), npc.getZ());
        fakePlayer.setXRot(npc.getXRot());
        fakePlayer.setYRot(npc.getYRot());
        var atkAttr = fakePlayer.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkAttr != null) {
            atkAttr.setBaseValue(npc.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        this.executeEffect(serverLevel, fakePlayer, powerMultiplier, finalDamage);
    }

    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {

    }

    public String getDescriptionKey() {
        return this.getDescriptionId() + ".description";
    }

    public Component getDynamicDescription(float costMultiplier) {
        float finalCost = getBaseCost() * costMultiplier;
        return Component.translatable(getDescriptionKey(), String.format("%.1f", finalCost));
    }

    public float getPowerMultiplier(long nianxian) {
        if (nianxian >= 100000000) return 50.0f;
        if (nianxian >= 10000000) return 25.0f;
        if (nianxian >= 1000000) return 15.0f;
        if (nianxian >= 100000) return 8.0f;
        if (nianxian >= 10000) return 4.0f;
        if (nianxian >= 1000) return 2.5f;
        if (nianxian >= 100) return 1.5f;
        return 1.0f;
    }

    public float getCostMultiplier(int nianxian) {
        if (nianxian <= 100) return 1.0f;
        float costFactor = (float) (1.0 + Math.log10(nianxian / 10.0) * 0.4);
        return Math.min(3.5f, costFactor);
    }

    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float finalCost = getBaseCost() * costMultiplier;
            cap.setJingshenli(Math.max(0, cap.getJingshenli() - finalCost));
        });
    }

    /**
     * 释放前是否要校验并<b>一次性</b>扣除 {@code getBaseCost() × 年限系数}。
     *
     * <p>开关型技能（武魂真身，见 {@code BaseZhenshenSkill}）返回 {@code false}：
     * 它们的成本体现在「开启期间每 20 tick 扣 10 倍武魂维持费」，
     * 而不是释放瞬间扣一次。所以 {@code CPacketReleaseSkill} 会跳过
     * 「精神力是否够一次消耗」的前置校验。</p>
     */
    public boolean hasUpfrontCost() {
        return true;
    }

    /**
     * 开关型技能的「已开启时再按 = 关闭」机会。
     *
     * <p>调用点在 {@code CPacketReleaseSkill} 里、<b>所有校验之前</b> ——
     * 因为开启后立刻处于冷却中，若走正常流程关闭会被冷却拦住。</p>
     *
     * @return {@code true} 表示本次释放已被消费（只做了关闭），调用方应立刻结束，
     *         既不再做精神力校验，也不刷新冷却时间戳
     */
    public boolean tryToggleOff(ServerPlayer player) {
        return false;
    }

    /**
     * 释放这个技能。
     *
     * @return 本次释放是否真的生效。{@code false} 表示没做事
     *         （精神力不足 / 冷却中 / 只是关闭动作），调用方据此决定
     *         要不要刷新冷却时间戳
     */
    public boolean handleRelease(Level level, Player player, int nianxian) {
        float costMultiplier = getCostMultiplier(nianxian);
        float finalCost = getBaseCost() * costMultiplier;

        boolean[] cast = {false};
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getJingshenli() >= finalCost && !player.getCooldowns().isOnCooldown(this)) {
                float powerMultiplier = getPowerMultiplier(nianxian);
                float playerGongji = ModAttributeAPI.getGongji(player);
                float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;
                executeEffect(level, player, powerMultiplier, finalDamage);

                applyPenalty(player, costMultiplier);
                player.getCooldowns().addCooldown(this, getCooldownTicks());
                SynsAPI.synsPlayerAttribute(player);
                cast[0] = true;
            }
        });
        return cast[0];
    }

    // ==================================================================================
    //  ⚠️ 魂技伤害的统一入口 —— 务必用这个，不要直接调 target.hurt(...)
    // ==================================================================================

    /**
     * 标记技能伤害的 NBT 键。由 {@link #dealSkillDamage} 写入，
     * {@code CombatEventHandler} 读取它来判断「这次伤害来自魂技、不要再叠加攻击力」。
     *
     * <p>用完立刻 {@link #clearSkillDamageMark} 清除 —— 因为 {@code hurt()} 是<b>同步</b>执行
     * {@code LivingHurtEvent} 的，事件返回后这个标记就必须复原，
     * 否则同一 tick 内的普攻会被误判成魂技伤害。</p>
     */
    public static final String SKILL_DAMAGE_MARK = "KL_SkillDamage";

    /**
     * 造成一次<b>魂技伤害</b>。
     *
     * <p><b>为什么不直接用 {@code target.hurt(source, finalDamage)}</b>：
     * 那样做会让 {@code CombatEventHandler} 把 {@code finalDamage} 当成
     * 「原版算出的武器基础伤害」，执行</p>
     * <pre>{@code baseDamage = (gongji + event.getAmount()) * reductionFactor;}</pre>
     * 于是<b>攻击力被重复加了一次</b>（{@code finalDamage} 里本来就含 gongji），
     * 玩家攻击力越高，技能倍率与年限倍率就相对越弱。</p>
     *
     * @param finalDamage 已算好的伤害（= gongji × 技能倍率 × 年限倍率）
     */
    public static void dealSkillDamage(LivingEntity target, float finalDamage,
                                        net.minecraft.world.damagesource.DamageSource source) {
        if (target == null || finalDamage <= 0.0f) return;
        target.getPersistentData().putBoolean(SKILL_DAMAGE_MARK, true);
        try {
            target.hurt(source, finalDamage);
        } finally {
            // 必须用 finally：hurt 内部若抛异常，标记也要复原，否则目标会一直带着它
            clearSkillDamageMark(target);
        }
    }

    /** 清除魂技伤害标记（供 handler 内部调用；技能侧一般不需要手动调）。 */
    public static void clearSkillDamageMark(LivingEntity target) {
        target.getPersistentData().remove(SKILL_DAMAGE_MARK);
    }

    /** 判断这次伤害是否来自魂技。由 {@code CombatEventHandler} 使用。 */
    public static boolean isSkillDamage(LivingEntity target) {
        return target != null && target.getPersistentData().getBoolean(SKILL_DAMAGE_MARK);
    }
}