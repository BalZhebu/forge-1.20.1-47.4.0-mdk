package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * 指令注册工具：把「注册根节点 + 挂子命令」这层样板收敛到一处。
 *
 * <h3>为什么需要它</h3>
 * 改造前<b>每个</b>指令类都自己写一遍：
 * <pre>{@code
 * dispatcher.register(Commands.literal("kunluncontinent")
 *         .requires(source -> source.hasPermission(2))
 *         .then(...));
 * }</pre>
 * 12 个类重复同一段样板，权限等级、命名、出错信息各写一遍，容易不一致 ——
 * 而且「根节点被重复 register 多次」这个隐患只有靠人眼才能发现。
 *
 * <p>现在改为：<b>根节点只注册一次</b>（{@link ModCommands}），
 * 各指令类只提供自己的「子命令树」，由这里统一挂到 {@code kunluncontinent} 下。</p>
 *
 * <h3>用法</h3>
 * <pre>{@code
 * // 在 ModCommands 里：
 * KLCommands.register(dispatcher, AttributeCommand.node());
 * KLCommands.register(dispatcher, CoinDropCommand.node());
 *
 * // 在指令类里：
 * public static LiteralArgumentBuilder<CommandSourceStack> node() {
 *     return Commands.literal("coindrop").then(...);
 * }
 * }</pre>
 */
public final class KLCommands {

    /** 根指令名。 */
    public static final String ROOT = "kunluncontinent";

    /**
     * 统一的管理员权限门槛（OP Level 2）。
     * <p>改造前 12 个类各自硬编码 {@code hasPermission(2)}，这里统一一处。</p>
     */
    public static final int ADMIN_PERMISSION = 2;

    private KLCommands() {
    }

    /**
     * 把一个子命令树挂到 {@code /kunluncontinent} 下。
     *
     * @param node 子命令，例如 {@code Commands.literal("coindrop").then(...)}
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                LiteralArgumentBuilder<CommandSourceStack> node) {
        dispatcher.register(Commands.literal(ROOT)
                .requires(source -> source.hasPermission(ADMIN_PERMISSION))
                .then(node));
    }

    /**
     * 「神位 ID」参数的补全器：列出 {@link GodRegistry#GODS} 里所有神位 id。
     * <p>原先在 4 处重复写 {@code suggests((ctx,b) -> {...})}，抽出来统一。</p>
     */
    public static SuggestionProvider<CommandSourceStack> godIdSuggestions() {
        return (context, builder) -> {
            for (String godId : GodRegistry.GODS.keySet()) {
                builder.suggest(godId);
            }
            return builder.buildFuture();
        };
    }

    /** 标准的神位 id 参数（含补全）。 */
    public static RequiredArgumentBuilder<CommandSourceStack, String> godIdArg() {
        return Commands.argument("godid", StringArgumentType.string())
                .suggests(godIdSuggestions());
    }
}