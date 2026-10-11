package com.TovidY.kunluncontinent.command.shenkao;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.command.KLCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 神位<b>测试参数</b>开关（与 {@code ShenweiCommand} 的神考指令是两回事，故独立成类）。
 *
 * <p>语法：{@code /kunluncontinent shenming <玩家> <ignoreLimit> <forceSuccess>}</p>
 *
 * <p>⚠️ 类名与 {@code command.tovid.ShenweiCommand} 同名（不同包），
 * 历史上容易 import 错。已在此注明职责差异：
 * <ul>
 *   <li>本类：改玩家的 {@code debugIgnoreTianfu} / {@code debugForceSuccess} 测试开关</li>
 *   <li>tovid 那个：开启/重置神考、直接封神</li>
 * </ul></p>
 */
public class ShenweiCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        KLCommands.register(dispatcher, node());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("shenming")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("ignoreLimit", BoolArgumentType.bool())
                                .then(Commands.argument("forceSuccess", BoolArgumentType.bool())
                                        // 用普通 lambda 而非方法引用：
                                        // executes 的函数式接口声明了 throws CommandSyntaxException，
                                        // 而本类没有声明该受检异常，方法引用会要求上层也抛。
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            boolean ignore = BoolArgumentType.getBool(ctx, "ignoreLimit");
                                            boolean force = BoolArgumentType.getBool(ctx, "forceSuccess");
                                            target.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                                                    .ifPresent(cap -> {
                                                        cap.debugIgnoreTianfu = ignore;
                                                        cap.debugForceSuccess = force;
                                                    });
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§a已设置 " + target.getName().getString() + " 的神位测试参数！"), true);
                                            return 1;
                                        }))));
    }
}