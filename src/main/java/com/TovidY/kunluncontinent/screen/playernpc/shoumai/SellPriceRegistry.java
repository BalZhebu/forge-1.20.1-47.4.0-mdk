package com.TovidY.kunluncontinent.screen.playernpc.shoumai;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanQuality;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    private static final Map<Item, Map<NeidanQuality, PriceEntry>> NEIDAN_PRICES = new HashMap<>();

    public static void init() {
        // ===== 原版/基础物品 =====
        register(ModItems.GRAY_IRON_INGOT.get(),  new PriceEntry(1, 0, 0));
        register(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(), new PriceEntry(2, 0, 0));
        register(ModItems.RED_FIRE_INGOT.get(),  new PriceEntry(10, 0, 0));
        register(ModItems.SUNKEN_SILVER_INGOT.get(), new PriceEntry(0, 2, 0));
        register(ModItems.COLD_HEARTED_STEEL_INGOT.get(), new PriceEntry(0, 4, 0));
        register(ModItems.RINSEI_INGOT.get(),  new PriceEntry(0, 7, 0));

        register(Items.DIAMOND ,new PriceEntry(3, 1, 0));
        register(Items.NETHERITE_INGOT, new PriceEntry(0, 5, 0));

        // ===== 1阶到9阶 内丹品质独立价格配置 (共 54 种组合) =====

        // --- 1阶内丹 (NEIDAN1)十年 ---
        registerNeidan(ModItems.NEIDAN1.get(), NeidanQuality.FAN,   new PriceEntry(1, 0, 0));
        registerNeidan(ModItems.NEIDAN1.get(), NeidanQuality.LIANG, new PriceEntry(3, 0, 0));
        registerNeidan(ModItems.NEIDAN1.get(), NeidanQuality.SHANG, new PriceEntry(5, 0, 0));
        registerNeidan(ModItems.NEIDAN1.get(), NeidanQuality.ZHEN,  new PriceEntry(8, 0, 0));
        registerNeidan(ModItems.NEIDAN1.get(), NeidanQuality.JUE,   new PriceEntry(10, 0, 0));

        // --- 2阶内丹 (NEIDAN2)百年 ---
        registerNeidan(ModItems.NEIDAN2.get(), NeidanQuality.FAN,   new PriceEntry(5, 0, 0));
        registerNeidan(ModItems.NEIDAN2.get(), NeidanQuality.LIANG, new PriceEntry(8, 0, 0));
        registerNeidan(ModItems.NEIDAN2.get(), NeidanQuality.SHANG, new PriceEntry(13, 0, 0));
        registerNeidan(ModItems.NEIDAN2.get(), NeidanQuality.ZHEN,  new PriceEntry(15, 0, 0));
        registerNeidan(ModItems.NEIDAN2.get(), NeidanQuality.JUE,   new PriceEntry(0, 1, 0));

        // --- 3阶内丹 (NEIDAN3)千年 ---
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.FAN,   new PriceEntry(8, 0, 0));
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.LIANG, new PriceEntry(13, 0, 0));
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.SHANG, new PriceEntry(20, 0, 0));
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.ZHEN,  new PriceEntry(32, 1, 0));
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.JUE,   new PriceEntry(0, 2, 0));
        registerNeidan(ModItems.NEIDAN3.get(), NeidanQuality.XIAN,  new PriceEntry(0, 3, 0));

        // --- 4阶内丹 (NEIDAN4)万年 ---
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.FAN,   new PriceEntry(15, 0, 0));
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.LIANG, new PriceEntry(32, 0, 0));
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.SHANG, new PriceEntry(64, 3, 0));
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.ZHEN,  new PriceEntry(10, 5, 0));
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.JUE,   new PriceEntry(20, 8, 0));
        registerNeidan(ModItems.NEIDAN4.get(), NeidanQuality.XIAN,  new PriceEntry(0, 10, 0));

        // --- 5阶内丹 (NEIDAN5)十万年 ---
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.FAN,   new PriceEntry(0, 1, 0));
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.LIANG, new PriceEntry(0, 3, 0));
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.SHANG, new PriceEntry(0, 5, 0));
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.ZHEN,  new PriceEntry(0, 10, 0));
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.JUE,   new PriceEntry(0, 30, 0));  // 1金5银
        registerNeidan(ModItems.NEIDAN5.get(), NeidanQuality.XIAN,  new PriceEntry(0, 10, 1));  // 5金

        // --- 6阶内丹 (NEIDAN6)百万年 ---
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.FAN,   new PriceEntry(0, 10, 0));
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.LIANG, new PriceEntry(0, 20, 1));
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.SHANG, new PriceEntry(0, 30, 2));
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.ZHEN,  new PriceEntry(0, 40, 3));
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.JUE,   new PriceEntry(0, 30, 5));
        registerNeidan(ModItems.NEIDAN6.get(), NeidanQuality.XIAN,  new PriceEntry(0, 20, 10));

        // --- 7阶内丹 (NEIDAN7) ---
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.FAN,   new PriceEntry(0, 20, 1));
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.LIANG, new PriceEntry(0, 40, 3));
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.SHANG, new PriceEntry(0, 64, 5));
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.ZHEN,  new PriceEntry(0, 0, 15));
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.JUE,   new PriceEntry(0, 0, 20));
        registerNeidan(ModItems.NEIDAN7.get(), NeidanQuality.XIAN,  new PriceEntry(0, 0, 35));

        // --- 8阶内丹 (NEIDAN8) ---
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.FAN,   new PriceEntry(14, 45, 11));
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.LIANG, new PriceEntry(14, 45, 11));
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.SHANG, new PriceEntry(14, 45, 11));
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.ZHEN,  new PriceEntry(14, 45, 11));
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.JUE,   new PriceEntry(14, 45, 11));
        registerNeidan(ModItems.NEIDAN8.get(), NeidanQuality.XIAN,  new PriceEntry(14, 45, 11));

        // --- 9阶内丹 (NEIDAN9) ---
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.FAN,   new PriceEntry(3, 0, 0));
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.LIANG, new PriceEntry(8, 0, 0));
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.SHANG, new PriceEntry(18, 0, 0));
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.ZHEN,  new PriceEntry(40, 0, 0));
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.JUE,   new PriceEntry(70, 0, 0));
        registerNeidan(ModItems.NEIDAN9.get(), NeidanQuality.XIAN,  new PriceEntry(100, 0, 0)); // 100金
    }

    /**
     * 注册内丹独立品质价格
     */
    public static void registerNeidan(Item item, NeidanQuality quality, PriceEntry price) {
        NEIDAN_PRICES.computeIfAbsent(item, k -> new HashMap<>()).put(quality, price);
    }

    /**
     * 注册普通物品价格
     */
    public static void register(Item item, PriceEntry price) {
        PRICES.put(item, price);
    }

    /**
     * 支持优先读取 ItemStack 内丹 NBT 品质对应的专属价格
     */
    public static PriceEntry getPrice(ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.getItem() instanceof NeidanItem neidan) {
            NeidanQuality quality = neidan.getQuality(stack);
            Map<NeidanQuality, PriceEntry> qualityMap = NEIDAN_PRICES.get(stack.getItem());
            if (qualityMap != null && qualityMap.containsKey(quality)) {
                return qualityMap.get(quality);
            }
        }

        return getPrice(stack.getItem());
    }

    public static PriceEntry getPrice(Item item) {
        return PRICES.getOrDefault(item, null);
    }

    public static String getFormattedPrice(ItemStack stack) {
        PriceEntry entry = getPrice(stack);
        if (entry == null) return null;
        return formatPrice(entry);
    }

    public static String getFormattedPrice(Item item) {
        PriceEntry entry = getPrice(item);
        if (entry == null) return null;
        return formatPrice(entry);
    }

    /**
     * 格式化价格显示，三种货币独立显示
     */
    public static String formatPrice(PriceEntry entry) {
        if (entry == null) return "?";
        StringBuilder sb = new StringBuilder();
        if (entry.gold > 0)   sb.append(entry.gold).append("金魂币");
        if (entry.silver > 0) sb.append(entry.silver).append("银魂币");
        if (entry.copper > 0 || sb.length() == 0) sb.append(entry.copper).append("铜魂币");
        return sb.toString();
    }
}