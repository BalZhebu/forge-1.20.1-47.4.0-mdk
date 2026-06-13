package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TowerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2)) // 需要管理员(OP)权限
                .then(Commands.literal("tower")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");

                                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                                attr.setCurrentTowerFloor(0);
                                                context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 成功将修士 " + player.getName().getString() + " 的幻境层数重置为第 1 层"), true);
                                            });
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                                        int oldFloor = attr.getCurrentTowerFloor();
                                                        attr.setCurrentTowerFloor(oldFloor + amount);
                                                        context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 成功将修士 " + player.getName().getString() + " 的幻境层数变动 " + amount + " 层（当前处于: " + (attr.getCurrentTowerFloor() + 1) + " 层）"), true);
                                                    });
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }
}