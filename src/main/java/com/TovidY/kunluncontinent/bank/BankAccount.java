package com.TovidY.kunluncontinent.bank;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * 昆仑银行：金 / 银 / 铜魂币的存取逻辑（<b>纯业务层，不碰界面也不发包</b>）。
 *
 * <h3>账户存在哪</h3>
 * <p>存在玩家 capability（{@code PlayerAttributeCapability} 的 {@code bankGold/Silver/Copper}），
 * <b>不是</b>方块的 BlockEntity。理由：</p>
 * <ul>
 *   <li>银行是「<b>个人账户</b>」，村庄里生成的每一座银行都只是同一个账户的入口，
 *       玩家换一座银行不用搬家；</li>
 *   <li>随玩家 NBT 走 → 死亡不掉钱；</li>
 *   <li>随 {@code SPacketPlayerAttribute} 同步 → 面板不用额外的同步包。</li>
 * </ul>
 *
 * <h3>为什么「取出」会被背包卡住</h3>
 * <p>魂币是<b>普通物品</b>，一种魂币最多 64 个/格。空背包也只有 36 格 = 2304 枚，
 * 而金库上限是 2000 万 —— 一次取不完是必然的。所以取出的规则是：</p>
 * <pre>
 *   实际取出 = min(想要的数量, 银行余额, 背包剩余空间)
 * </pre>
 * <p>取不完的部分<b>留在银行</b>，不会掉在地上（掉地上等于送给别人）。</p>
 */
public final class BankAccount {

    /** 每种魂币的存储上限（2000 万）。 */
    public static final int MAX_STORED = 20_000_000;

    /**
     * {@code configFlags} 里「交易自动取币」的位。
     *
     * <p><b>默认开启</b>（见 {@code DEFAULT_CONFIG_FLAGS}）。开启时，在商人界面点左侧商品、
     * 而背包魂币不够，会自动从银行取出差额（{@link BankAutoTrade}）；
     * 关闭后完全走原版逻辑，钱不够就是买不了。</p>
     *
     * <p>⚠️ 0~15 已被 {@code ConfigScreen} 占用，新位从 16 起；加位记得同步
     * {@code PlayerAttributeCapability.CONFIG_FLAGS_VERSION}（老存档补成默认值）。</p>
     */
    public static final int CONFIG_AUTO_TRADE = 16;

    private BankAccount() {
    }

    // ==================================================================================
    //  币种
    // ==================================================================================

    /**
     * 三种魂币。顺序就是面板从上到下的顺序（金 → 银 → 铜），也是「换钱」的汇率方向。
     *
     * <p>物品用 {@link Supplier} 延迟取：枚举常量可能被界面在很早的时机碰到，
     * 直接写 {@code ModItems.X.get()} 有踩到「注册表还没填好」的风险。</p>
     */
    public enum Coin {
        GOLD("金魂币", () -> ModItems.GOLDEN_SOUL_COIN.get(), 0xFFD24A),
        SILVER("银魂币", () -> ModItems.SILVER_SOUL_COIN.get(), 0xD9E2EC),
        COPPER("铜魂币", () -> ModItems.COPPER_SOUL_COIN.get(), 0xE08A4B);

        private final String displayName;
        private final Supplier<Item> item;
        private final int color;

        Coin(String displayName, Supplier<Item> item, int color) {
            this.displayName = displayName;
            this.item = item;
            this.color = color;
        }

        /** 显示名（金魂币 / 银魂币 / 铜魂币）。 */
        public String displayName() {
            return displayName;
        }

        /** 对应的物品。 */
        public Item item() {
            return item.get();
        }

        /** 界面配色（0xRRGGBB）。 */
        public int color() {
            return color;
        }

        /** 安全取枚举（网络包里传的就是序号）。 */
        public static Coin byIndex(int index) {
            Coin[] all = values();
            return all[Math.floorMod(index, all.length)];
        }

        /**
         * 按物品反查币种；不是魂币返回 {@code null}。
         *
         * <p>交易自动取币用它判断「这个价格要不要从银行补」——
         * 原版村民的绿宝石价格、玩家自己设的杂项价格都会返回 {@code null}，直接跳过。</p>
         */
        public static Coin byItem(Item item) {
            if (item == null) {
                return null;
            }
            for (Coin coin : values()) {
                if (coin.item() == item) {
                    return coin;
                }
            }
            return null;
        }
    }

