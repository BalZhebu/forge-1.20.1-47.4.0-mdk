package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

//适配指令赋予经验升级测试（辅助方法）
public class UpgradeTestCommand {

    /**
     * 独立根节点 {@code /testupgrade}。
     *
     * <p>⚠️ 这个命令历史上就<b>不在</b> {@code /kunluncontinent} 下，
     * 所以不能走 {@link com.TovidY.kunluncontinent.command.KLCommands#register}，
     * 仍需自己注册根节点。</p>
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(node());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("testupgrade")
                .requires(source -> source.hasPermission(2))
                .executes(UpgradeTestCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("该命令只能在游戏内执行"));
            return 0;
        }
        PlayerUpgradeSystem.triggerUpgradeCheck(player);
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentExp = cap.getJingyan();
            float maxExp = cap.getMaxjingyan();
            if (currentExp < maxExp) {
                cap.setJingyan(maxExp);
                context.getSource().sendSuccess(() -> Component.literal("已将经验值设置为满值，触发升级检查"), true);
            } else {
                context.getSource().sendSuccess(() -> Component.literal("当前经验值已满，正在执行升级检查"), true);
            }
            PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
        });

        return 1;
    }
}