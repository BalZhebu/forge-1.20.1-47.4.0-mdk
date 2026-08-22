package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.event.client.CoinDropHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /kunluncontinent <玩家名> coindrop true
 * 强制该玩家击杀生物时 100% 触发第一阶段掉落；同时大幅提升品质分布，便于快速测试金/银/铜币和大/中/小钱袋。
 * /kunluncontinent <玩家名> coindrop false
 * 强制关闭该玩家击杀时的硬币与钱袋掉落。
 * /kunluncontinent <玩家名> coindrop reset
 * 清除临时测试配置，恢复原本根据年限计算概率的正常玩法逻辑。
 */

public class CoinDropCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kunluncontinent")
                        .requires(source -> source.hasPermission(2)) // 要求管理员权限 (OP Level 2)
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("coindrop")
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("true");
                                                    builder.suggest("false");
                                                    builder.suggest("reset");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
                                                    String mode = StringArgumentType.getString(context, "mode").toLowerCase();

                                                    if (!mode.equals("true") && !mode.equals("false") && !mode.equals("reset")) {
                                                        context.getSource().sendFailure(Component.literal("§c[昆仑大陆] 无效参数！仅限使用: true, false, reset"));
                                                        return 0;
                                                    }

                                                    CoinDropHandler.setTestMode(targetPlayer.getUUID(), mode);

                                                    switch (mode) {
                                                        case "true":
                                                            context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 已为玩家 §e" + targetPlayer.getName().getString() + " §a开启【硬币/钱袋 100%掉落】测试模式！"), true);
                                                            break;
                                                        case "false":
                                                            context.getSource().sendSuccess(() -> Component.literal("§c[昆仑大陆] 已为玩家 §e" + targetPlayer.getName().getString() + " §c强制关闭【硬币/钱袋】掉落！"), true);
                                                            break;
                                                        case "reset":
                                                            context.getSource().sendSuccess(() -> Component.literal("§b[昆仑大陆] 已重置玩家 §e" + targetPlayer.getName().getString() + " §b的掉落概率，恢复默认逻辑。"), true);
                                                            break;
                                                    }

                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}