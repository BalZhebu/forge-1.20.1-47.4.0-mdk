package com.TovidY.kunluncontinent.screen.playernpc.shoumai;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * NPC 售卖价格全局注册表。
 * 铜、银、金魂币三者完全独立，互不换算。
 *
 * <p>新增物品只需一行：
 * <pre>{@code
 *     register(Items.IRON_INGOT,   new PriceEntry(30, 0, 0));
 *     register(ModItems.RUBY_ORE.get(), new PriceEntry(0, 5, 2));
 *     register(ModItems.AMETHYST_ORE.get(), new PriceEntry(1, 0, 0));
 * }</pre>
 */

public class SellPriceRegistry {

    public static final class PriceEntry {
        public final int copper;
        public final int silver;
        public final int gold;

        public PriceEntry(int copper, int silver, int gold) {
            this.copper = copper;
            this.silver = silver;
            this.gold = gold;
        }
    }

    private static final Map<Item, PriceEntry> PRICES = new HashMap<>();

    public static void init() {
        // ===== 原版物品示例 =====
        register(com.TovidY.kunluncontinent.item.ModItems.GRAY_IRON_INGOT.get(),  new PriceEntry(30, 0, 0));
        register(com.TovidY.kunluncontinent.item.ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(), new PriceEntry(300, 0, 0));
        register(com.TovidY.kunluncontinent.item.ModItems.RED_FIRE_INGOT.get(),  new PriceEntry(500, 0, 0));
        register(com.TovidY.kunluncontinent.item.ModItems.SUNKEN_SILVER_INGOT.get(), new PriceEntry(400, 0, 0));
        register(com.TovidY.kunluncontinent.item.ModItems.COLD_HEARTED_STEEL_INGOT.get(), new PriceEntry(700, 0, 0));
        register(net.minecraft.world.item.Items.GOLD_INGOT,                     new PriceEntry(0, 1, 0));
        register(net.minecraft.world.item.Items.DIAMOND,                        new PriceEntry(0, 0, 1));
        register(net.minecraft.world.item.Items.COAL,                           new PriceEntry(10, 0, 0));
        register(net.minecraft.world.item.Items.COPPER_INGOT,                   new PriceEntry(15, 0, 0));
        register(net.minecraft.world.item.Items.IRON_INGOT,                     new PriceEntry(30, 0, 0));

    }

    // SellPriceRegistry.java

    public static String getFormattedPrice(Item item) {
        PriceEntry entry = getPrice(item);
        if (entry == null) return null;
        return formatPrice(entry);
    }

    public static void register(Item item, PriceEntry price) {
        PRICES.put(item, price);
    }

    public static PriceEntry getPrice(Item item) {
        return PRICES.getOrDefault(item, null);
    }

    /**
     * 格式化价格显示，三种货币完全独立，不互相换算。
     * (0,0,80)->"80铜"  (0,5,20)->"5银20铜"  (1,0,0)->"1金"  (1,3,7)->"1金3银7铜"
     */
    public static String formatPrice(PriceEntry entry) {
        if (entry == null) return "?";
        StringBuilder sb = new StringBuilder();
        if (entry.gold > 0)  sb.append(entry.gold).append("金魂币");
        if (entry.silver > 0) sb.append(entry.silver).append("银魂币");
        if (entry.copper > 0 || sb.length() == 0) sb.append(entry.copper).append("铜魂币");
        return sb.toString();
    }
}