    // ==================================================================================
    //  余额读写
    // ==================================================================================

    /** 该玩家 capability；取不到返回 {@code null}（理论上不会发生）。 */
    public static PlayerAttributeCapability capability(Player player) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
    }

    public static int getStored(PlayerAttributeCapability cap, Coin coin) {
        if (cap == null) return 0;
        return switch (coin) {
            case GOLD -> cap.getBankGold();
            case SILVER -> cap.getBankSilver();
            case COPPER -> cap.getBankCopper();
        };
    }

    public static void setStored(PlayerAttributeCapability cap, Coin coin, int value) {
        if (cap == null) return;
        int v = Math.max(0, Math.min(MAX_STORED, value));
        switch (coin) {
            case GOLD -> cap.setBankGold(v);
            case SILVER -> cap.setBankSilver(v);
            case COPPER -> cap.setBankCopper(v);
        }
    }

    /** 便捷方法：直接读某个玩家的余额（客户端面板也走这个）。 */
    public static int getStored(Player player, Coin coin) {
        return getStored(capability(player), coin);
    }

    // ==================================================================================
    //  背包统计
    // ==================================================================================

    /**
     * 背包里一共有多少枚该魂币。
     *
     * <p>把 41 格里所有槽位都算上（含盔甲/副手）：魂币放不进盔甲槽，
     * 所以多算不会偏，但能保证<b>副手持有的魂币也能存</b>。</p>
     */
    public static int countInInventory(Player player, Coin coin) {
        Inventory inv = player.getInventory();
        Item item = coin.item();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /**
     * 背包还能装下多少枚该魂币（<b>按 64 上限算</b>）。
     *
     * <p>= 空槽位数 × 64 + 已有堆叠的空位。这是「取出」的上限来源，
     * 界面会把它显示成「可取出」。</p>
     */
    public static int freeSpace(Player player, Coin coin) {
        Inventory inv = player.getInventory();
        Item item = coin.item();
        int space = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                space += 64;
            } else if (stack.is(item)) {
                space += Math.max(0, stack.getMaxStackSize() - stack.getCount());
            }
        }
        return space;
    }

    // ==================================================================================
    //  存入 / 取出
    // ==================================================================================

    /**
     * 把背包里的魂币存进银行。
     *
     * @param want 想存多少；{@code <= 0} 表示「全部」
     */
    public static Result deposit(Player player, Coin coin, int want) {
        PlayerAttributeCapability cap = capability(player);
        if (cap == null) {
            return Result.failure("银行账户读取失败");
        }
        int stored = getStored(cap, coin);
        int room = MAX_STORED - stored;
        if (room <= 0) {
            return Result.failure(coin.displayName() + "库已满（" + fmt(MAX_STORED) + "）");
        }
        int have = countInInventory(player, coin);
        if (have <= 0) {
            return Result.failure("背包里没有" + coin.displayName());
        }

        int move = Math.min(want <= 0 ? have : want, have);
        boolean clippedByBank = move > room && room < have;
        move = Math.min(move, room);
        if (move <= 0) {
            return Result.failure(coin.displayName() + "库已满（" + fmt(MAX_STORED) + "）");
        }

        int moved = takeFromInventory(player, coin, move);
        if (moved <= 0) {
            return Result.failure("背包里没有" + coin.displayName());
        }
        setStored(cap, coin, stored + moved);

        String extra = clippedByBank ? "（库位只剩 " + fmt(room) + "）" : "";
        return Result.success(moved, "已存入 " + fmt(moved) + " 枚" + coin.displayName() + extra);
    }

    /**
     * 从银行取魂币。
     *
     * <p><b>取不完的部分留在银行</b>：锚定的是背包剩余空间，取不到的不会掉在地上。</p>
     *
     * @param want 想取多少；{@code <= 0} 表示「全部」（= 余额与背包空间取小）
     */
    public static Result withdraw(Player player, Coin coin, int want) {
        PlayerAttributeCapability cap = capability(player);
        if (cap == null) {
            return Result.failure("银行账户读取失败");
        }
        int stored = getStored(cap, coin);
        if (stored <= 0) {
            return Result.failure("银行里没有" + coin.displayName());
        }
        int free = freeSpace(player, coin);
        if (free <= 0) {
            return Result.failure("背包已满，先腾出空位");
        }

        int wantClamped = want <= 0 ? stored : Math.min(want, stored);
        int give = Math.min(wantClamped, free);
        int given = giveToInventory(player, coin, give);
        if (given <= 0) {
            return Result.failure("背包已满，先腾出空位");
        }
        setStored(cap, coin, stored - given);

        if (given < wantClamped) {
            return Result.success(given, "背包空间不足，只取出 " + fmt(given)
                    + " 枚" + coin.displayName() + "（要 " + fmt(wantClamped) + " 枚）");
        }
        return Result.success(given, "已取出 " + fmt(given) + " 枚" + coin.displayName());
    }

    // ==================================================================================
    //  底层搬运
    // ==================================================================================

    /** 从背包里拿走 {@code amount} 枚，返回实际拿走多少。 */
    private static int takeFromInventory(Player player, Coin coin, int amount) {
        Inventory inv = player.getInventory();
        Item item = coin.item();
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !stack.is(item)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
            if (stack.isEmpty()) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        int moved = amount - remaining;
        if (moved > 0) {
            inv.setChanged();
        }
        return moved;
    }

    /**
     * 往背包里塞 {@code amount} 枚，返回实际塞进去多少。
     *
     * <p>分批 64 个塞：{@link Inventory#add(ItemStack)} 只会「能塞多少塞多少」，
     * 并把剩余数量写回传入的 {@code stack}，所以用 {@code 批次 - 剩余} 就是这一批进去的数量。</p>
     */
    private static int giveToInventory(Player player, Coin coin, int amount) {
        Inventory inv = player.getInventory();
        Item item = coin.item();
        int remaining = amount;
        while (remaining > 0) {
            int batch = Math.min(remaining, item.getMaxStackSize());
            ItemStack stack = new ItemStack(item, batch);
            inv.add(stack);
            int placed = batch - stack.getCount();
            remaining -= placed;
            if (placed <= 0) {
                break; // 塞不进去了，防止死循环
            }
        }
        int given = amount - remaining;
        if (given > 0) {
            inv.setChanged();
        }
        return given;
    }

    // ==================================================================================
    //  杂项
    // ==================================================================================

    /** 千分位格式：{@code 20000000 → "20,000,000"}。 */
    public static String fmt(int value) {
        return String.format("%,d", value);
    }

    // ==================================================================================
    //  汇率显示
    // ==================================================================================

    /**
     * 服务端：把「当前这一汇率日」的汇率写进 capability，好随属性包同步给客户端。
     *
     * <p>调用点只有两个：开面板前、换完钱后 —— <b>不需要每日定时器</b>，
     * 因为汇率本身是由时间算出来的（见 {@link BankRates}）。</p>
     */
    public static void refreshRates(ServerPlayer player) {
        PlayerAttributeCapability cap = capability(player);
        if (cap == null) return;
        long day = BankRates.dayIndex(player.serverLevel().getDayTime());
        cap.setBankRateSilverPerGold(BankRates.silverPerGold(day));
        cap.setBankRateCopperPerSilver(BankRates.copperPerSilver(day));
    }

    /** 客户端可用：1 金 = ? 银。缓存为 0 时按本地时间自己算（老存档 / 还没同步过）。 */
    public static int rateSilverPerGold(Player player) {
        PlayerAttributeCapability cap = capability(player);
        if (cap != null && cap.getBankRateSilverPerGold() > 0) {
            return cap.getBankRateSilverPerGold();
        }
        return BankRates.silverPerGold(BankRates.dayIndex(player.level().getDayTime()));
    }

    /** 客户端可用：1 银 = ? 铜。 */
    public static int rateCopperPerSilver(Player player) {
        PlayerAttributeCapability cap = capability(player);
        if (cap != null && cap.getBankRateCopperPerSilver() > 0) {
            return cap.getBankRateCopperPerSilver();
        }
        return BankRates.copperPerSilver(BankRates.dayIndex(player.level().getDayTime()));
    }

    /**
     * 一次存取的结果。
     *
     * @param success 是否真的动了账
     * @param amount  实际存/取的枚数
     * @param message 给玩家看的一句话（不带颜色码，调用方自己上色）
     */
    public record Result(boolean success, int amount, String message) {
        public static Result success(int amount, String message) {
            return new Result(true, amount, message);
        }

        public static Result failure(String message) {
            return new Result(false, 0, message);
        }
    }
}
