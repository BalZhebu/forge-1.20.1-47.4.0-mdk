package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * 神位被动的<b>统一入口与工具方法</b>。
 *
 * <p>神考全部完成（{@code cap.isGod() == true}）后，capability 里的
 * {@code godName} 就是神位 id（对应 {@code GodRegistry.GODS} 的 key）。
 * 各类被动都放在 {@code godclass/buff/} 下各自的类里，<b>新增神位只加一个类</b>，
 * 然后在 {@link #ALL_PASSIVES} 登记即可，面板文案也自动跟着长出来。</p>
 */
public final class GodBuffs {

    private GodBuffs() {
    }

    /**
     * 已实现的神位被动列表。
     *
     * <p>⭐ <b>新增神位被动 = 在这里加一行</b>，其余（事件订阅、面板展示）全自动。</p>
     */
    public static final java.util.List<GodBuff> ALL_PASSIVES = java.util.List.of(
            SeaGodBuff.INSTANCE,
            AngelGodBuff.INSTANCE,
            AsuraGodBuff.INSTANCE
    );

    /** 按神位 id 取被动；没有实现被动就返回 null。 */
    public static GodBuff of(String godId) {
        if (godId == null) return null;
        for (GodBuff buff : ALL_PASSIVES) {
            if (buff.godId().equals(godId)) return buff;
        }
        return null;
    }

    // ==================== 玩家判定 ====================

    /**
     * 玩家是否已获得指定神位。
     *
     * @return 没封神 / 不是该神位 → false
     */
    public static boolean hasGod(ServerPlayer player, String godId) {
        if (player == null) return false;
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.isGod() && godId.equals(cap.getGodName()))
                .orElse(false);
    }

    /** 玩家已获得的神位 id（没封神返回空串）。 */
    public static String godIdOf(ServerPlayer player) {
        if (player == null) return "";
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .filter(PlayerAttributeCapability::isGod)
                .map(PlayerAttributeCapability::getGodName)
                .orElse("");
    }

    // ==================== 生物判定 ====================

    /**
     * 读生物的年限；读不到（无 capability）返回 <b>0</b>。
     *
     * <p>本项目里所有生物生成时都会挂年限（{@code MobSpawnEvent.FinalizeSpawn}），
     * 所以正常情况下都有值；但 <b>NPC 不挂</b> → 0。</p>
     */
    public static long nianxianOf(LivingEntity entity) {
        return entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                .map(MobAttributeCapability::getNianxian)
                .orElse(0L);
    }
}
