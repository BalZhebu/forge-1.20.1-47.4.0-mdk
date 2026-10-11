package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanQuality;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * 炼丹炉品质概率系统 —— <b>客户端与服务端共用同一份实现</b>。
 *
 * <p>设计目标：同一种内丹，品质越高 → 产出越偏向高品质丹药，且变化是<b>连续平滑</b>的。
 * 旧实现是「只要有 1 颗仙品内丹就把整个权重数组覆盖成固定值」，导致
 * 「4 凡品 + 1 仙品」和「5 仙品」概率完全一致，其余材料的质量被忽略。</p>
 *
 * <p>算法分三步：</p>
 * <ol>
 *   <li>读取 5 个材料槽的品质，换算成「药性当量」，取平均值得 {@code avg}；</li>
 *   <li>用 {@code TILT} 把基础权重曲线整体向高品质方向拉伸：
 *       {@code w[i] = base[i] * TILT^i}，其中 {@code TILT = 1 + (avg + 炉温加成) * 0.28}。
 *       该变换单调、无负数、任何档位都不会归零，品质每提升一档概率就平滑地挪一点；</li>
 *   <li>叠加药渣块（大幅压低破碎）与特殊配方（草药炼丹不出废丹）。</li>
 * </ol>
 *
 * <p>因为服务端与客户端调用的是同一份代码，GUI 上显示的概率就是实际开出的概率。</p>
 */
public final class AlchemicalCalculator {

    private AlchemicalCalculator() {
    }

    // ==================== 可调参数 ====================

    /** 产出档位数量：0 破碎 / 1 药散 / 2 药丹 / 3 灵丹 / 4 宝丹 / 5 仙丹。 */
    public static final int TIER_COUNT = 6;

    /** 材料槽数量。 */
    public static final int MATERIAL_SLOTS = 5;

    /** 药渣槽在 ItemHandler 中的下标。 */
    public static final int DROSS_SLOT = 17;

    /** 基础权重曲线（全部凡品内丹时的分布）。 */
    private static final double[] BASE_WEIGHTS = {40.0, 30.0, 15.0, 10.0, 4.0, 1.0};

    /** 每种内丹品质的「药性当量」，下标 = {@link NeidanQuality#ordinal()}。 */
    private static final double[] POTENTIAL = {0.0, 0.6, 1.4, 2.4, 3.8, 6.0};

    /** 每点平均药性，把权重曲线向高品质拉伸的比例。 */
    private static final double TILT_PER_POTENTIAL = 0.28;

    /** 丹炉等级高于丹方等级时，每级额外提供的药性（高等级炉不只是「更快」，也能「炼得更好」）。 */
    private static final double FURNACE_POTENTIAL_PER_LEVEL = 0.08;

    /** 炉温药性加成上限，避免高等级炉把低阶丹直接炼成仙丹。 */
    private static final double FURNACE_POTENTIAL_CAP = 0.50;

    /** 药渣块：破碎权重保留比例。 */
    private static final double DROSS_KEEP = 0.15;

    /** 药渣块：被抹掉的破碎权重中转入「药散」的比例，剩余部分转入「灵丹」。 */
    private static final double DROSS_TO_SAN = 0.65;

    // ==================== 药性 ====================

    /**
     * 单个材料槽的药性当量。非 {@link NeidanItem}（例如草药）一律为 0。
     *
     * <p>注意这里走的是 {@link NeidanItem#getQuality(ItemStack)}，而不是裸 NBT ——
     * 裸 NBT 会漏掉「7 阶→上品 / 8 阶→珍品 / 9 阶→绝品」的 tier 兜底，
     * 导致无 NBT 的高阶内丹 tooltip 显示【绝品】但炼丹时按凡品算。
     * 同时避免 {@code getOrCreateTag()} 在只读路径上污染物品 NBT。</p>
     */
    private static double potentialOf(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0;
        if (stack.getItem() instanceof NeidanItem neidan) {
            NeidanQuality quality = neidan.getQuality(stack);
            return POTENTIAL[quality.ordinal()];
        }
        return 0.0;
    }

    /** 5 个材料槽的平均药性（空槽按 0 计，因此投满 5 颗收益最高）。 */
    private static double averagePotential(@Nullable ItemStack[] materials) {
        if (materials == null || materials.length == 0) return 0.0;
        double sum = 0.0;
        for (ItemStack stack : materials) {
            sum += potentialOf(stack);
        }
        return sum / materials.length;
    }

