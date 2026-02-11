package com.TovidY.kunluncontinent.Init;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {

    public static final ForgeConfigSpec.BooleanValue ENABLE_HUNHUAN_PROBABILITY;
    public static final ForgeConfigSpec.DoubleValue TIER1_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER2_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER3_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER4_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER5_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER6_PROB;
    public static final ForgeConfigSpec.DoubleValue TIER7_PROB;
    static{
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("kunluncontinent_settings");

        ENABLE_HUNHUAN_PROBABILITY = builder
                .comment("是否启用基于年限的魂环概率生成")
                .define("enableHunhuanProbability", true);

        // 各阶段概率配置
        TIER1_PROB = builder
                .comment("10年内魂环生成概率")
                .defineInRange("hunhuanTier1Prob", 0.6, 0.0, 1.0);
        TIER2_PROB = builder
                .comment("100年内魂环生成概率")
                .defineInRange("hunhuanTier2Prob", 0.5, 0.0, 1.0);
        TIER3_PROB = builder
                .comment("1000年内魂环生成概率")
                .defineInRange("hunhuanTier3Prob", 0.4, 0.0, 1.0);
        TIER4_PROB = builder
                .comment("10000年内魂环生成概率")
                .defineInRange("hunhuanTier4Prob", 0.35, 0.0, 1.0);
        TIER5_PROB = builder
                .comment("100000年内魂环生成概率")
                .defineInRange("hunhuanTier5Prob", 0.3, 0.0, 1.0);
        TIER6_PROB = builder
                .comment("1000000年内魂环生成概率")
                .defineInRange("hunhuanTier6Prob", 0.32, 0.0, 1.0);
        TIER7_PROB = builder
                .comment("10000000年以上魂环生成概率")
                .defineInRange("hunhuanTier7Prob", 0.70, 0.0, 1.0);

        builder.pop();
        CONFIG = builder.build();
    }

    public static final ForgeConfigSpec CONFIG;

}
