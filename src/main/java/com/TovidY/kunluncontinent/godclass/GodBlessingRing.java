package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

/**
 * 封神后自动赐予的<b>神赐魂环</b>。
 *
 * <h3>选环规则（按用户需求）</h3>
 * <ol>
 *   <li>找出玩家<b>魂环数量最多</b>的武魂；</li>
 *   <li>数量并列时，比较各自<b>第九魂环的年限</b>，取年限更高的那个武魂；</li>
 *   <li>神赐魂环的年限 = 该武魂第九环年限 <b>× 3</b>；</li>
 *   <li>用 {@link MobAttributeCapability#initNianxian(long)} 按年限生成 11 项属性
 *       （和普通魂环完全同一条继承链），再把这个环追加到该武魂的魂环列表末尾。</li>
 * </ol>
 *
 * <h3>为什么标记 {@code setShenci(true)} + {@code setShenciGod(godId)}</h3>
 * <ul>
 *   <li>吸收扣精神力、{@code HunhuanWeakener} 削弱都会跳过神赐环（已有逻辑）；</li>
 *   <li>客户端渲染据此走**该神位自己的**神环贴图（海神 {@code haishenhunhuan.png}，
 *       见 {@code PWRenderPlayerEvent#GOD_RING_TEXTURES}）；不同神位各用各的图，
 *       所以必须记神位 id，不能只用一个 boolean；</li>
 *   <li>{@link #hasGodRing} 用它做幂等判断 —— 不额外加存档字段。</li>
 * </ul>
 *
 * <p>追加位置在列表末尾 → 渲染时下标 ≥ 9，本来就属于「神环」层（背后的环），
 * 位置/大小沿用神环那一套。</p>
 */
public final class GodBlessingRing {

    /** 神赐环年限相对第九环的倍率。 */
    private static final int NIANXIAN_MULTIPLIER = 3;

    private GodBlessingRing() {
    }

    /**
     * 尝试给玩家赐环。
     *
     * @return 真的赐出去了才返回 {@code true}（已拥有神赐环 / 没有魂环时返回 false）
     */
    public static boolean grant(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> grant(player, cap))
                .orElse(false);
    }

    private static boolean grant(ServerPlayer player, PlayerAttributeCapability cap) {
        Map<String, List<MobAttributeCapability>> allRings = cap.getMonsterCapabilityLists();
        if (allRings == null || allRings.isEmpty()) {
            return false;
        }
        // 幂等：已经有神赐环就不再发（读档、重复封神都安全）
        if (hasGodRing(allRings)) {
            return false;
        }

        String bestName = null;
        int bestCount = -1;
        long bestNinth = -1L;

        for (Map.Entry<String, List<MobAttributeCapability>> entry : allRings.entrySet()) {
            List<MobAttributeCapability> rings = entry.getValue();
            if (rings == null || rings.isEmpty()) {
                continue;
            }
            int count = rings.size();
            long ninth = count >= 9 ? rings.get(8).getNianxian() : 0L;

            // ① 环数多的优先；② 并列时比第九环年限
            if (count > bestCount || (count == bestCount && ninth > bestNinth)) {
                bestName = entry.getKey();
                bestCount = count;
                bestNinth = ninth;
            }
        }

        if (bestName == null) {
            return false;
        }

        List<MobAttributeCapability> targetRings = allRings.get(bestName);
        if (targetRings == null) {
            return false;
        }

        // 没有第九环（不足 9 个环）时退回"该武魂最高年限的环"，
        // 避免算出 0 年这种没意义的保底值
        long baseNianxian = bestNinth > 0 ? bestNinth : highestNianxian(targetRings);
        long nianxian = Math.max(1L, baseNianxian * NIANXIAN_MULTIPLIER);

        MobAttributeCapability ring = new MobAttributeCapability();
        ring.initNianxian(nianxian);   // 按生物年限生成 11 项属性
        ring.setShenci(true);
        // 记下这枚环来自哪个神位 —— 客户端按它选各自的神环贴图（海神/修罗神/天使神…）
        String godId = cap.getGodName();
        ring.setShenciGod(godId);
        targetRings.add(ring);

        // 属性重算 + 把新环列表广播给所有客户端（刷新渲染缓存）
        SynsAPI.synsPlayerAttribute(player);
        PlayerHunhuanAPI.broadcastWuhunRings(player, true);
        // 成就：神赐魂环
        AchievementAPI.onGodRingBlessed(player);

        player.sendSystemMessage(Component.literal(
                "§d§l【" + godDisplayName(godId) + "神赐魂环】§f神格显化，你的 §b" + bestName
                        + " §f获得了一枚 §e" + nianxian + "年 §f的神赐魂环！"));
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal("§d§l神赐魂环").withStyle(ChatFormatting.LIGHT_PURPLE)));
        return true;
    }

    /** godId → 显示名（{@code sea_god} → 海神）；查不到就退回 id，至少不会 NPE。 */
    private static String godDisplayName(String godId) {
        if (godId == null || godId.isBlank()) {
            return "";
        }
        GodInfo info = GodRegistry.GODS.get(godId);
        return info != null && info.getName() != null ? info.getName() : godId;
    }

    private static boolean hasGodRing(Map<String, List<MobAttributeCapability>> allRings) {
        for (List<MobAttributeCapability> rings : allRings.values()) {
            if (rings == null) {
                continue;
            }
            for (MobAttributeCapability ring : rings) {
                if (ring != null && ring.isShenci()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long highestNianxian(List<MobAttributeCapability> rings) {
        long highest = 0L;
        for (MobAttributeCapability ring : rings) {
            if (ring != null && ring.getNianxian() > highest) {
                highest = ring.getNianxian();
            }
        }
        return highest;
    }
}
