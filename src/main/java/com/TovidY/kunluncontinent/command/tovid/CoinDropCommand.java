package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.command.KLCommands;
import com.TovidY.kunluncontinent.event.client.CoinDropHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 硬币 / 钱袋掉落测试开关。
 *
 * <p><b>实际语法</b>：{@code /kunluncontinent <玩家> coindrop <true|false|reset>}
 * （注意 {@code <玩家>} 在<b>子命令之前</b> —— 老的类注释写反了，这里以代码为准。）</p>
 */
public class CoinDropCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        KLCommands.register(dispatcher, node());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("coindrop")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("true");
                                    builder.suggest("false");
                                    builder.suggest("reset");
                                    return builder.buildFuture();
                                })
                                .executes(CoinDropCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
        String mode = StringArgumentType.getString(context, "mode").toLowerCase();

        if (!mode.equals("true") && !mode.equals("false") && !mode.equals("reset")) {
            context.getSource().sendFailure(Component.literal("§c[昆仑大陆] 无效参数！仅限使用: true, false, reset"));
            return 0;
        }

        CoinDropHandler.setTestMode(targetPlayer.getUUID(), mode);

        switch (mode) {
            case "true" -> context.getSource().sendSuccess(() -> Component.literal(
                    "§a[昆仑大陆] 已为玩家 §e" + targetPlayer.getName().getString() + " §a开启【硬币/钱袋 100%掉落】测试模式！"), true);
            case "false" -> context.getSource().sendSuccess(() -> Component.literal(
                    "§c[昆仑大陆] 已为玩家 §e" + targetPlayer.getName().getString() + " §c强制关闭【硬币/钱袋】掉落！"), true);
            case "reset" -> context.getSource().sendSuccess(() -> Component.literal(
                    "§b[昆仑大陆] 已重置玩家 §e" + targetPlayer.getName().getString() + " §b的掉落概率，恢复默认逻辑。"), true);
        }

        return 1;
    }
}