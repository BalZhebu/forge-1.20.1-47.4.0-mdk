package com.TovidY.kunluncontinent.item.spiritstone;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 蕴灵石的品质。
 *
 * <h3>两套维度，别搞混</h3>
 * <ul>
 *   <li><b>阶</b>（1~9）—— 由物品本身决定，写在 {@link SpiritStoneItem#getTier()}；</li>
 *   <li><b>品质</b>（本枚举）—— 同一个「三阶蕴灵石」可以有不同的品质，存在 {@code ItemStack} 的 NBT 里。</li>
 * </ul>
 *
 * <h3>倍率：上品是基准线</h3>
 * <p>七档按 {@code 废品 < 下品 < 中品 < 上品(基准 ±0%) < 极品 < 绝品 < 仙品} 排列，
 * 低于上品的削弱、高于上品的增幅。倍率就写在每个枚举常量的第三个构造参数里，
 * 想改「上品这个值」= 改 {@code HIGH("上品", GREEN, 1.0f)} 那个 {@code 1.0f}。</p>
 *
 * <p>最终修为 = {@code 基数(阶) × 品质倍率}：基数逐阶写在 {@link #BASE_EXP} 表里，品质只做乘算。
 * 具体数值见 {@link #expOf(int, SpiritStoneQuality)}。</p>
 *
 * <p>⚠️ <b>品质怎么获得还没做</b>：目前读不到 NBT 时按基准档「上品」处理（方便直接测试），
 * 以后产出逻辑里调 {@link #set(ItemStack, SpiritStoneQuality)} 写入即可。</p>
 */
public enum SpiritStoneQuality {

    /** 废品：彻底空掉，吸不出任何东西（效率 -100%）。 */
    WASTE("废品", ChatFormatting.DARK_GRAY, 0.0f),
    /** 下品：只有基准的四成。 */
    LOW("下品", ChatFormatting.GRAY, 0.4f),
    /** 中品：基准的七成。 */
    MIDDLE("中品", ChatFormatting.WHITE, 0.7f),
    /**
     * 上品：<b>基准线</b>，效率 ±0% —— 不增幅也不减弱。
     *
     * <p>⚠️ 「基准」就靠这里的 {@code 1.0f} 表达：改这一格等于整体平移所有品质的强度。
     * 想让基准更高/更低，直接改它；想拉大上下差距，改上下几行。</p>
     */
    HIGH("上品", ChatFormatting.GREEN, 1.0f),
    /** 极品：基准的 1.5 倍。 */
    EXCELLENT("极品", ChatFormatting.BLUE, 1.5f),
    /** 绝品：基准的 2 倍。 */
    SUPREME("绝品", ChatFormatting.LIGHT_PURPLE, 2.0f),
    /** 仙品：基准的 3 倍。 */
    IMMORTAL("仙品", ChatFormatting.GOLD, 3.0f);

    /** 物品 NBT 里存品质序号的键。 */
    public static final String NBT_KEY = "SpiritStoneQuality";

    /** 没写 NBT 时的默认品质 —— 取基准档「上品」，即不增不减。 */
    public static final SpiritStoneQuality DEFAULT = HIGH;

    private final String displayName;
    private final ChatFormatting color;
    private final float multiplier;

    SpiritStoneQuality(String displayName, ChatFormatting color, float multiplier) {
        this.displayName = displayName;
        this.color = color;
        this.multiplier = multiplier;
    }

    public String displayName() {
        return displayName;
    }

    public ChatFormatting color() {
        return color;
    }

    /** 修为倍率；废品是 0。上品 = 1.0 是基准线。 */
    public float multiplier() {
        return multiplier;
    }

    /**
     * 相对基准（上品）的效率偏移，单位 %。
     *
     * <p>上品 = 0、极品 = +50、下品 = -60、废品 = -100。只用来显示，不参与计算。</p>
     */
    public int efficiencyPercent() {
        return Math.round((multiplier - 1.0f) * 100.0f);
    }

    /** 效率偏移的带色文本，例如 {@code §a+50%} / {@code §c-60%} / {@code §f±0%}。 */
    public String efficiencyText() {
        int p = efficiencyPercent();
        if (p == 0) {
            return ChatFormatting.WHITE.toString() + "±0%";
        }
        String color = p > 0 ? ChatFormatting.GREEN.toString() : ChatFormatting.RED.toString();
        return color + (p > 0 ? "+" : "") + p + "%";
    }

    /** 带颜色的显示名，例如「§6仙品」。 */
    public String coloredName() {
        return color + displayName;
    }

    public boolean canAbsorb() {
        return multiplier > 0f;
    }

    /** 是否高品质（给物品加附魔光效用）。 */
    public boolean isPrecious() {
        return this.ordinal() >= SUPREME.ordinal();
    }

    // ==================== 读写 ====================

    /** 读物品品质；没写过 NBT 就返回 {@link #DEFAULT}。 */
    public static SpiritStoneQuality get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_KEY)) {
            return DEFAULT;
        }
        int ordinal = tag.getInt(NBT_KEY);
        SpiritStoneQuality[] all = values();
        return (ordinal >= 0 && ordinal < all.length) ? all[ordinal] : DEFAULT;
    }

    /** 写物品品质。等于默认值时清掉 NBT，省空间。 */
    public static void set(ItemStack stack, SpiritStoneQuality quality) {
        if (quality == null || quality == DEFAULT) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(NBT_KEY);
            }
            return;
        }
        stack.getOrCreateTag().putInt(NBT_KEY, quality.ordinal());
    }

    private static final int[] BASE_EXP = {
            0,
            4,     // 1 阶
            8,    // 2 阶
            14,    // 3 阶
            20,    // 4 阶
            40,   // 5 阶
            88,   // 6 阶
            150,   // 7 阶
            241,   // 8 阶
            354    // 9 阶
    };

    /** 某一阶的基数（品质倍率之前）。越界会钳到 1~9，不会抛异常。 */
    public static int baseExpOf(int tier) {
        int index = Math.max(1, Math.min(tier, BASE_EXP.length - 1));
        return BASE_EXP[index];
    }

    /**
     * 计算一颗蕴灵石可提供的修为（经验值）。
     *
     * @param tier    1~9 阶
     * @param quality 品质
     * @return 修为点数；废品恒为 0
     */
    public static int expOf(int tier, SpiritStoneQuality quality) {
        if (quality == null || !quality.canAbsorb()) {
            return 0;
        }
        return Math.round(baseExpOf(tier) * quality.multiplier());
    }

    /** 便捷重载：直接从物品读品质。 */
    public static int expOf(int tier, ItemStack stack) {
        return expOf(tier, get(stack));
    }
}
