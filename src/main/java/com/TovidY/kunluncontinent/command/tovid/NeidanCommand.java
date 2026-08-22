package com.TovidY.kunluncontinent.command.tovid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
public class NeidanCommand {
    private static final String[] QUALITY_SUGGESTIONS = {
            "fanpin", "liangpin", "shangpin", "zhenpin", "juepin", "xianpin", "false"
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("neidan")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("probability")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    player.getPersistentData().putBoolean("KL_Neidan_Prob_Cheat", value);
                                                    context.getSource().sendSuccess(() -> Component.literal("§a[Debug] 玩家 " + player.getName().getString() + " 掉落触发判定固定为: " + value), true);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("gailv")
                                        .then(Commands.argument("quality", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(QUALITY_SUGGESTIONS, builder))
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    String q = StringArgumentType.getString(context, "quality");

                                                    if ("false".equals(q)) {
                                                        player.getPersistentData().remove("KL_Neidan_Quality_Cheat");
                                                        context.getSource().sendSuccess(() -> Component.literal("§e[Debug] 已取消强制品级掉落"), true);
                                                    } else {
                                                        player.getPersistentData().putString("KL_Neidan_Quality_Cheat", q);
                                                        context.getSource().sendSuccess(() -> Component.literal("§a[Debug] 固定品级设为: " + q), true);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }
}