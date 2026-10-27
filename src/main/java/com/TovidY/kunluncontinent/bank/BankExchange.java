package com.TovidY.kunluncontinent.bank;

import com.TovidY.kunluncontinent.bank.BankAccount.Coin;
import com.TovidY.kunluncontinent.bank.BankAccount.Result;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import net.minecraft.world.entity.player.Player;

/**
 * 银行换钱：<b>只在银行余额之间倒，不碰玩家背包</b>。
 *
 * <h3>四个方向</h3>
 * <table border="1">
 *   <tr><th>方向</th><th>价格</th><th>等级门槛</th></tr>
 *   <tr><td>金 → 银</td><td>当日汇率（30~75）</td><td>无</td></tr>
 *   <tr><td>银 → 金</td><td>固定 90 银</td><td>&gt; 75 级</td></tr>
 *   <tr><td>银 → 铜</td><td>当日汇率（20~50）</td><td>无</td></tr>
 *   <tr><td>铜 → 银</td><td>固定 60 铜</td><td>&gt; 55 级</td></tr>
 * </table>
 *
 * <h3>单笔上限</h3>
 * <p>除了余额，还受<b>目标币种的 2000 万上限</b>约束：装不下的部分不换（余额不动），
 * 只换能放下的数量，并提示一句，避免"扣了钱却没到账"。</p>
 */
public final class BankExchange {

    /**
     * 每个方向<b>每个汇率日</b>最多换多少次（跨日 05:00 清零）。
     *
     * <p>注意是「次数」不是「数量」：点一次「全部」也只算 1 次。所以想搬大额就调大单次数量，
     * 想限制搬砖就卡次数 —— 两者互不影响。</p>
     */
    public static final int DAILY_LIMIT = 100;

    /**
     * 交易方向。
     *
     * <p>{@code minLevel} 是「解锁等级」，语义为 {@code 玩家等级 >= minLevel}：
     * 用户的原话是"必须超过 55 级"→ 56 级起可用；"超过 75 级"→ 76 级起可用。</p>
     */
    public enum Trade {
        GOLD_TO_SILVER(Coin.GOLD, Coin.SILVER, "金 → 银", 0),
        SILVER_TO_GOLD(Coin.SILVER, Coin.GOLD, "银 → 金", 76),
        SILVER_TO_COPPER(Coin.SILVER, Coin.COPPER, "银 → 铜", 0),
        COPPER_TO_SILVER(Coin.COPPER, Coin.SILVER, "铜 → 银", 56);

        private final Coin from;
        private final Coin to;
        private final String label;
        private final int minLevel;

        Trade(Coin from, Coin to, String label, int minLevel) {
            this.from = from;
            this.to = to;
            this.label = label;
            this.minLevel = minLevel;
        }

        /** 付出（也是界面上画图标用的那一枚）。 */
        public Coin from() {
            return from;
        }

        /** 得到。 */
        public Coin to() {
            return to;
        }

        public String label() {
            return label;
        }

        /** 解锁所需等级；0 = 无限制。 */
        public int minLevel() {
            return minLevel;
        }

        public boolean unlockedAt(int level) {
            return minLevel <= 0 || level >= minLevel;
        }

        /** 是否属于「往上换」（会消耗更高一级的币）。 */
        public boolean isUpgrade() {
            return minLevel > 0;
        }

        public static Trade byIndex(int index) {
            Trade[] all = values();
            return all[Math.floorMod(index, all.length)];
        }
    }

    private BankExchange() {
    }

    /**
     * 执行一次换钱。
     *
     * @param amount         想要换到的<b>目标币数量</b>（{@code <= 0} = 全部能换的）
     * @param silverPerGold  当日汇率（金→银）
     * @param copperPerSilver 当日汇率（银→铜）
     */
    public static Result trade(Player player, Trade trade, int amount,
                               int silverPerGold, int copperPerSilver) {
        PlayerAttributeCapability cap = BankAccount.capability(player);
        if (cap == null) {
            return Result.failure("银行账户读取失败");
        }
        int level = ModAttributeAPI.getDengji(player);
        if (!trade.unlockedAt(level)) {
            return Result.failure(trade.label() + "需要 §e" + trade.minLevel() + " 级§c 以上才能进行");
        }

        // 先把「今天是不是新的一天」判掉，再判次数 —— 顺序反了的话跨日第一次会被误拦
        long day = BankRates.dayIndex(player.level().getDayTime());
        ensureTradeDay(cap, day);

        int used = cap.getBankTradeCount(trade.name());
        if (used >= DAILY_LIMIT) {
            return Result.failure("今日「" + trade.label() + "」已换满 " + DAILY_LIMIT
                    + " 次，明日 05:00 重置");
        }

        int gold = BankAccount.getStored(cap, Coin.GOLD);
        int silver = BankAccount.getStored(cap, Coin.SILVER);
        int copper = BankAccount.getStored(cap, Coin.COPPER);

        Result result = switch (trade) {
            // 向下换：用当日浮动汇率，1 单位能换 rate 个低一级币
            case GOLD_TO_SILVER -> sellDown(cap, Coin.GOLD, gold, Coin.SILVER, silver,
                    Math.max(1, silverPerGold), amount);
            case SILVER_TO_COPPER -> sellDown(cap, Coin.SILVER, silver, Coin.COPPER, copper,
                    Math.max(1, copperPerSilver), amount);
            // 向上换：固定买价，不随汇率浮动（防跨日套利）
            case SILVER_TO_GOLD -> buyUp(cap, Coin.SILVER, silver, Coin.GOLD, gold,
                    BankRates.GOLD_BUY_PRICE_SILVER, amount);
            case COPPER_TO_SILVER -> buyUp(cap, Coin.COPPER, copper, Coin.SILVER, silver,
                    BankRates.SILVER_BUY_PRICE_COPPER, amount);
        };

        // 只有真的成交才计一次
        if (result.success()) {
            cap.setBankTradeCount(trade.name(), used + 1);
        }
        return result;
    }

