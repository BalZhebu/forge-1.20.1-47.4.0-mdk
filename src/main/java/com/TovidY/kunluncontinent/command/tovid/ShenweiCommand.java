package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.command.KLCommands;
import com.TovidY.kunluncontinent.godclass.GodExamDebug;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 神考（神位）相关指令。
 *
 * <p>语法：
 * <ul>
 *   <li>{@code /kunluncontinent shenwei <玩家> reset} —— 剥离该玩家所有神考数据</li>
 *   <li>{@code /kunluncontinent shenwei <玩家> <神位id>} —— 开启神考</li>
 *   <li>{@code /kunluncontinent shenwei_complete <玩家> [次数]} —— 完成当前考，次数 1~9（9=一路封神）</li>
 *   <li>{@code /kunluncontinent shenwei_grant <玩家> [神位id]} —— 直接封神 + 播成神动画</li>
 * </ul>
 *
 * <p>⚠️ 已删除 {@code shenqi_test} —— 它的实现（{@code GodInfo.startAscensionAnimation}）
 * 与 {@code shenwei_grant} <b>完全相同</b>，属于重复入口。
 * 需要播成神动画时用 {@code shenwei_grant} 即可。</p>
 */
public class ShenweiCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        KLCommands.register(dispatcher, node());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("shenwei")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("reset")
                                .executes(ShenweiCommand::reset))
                        .then(KLCommands.godIdArg()
                                .executes(ShenweiCommand::startExam)));
    }

    /** {@code shenwei_complete}：默认完成当前一考，带 times(1~9) 时一路推到封神。 */
    public static LiteralArgumentBuilder<CommandSourceStack> completeNode() {
        return Commands.literal("shenwei_complete")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> completeTask(ctx, 1))
                        .then(Commands.argument("times", IntegerArgumentType.integer(1, 9))
                                .executes(ctx -> completeTask(ctx,
                                        IntegerArgumentType.getInteger(ctx, "times")))));
    }

    /** {@code shenwei_grant}：直接封神（不传 godid 则沿用玩家当前考的神位）。 */
    public static LiteralArgumentBuilder<CommandSourceStack> grantNode() {
        return Commands.literal("shenwei_grant")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> grant(ctx, null))
                        .then(KLCommands.godIdArg()
                                .executes(ctx -> grant(ctx,
                                        StringArgumentType.getString(ctx, "godid")))));
    }

    // ==================== 实现 ====================

    private static int reset(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.resetGodSystem();
            NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
            SynsAPI.synsPlayerAttribute(player);
            context.getSource().sendSuccess(() -> Component.literal(
                    "§a[昆仑大陆] 成功调用 resetGodSystem，已彻底剥离修士 §e"
                            + player.getScoreboardName() + " §a身上的所有神考数据！"), true);
        });
        return 1;
    }

    private static int startExam(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        String godId = StringArgumentType.getString(context, "godid");
        if (!GodRegistry.GODS.containsKey(godId)) {
            context.getSource().sendFailure(Component.literal("§c错误：神位 ID '" + godId + "' 不存在！"));
            return 0;
        }
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.initializeGodExam(player, godId);
            NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
            context.getSource().sendSuccess(() -> Component.literal(
                    "§a成功为玩家 §e" + player.getScoreboardName() + " §a开启 §6" + godId + " §a神试！"), true);
        });
        return 1;
    }

    private static int completeTask(CommandContext<CommandSourceStack> context, int times) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        String msg = GodExamDebug.completeCurrentTask(player, times);
        context.getSource().sendSuccess(() -> Component.literal(msg), true);
        return 1;
    }

    private static int grant(CommandContext<CommandSourceStack> context, String godId) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        String msg = GodExamDebug.grantGodDirectly(player, godId);
        context.getSource().sendSuccess(() -> Component.literal(msg), true);
        return 1;
    }
}