    /** 丹炉等级相对丹方等级带来的药性加成。 */
    public static double furnacePotential(int furnaceLevel, int recipeLevel) {
        int diff = furnaceLevel - recipeLevel;
        if (diff <= 0) return 0.0;
        return Math.min(FURNACE_POTENTIAL_CAP, diff * FURNACE_POTENTIAL_PER_LEVEL);
    }

    /** 材料槽是否至少放了一颗可炼制的内丹。 */
    public static boolean hasAnyMaterial(@Nullable ItemStack[] materials) {
        if (materials == null) return false;
        for (ItemStack stack : materials) {
            if (stack != null && stack.getItem() instanceof NeidanItem) return true;
        }
        return false;
    }

    // ==================== 概率计算 ====================

    /**
     * 计算 6 档产出品质的概率权重。<b>返回的是未归一化的权重，客户端与服务端必须走同一份。</b>
     *
     * @param materials    5 个材料槽（可为 null / 含空槽）
     * @param hasDross     药渣槽是否有药渣块
     * @param special      是否为草药类特殊配方（草药炼丹不出破碎废丹）
     * @param furnaceLevel 丹炉等级
     * @param recipeLevel  丹方等级
     */
    public static double[] computeWeights(@Nullable ItemStack[] materials,
                                          boolean hasDross,
                                          boolean special,
                                          int furnaceLevel,
                                          int recipeLevel) {
        double avg = averagePotential(materials);
        double tilt = 1.0 + (avg + furnacePotential(furnaceLevel, recipeLevel)) * TILT_PER_POTENTIAL;

        double[] weights = new double[TIER_COUNT];
        double pow = 1.0;
        for (int i = 0; i < TIER_COUNT; i++) {
            weights[i] = BASE_WEIGHTS[i] * pow;
            pow *= tilt;
        }

        // 草药类特殊配方：不存在「炸炉」，破碎权重全部并入药散
        if (special) {
            weights[1] += weights[0];
            weights[0] = 0.0;
        }

        // 药渣块：稳定剂，大幅压低破碎，被抹掉的部分按比例转成药散 / 灵丹
        if (hasDross) {
            double broken = weights[0];
            weights[0] = broken * DROSS_KEEP;
            double removed = broken - weights[0];
            weights[1] += removed * DROSS_TO_SAN;
            weights[3] += removed * (1.0 - DROSS_TO_SAN);
        }

        return weights;
    }

    /** 服务端便捷重载：直接从 ItemHandler 取材料（材料槽 0-4，药渣槽 17）。 */
    public static double[] computeWeights(IItemHandler handler,
                                          boolean special,
                                          int furnaceLevel,
                                          int recipeLevel) {
        ItemStack[] materials = new ItemStack[MATERIAL_SLOTS];
        for (int i = 0; i < MATERIAL_SLOTS; i++) {
            materials[i] = handler.getStackInSlot(i);
        }
        boolean hasDross = handler.getStackInSlot(DROSS_SLOT) != null
                && !handler.getStackInSlot(DROSS_SLOT).isEmpty();
        return computeWeights(materials, hasDross, special, furnaceLevel, recipeLevel);
    }

    /**
     * 客户端便捷重载：从 6 档权重里随机抽一档。
     *
     * @return 0-5，对应 {@code DanYaoQuality} 的 ordinal
     */
    public static int roll(double[] weights, RandomSource random) {
        double total = 0.0;
        for (double w : weights) {
            if (w > 0.0) total += w;
        }
        if (total <= 1.0E-9) return 1;

        double r = random.nextDouble() * total;
        double acc = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > 0.0) acc += weights[i];
            if (r < acc) return i;
        }
        return weights.length - 1;
    }

    /**
     * 归一化成百分比，供 GUI 显示（长度 = {@link #TIER_COUNT}）。
     */
    public static double[] toPercent(double[] weights) {
        double total = 0.0;
        for (double w : weights) {
            if (w > 0.0) total += w;
        }
        double[] percent = new double[TIER_COUNT];
        if (total <= 1.0E-9) return percent;
        for (int i = 0; i < TIER_COUNT; i++) {
            percent[i] = Math.max(0.0, weights[i]) / total * 100.0;
        }
        return percent;
    }

    /** 供 GUI 显示的档位标签。 */
    public static final String[] TIER_LABELS = {"碎", "散", "丹", "灵", "宝", "仙"};

    /** 供 GUI 显示的档位颜色。 */
    public static final int[] TIER_COLORS = {
            0x777777, 0x55FF55, 0x5555FF, 0xAA00AA, 0xFFAA00, 0xFF5555
    };
}