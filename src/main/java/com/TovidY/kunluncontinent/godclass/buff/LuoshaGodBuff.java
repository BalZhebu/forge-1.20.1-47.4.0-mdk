package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 罗刹神神位被动 ——「罗刹嗜血」。
 *
 * <p>造成伤害 +{@value #DAMAGE_BONUS_PERCENT}%，并按伤害的
 * {@value #LIFESTEAL_PERCENT}% 回复生命。定位与修罗神错开：
 * 修罗是<b>威慑</b>（怪不敢靠近），罗刹是<b>越杀越强</b>（正面输出 + 续航）。</p>
 *
 * <p>实现挂在 {@link GodBuff#onLivingHurt}，靠 {@code GodBuffEventHandler} 的
 * <b>LOWEST</b> 优先级保证读到的是 {@code CombatEventHandler}（HIGHEST）重算之后的最终伤害 ——
 * 否则乘出来的数会被那边直接 {@code setAmount} 覆盖掉，等于没加。</p>
 */
public class LuoshaGodBuff extends GodBuff {

    public static final LuoshaGodBuff INSTANCE = new LuoshaGodBuff();

    /** 增伤倍率。 */
    public static final float DAMAGE_MULTIPLIER = 1.25F;

    /** 吸血比例。 */
    public static final float LIFESTEAL_RATIO = 0.08F;

    private static final int DAMAGE_BONUS_PERCENT = 25;
    private static final int LIFESTEAL_PERCENT = 8;

    public LuoshaGodBuff() {
        super("luosha_god", ChatFormatting.DARK_RED, "罗刹嗜血",
                "造成伤害 +" + DAMAGE_BONUS_PERCENT + "%，并按伤害的 "
                        + LIFESTEAL_PERCENT + "% 回复生命");
    }

    @Override
    public float onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return Float.NaN;
        }
        if (!GodBuffs.hasGod(player, godId())) {
            return Float.NaN;
        }
        float amount = event.getAmount();
        if (amount <= 0.0F) {
            return Float.NaN;
        }

        // 吸血：heal 本身不会超过最大生命；只有真的回了血才同步
        // （满血时每次攻击都同步一份完整 capability NBT 太浪费）
        float before = player.getHealth();
        player.heal(amount * LIFESTEAL_RATIO);
        if (player.getHealth() > before) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                cap.setShengming(player.getHealth());
                SynsAPI.synsPlayerAttribute(player);
            });
        }

        return amount * DAMAGE_MULTIPLIER;
    }
}
