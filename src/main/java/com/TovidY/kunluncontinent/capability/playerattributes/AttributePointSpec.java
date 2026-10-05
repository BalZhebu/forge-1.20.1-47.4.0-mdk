package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * 属性加点面板的<b>唯一权威定义表</b>：11 条可加点属性。
 *
 * <p><b>数值设计（99 级 = 99 点，单条上限 99 点，所以一个 build 最多点满一条）</b>：
 * <pre>
 * 属性      命名   每点收益  99点满收益  结算方式
 * 生命值    强健   +1.5%    +148.5%    百分比乘算
 * 攻击力    强攻   +1.5%    +148.5%    百分比乘算
 * 防御力    铁壁   +1.2%    +118.8%    百分比乘算
 * 暴击率    锐意   +0.3%    +29.7%     百分比加算
 * 暴击伤害  破军   +2.0%    +198%      百分比乘算
 * 生命恢复  回春   +1.5%    +148.5%    百分比乘算
 * 吸血      噬魂   +0.4%    +39.6%     百分比乘算
 * 闪避      游龙   +0.25%   +24.75%    百分比加算
 * 命中      锁魂   +0.3%    +29.7%     百分比加算
 * 物穿      破甲   +1.0%    +99%       百分比乘算
 * 抗暴      坚心   +0.3%    +29.7%     百分比加算
 * </pre>
 *
 * <p><b>为什么要区分 {@link Mode#MULT} 和 {@link Mode#FLAT}？</b><br>
 * 概率类属性（暴击率/闪避/命中/抗暴）走<b>加算</b>，否则"封顶 30% 左右"的设计定位根本达不到
 * —— 基础暴击率只有 5%，乘算 99 点也只有 12.4%；加算则是 5% + 29.7% = 34.7%，正好对上。<br>
 * 数值类属性（攻击/防御/生命/暴伤/物穿…）走<b>乘算</b>，因为它们基数是几千万的大数字，
 * 加一个固定的百分点毫无意义。
 *
 * <p><b>结算入口只有一个</b>：{@link ModAttributeAPI} 的 11 个聚合 getter 在返回前
 * 统一调 {@link #applyBonus}，不要在别处再算一遍。
 */
public enum AttributePointSpec {

    // ==================== 枚举顺序 = 页签/网络包里的下标，改动会导致老存档点错属性 ====================
    // 顺序按用户指定：生命值 → 攻击力 → 防御力 → 暴击率 → 暴击伤害 → 其余随意

    SHENGMING("maxshengming", "强健", "生命值", 3.0f, Mode.MULT, 0xFFFF5555, "§c"),
    GONGJI("gongji", "强攻", "攻击力", 1.5f, Mode.MULT, 0xFFFFA64D, "§6"),
    FANGYU("fangyu", "铁壁", "防御力", 2.0f, Mode.MULT, 0xFF55AAFF, "§b"),
    BAOJILV("baojilv", "锐意", "暴击率", 0.3f, Mode.FLAT, 0xFFFFD700, "§e"),
    BAOJISHANGHAI("baojishanghai", "破军", "暴击伤害", 2.0f, Mode.MULT, 0xFFFF66CC, "§d"),
    SHENGMINGHUIFU("shengminghuifu", "回春", "生命恢复", 2.0f, Mode.MULT, 0xFF66DD66, "§a"),
    XIXUE("xixue", "噬魂", "吸血", 0.4f, Mode.MULT, 0xFFB060FF, "§5"),
    SHANBI("shanbi", "游龙", "闪避", 0.25f, Mode.FLAT, 0xFF66E0E0, "§3"),
    MINGZHONG("mingzhong", "锁魂", "命中", 0.15F, Mode.FLAT, 0xFFFFCC66, "§e"),
    WUCHUAN("wuchuan", "破甲", "物穿", 1.0f, Mode.MULT, 0xFFAAAAAA, "§7"),
    KANGBAO("kangbao", "坚心", "抗暴", 0.3f, Mode.FLAT, 0xFF88CCFF, "§9");

    /** 结算方式。 */
    public enum Mode {
        /** 百分比乘算：{@code value * (1 + 每点% × 点数)}。 */
        MULT,
        /** 百分比加算：{@code value + 每点% × 点数}（概率类属性）。 */
        FLAT
    }

    /** 单条属性的点数上限。 */
    public static final int MAX_POINTS = 99;

    private final String key;
    private final String displayName;
    private final String attrName;
    private final float perPointPercent;
    private final Mode mode;
    private final int color;
    private final String colorCode;

    AttributePointSpec(String key, String displayName, String attrName,
                       float perPointPercent, Mode mode, int color, String colorCode) {
        this.key = key;
        this.displayName = displayName;
        this.attrName = attrName;
        this.perPointPercent = perPointPercent;
        this.mode = mode;
        this.color = color;
        this.colorCode = colorCode;
    }

    public String getKey() {
        return key;
    }

    /** 面板上显示的名字（如"强攻"），<b>不是</b>原版属性名。 */
    public String getDisplayName() {
        return displayName;
    }

    /** 对应的原属性名（如"攻击力"），tooltip 第一行用。 */
    public String getAttrName() {
        return attrName;
    }

    /** 每 1 点带来的收益（百分数，如 1.5f 表示 +1.5%）。 */
    public float getPerPointPercent() {
        return perPointPercent;
    }

    public Mode getMode() {
        return mode;
    }

    /** ARGB 颜色，供面板画色块/进度条。 */
    public int getColor() {
        return color;
    }

    /** § 颜色代码，供文本使用。 */
    public String getColorCode() {
        return colorCode;
    }

    /** 满 99 点的总收益（百分数），用于面板上写"满值 +148.5%"。 */
    public float getMaxTotalPercent() {
        return perPointPercent * MAX_POINTS;
    }

    public Component title() {
        return Component.literal(colorCode + displayName);
    }

    // ==================== 查表 ====================

    /** 按枚举下标取（网络包传的就是下标）。 */
    public static AttributePointSpec byIndex(int index) {
        AttributePointSpec[] values = values();
        return index >= 0 && index < values.length ? values[index] : null;
    }

    /** 按属性键取（{@code ModAttributeAPI} 内部用）。 */
    public static AttributePointSpec byKey(String key) {
        for (AttributePointSpec spec : values()) {
            if (spec.key.equals(key)) return spec;
        }
        return null;
    }

    // ==================== 结算 ====================

    /**
     * 把已加的点数算进最终属性值。<b>只有这里能算</b>。
     *
     * @param value 该属性的聚合值（含魂环/魂骨/装备/药水）
     * @return 加点后的最终值；点数为 0 时原样返回
     */
    public float applyBonus(Player player, float value) {
        int points = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getAllocatedPoints(this))
                .orElse(0);
        if (points <= 0) return value;
        float delta = perPointPercent * points;
        return mode == Mode.FLAT ? value + delta : value * (1f + delta / 100f);
    }

    /**
     * {@link ModAttributeAPI} 的统一入口：按属性键取对应条目并结算。
     * 键不存在或实体不是玩家时原样返回。
     */
    public static float applyBonusByKey(String key, float value, net.minecraft.world.entity.Entity entity) {
        if (entity instanceof Player player) {
            AttributePointSpec spec = byKey(key);
            if (spec != null) return spec.applyBonus(player, value);
        }
        return value;
    }
}