package com.TovidY.kunluncontinent.godclass;

import net.minecraft.ChatFormatting;

/**
 * 封神之后的「神祇称谓」——按玩家等级分档，同时决定**跨档突破需要多少魂环**。
 *
 * <h3>等级区间（用户定的规格，改档位只改这里）</h3>
 * <pre>
 * 100 ~ 120  半神
 * 121 ~ 140  准神
 * 141 ~ 160  三级神
 * 161 ~ 180  二级神
 * 181 ~ 195  一级神
 * 196 +      神王
 * </pre>
 *
 * <h3>与魂环的关系</h3>
 * <p>原来 100 级以后是「每 10 级吸一枚环」（{@code 等级/10}），现在改成<b>按称谓段</b>：
 * 段内可以自由升级，走到段末（120 / 140 / 160 / 180 / 195）必须吸收下一枚魂环才能跨段。
 * 需要的总数 = {@code 10 + 段序号}：半神 10、准神 11、三级神 12、二级神 13、一级神 14、神王 15。</p>
 *
 * <p>例如：玩家 120 级（半神顶）时，{@code requiredRingsForLevel(121) = 11} ——
 * 手里有 10 枚环，可吸第 11 枚，吸完才能突破到 121（准神）。</p>
 *
 * <p>⚠️ 这个类是 <b>common 包</b>，服务端（{@code PlayerUpgradeSystem} / {@code PlayerHunhuanAPI}）
 * 与客户端（{@code ShenkaoScreen}）都会读，不要往里加只有一端才有的依赖。</p>
 */
public enum GodRank {

    BAN_SHEN("半神", 100, 120, ChatFormatting.AQUA),
    ZHUN_SHEN("准神", 121, 140, ChatFormatting.DARK_AQUA),
    SAN_JI_SHEN("三级神", 141, 160, ChatFormatting.GREEN),
    ER_JI_SHEN("二级神", 161, 180, ChatFormatting.BLUE),
    YI_JI_SHEN("一级神", 181, 195, ChatFormatting.LIGHT_PURPLE),
    SHEN_WANG("神王", 196, Integer.MAX_VALUE, ChatFormatting.GOLD);

    /** 称谓系统的起始等级：100 级以下没有称谓。 */
    public static final int MIN_LEVEL = 100;

    /** 半神段的魂环数（往上一段 +1）。 */
    private static final int BASE_RINGS = 10;

    private final String title;
    private final int minLevel;
    private final int maxLevel;
    private final ChatFormatting color;

    GodRank(String title, int minLevel, int maxLevel, ChatFormatting color) {
        this.title = title;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.color = color;
    }

    public String title() {
        return title;
    }

    public int minLevel() {
        return minLevel;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public ChatFormatting color() {
        return color;
    }

    /** 界面用：「§b半神」这种带色文本。 */
    public String display() {
        return color + title;
    }

    /** 区间文案：「100~120」/「196+」。 */
    public String rangeText() {
        return maxLevel == Integer.MAX_VALUE ? (minLevel + "+") : (minLevel + "~" + maxLevel);
    }

    /** 等级对应的称谓；100 级以下返回 {@code null}。 */
    public static GodRank of(int level) {
        for (GodRank rank : values()) {
            if (level >= rank.minLevel && level <= rank.maxLevel) {
                return rank;
            }
        }
        return null;
    }

    /**
     * 升到 {@code level} 级需要持有的魂环数。
     *
     * <p>100 级以下不走这套（返回 {@code -1}，调用方自己回退到「每 10 级一环」的老规则）。</p>
     */
    public static int requiredRingsForLevel(int level) {
        GodRank rank = of(level);
        return rank == null ? -1 : BASE_RINGS + rank.ordinal();
    }

    /**
     * 该等级是不是「段末」（120 / 140 / 160 / 180 / 195）——只有这些等级才允许吸环跨段。
     *
     * <p>判定方式是「这个等级和下一级不是同一个称谓」，所以改档位边界时不用同步改别处。</p>
     */
    public static boolean isStageBoundary(int level) {
        GodRank current = of(level);
        GodRank next = of(level + 1);
        return current != null && next != null && current != next;
    }
}
