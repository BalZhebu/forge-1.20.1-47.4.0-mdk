package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.GodExamDebug;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class ShenweiCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("shenwei")
                        .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.literal("reset")
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                                        cap.resetGodSystem();
                                                        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
                                                        SynsAPI.synsPlayerAttribute(player);
                                                        context.getSource().sendSuccess(() ->
                                                                Component.literal("§a[昆仑大陆] 成功调用 resetGodSystem，已彻底剥离修士 §e" + player.getScoreboardName() + " §a身上的所有神考数据！"), true);
                                                    });
                                                    return 1;
                                                })
                                        )
                                .then(Commands.argument("godid", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            GodRegistry.GODS.keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            String godId = StringArgumentType.getString(context, "godid");
                                            if (!GodRegistry.GODS.containsKey(godId)) {
                                                context.getSource().sendFailure(Component.literal("§c错误：神位 ID '" + godId + "' 不存在！"));
                                                return 0;
                                            }
                                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                                // 1. 初始化 1-9 考的所有数据
                                                cap.initializeGodExam(player, godId);

                                                // 2. 同步数据到客户端，让 UI 刷新
                                                NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);

                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("§a成功为玩家 §e" + player.getScoreboardName() + " §a开启 §6" + godId + " §a神试！"), true);
                                            });

                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("shenqi_test")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("godid", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            GodRegistry.GODS.keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            String godId = StringArgumentType.getString(context, "godid");
                                            GodInfo godInfo = GodRegistry.GODS.get(godId);

                                            if (godInfo == null) {
                                                context.getSource().sendFailure(Component.literal("§c错误：找不到神位 " + godId));
                                                return 0;
                                            }

                                            godInfo.startAscensionAnimation(player);

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§d§l[测试] §f正在开启 §e" + godInfo.getName() + " §f的10秒成神仪式动画！"), true);

                                            return 1;
                                        })
                                )
                        )
                )
                // ⭐ 测试用：一键完成当前神考任务（不新增指令类，逻辑在 GodExamDebug）
                .then(Commands.literal("shenwei_complete")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                // /kunc shenwei_complete <玩家>            → 完成当前 1 考
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                    String msg = GodExamDebug.completeCurrentTask(player, 1);
                                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                                    return 1;
                                })
                                // /kunc shenwei_complete <玩家> 9          → 一路推到封神
                                .then(Commands.argument("times", IntegerArgumentType.integer(1, 9))
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            int times = IntegerArgumentType.getInteger(context, "times");
                                            String msg = GodExamDebug.completeCurrentTask(player, times);
                                            context.getSource().sendSuccess(() -> Component.literal(msg), true);
                                            return 1;
                                        })
                                )
                        )
                )
                // ⭐ 测试用：直接封神 + 播成神动画（跳过全部九考）
                .then(Commands.literal("shenwei_grant")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                    String msg = GodExamDebug.grantGodDirectly(player, null);
                                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                                    return 1;
                                })
                                .then(Commands.argument("godid", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            GodRegistry.GODS.keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            String godId = StringArgumentType.getString(context, "godid");
                                            String msg = GodExamDebug.grantGodDirectly(player, godId);
                                            context.getSource().sendSuccess(() -> Component.literal(msg), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}