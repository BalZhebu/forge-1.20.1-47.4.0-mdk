package com.TovidY.kunluncontinent.datagen.advancement;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 成就数据生成。
 *
 * <p>所有条目都通过 {@link AdvancementHelper} 生成，新增成就只需一行：
 * {@code AdvancementHelper.task(saver, helper, parent, "路径", 图标, 条件)}</p>
 *
 * <p>分支结构：</p>
 * <pre>
 * root ─┬─ 宝石线 / 金属线（原）
 *       ├─ 炼丹炉线 → 丹道（新）
 *       ├─ 等级线 ─┬─ 10→99 级（新）
 *       │          └─ 丹药 → 批量服丹（新）
 *       ├─ 魂师之路 → 魂环 → 年限攀登 / 九环圆满 / 魂核（新）
 *       ├─ 魂塔 ─┬─ 10 / 30 / 50 / 100 层（新）
 *       │        └─ 神考 → 第一考 → 第五考 → 成就神位（新）
 *       └─ 维度探索 ─┬─ 雷界（原）
 *                    ├─ 神界（新）
 *                    ├─ 极冰界（新）
 *                    └─ 魂塔秘境（新）
 * </pre>
 */
public class ModAdvancementProvider extends ForgeAdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ModAdvancements()));
    }

    public static class ModAdvancements implements AdvancementGenerator {

        /** 项目早期成就沿用 "advancements.kunluncontinent." 前缀，这里保持兼容。 */
        private static final String LEGACY = "advancements.kunluncontinent.";

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper helper) {

            // ==================================================================
            //  根成就
            // ==================================================================
            Advancement root = AdvancementHelper.root(saver, helper, "root", LEGACY + "root",
                    ModItems.HUNHUAN_BUTTON.get(),
                    ResourceLocation.tryParse("minecraft:textures/gui/advancements/backgrounds/stone.png"),
                    AdvancementHelper.onJoin());

            // ==================================================================
            //  宝石线（原）
            // ==================================================================
            Advancement ruby = AdvancementHelper.task(saver, helper, root, "get_ruby", LEGACY + "ruby",
                    ModItems.RUBY.get(), AdvancementHelper.hasItems(ModItems.RUBY.get()));

            Advancement sapphire = AdvancementHelper.task(saver, helper, ruby, "get_sapphire", LEGACY + "sapphire",
                    ModItems.SAPPHIRE.get(), AdvancementHelper.hasItems(ModItems.SAPPHIRE.get()));

            Advancement amethyst = AdvancementHelper.challenge(saver, helper, sapphire, "get_amethyst", LEGACY + "amethyst",
                    ModItems.AMETHYST.get(), AdvancementHelper.hasItems(ModItems.AMETHYST.get()));

            AdvancementHelper.goal(saver, helper, amethyst, "get_starlight_stone", LEGACY + "starlight_stone",
                    ModItems.STARLIGHT_STONE.get(), AdvancementHelper.hasItems(ModItems.STARLIGHT_STONE.get()));

            // ==================================================================
            //  金属线（原）
            // ==================================================================
            Advancement grayIron = AdvancementHelper.task(saver, helper, root, "get_gray_iron_ingot", LEGACY + "gray_iron_ingot",
                    ModItems.GRAY_IRON_INGOT.get(), AdvancementHelper.hasItems(ModItems.GRAY_IRON_INGOT.get()));

            Advancement bronze = AdvancementHelper.task(saver, helper, grayIron, "get_gray_iron_ingot2", LEGACY + "cloud_patterned_bronze_ingot",
                    ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(), AdvancementHelper.hasItems(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()));

            Advancement redFire = AdvancementHelper.task(saver, helper, bronze, "get_gray_iron_ingot3", LEGACY + "red_fire_ingot",
                    ModItems.RED_FIRE_INGOT.get(), AdvancementHelper.hasItems(ModItems.RED_FIRE_INGOT.get()));

            Advancement sunkenSilver = AdvancementHelper.task(saver, helper, redFire, "get_gray_iron_ingot4", LEGACY + "sunken_silver_ingot",
                    ModItems.SUNKEN_SILVER_INGOT.get(), AdvancementHelper.hasItems(ModItems.SUNKEN_SILVER_INGOT.get()));

            Advancement coldSteel = AdvancementHelper.challenge(saver, helper, sunkenSilver, "get_gray_iron_ingot5", LEGACY + "cold_heated_steel_ingot",
                    ModItems.COLD_HEARTED_STEEL_INGOT.get(), AdvancementHelper.hasItems(ModItems.COLD_HEARTED_STEEL_INGOT.get()));

            AdvancementHelper.challenge(saver, helper, coldSteel, "get_gray_iron_ingot6", LEGACY + "rinsei_ingot",
                    ModItems.RINSEI_INGOT.get(), AdvancementHelper.hasItems(ModItems.RINSEI_INGOT.get()));

            // ==================================================================
            //  炼丹炉线（原）
            // ==================================================================
            Advancement lu1 = AdvancementHelper.task(saver, helper, root, "liandanlu1", "adv.kunlun.liandanlu1",
                    ModBlocks.LIANDANLU1.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU1.get()));

            Advancement lu2 = AdvancementHelper.task(saver, helper, lu1, "liandanlu2", "adv.kunlun.liandanlu2",
                    ModBlocks.LIANDANLU2.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU2.get()));

            Advancement lu3 = AdvancementHelper.task(saver, helper, lu2, "liandanlu3", "adv.kunlun.liandanlu3",
                    ModBlocks.LIANDANLU3.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU3.get()));

            Advancement lu4 = AdvancementHelper.goal(saver, helper, lu3, "liandanlu4", "adv.kunlun.liandanlu4",
                    ModBlocks.LIANDANLU4.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU4.get()));

            Advancement lu5 = AdvancementHelper.goal(saver, helper, lu4, "liandanlu5", "adv.kunlun.liandanlu5",
                    ModBlocks.LIANDANLU5.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU5.get()));

            Advancement lu6 = AdvancementHelper.goal(saver, helper, lu5, "liandanlu6", "adv.kunlun.liandanlu6",
                    ModBlocks.LIANDANLU6.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU6.get()));

            Advancement lu7 = AdvancementHelper.challenge(saver, helper, lu6, "liandanlu7", "adv.kunlun.liandanlu7",
                    ModBlocks.LIANDANLU7.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU7.get()));

            Advancement lu8 = AdvancementHelper.challenge(saver, helper, lu7, "liandanlu8", "adv.kunlun.liandanlu8",
                    ModBlocks.LIANDANLU8.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU8.get()));

            AdvancementHelper.challenge(saver, helper, lu8, "liandanlu9", "adv.kunlun.liandanlu9",
                    ModBlocks.LIANDANLU9.get(), AdvancementHelper.hasItems(ModBlocks.LIANDANLU9.get()));

            // ==================================================================
            //  等级线（原）
            // ==================================================================
            Advancement lvl10 = AdvancementHelper.goal(saver, helper, root, "level_10", "adv.kunlun.lvl10",
                    ModItems.CHUYUAN_DAN.get(), AdvancementHelper.levelReached(10));
            Advancement lvl20 = AdvancementHelper.goal(saver, helper, lvl10, "level_20", "adv.kunlun.lvl20",
                    ModItems.BAICAOLING_DAN.get(), AdvancementHelper.levelReached(20));
            Advancement lvl30 = AdvancementHelper.goal(saver, helper, lvl20, "level_30", "adv.kunlun.lvl30",
                    ModItems.QIANHUABAO_DAN.get(), AdvancementHelper.levelReached(30));
            Advancement lvl40 = AdvancementHelper.goal(saver, helper, lvl30, "level_40", "adv.kunlun.lvl40",
                    ModItems.WANPOXUAN_DAN.get(), AdvancementHelper.levelReached(40));
            Advancement lvl50 = AdvancementHelper.goal(saver, helper, lvl40, "level_50", "adv.kunlun.lvl50",
                    ModItems.SHIFANGJIE_DAN.get(), AdvancementHelper.levelReached(50));
            Advancement lvl60 = AdvancementHelper.goal(saver, helper, lvl50, "level_60", "adv.kunlun.lvl60",
                    ModItems.HUANYUANYIQI_DAN.get(), AdvancementHelper.levelReached(60));
            Advancement lvl70 = AdvancementHelper.goal(saver, helper, lvl60, "level_70", "adv.kunlun.lvl70",
                    ModItems.TAIXUPOWANG_DAN.get(), AdvancementHelper.levelReached(70));
            Advancement lvl80 = AdvancementHelper.challenge(saver, helper, lvl70, "level_80", "adv.kunlun.lvl80",
                    ModItems.QIANWANXINGCHEN_DAN.get(), AdvancementHelper.levelReached(80));
            Advancement lvl90 = AdvancementHelper.challenge(saver, helper, lvl80, "level_90", "adv.kunlun.lvl90",
                    ModItems.YIZAICHUANGSHENG_DAN.get(), AdvancementHelper.levelReached(90));
            Advancement lvl99 = AdvancementHelper.challenge(saver, helper, lvl90, "level_99", "adv.kunlun.lvl99",
                    ModItems.YIZAICHUANGSHENG_DAN.get(), AdvancementHelper.levelReached(99));

            // ==================================================================
            //  分支：魂师之路 —— 武魂 / 魂环 / 年限
            // ==================================================================
            Advancement awaken = AdvancementHelper.task(saver, helper, root, "wuhun_awaken",
                    ModItems.HUNHUAN_BUTTON.get(), AdvancementHelper.event(AchievementAPI.WUHUN_AWAKEN));

            // 双生 / 三生：一次觉醒出 2 个 / 3 个武魂（三生的玩家会同时点亮双生）
            // ⚠️ 图标别用 GUOSHI_TIANSHI —— 天使精华的贴图还没画，runData 会直接报
            //    "Texture kunluncontinent:item/tianshi does not exist"。
            Advancement wuhunDuo = AdvancementHelper.challenge(saver, helper, awaken, "wuhun_duo",
                    ModItems.GUOSHI_BAHUANGJI.get(), AdvancementHelper.event(AchievementAPI.WUHUN_DUO));

            AdvancementHelper.challenge(saver, helper, wuhunDuo, "wuhun_triple",
                    ModItems.GUOSHI_PANSHIJUYUAN.get(), AdvancementHelper.event(AchievementAPI.WUHUN_TRIPLE));

            Advancement firstRing = AdvancementHelper.task(saver, helper, awaken, "hunhuan_first",
                    ModItems.LOW_HUNHUAN_STORAGE_CORE.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_FIRST));

            // 年限攀登：一条递进链
            Advancement thousand = AdvancementHelper.task(saver, helper, firstRing, "hunhuan_thousand",
                    ModItems.HUNHUAN_STORAGE_ONE.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_THOUSAND));

            Advancement myriad = AdvancementHelper.task(saver, helper, thousand, "hunhuan_myriad",
                    ModItems.HUNHUAN_STORAGE_TWO.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_MYRIAD));

            Advancement hundredThousand = AdvancementHelper.goal(saver, helper, myriad, "hunhuan_hundred_thousand",
                    ModItems.HUNHUAN_STORAGE_THREE.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_HUNDRED_THOUSAND));

            Advancement million = AdvancementHelper.challenge(saver, helper, hundredThousand, "hunhuan_million",
                    ModItems.HUNHUAN_STORAGE_FOUR.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_MILLION));

            AdvancementHelper.challenge(saver, helper, million, "hunhuan_divine",
                    ModItems.HUNHUAN_STORAGE_FIVE.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_DIVINE));

            // 九环圆满
            AdvancementHelper.goal(saver, helper, firstRing, "hunhuan_nine",
                    ModItems.TOP_HUNHUAN_STORAGE_CORE.get(), AdvancementHelper.event(AchievementAPI.HUNHUAN_NINE));

            // 魂核：只要背包里出现过魂核（任一阶）即达成
            AdvancementHelper.task(saver, helper, firstRing, "soul_core_first",
                    ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get(), AdvancementHelper.hasItems(
                            ModItems.LOW_HUNHUAN_STORAGE_CORE.get(),
                            ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get(),
                            ModItems.HIGH_HUNHUAN_STORAGE_CORE.get(),
                            ModItems.TOP_HUNHUAN_STORAGE_CORE.get()));

            // ==================================================================
            //  分支：丹道 —— 挂在炼丹炉线之后
            // ==================================================================
            Advancement alchemyFirst = AdvancementHelper.task(saver, helper, lu1, "alchemy_first",
                    ModItems.CHUYUAN_DAN.get(), AdvancementHelper.event(AchievementAPI.ALCHEMY_FIRST));

            Advancement alchemySpirit = AdvancementHelper.task(saver, helper, alchemyFirst, "alchemy_spirit",
                    ModItems.WANPOXUAN_DAN.get(), AdvancementHelper.event(AchievementAPI.ALCHEMY_SPIRIT));

            Advancement alchemyTreasure = AdvancementHelper.goal(saver, helper, alchemySpirit, "alchemy_treasure",
                    ModItems.TAIXUPOWANG_DAN.get(), AdvancementHelper.event(AchievementAPI.ALCHEMY_TREASURE));

            AdvancementHelper.challenge(saver, helper, alchemyTreasure, "alchemy_immortal",
                    ModItems.YIZAICHUANGSHENG_DAN.get(), AdvancementHelper.event(AchievementAPI.ALCHEMY_IMMORTAL));

            // ==================================================================
            //  分支：转生 —— 挂在满级之后
            // ==================================================================
            Advancement reincarnationFirst = AdvancementHelper.challenge(saver, helper, lvl99, "reincarnation_first",
                    ModItems.GUOSHI_POHUNQIANG.get(), AdvancementHelper.event(AchievementAPI.REINCARNATION_FIRST));

            // ==================================================================
            //  分支：魂塔 → 三十 / 五十 / 百层 → 神之试炼 → 封神
            // ==================================================================
            Advancement towerFirst = AdvancementHelper.task(saver, helper, root, "tower_first",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_FIRST));

            Advancement towerTen = AdvancementHelper.goal(saver, helper, towerFirst, "tower_ten",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_TEN));

            Advancement towerThirty = AdvancementHelper.goal(saver, helper, towerTen, "tower_thirty",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_THIRTY));

            Advancement towerFifty = AdvancementHelper.goal(saver, helper, towerThirty, "tower_fifty",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_FIFTY));

            Advancement towerHundred = AdvancementHelper.challenge(saver, helper, towerFifty, "tower_hundred",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_HUNDRED));

            // 神考：接下试炼 → 第一考 → 第五考 → 成就神位
            Advancement godExamStart = AdvancementHelper.task(saver, helper, towerFirst, "god_exam_start",
                    ModItems.SHENKAO_BUTTON.get(), AdvancementHelper.event(AchievementAPI.GOD_EXAM_START));

            Advancement godStage1 = AdvancementHelper.task(saver, helper, godExamStart, "god_exam_stage1",
                    ModItems.DEMON_WHALE_MEDAL.get(), AdvancementHelper.event(AchievementAPI.GOD_EXAM_STAGE1));

            Advancement godStage5 = AdvancementHelper.goal(saver, helper, godStage1, "god_exam_stage5",
                    ModItems.GUOSHI_POHUNQIANG.get(), AdvancementHelper.event(AchievementAPI.GOD_EXAM_STAGE5));

            Advancement godAscended = AdvancementHelper.challenge(saver, helper, godStage5, "god_ascended",
                    ModItems.HUNHUAN_STORAGE_FIVE.get(), AdvancementHelper.event(AchievementAPI.GOD_ASCENDED));

            // ==================================================================
            //  分支：维度探索 —— 雷界 / 神界 / 极冰界 / 魂塔秘境
            // ==================================================================
            Advancement thunderRealm = AdvancementHelper.goal(saver, helper, root, "thunder_realm_enter",
                    ModItems.FANGSHANHUNDAOQI_3.get(), AdvancementHelper.event(AchievementAPI.THUNDER_REALM_ENTER));

            AdvancementHelper.goal(saver, helper, thunderRealm, "celestial_realm_enter",
                    ModItems.GOLDEN_SOUL_COIN.get(), AdvancementHelper.event(AchievementAPI.CELESTIAL_REALM_ENTER));

            Advancement polarIce = AdvancementHelper.goal(saver, helper, thunderRealm, "polar_ice_realm_enter",
                    ModItems.EXTREME_COLD_SNOWFLAKE.get(), AdvancementHelper.event(AchievementAPI.POLAR_ICE_REALM_ENTER));

            AdvancementHelper.goal(saver, helper, thunderRealm, "tower_realm_enter",
                    ModBlocks.SUMMON_TOWER.get(), AdvancementHelper.event(AchievementAPI.TOWER_REALM_ENTER));

            // ==================================================================
            //  分支：丹药 —— 挂在一阶丹（等级线起点）之后
            // ==================================================================
            Advancement danyaoFirst = AdvancementHelper.task(saver, helper, lvl10, "danyao_first",
                    ModItems.CHUYUAN_DAN.get(), AdvancementHelper.event(AchievementAPI.DANYAO_FIRST));

            AdvancementHelper.goal(saver, helper, danyaoFirst, "danyao_batch",
                    ModItems.NEIDAN9.get(), AdvancementHelper.event(AchievementAPI.DANYAO_BATCH));

            // ==================================================================
            //  分支：魂骨 / 魂导器 / 八卦 —— 挂在第一魂环之后
            // ==================================================================
            Advancement boneFirst = AdvancementHelper.task(saver, helper, firstRing, "bone_first",
                    ModItems.SOUL_BEAST_SKULL.get(), AdvancementHelper.event(AchievementAPI.BONE_FIRST));

            AdvancementHelper.goal(saver, helper, boneFirst, "bone_seven",
                    ModItems.SOUL_BEAST_EXTERNAL_APPENDAGES.get(), AdvancementHelper.event(AchievementAPI.BONE_SEVEN));

            // 魂导器：背包里出现任意一件即达成（五件都在 HUNGULIST 的前 five 项）
            AdvancementHelper.task(saver, helper, boneFirst, "hundaoqi_first",
                    ModItems.FANGSHANHUNDAOQI_1.get(), AdvancementHelper.hasItems(
                            ModItems.FANGSHANHUNDAOQI_1.get(),
                            ModItems.FANGSHANHUNDAOQI_2.get(),
                            ModItems.FANGSHANHUNDAOQI_3.get(),
                            ModItems.FANGSHANHUNDAOQI_4.get(),
                            ModItems.FANGSHANHUNDAOQI_5.get()));

            // 分解八卦：同上
            AdvancementHelper.task(saver, helper, boneFirst, "bagua_first",
                    ModItems.FIRST_DECOMPOSITION_GOSSIP.get(), AdvancementHelper.hasItems(
                            ModItems.FIRST_DECOMPOSITION_GOSSIP.get(),
                            ModItems.TWO_DECOMPOSITION_GOSSIP.get(),
                            ModItems.THREE_DECOMPOSITION_GOSSIP.get(),
                            ModItems.FOUR_DECOMPOSITION_GOSSIP.get(),
                            ModItems.FIVE_DECOMPOSITION_GOSSIP.get()));

            // ==================================================================
            //  分支：极冰界 —— 极寒精华 / 雪帝 / 唱片
            // ==================================================================
            AdvancementHelper.task(saver, helper, polarIce, "extreme_cold_essence",
                    ModItems.EXTREME_COLD_ESSENCE.get(), AdvancementHelper.hasItems(ModItems.EXTREME_COLD_ESSENCE.get()));

            Advancement iceEmpress = AdvancementHelper.challenge(saver, helper, polarIce, "ice_empress_slain",
                    ModItems.ICE_EMPRESS_HEART.get(), AdvancementHelper.killEntity(EntityInit.ICE_EMPRESS.get()));

            AdvancementHelper.task(saver, helper, iceEmpress, "ice_empress_heart",
                    ModItems.ICE_EMPRESS_HEART.get(), AdvancementHelper.hasItems(ModItems.ICE_EMPRESS_HEART.get()));

            AdvancementHelper.task(saver, helper, polarIce, "frozen_throne_disc",
                    ModItems.THRONE_OF_FROZEN_STONE.get(), AdvancementHelper.hasItems(ModItems.THRONE_OF_FROZEN_STONE.get()));

            // ==================================================================
            //  分支：神祇进阶 —— 神赐魂环 → 称谓（半神之后一路到神王）
            // ==================================================================
            Advancement godRing = AdvancementHelper.goal(saver, helper, godAscended, "god_ring_blessed",
                    ModItems.HUNHUAN_STORAGE_FIVE.get(), AdvancementHelper.event(AchievementAPI.GOD_RING_BLESSED));

            // 称谓门槛与 GodRank 完全对齐：121 准神 / 141 三级神 / 161 二级神 / 181 一级神 / 196 神王
            Advancement rankZhun = AdvancementHelper.goal(saver, helper, godRing, "rank_zhun_shen",
                    ModItems.HUNHUAN_STORAGE_ONE.get(), AdvancementHelper.levelReached(121));
            Advancement rankSan = AdvancementHelper.goal(saver, helper, rankZhun, "rank_san_ji_shen",
                    ModItems.HUNHUAN_STORAGE_TWO.get(), AdvancementHelper.levelReached(141));
            Advancement rankEr = AdvancementHelper.goal(saver, helper, rankSan, "rank_er_ji_shen",
                    ModItems.HUNHUAN_STORAGE_THREE.get(), AdvancementHelper.levelReached(161));
            Advancement rankYi = AdvancementHelper.goal(saver, helper, rankEr, "rank_yi_ji_shen",
                    ModItems.HUNHUAN_STORAGE_FOUR.get(), AdvancementHelper.levelReached(181));
            AdvancementHelper.challenge(saver, helper, rankYi, "rank_shen_wang",
                    ModItems.HUNHUAN_STORAGE_FIVE.get(), AdvancementHelper.levelReached(196));

            // ==================================================================
            //  分支：武魂 / 装备 / 转生
            // ==================================================================
            AdvancementHelper.goal(saver, helper, awaken, "wuhun_zhenshen",
                    ModItems.GUOSHI_POHUNQIANG.get(), AdvancementHelper.event(AchievementAPI.WUHUN_ZHENSHEN));

            AdvancementHelper.goal(saver, helper, firstRing, "special_soul_ring",
                    ModItems.TOP_HUNHUAN_STORAGE_CORE.get(), AdvancementHelper.event(AchievementAPI.SPECIAL_SOUL_RING));

            AdvancementHelper.goal(saver, helper, coldSteel, "armor_set_full",
                    ModItems.RINSEI_CHESTPLATE.get(), AdvancementHelper.event(AchievementAPI.ARMOR_SET_FULL));

            AdvancementHelper.challenge(saver, helper, reincarnationFirst, "reincarnation_second",
                    ModItems.GUOSHI_POHUNQIANG.get(), AdvancementHelper.event(AchievementAPI.REINCARNATION_SECOND));
        }
    }
}