    // ==================================================================================
    //  每日次数
    // ==================================================================================

    /** 服务端：跨日就把所有方向的计数清零（开面板时也调一次，好让客户端显示正确的剩余次数）。 */
    public static void ensureTradeDay(Player player) {
        PlayerAttributeCapability cap = BankAccount.capability(player);
        if (cap == null) return;
        ensureTradeDay(cap, BankRates.dayIndex(player.level().getDayTime()));
    }

    private static void ensureTradeDay(PlayerAttributeCapability cap, long day) {
        if (cap.getBankTradeDay() != day) {
            cap.setBankTradeDay(day);
            cap.resetBankTradeCounts();
        }
    }

    /** 今天这个方向还剩几次（客户端也能读，数据来自同步过去的 capability）。 */
    public static int remainingToday(Player player, Trade trade) {
        PlayerAttributeCapability cap = BankAccount.capability(player);
        int used = cap == null ? 0 : cap.getBankTradeCount(trade.name());
        return Math.max(0, DAILY_LIMIT - used);
    }

    /** 卖出高一级币，按当日汇率换低一级。 */
    private static Result sellDown(PlayerAttributeCapability cap, Coin from, int fromBal,
                                   Coin to, int toBal, int rate, int amount) {
        if (fromBal <= 0) {
            return Result.failure("银行里没有" + from.displayName());
        }
        int room = BankAccount.MAX_STORED - toBal;
        if (room <= 0) {
            return Result.failure(to.displayName() + "库已满（" + BankAccount.fmt(BankAccount.MAX_STORED) + "）");
        }

        long want = amount <= 0 ? fromBal : Math.min(amount, fromBal);
        long byRoom = room / rate;          // 库位最多容得下多少个「源币」
        long use = Math.min(want, byRoom);
        if (use <= 0) {
            return Result.failure(to.displayName() + "库位不足一次兑换（1 " + from.displayName()
                    + " = " + rate + " " + to.displayName() + "）");
        }

        long gain = use * rate;
        BankAccount.setStored(cap, from, (int) (fromBal - use));
        BankAccount.setStored(cap, to, (int) (toBal + gain));

        StringBuilder msg = new StringBuilder()
                .append("卖出 ").append(BankAccount.fmt((int) use)).append(" 枚").append(from.displayName())
                .append("，得到 ").append(BankAccount.fmt((int) gain)).append(" 枚").append(to.displayName())
                .append("（1:").append(rate).append("）");
        if (use < want) {
            msg.append(" §7[库位只够换 ").append(BankAccount.fmt((int) use)).append("]");
        }
        return Result.success((int) Math.min(Integer.MAX_VALUE, gain), msg.toString());
    }

    /** 用低一级币买入高一级币，价格为固定值。 */
    private static Result buyUp(PlayerAttributeCapability cap, Coin pay, int payBal,
                                Coin get, int getBal, int price, int amount) {
        if (payBal <= 0) {
            return Result.failure("银行里没有" + pay.displayName());
        }
        int room = BankAccount.MAX_STORED - getBal;
        if (room <= 0) {
            return Result.failure(get.displayName() + "库已满（" + BankAccount.fmt(BankAccount.MAX_STORED) + "）");
        }

        long affordable = payBal / price;
        long want = amount <= 0 ? affordable : Math.min(amount, affordable);
        long use = Math.min(want, room);
        if (use <= 0) {
            if (affordable <= 0) {
                return Result.failure(pay.displayName() + "不够：1 " + get.displayName()
                        + " = " + price + " " + pay.displayName());
            }
            return Result.failure(get.displayName() + "库已满（" + BankAccount.fmt(BankAccount.MAX_STORED) + "）");
        }

        long cost = use * price;
        BankAccount.setStored(cap, pay, (int) (payBal - cost));
        BankAccount.setStored(cap, get, (int) (getBal + use));

        String msg = "用 " + BankAccount.fmt((int) cost) + " 枚" + pay.displayName()
                + " 换入 " + BankAccount.fmt((int) use) + " 枚" + get.displayName()
                + "（1:" + price + "）";
        return Result.success((int) Math.min(Integer.MAX_VALUE, use), msg);
    }
}
