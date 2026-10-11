package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 神考「考核通过」的<b>公共演出</b>。
 *
 * <h3>为什么抽出来</h3>
 * 以前这套演出（标题 / 副标题 / 音效 / 雷声 / 全服公告）只写在客户端点击
 * {@code C2SCheckTaskPacket} 里，而击杀生物走的 {@code KLivingDeathEvent.handleGodKillTask}
 * 只调了 {@code checkTaskCompletion()} —— 那个方法只发奖励 + 推进阶段，<b>不发任何提示</b>。
 * 结果就是：
 * <ul>
 *   <li>击杀满足 KILL 类考核后，屏幕上什么都不出；</li>
 *   <li>玩家必须自己点面板的「进度检查」才能看到通过提示。</li>
 * </ul>
 *
 * <p>现在两条路径统一调用 {@link #announcePass}，行为完全一致。
 * 顺序约定：<b>先发演出，再 {@code checkTaskCompletion}</b>——
 * 因为 {@code completedStage} 必须在阶段被推进<b>之前</b>取，
 * 否则显示的会是下一考的序号。</p>
 */
public final class GodExamFeedback {

    private GodExamFeedback() {
    }

    /**
     * 播放「考核通过」演出：屏幕标题 + 副标题 + 音效 + 全服公告。
     *
     * @param player        通过考核的玩家（必须是服务端玩家，客户端会跳过）
     * @param completedStage 刚刚完成的考号（1~9）。<b>调用方必须在推进阶段前先取好</b>，
     *                       因为 {@code checkTaskCompletion} 会立刻把 currentStage 加 1。
     */
    public static void announcePass(ServerPlayer player, int completedStage) {
        if (player == null) return;
        PlayerAttributeCapability cap = player.getCapability(
                com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        String godDisplayName = "未知神位";
        if (cap != null) {
            GodInfo info = GodRegistry.GODS.get(cap.getGodName());
            if (info != null) godDisplayName = info.getName();
        }

        Component title = Component.literal("§6§l★ 考 核 通 过 ★").withStyle(Style.EMPTY.withBold(true));
        Component subtitle = Component.literal("§f" + godDisplayName + " §e第 " + completedStage + " 考 §a完成");
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));

        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.8F, 0.7F);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F);

        if (player.getServer() != null) {
            Component broadcastMsg = Component.literal("§6§l【神祇公告】 §e" + player.getScoreboardName()
                    + " §f成功突破 §b" + godDisplayName + " §f第 §c" + completedStage + " §f考试炼！");
            player.getServer().getPlayerList().broadcastSystemMessage(broadcastMsg, false);
        }
    }
}
