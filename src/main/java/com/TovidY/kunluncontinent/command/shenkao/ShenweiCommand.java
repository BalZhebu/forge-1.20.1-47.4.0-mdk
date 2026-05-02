package com.TovidY.kunluncontinent.command.shenkao;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ShenweiCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("shenming")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("ignoreLimit", BoolArgumentType.bool())
                                        .then(Commands.argument("forceSuccess", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                    boolean ignore = BoolArgumentType.getBool(ctx, "ignoreLimit");
                                                    boolean force = BoolArgumentType.getBool(ctx, "forceSuccess");
                                                    target.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                                        cap.debugIgnoreTianfu = ignore;
                                                        cap.debugForceSuccess = force;
                                                    });
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("§a已设置 " + target.getName().getString() + " 的神位测试参数！"),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }
}