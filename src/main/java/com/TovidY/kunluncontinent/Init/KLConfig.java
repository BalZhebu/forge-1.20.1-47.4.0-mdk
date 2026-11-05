package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//配置信息生成类
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KLConfig {

    // ==================================================================================
    //  配置结构版本与自动重置
    // ==================================================================================

    /**
     * <b>配置结构版本</b> —— 每次「新增配置项 / 改默认值 / 改取值范围 / 换键名」都把它 +1。
     *
     * <p>老玩家存档里存的是旧版本号，启动时会自动把整份配置重写成新默认值，
     * 不用再让玩家手动删 {@code config/kunluncontinent-common.toml}。</p>
     *
     * <p>⚠️ 只做「+1」这一件事，不要拿它做别的判断 —— 重置逻辑在
     * {@link #applyConfigResetIfNeeded(ModConfig)}。</p>
     */
    public static final int CONFIG_SCHEMA_VERSION = 2;

    /** 配置文件里记录的版本号。<b>默认值必须是 0</b>：老玩家的文件里没有这个键 → 读到 0 → 触发一次重置。 */
    public static final ForgeConfigSpec.IntValue CONFIG_SCHEMA;

    /**
     * 配置项「键名集合」的签名（由代码自动算出，玩家文件里存一份）。
     *
     * <p><b>这一项让"新增配置项"完全不需要人维护</b>：只要代码里多了/少了/改名了任何一个键，
     * 签名就会变 → 启动时自动重置一次，玩家不用删 toml。</p>
     *
     * <p>它只认「键名」，<b>不认默认值</b> —— 只改默认值不换键时签名不变，
     * 那种情况请把 {@link #CONFIG_SCHEMA_VERSION} +1。</p>
     */
    public static final ForgeConfigSpec.IntValue CONFIG_SIGNATURE;

    /**
     * 测试开关：{@code true} = <b>每次启动都把配置文件重置为默认值</b>（重置后本开关会自己写回 true，
     * 所以下次启动还会重置；想关掉就手动改成 false，那一轮之后就不再重置）。
     *
     * <p>开发/测试专用，正式整合包里保持 false。</p>
     */
    public static final ForgeConfigSpec.BooleanValue RESET_CONFIG_ON_START;

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

        CONFIG_SCHEMA = builder
                .comment("配置文件结构版本（由 MOD 自己维护，不要手改）",
                        "当它小于 MOD 内置的版本时，本文件会被自动重置为新默认值——这样新增配置项不必让玩家手动删文件")
                .defineInRange("config_schema_version", 0, 0, Integer.MAX_VALUE);

        CONFIG_SIGNATURE = builder
                .comment("配置项键名签名（由 MOD 自己算，不要手改）",
                        "和 MOD 内置签名不一致时会自动重置一次——新增/改名配置项后玩家无需删文件")
                .defineInRange("config_signature", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        RESET_CONFIG_ON_START = builder
                .comment("测试开关：true = 每次启动都把这份配置重置为默认值（重置后本项会自动写回 true）",
                        "想停止重置就改成 false，那一轮之后不再重置。正式游玩请保持 false")
                .define("reset_config_on_start", false);

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

    // ==================================================================================
    //  启动时自动替换配置 —— 新增配置项不必再让玩家手动删 toml
    // ==================================================================================

    /**
     * 配置加载完成后（MOD 总线，{@code Loading} 阶段）检查是否需要重置。
     *
     * <p>⚠️ <b>只监听 {@code Loading}，绝对不要加 {@code Reloading}</b>：
     * 我们重置完会 {@code save()} 写盘，文件变动会触发 {@code Reloading}；
     * 如果那里也重置，就会「重置 → 写盘 → 重载 → 再重置」无限循环。</p>
     */
    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        ModConfig config = event.getConfig();
        // 别的 config（比如以后可能加的 CLIENT/SERVER）与本次无关
        if (config.getSpec() != CONFIG) {
            return;
        }
        applyConfigResetIfNeeded(config);
    }

    /**
     * 触发条件：<b>测试开关为 true</b> 或 <b>文件里的结构版本不等于代码里的版本</b>。
     *
     * <p>重置 = 把每个配置项都 {@code set(getDefault())} 再写盘，所以玩家这次启动拿到的
     * 就是「代码里写的新默认值」，效果等同于手动删掉 toml 让 MOD 重新生成，
     * 但不需要重启第二次。</p>
     */
    private static void applyConfigResetIfNeeded(ModConfig config) {
        boolean forceReset = RESET_CONFIG_ON_START.get();
        int storedVersion = CONFIG_SCHEMA.get();
        int storedSignature = CONFIG_SIGNATURE.get();

        int currentSignature = computeSignature();
        boolean versionOutdated = storedVersion != CONFIG_SCHEMA_VERSION;
        boolean keysChanged = storedSignature != currentSignature;

        if (!forceReset && !versionOutdated && !keysChanged) {
            return;
        }

        try {
            String reason;
            if (forceReset) {
                reason = "reset_config_on_start = true（测试开关）";
            } else if (keysChanged) {
                reason = "配置项有增删/改名（签名 " + storedSignature + " → " + currentSignature + "）";
            } else {
                reason = "配置结构版本 " + storedVersion + " → " + CONFIG_SCHEMA_VERSION;
            }

            Path backup = backupConfigFile(config);
            int count = resetAllToDefaults();

            // 重置会把这两项也刷成默认值，所以要重新写一遍
            CONFIG_SCHEMA.set(CONFIG_SCHEMA_VERSION);
            CONFIG_SIGNATURE.set(currentSignature);
            if (forceReset) {
                // 开关自己写回 true，才能"每次启动都重置"；想停就手动改成 false。
                RESET_CONFIG_ON_START.set(true);
            }
            config.save();

            KlMain.LOGGER.info("[昆仑大陆] 配置文件已重置为默认值：{} 项（原因：{}）{}",
                    count, reason,
                    backup == null ? "" : "；旧文件已备份为 " + backup.getFileName());
        } catch (Exception e) {
            // 重置失败不能把游戏搞崩：沿用文件里的原值继续跑
            KlMain.LOGGER.error("[昆仑大陆] 重置配置文件失败，本次沿用文件里的原值", e);
        }
    }

    /**
     * 算出「所有配置项键名」的签名。
     *
     * <p>必须<b>递归进子节点</b>：{@code getValues()} 是一棵树，顶层键
     * （{@code kunluncontinent_settings}、{@code 魂骨爆率配置}）的值是 nightconfig 的
     * {@code Config}（section），真正的 {@code ConfigValue} 只在叶子层。
     * 只取顶层 key 的话，在某个 section 里加一项签名不会变 —— 等于没检测到。</p>
     */
    private static int computeSignature() {
        java.util.List<String> paths = new java.util.ArrayList<>();
        collectPaths(CONFIG.getValues(), "", paths);
        java.util.Collections.sort(paths);
        return paths.hashCode();
    }

    private static void collectPaths(com.electronwill.nightconfig.core.UnmodifiableConfig config, String prefix,
                                     java.util.List<String> out) {
        for (var entry : config.entrySet()) {
            String key = String.join(".", entry.getKey());
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            if (entry.getValue() instanceof com.electronwill.nightconfig.core.Config inner) {
                collectPaths(inner, path, out);
            } else if (entry.getValue() instanceof ForgeConfigSpec.ConfigValue) {
                out.add(path);
            }
        }
    }

    /**
     * 把每一个配置项恢复成「代码里的默认值」。
     *
     * <p>递归遍历，<b>新增配置项不用动这里</b>。
     * ⚠️ 只扫一层会一个都匹配不到（踩过：日志显示"重置 0 项"，实际等于没重置）。</p>
     *
     * @return 实际重置的项数
     */
    private static int resetAllToDefaults() {
        // ⚠️ last_seen_version 是"运行时状态"（玩家上次进游戏时的版本号），不是设置项：
        //    重置它会让更新日志书再发一遍，所以单独保住原值。
        String lastSeenVersion = LAST_SEEN_VERSION.get();
        int count = resetValues(CONFIG.getValues());
        LAST_SEEN_VERSION.set(lastSeenVersion);
        return count;
    }

    private static int resetValues(com.electronwill.nightconfig.core.UnmodifiableConfig config) {
        int count = 0;
        for (var entry : config.entrySet()) {
            if (entry.getValue() instanceof com.electronwill.nightconfig.core.Config inner) {
                count += resetValues(inner);
            } else if (entry.getValue() instanceof ForgeConfigSpec.ConfigValue cv) {
                setDefaultRaw(cv);
                count++;
            }
        }
        return count;
    }

    /** {@code ConfigValue<?>} 无法直接调带参 {@code set}，用原始类型转一下。 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setDefaultRaw(ForgeConfigSpec.ConfigValue value) {
        value.set(value.getDefault());
    }

    /**
     * 重置前把旧文件拷贝成 {@code xxx.toml.bak}（覆盖上一次的备份）。
     *
     * <p>重置是破坏性的（玩家可能手工调过爆率），留一份备份让后悔值低一点。
     * 文件不存在（首次安装）时返回 {@code null}。</p>
     */
    private static Path backupConfigFile(ModConfig config) throws IOException {
        Path file = config.getFullPath();
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        Files.copy(file, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return backup;
    }
}