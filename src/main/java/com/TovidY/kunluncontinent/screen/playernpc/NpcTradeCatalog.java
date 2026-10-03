package com.TovidY.kunluncontinent.screen.playernpc;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.*;

/**
 * NPC 交易目录生成器。
 * 交易规则：
 *   - 每个交易槽 = 1 件物品 ←→ N 个货币物品，货币与价格均在 TradeEntry 中定义
 *   - 交易槽数量随 NPC 等级增长：min=1, max=12
 *   - 品质随等级决定：低等级 NPC 几乎出不了金，85 级以上必定至少 1 个金色交易
 *
 * <p><b>⚠️ 品质标签的存放位置：</b>交易结果 ItemStack <b>绝不能挂任何 NBT</b>
 * （例如早期的 {@code KlNpcQuality}）——NBT 参与 {@link ItemStack} 的堆叠判定，
 * 一旦挂上，买来的物品就永远无法和正常途径获得的同名物品合并。
 * 交易列表的品级染色改由 {@link #getOfferQualityColor(MerchantOffer)} 从
 * 「货币 + 价格 + 物品」反查，见 {@link #BY_OFFER}。</p>
 */

public class NpcTradeCatalog {

    private static final Random RAND = new Random();

    private NpcTradeCatalog() {}

    // ── 品质定义 ─────────────────────────────────────
    public record TradeEntry(Item currency, int cost, Item item, int perTrade) {}

    private static final class Quality {
        final String name;         // 品质名
        final int priority;        // 【新增】：品质优先级/权重，越大越高级（金=6, 红=5 ... 白=1）
        final int color;           // 交易列表上的品级染色（ARGB），原本散落在 UI 里，现归到品质定义处
        final int minLevel;        // 该品质首次出现的 NPC 最低等级
        final int maxLevel;        // 该品质最后一次出现的 NPC 最高等级（-1 = 无上限）
        final List<TradeEntry> pool;

        Quality(String name, int priority, int color, int minLevel, int maxLevel, List<TradeEntry> pool) {
            this.name = name;
            this.priority = priority;
            this.color = color;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.pool = pool;
        }
    }

