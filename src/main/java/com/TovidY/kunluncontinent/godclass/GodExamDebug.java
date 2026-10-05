package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 神考相关指令的<b>共用的实现逻辑</b>。
 *
 * <p>用户已有 {@code ShenweiCommand}，本类<b>不注册任何新指令</b>，
 * 只把逻辑抽出来给 {@code ShenweiCommand} 里的子命令复用 ——
 * 想加新的调试子命令时，在指令类里写一行 {@code GodExamDebug.complete(...)} 即可。</p>
 */
public final class GodExamDebug {

    private GodExamDebug() {
    }

    /**
     * ⭐ 一键完成<b>当前</b>神考任务（只完成当前这一考，不推进）。
     *
     * <p>用途：测试"任务完成 → 发奖励 → 推进到下一考"这条链路的中间环节。</p>
     *
     * <p>做法：把进度直接顶到上限，然后调 {@code checkTaskCompletion} ——
     * 也就是玩家真实点"进度检查"按钮走的同一条路径，所以奖励/动画/推进行为完全一致。</p>
     *
     * @param times 执行次数（1 = 完成当前一考；9 = 从第 1 考一路推到封神）
     * @return 执行后的说明；失败时给玩家发错误消息
     */
    public static String completeCurrentTask(ServerPlayer player, int times) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> {
                    GodInfo info = GodRegistry.GODS.get(cap.getGodName());
                    if (info == null) {
                        return fail(player, "该玩家当前没有进行中的神考（先用 §e/kunluncontinent shenwei <玩家> <godid> §c开启）");
                    }
                    if (cap.isGod()) {
                        return fail(player, "§c该玩家已成就神位，无需再完成神考。");
                    }
                    if (!cap.hasActiveTask()) {
                        return fail(player, "§c当前阶段异常（第 " + cap.getCurrentStage() + " 考），无法继续。");
                    }

                    int done = 0;
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < times; i++) {
                        if (cap.isGod() || !cap.hasActiveTask()) break;

                        int stageBefore = cap.getCurrentStage();
                        int need = cap.getAssignedCounts()[stageBefore];

                        // ① 进度顶满
                        cap.setGodTaskProgress(need);
                        // ② 走真实的完成路径（发奖励 + 推进阶段 + 封神动画）
                        cap.checkTaskCompletion(player);

                        done++;
                        sb.append("第 §e").append(stageBefore).append(" §f考 → ");
                        if (cap.isGod()) {
                            sb.append("§6§l成就神位");
                            break;
                        }
                        sb.append("第 §e").append(cap.getCurrentStage()).append(" §f考");
                        if (i < times - 1) sb.append("，");
                    }

                    sync(player, cap);

                    String head = "§d§l[测试] §f已完成 §e" + done + " §f次神考任务"
                            + (cap.isGod() ? " §6§l—— 已封神！" : "");
                    return head + "§r" + sb;
                })
                .orElseGet(() -> fail(player, "§c读取玩家能力失败。"));
    }

    /**
     * 一键<b>直接封神</b>（跳过全部九考）。
     *
     * <p>用途：测试神祇面板的展示 + 神位被动效果。</p>
     *
     * @param godId 要封的神位 id；传 null / 空则沿用玩家当前考的神位
     */
    public static String grantGodDirectly(ServerPlayer player, String godId) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> {
                    String target = (godId == null || godId.isBlank())
                            ? cap.getGodName() : godId;

                    GodInfo info = GodRegistry.GODS.get(target);
                    if (info == null) {
                        return fail(player, "§c神位 ID '" + target + "' 不存在。");
                    }

                    // 玩家等级必须到 99（和真实飞升一样，否则飞升动画会被卡住）
                    int lv = cap.getDengji();
                    if (lv < 99) {
                        return fail(player, "§c玩家等级仅 " + lv + " 级，需达到 §e99级 §c才能承载神位。");
                    }

                    // 初始化九考数据（为让面板/存档结构完整）
                    cap.initializeGodExam(player, target);

                    // ⚠️ isGod 只在 nextGodStage() 从第 9 考再推一次时置位，
                    //   没有 setter。所以要把 1~9 考全部走完（nextGodStage 9 次）才封神。
                    //   这里只推阶段、不发奖、不播动画 —— 动画最后单独播。
                    while (!cap.isGod()) {
                        cap.nextGodStage();
                        if (cap.getCurrentStage() > 9) break;   // 防御：万一没置位
                    }

                    sync(player, cap);

                    // 走真实的飞升动画（和第九考结束时同一条路）
                    info.startAscensionAnimation(player);

                    return "§d§l[测试] §f已授予 §e" + info.getName()
                            + " §f神位并开启成神仪式动画。";
                })
                .orElseGet(() -> fail(player, "§c读取玩家能力失败。"));
    }

    // ==================== 内部工具 ====================

    private static String fail(ServerPlayer player, String msg) {
        player.sendSystemMessage(Component.literal(msg));
        return msg;
    }

    /** 同步客户端面板数据 + 属性同步。 */
    private static void sync(ServerPlayer player, PlayerAttributeCapability cap) {
        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
        SynsAPI.synsPlayerAttribute(player);
    }
}
