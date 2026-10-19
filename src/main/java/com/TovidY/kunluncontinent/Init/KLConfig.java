package com.TovidY.kunluncontinent.Init;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

//配置信息生成类
public class KLConfig {

    public static final ForgeConfigSpec.BooleanValue hungupingheng;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HUNHUAN_PROBABILITY;
    public static final ForgeConfigSpec.DoubleValue TIER1_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER2_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER3_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER4_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER5_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER6_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER7_PROB;

    // 新增魂骨爆率配置项
    public static final ForgeConfigSpec.DoubleValue baseDropChance; // 基础爆率(hungupingheng=false时使用)
    public static final ForgeConfigSpec.DoubleValue dropChanceTier1; // 10-99年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier2; // 100-999年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier3; // 1000-9999年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier4; // 10000-99999年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier5; // 100000-999999年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier6; // 1000000-9999999年
    public static final ForgeConfigSpec.DoubleValue dropChanceTier7; // 10000000+年

    // ================= 新增：版本检查与更新日志记录配置 =================
    public static final ForgeConfigSpec.ConfigValue<String> LAST_SEEN_VERSION;

    public static final ForgeConfigSpec CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("kunluncontinent_settings");

        // 记录上次运行 MOD 的版本号
        LAST_SEEN_VERSION = builder
                .comment("记录上次运行 MOD 时的版本号，用于检测玩家是否为刚更新 MOD 后的首次登录")
                .define("last_seen_version", "0.0.0");

        ENABLE_HUNHUAN_PROBABILITY = builder
                .comment("是否启用基于年限的魂环概率生成(本功能供开发测试用，私自修改导致存档问题不予解决！)")
                .define("enableHunhuanProbability", true);

        hungupingheng = builder.comment("魂骨掉率模式。true = 使用下方「魂骨爆率配置」里的年限档位表；false = 所有魂兽统一用 baseDropChance(默认 1/16)")
                .define("hungu_balance", true);

        // 各阶段概率配置
        // 档位边界唯一实现在 KLivingDeathEvent#nianxianTier：10 / 100 / 1000 / 1万 / 10万 / 100万 / 1000万
        TIER1_PROB = builder
                .comment("10 年以下 ~ 99 年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier1Prob", 0.6, 0.0, 1.0);
        TIER2_PROB = builder
                .comment("100 ~ 999 年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier2Prob", 0.5, 0.0, 1.0);
        TIER3_PROB = builder
                .comment("1000 ~ 9999 年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier3Prob", 0.5, 0.0, 1.0);
        TIER4_PROB = builder
                .comment("1 万 ~ 9.9 万年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier4Prob", 0.45, 0.0, 1.0);
        TIER5_PROB = builder
                .comment("10 万 ~ 99 万年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier5Prob", 0.5, 0.0, 1.0);
        TIER6_PROB = builder
                .comment("100 万 ~ 999 万年魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier6Prob", 1.0, 0.0, 1.0);
        TIER7_PROB = builder
                .comment("1000 万年以上魂环生成概率（设置为1.0代表100%）")
                .defineInRange("hunhuanTier7Prob", 1.0, 0.0, 1.0);

        // 新增爆率配置项
        builder.push("魂骨爆率配置").comment("魂骨平衡为 true 时用下面的年限档位；baseDropChance 只在平衡为 false 时生效");
        baseDropChance = builder.comment("统一基础爆率(仅当魂骨平衡=false时使用，为 true 时忽略本项)")
                .defineInRange("baseDropChance", 1.0 / 16.0, 0.0, 1.0);
        dropChanceTier1 = builder.comment("10-99 年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier1", 0.001, 0.0, 1.0);
        dropChanceTier2 = builder.comment("100-999 年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier2", 0.008, 0.0, 1.0);
        dropChanceTier3 = builder.comment("1000-9999 年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier3", 0.01, 0.0, 1.0);
        dropChanceTier4 = builder.comment("1 万 - 9.9 万年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier4", 0.03, 0.0, 1.0);
        dropChanceTier5 = builder.comment("10 万 - 99 万年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier5", 0.065, 0.0, 1.0);
        dropChanceTier6 = builder.comment("100 万 - 999 万年魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier6", 0.5, 0.0, 1.0);
        dropChanceTier7 = builder.comment("1000 万年以上魂兽的魂骨爆率（设置为1.0代表100%）")
                .defineInRange("tier7", 1.0, 0.0, 1.0);

        builder.pop();
        builder.pop();

        CONFIG = builder.build();
    }

    /**
     * 向 Forge 注册配置项（在 KlMain 主类构造函数中调用此方法）
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
    }
}