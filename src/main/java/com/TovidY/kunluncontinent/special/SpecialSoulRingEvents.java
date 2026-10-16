package com.TovidY.kunluncontinent.special;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 特殊魂技的通用事件处理。
 *
 * <p>目前只有一个职责：给「把玩家抛到空中」的特殊魂技提供<b>免坠落伤害</b>的通用工具。
 * 谁需要就在技能里调 {@link #grantFallImmunity(Player, int)}，剩下的事这里办。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpecialSoulRingEvents {

    /**
     * 「免坠落伤害截止时间」（游戏刻）写在玩家 {@code persistentData} 里的键。
     * <p>存<b>绝对时间</b>而不是倒计时，这样不需要任何 tick 回调，
     * 也不会因为存档/换维度而留下永久标记。</p>
     */
    private static final String NBT_FALL_GUARD_UNTIL = "KL_FallGuardUntil";

    private SpecialSoulRingEvents() {
    }

    /**
     * 授予玩家一段免坠落伤害时间。
     *
     * <p>典型用法：起跳技能把竖直速度顶上去之后立刻调用 —— 否则从 6 格落下
     * 会结结实实吃一次坠落伤害，玩家会把「娱乐技能」记成「自残技能」。</p>
     *
     * @param durationTicks 从当前时刻起的有效时长（tick）。一次 6 格起跳的滞空
     *                      约 30 tick，给 100 tick 足够宽裕。
     */
    public static void grantFallImmunity(Player player, int durationTicks) {
        player.getPersistentData().putLong(NBT_FALL_GUARD_UNTIL, player.level().getGameTime() + durationTicks);
    }

    /**
     * 落地时判定：还在免伤窗口内就取消这次坠落伤害。
     *
     * <p>标记在第一次结算时就被清掉 —— 坠落伤害每次落地只结算一次，
     * 留着只会影响下一次真正的自由落体。</p>
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        long until = player.getPersistentData().getLong(NBT_FALL_GUARD_UNTIL);
        if (until <= 0L) {
            return;
        }
        player.getPersistentData().remove(NBT_FALL_GUARD_UNTIL);
        if (player.level().getGameTime() <= until) {
            event.setCanceled(true);
            event.setDistance(0.0F);
        }
    }
}
