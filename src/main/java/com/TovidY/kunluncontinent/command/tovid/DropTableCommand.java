package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.drop.DropRegistry;
import com.TovidY.kunluncontinent.drop.DropRule;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 掉落表查看指令：{@code /kunluncontinent droptable}（权限 2）
 *
 * <p>用来核对 {@link DropRegistry} 里到底登记了哪些规则、年限门槛和概率，
 * 不用翻代码或进游戏试。</p>
 *
 * <p><b>新增掉落只改 {@code drop/DropRegistry.java}</b>，本指令无需改动。</p>
 */
public class DropTableCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kunluncontinent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("droptable")
                                .executes(ctx -> showAll(ctx.getSource()))
                                // /kunluncontinent droptable test 50000
                                // 模拟"一只 Lv X年限的生物会命中哪些规则"
                                .then(Commands.literal("test")
                                        .then(Commands.argument("nianxian", IntegerArgumentType.integer(0, 1_000_000_000))
                                                .executes(ctx -> simulate(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "nianxian"))))))
        );
    }

    private static int showAll(CommandSourceStack source) {
        List<DropRule> rules = DropRegistry.rules();
        if (rules.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7[掉落表] 没有任何规则"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "§6[掉落表] §f共 §e" + rules.size() + " §f条规则（新增掉落只改 drop/DropRegistry.java）"), false);
        for (int i = 0; i < rules.size(); i++) {
            final String text = "§f" + (i + 1) + ". " + rules.get(i);
            source.sendSuccess(() -> Component.literal(text), false);
        }
        return rules.size();
    }

    private static int simulate(CommandSourceStack source, long nianxian) {
        source.sendSuccess(() -> Component.literal(
                "§6[掉落表] §f模拟年限 §e" + nianxian + " §f（假设无生物种类限制）："), false);
        for (DropRule rule : DropRegistry.rules()) {
            double c = rule.effectiveChance(nianxian);
            boolean rangeOk = c > 0;
            final String line = String.format("§f - §b%s §8| §7年限内§8=§f%s §8| §7概率 §e%.3f%%",
                    rule.id(),
                    rangeOk ? "是" : "否",
                    c * 100);
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return DropRegistry.rules().size();
    }
}