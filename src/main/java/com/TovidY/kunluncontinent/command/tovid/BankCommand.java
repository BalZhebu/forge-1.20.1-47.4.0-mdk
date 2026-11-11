package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.command.KLCommands;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * 银行管理员指令：<b>直接往某个玩家的银行账户里加魂币</b>（不进背包、不丢地上）。
 *
 * <h3>语法</h3>
 * <pre>
 *   /kunluncontinent bank give &lt;玩家&gt; &lt;币种&gt; &lt;数量&gt;
 *
 *   /kunluncontinent bank give Steve gold 10000        → 给 Steve 的银行存 1 万金魂币
 *   /kunluncontinent bank give Steve 银 500            → 币种支持中文简写
 * </pre>
 *
 * <p>币种可写：{@code gold / silver / copper}、{@code 金 / 银 / 铜}、
 * {@code 金魂币 / 银魂币 / 铜魂币}（大小写不敏感）。</p>
 *
 * <h3>为什么直接写 capability</h3>
 * <p>银行余额存在 {@code PlayerAttributeCapability.bankGold/Silver/Copper}（见
 * {@link BankAccount} 类注释），和「存入」走的是同一个字段 —— 所以这条指令等价于
 * 「凭空出现一叠魂币并全部存进银行」，<b>不会</b>经过 {@code BankAccount#deposit}
 * （那条路要先数背包里有多少、再扣物品，凭空发钱时没有物品可扣）。</p>
 *
 * <p>写入后立刻 {@link SynsAPI#synsPlayerAttribute} 同步一次，否则玩家开着面板时会看到旧余额。</p>
 */
public final class BankCommand {

    private BankCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("bank")
                .then(Commands.literal("give")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("coin", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("gold");
                                            builder.suggest("silver");
                                            builder.suggest("copper");
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("amount",
                                                        IntegerArgumentType.integer(1, BankAccount.MAX_STORED))
                                                .executes(BankCommand::give)))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        KLCommands.register(dispatcher, node());
    }

    private static int give(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String coinArg = StringArgumentType.getString(context, "coin");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        BankAccount.Coin coin = parseCoin(coinArg);
        if (coin == null) {
            context.getSource().sendFailure(Component.literal(
                    "§c[昆仑大陆] 未知币种：§e" + coinArg + "§c。可用：gold / silver / copper（金 / 银 / 铜）"));
            return 0;
        }

        PlayerAttributeCapability cap = BankAccount.capability(target);
        if (cap == null) {
            context.getSource().sendFailure(Component.literal(
                    "§c[昆仑大陆] 读不到玩家 §e" + target.getName().getString() + " §c的属性数据，存入失败。"));
            return 0;
        }

        int before = BankAccount.getStored(cap, coin);
        // 上限截断：银行单币种上限 2000 万，超出部分不会"溢出"成别的币，直接丢弃（并如实汇报）
        int after = (int) Math.min(BankAccount.MAX_STORED, (long) before + amount);
        int added = after - before;

        if (added <= 0) {
            context.getSource().sendFailure(Component.literal(
                    "§c[昆仑大陆] §e" + target.getName().getString() + " §c的" + coin.displayName()
                            + "已达上限 §e" + fmt(BankAccount.MAX_STORED) + "§c，无法再存入。"));
            return 0;
        }

        BankAccount.setStored(cap, coin, after);
        // 面板是读 capability 的：不同步的话玩家开着银行界面会看到旧余额
        SynsAPI.synsPlayerAttribute(target);

        String name = target.getName().getString();
        String truncated = added < amount
                ? "§7（受上限限制，实际存入 §e" + fmt(added) + "§7 枚）"
                : "";

        context.getSource().sendSuccess(() -> Component.literal(
                "§a[昆仑大陆] 已向 §e" + name + " §a的银行存入 §e" + fmt(added) + " §a枚 §e"
                        + coin.displayName() + "§a" + truncated
                        + "，当前余额：§e" + fmt(after) + "§7 / " + fmt(BankAccount.MAX_STORED)), true);

        target.sendSystemMessage(Component.literal(
                "§6[昆仑银行] §a你的账户收到 §e" + fmt(added) + " §a枚 §e" + coin.displayName()
                        + "§a（管理员发放）。当前余额：§e" + fmt(after)));

        return added;
    }

    /**
     * 解析币种：{@code gold/silver/copper}、{@code 金/银/铜}、{@code 金魂币/银魂币/铜魂币}。
     *
     * @return 解析失败返回 {@code null}
     */
    private static BankAccount.Coin parseCoin(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "gold", "g", "金", "金魂币" -> BankAccount.Coin.GOLD;
            case "silver", "s", "银", "银魂币" -> BankAccount.Coin.SILVER;
            case "copper", "c", "铜", "铜魂币" -> BankAccount.Coin.COPPER;
            // 兜底：按枚举名（GOLD/SILVER/COPPER）再试一次
            default -> {
                for (BankAccount.Coin coin : BankAccount.Coin.values()) {
                    if (coin.name().equalsIgnoreCase(key) || coin.displayName().equals(raw.trim())) {
                        yield coin;
                    }
                }
                yield null;
            }
        };
    }

    /** 带千分位的数字，方便管理员核对（20000000 → 20,000,000）。 */
    private static String fmt(int value) {
        return String.format("%,d", value);
    }
}
