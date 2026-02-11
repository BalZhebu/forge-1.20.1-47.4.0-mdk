package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TianfuCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tianfu")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("tiancai");
                                    builder.suggest("zhuoyue");
                                    builder.suggest("youxiu");
                                    builder.suggest("feiwu");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            String type = StringArgumentType.getString(context, "type");
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");

                                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                                // 假设你在 Capability 已经加了 setForcedTalent 方法
                                                cap.setForcedTalent(type);
                                            });

                                            context.getSource().sendSuccess(() -> Component.literal("已将玩家 " + player.getName().getString() + " 的下次觉醒天赋设为：" + type), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}