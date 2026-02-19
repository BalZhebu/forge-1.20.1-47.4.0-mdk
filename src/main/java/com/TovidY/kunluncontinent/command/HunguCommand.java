package com.TovidY.kunluncontinent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HunguCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("hungu")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            HunguAdminStatus.setStatus(player, enabled);

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§6[系统] §r玩家 " + player.getScoreboardName() + " 的魂骨必掉模式已设置为: " + (enabled ? "§a开启" : "§c关闭")), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