    /** 白：1-20 级 NPC 专属 */
    private static final Quality WHITE = new Quality("白", 1, 0x55FFFFFF, 1, 20, List.of(
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 12, ModItems.GRAY_IRON_INGOT.get().asItem(),                  8),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 20, ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get().asItem(),      5),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 5, ModItems.DROSS.get().asItem(),                             1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.RUBY.get().asItem(),                            1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 15, ModItems.AMETHYST.get().asItem(),                          1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.SAPPHIRE.get().asItem(),                          2),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 64, ModItems.STRONG_POTION.get().asItem(),                          1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 64, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 8, ModBlocks.PUTUAN_BLOCK.get().asItem(), 1)


    ));

    /** 蓝：10 级起可出现 */
    private static final Quality BLUE = new Quality("蓝", 2, 0x555555FF, 10, 40, List.of(
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 20, ModItems.RED_FIRE_INGOT.get().asItem(),                  1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 5, ModItems.RED_FIRE_INGOT.get().asItem(),              1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 8, ModItems.QIANHUABAO_DAN.get().asItem(),                1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 4, ModItems.BAICAOLING_DAN.get().asItem(),                   1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModBlocks.PUTUAN_BLOCK.get().asItem(),                  1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 25, ModItems.BAICAOLING_DAN.get().asItem(),                 1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.NEIDAN1.get().asItem(),                           10),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.NEIDAN2.get().asItem(),                           5),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.NEIDAN3.get().asItem(),                           1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 50, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 10, ModItems.STRONG_POTION.get().asItem(),                          1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 15, ModItems.NEIDAN4.get().asItem(),                           1)

    ));

    /** 紫：25 级起可出现 */
    private static final Quality PURPLE = new Quality("紫", 3, 0x55AA44FF, 25, 60, List.of(
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 22, ModItems.RED_FIRE_HELMET.get().asItem(),     1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 30, ModItems.RED_FIRE_CHESTPLATE.get().asItem(), 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 25, ModItems.RED_FIRE_LEGGINGS.get().asItem(),    1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 15, ModItems.RED_FIRE_BOOTS.get().asItem(),       1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.NEIDAN1.get().asItem(),                           20),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.NEIDAN2.get().asItem(),                           10),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 5, ModItems.STRONG_POTION.get().asItem(),                          1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.NEIDAN3.get().asItem(),                           5),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.NEIDAN4.get().asItem(),                           1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 50, ModItems.LIGHTNING_FRAGMENTS.get().asItem(),               1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 32, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 5, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 10, ModItems.NEIDAN4.get().asItem(),                           1)

    ));

    /** 黑：45 级起可出现 */
    private static final Quality BLACK = new Quality("黑", 4, 0x77222222, 45, 80, List.of(
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 17, ModItems.RED_FIRE_HELMET.get().asItem(),                   1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 25, ModItems.RED_FIRE_CHESTPLATE.get().asItem(),              1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 20, ModItems.RED_FIRE_LEGGINGS.get().asItem(),                 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 10, ModItems.RED_FIRE_BOOTS.get().asItem(),                    1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 100, ModItems.SOUL_GATHERING_BOTTLE_4.get().asItem(),             1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 70, ModItems.SOUL_GATHERING_BOTTLE_3.get().asItem(),         1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 4, ModItems.NEIDAN3.get().asItem(),                           8),
            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 16, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 3, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 2, ModItems.NEIDAN4.get().asItem(),                           3)
    ));

    /** 红：65 级起可出现 */
    private static final Quality RED = new Quality("红", 5, 0x55FF2222, 65, -1, List.of(
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 5, ModItems.COLD_HEARTED_STEEL_INGOT.get().asItem(),         1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 3, ModItems.RINSEI_INGOT.get().asItem(),                     1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 30, ModItems.COLD_HEARTED_STEEL_INGOT.get().asItem(),           1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 15, ModItems.SUNKEN_SILVER_INGOT.get().asItem(),               1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 60, ModItems.SUNKEN_SILVER_HELMET.get().asItem(),             1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 100, ModItems.SUNKEN_SILVER_CHESTPLATE.get().asItem(),         1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 95, ModItems.SUNKEN_SILVER_LEGGINGS.get().asItem(),           1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 55, ModItems.SUNKEN_SILVER_BOOTS.get().asItem(),               1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 120, ModItems.FOUR_DECOMPOSITION_GOSSIP.get().asItem(),          1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 80, ModItems.SOUL_GATHERING_BOTTLE_4.get().asItem(),       1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 10, ModItems.NEIDAN5.get().asItem(),                          1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.RESET_SCROLL.get().asItem(), 1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 1, ModItems.NEIDAN6.get().asItem(),                          5)
    ));

    /** 金：80 级起可出现（85 级以上 NPC 必定至少包含 1 个） */
    private static final Quality GOLD = new Quality("金", 6, 0x55FFCC00, 80, -1, List.of(
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 10, ModItems.NEIDAN7.get().asItem(),                         1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 50, ModItems.NEIDAN8.get().asItem(),                         1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 60, ModItems.NEIDAN9.get().asItem(),                         1),

            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 1, ModItems.RESET_SCROLL.get().asItem(), 1),

            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 22, ModItems.COLD_HEARTED_STEEL_HELMET.get().asItem(),        1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 35, ModItems.COLD_HEARTED_STEEL_CHESTPLATE.get().asItem(),   1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 30, ModItems.COLD_HEARTED_STEEL_LEGGINGS.get().asItem(),     1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 18, ModItems.COLD_HEARTED_STEEL_BOOTS.get().asItem(),        1),

            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 20, ModItems.STARLIGHT_STONE.get().asItem(),                 1),

            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 20, ModItems.RINSEI_FORGING_TEMPLATE.get().asItem(),         1),

            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 64, ModItems.EYE_TRANSFORMATION.get().asItem(),              1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 15, ModItems.EYE_TRANSFORMATION.get().asItem(),              1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 3, ModItems.EYE_TRANSFORMATION.get().asItem(),              1),

            new TradeEntry(ModItems.COPPER_SOUL_COIN.get(), 128, ModItems.EYE_DEEP_SEA.get().asItem(),                    1),
            new TradeEntry(ModItems.SILVER_SOUL_COIN.get(), 64, ModItems.EYE_DEEP_SEA.get().asItem(),                    1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 10, ModItems.EYE_DEEP_SEA.get().asItem(),                    1),

            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 25, ModItems.GUOSHI_BAHUANGJI.get().asItem(),                1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 20, ModItems.GUOSHI_LEIJINHU.get().asItem(),                 1),
            new TradeEntry(ModItems.GOLDEN_SOUL_COIN.get(), 25, ModItems.GUOSHI_PANSHIJUYUAN.get().asItem(),             1)

    ));

    private static final List<Quality> QUALITIES = List.of(WHITE, BLUE, PURPLE, BLACK, RED, GOLD);

    /** 一笔交易的唯一标识：货币 + 总价 + 物品。已验证 68 条 TradeEntry 里零冲突。 */
    private record OfferKey(Item currency, int cost, Item result) {}

    /** (货币, 价格, 物品) → 品质。这是精确反查表。 */
    private static final Map<OfferKey, Quality> BY_OFFER = buildOfferLookup();

    /** 物品 → 品质。只在精确反查失败时兜底（同一物品可能出现在多个品质里，仅作保底）。 */
    private static final Map<Item, Quality> BY_ITEM = buildItemLookup();

    private static Map<OfferKey, Quality> buildOfferLookup() {
        Map<OfferKey, Quality> map = new HashMap<>();
        for (Quality q : QUALITIES) {
            for (TradeEntry e : q.pool) {
                Quality prev = map.putIfAbsent(new OfferKey(e.currency(), e.cost(), e.item()), q);
                if (prev != null && prev != q) {
                    KlMain.LOGGER.warn("[昆仑大陆] 交易品质反查冲突：条目 {} 用 {} 个 {} 兑换，同时属于『{}』与『{}』，" +
                                    "交易列表的品级染色会取前者。请给这两条 TradeEntry 换一个价格。",
                            e.item(), e.cost(), e.currency(), prev.name, q.name);
                }
            }
        }
        return map;
    }

    private static Map<Item, Quality> buildItemLookup() {
        Map<Item, Quality> map = new HashMap<>();
        for (Quality q : QUALITIES) {
            for (TradeEntry e : q.pool) {
                map.putIfAbsent(e.item(), q);
            }
        }
        return map;
    }

    /**
     * 反查一笔交易的品级展示色（ARGB）。返回 {@code 0} 表示没有品级 / 未能识别。
     *
     * <p>用反查而不是读物品 NBT 的原因：给交易结果挂 NBT 会让买到的物品
     * 与正常获得的同名物品**无法堆叠**（NBT 参与等价判定）。</p>
     */
    public static int getOfferQualityColor(MerchantOffer offer) {
        Quality q = findQuality(offer);
        return q == null ? 0 : q.color;
    }

    /**
     * 反查一笔交易的品级名（白/蓝/紫/黑/红/金）。返回 {@code null} 表示未能识别。
     */
    public static String getOfferQualityName(MerchantOffer offer) {
        Quality q = findQuality(offer);
        return q == null ? null : q.name;
    }

    private static Quality findQuality(MerchantOffer offer) {
        if (offer == null) {
            return null;
        }
        ItemStack result = offer.getResult();
        ItemStack costA = offer.getBaseCostA();
        if (result.isEmpty() || costA.isEmpty()) {
            return null;
        }
        ItemStack costB = offer.getCostB();
        // 价格可能被拆成 costA(64) + costB(余数)，反查要用总价
        int totalCost = costA.getCount() + (costB.isEmpty() ? 0 : costB.getCount());
        Quality exact = BY_OFFER.get(new OfferKey(costA.getItem(), totalCost, result.getItem()));
        return exact != null ? exact : BY_ITEM.get(result.getItem());
    }

    /**
     * 根据 NPC 等级生成交易清单。
     *
     * @param npcLevel NPC 等级 (1-99)
     * @return MerchantOffers (按品质降序排列：金 -> 红 -> 黑 -> 紫 -> 蓝 -> 白)
     */
    public static MerchantOffers generate(int npcLevel) {
        int level = Math.max(1, Math.min(99, npcLevel));
        int slotCount = calcSlotCount(level);
        boolean forceGold = level >= 85;
        List<Quality> available = pickQualities(level, slotCount, forceGold);
        Set<TradeEntry> uniqueEntries = new LinkedHashSet<>();
        for (Quality q : available) {
            if (!q.pool.isEmpty()) {
                uniqueEntries.add(q.pool.get(RAND.nextInt(q.pool.size())));
            }
        }
        List<TradeEntry> fallbackPool = new ArrayList<>(WHITE.pool);
        Collections.shuffle(fallbackPool, RAND);

        int fallbackIndex = 0;
        int maxAttempts = 100;
        while (uniqueEntries.size() < slotCount && maxAttempts-- > 0) {
            if (fallbackIndex < fallbackPool.size()) {
                uniqueEntries.add(fallbackPool.get(fallbackIndex++));
            } else {
                Quality randomQuality = QUALITIES.get(RAND.nextInt(QUALITIES.size()));
                if (!randomQuality.pool.isEmpty()) {
                    uniqueEntries.add(randomQuality.pool.get(RAND.nextInt(randomQuality.pool.size())));
                }
            }
        }

        List<TradeEntry> sortedEntries = new ArrayList<>(uniqueEntries);
        sortedEntries.sort((e1, e2) -> {
            Quality q1 = getQualityOfEntry(e1);
            Quality q2 = getQualityOfEntry(e2);
            // q2.priority - q1.priority 表示降序 (金 -> 白)
            return Integer.compare(q2.priority, q1.priority);
        });

        MerchantOffers offers = new MerchantOffers();
        for (TradeEntry entry : sortedEntries) {
            ItemStack resultStack = new ItemStack(entry.item(), entry.perTrade());

            int totalCost = entry.cost();
            Item currency = entry.currency();

            ItemStack costA;
            ItemStack costB = ItemStack.EMPTY;
            if (totalCost > 64) {
                costA = new ItemStack(currency, 64);
                costB = new ItemStack(currency, Math.min(64, totalCost - 64));
            } else {
                costA = new ItemStack(currency, totalCost);
            }
            offers.add(new MerchantOffer(
                    costA,
                    costB,
                    resultStack,
                    99,
                    0,
                    0.05f
            ));
        }


        return offers;
    }

    /**
     * 辅助方法：查找交易条目所属的品质
     */
    private static Quality getQualityOfEntry(TradeEntry entry) {
        for (Quality q : QUALITIES) {
            if (q.pool.contains(entry)) {
                return q;
            }
        }
        return WHITE;
    }

    public static int calcSlotCount(int level) {
        return Math.min(12, Math.max(1, 1 + level / 8));
    }

    private static List<Quality> pickQualities(int level, int count, boolean forceGold) {
        List<double[]> scored = new ArrayList<>();
        for (Quality q : QUALITIES) {
            double score;
            if (level < q.minLevel) {
                score = 0.0;
            } else if (q.maxLevel < 0) {
                score = 1.0;
            } else {
                float range = q.maxLevel - q.minLevel;
                score = (float) (level - q.minLevel) / range;
                score = Math.min(1.0, Math.max(0.0, score));
            }
            scored.add(new double[]{score, (double) QUALITIES.indexOf(q)});
        }

        scored.sort(Comparator.comparingDouble(a -> -a[0]));

        List<Quality> result = new ArrayList<>();
        for (double[] entry : scored) {
            Quality q = QUALITIES.get(((int) entry[1]));
            if (entry[0] > 0 && !result.contains(q)) {
                result.add(q);
            }
            if (result.size() >= count) break;
        }

        if (forceGold && !result.contains(GOLD)) {
            result.add(GOLD);
        }

        return result;
    }

    /** 早期版本写在交易结果上的 NBT 键。会让物品无法与正常物品堆叠，现已废弃。 */
    private static final String LEGACY_QUALITY_TAG = "KlNpcQuality";

    /**
     * 清掉一个 ItemStack 上残留的旧品级标记（只删这一个键，不动物品本身）。
     *
     * @return 是否真的改动了这个 ItemStack
     */
    public static boolean stripLegacyQualityTag(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(LEGACY_QUALITY_TAG)) {
            return false;
        }
        tag.remove(LEGACY_QUALITY_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
        return true;
    }

    /**
     * 清掉一个交易列表里所有结果物品上的旧品级标记。
     * 用于修复旧存档：NPC 的交易配方是持久化到自身 NBT 的，里面的 result 还带着旧标签。
     *
     * @return 被清理的交易条数
     */
    public static int stripLegacyQualityTag(MerchantOffers offers) {
        if (offers == null) {
            return 0;
        }
        int cleaned = 0;
        for (MerchantOffer offer : offers) {
            if (offer != null && stripLegacyQualityTag(offer.getResult())) {
                cleaned++;
            }
        }
        return cleaned;
    }
}