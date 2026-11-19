package com.TovidY.kunluncontinent.datagen.itemprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualRecipeBuilder;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * 汇灵台仪式配方专区。
 *
 * <p>刻意从 {@code ModRecipesProvider}（几千行）里拆出来：仪式配方参数多、写法特殊，
 * 混在大文件里翻起来痛苦。以后新增仪式照抄下面任意一个例子即可。</p>
 *
 * <h3>为什么是静态方法而不是第二个 RecipeProvider</h3>
 * <p>{@code DataGenerator} 按 {@code provider.getName()} 去重，而 {@code RecipeProvider#getName()}
 * 是 <b>final</b> 且恒为 {@code "Recipes"} —— 注册第二个会直接抛
 * {@code Duplicate provider: Recipes}，且没法改名绕过。
 * 所以这里做成「独立生成类」：由 {@code ModRecipesProvider#buildRecipes} 里调一行
 * {@link #register(Consumer)}，配方仍然写进同一个 recipes 目录，但代码是分开的。</p>
 *
 * <h3>参数速查</h3>
 * <table border="1">
 *   <tr><th>方法</th><th>含义</th><th>必填</th></tr>
 *   <tr><td>{@code create(必需品种类, 成品种类)}</td>
 *       <td>最简单的写法，成品出 1 个</td><td>✅</td></tr>
 *   <tr><td>{@code create(必需品种类, ItemStack 成品)}</td>
 *       <td>成品要出多个时用它，见下面「深海祭品 ×10」那条</td><td>✅</td></tr>
 *   <tr><td>{@code ingredient(物品, 数量)}</td>
 *       <td>一条副物品需求（摆在基座上）。调多次就加多条</td><td>✅ 至少一条</td></tr>
 *   <tr><td>{@code ingredient(物品, 数量, 副产物)}</td>
 *       <td>同上，但每消耗一件就把「副产物」弹在那座基座位置（如 水桶 → 空桶）</td><td>否</td></tr>
 *   <tr><td>{@code spiritCost(int)}</td>
 *       <td>精神力总消耗。按 {@code 总值 / 时长} 逐 tick 扣，最后一 tick 正好扣满</td><td>✅</td></tr>
 *   <tr><td>{@code duration(int)}</td>
 *       <td>制作时长，单位 <b>tick</b>（20 tick = 1 秒）。副物品在「结束前 3 秒」全部消耗完</td><td>✅</td></tr>
 *   <tr><td>{@code strictNbt()}</td>
 *       <td>匹配时连 NBT 一起比。不写 = 只看物品 id</td><td>否</td></tr>
 * </table>
 *
 * <h3>几条硬规则</h3>
 * <ul>
 *   <li><b>数量必须正好</b>：配方写 8 件，玩家摆 9 件就不触发（多一件也不行）。</li>
 *   <li><b>JEI 的 8 个槽只是显示约定</b>：实际件数由你写的 count 决定；
 *       单条超过 8 件时 JEI 会把余量压在最后一格。</li>
 *   <li>{@code save} 的第二个参数（文件名）必须全局唯一。</li>
 * </ul>
 */
public final class KlRecipeProvider {

    /** 这些仪式统一 20 秒（400 tick）。 */
    private static final int DURATION_20S = 400;

    /** 30 秒（600 tick）。 */
    private static final int DURATION_30S = 600;

    private KlRecipeProvider() {
    }

    /** 由 {@code ModRecipesProvider#buildRecipes} 调用。 */
    public static void register(Consumer<FinishedRecipe> pWriter) {

        // =====================================================================
        //  深海勋章（你原先写的那条，参数保持不动：10000 精神力 / 60 秒）
        //     主材料：魔鲸徽章    副物品：龙头 / 深海祭品 / 下界之星 / 幽匿催发体
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.DEMON_WHALE_BADGE.get(), ModItems.DEMON_WHALE_MEDAL.get())
                .ingredient(Items.DRAGON_HEAD, 1)
                .ingredient(ModItems.DEEPSEA_JINGHUA.get(), 1)
                .ingredient(Items.NETHER_STAR, 1)
                .ingredient(Items.SCULK_CATALYST, 1)
                .spiritCost(10000)
                .duration(1200)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_demon_whale_medal");

        // =====================================================================
        //  极寒石（原「极寒打火石」工作台配方 8 台阶 + 打火石 → 已删）
        //     主材料：打火石    副物品：雪晶碎片 ×8
        //     精神力 300，20 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(Items.FLINT_AND_STEEL, ModItems.EXTREME_COLD_SNOWFLAKE.get())
                .ingredient(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get(), 8)
                .spiritCost(300)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_extreme_cold_snowflake");

        // =====================================================================
        //  雷域石（结构同极寒石）
        //     主材料：打火石    副物品：雷电碎片 ×8
        //     精神力 300，20 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(Items.FLINT_AND_STEEL, ModItems.THUNDERREALM_SNOWFLAKE.get())
                .ingredient(ModItems.LIGHTNING_FRAGMENTS.get(), 8)
                .spiritCost(300)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_thunderrealm_snowflake");

        // =====================================================================
        //  极寒冰晶框架（传送门框架 ①）
        //     主材料：极寒冰晶    副物品：雪块 ×8（原配方里是围一圈的 8 个雪块）
        //     精神力 300，20 秒，产出 1 个
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.EXTREME_COLD.get(), ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())
                .ingredient(Items.SNOW_BLOCK, 8)
                .spiritCost(300)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_polar_ice_portal_frame");

        // =====================================================================
        //  万雷天域传送门框架（传送门框架 ②）
        //     主材料：极寒冰晶框架（原配方中心就是它 —— 两级框架是递进的）
        //     副物品：黑曜石 ×4 + 雷电碎片 ×4
        //     精神力 300，20 秒，<b>产出 5 个</b>（原配方 .shaped(...,5)）
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get(),
                        new ItemStack(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get(), 5))
                .ingredient(Items.OBSIDIAN, 4)
                .ingredient(ModItems.LIGHTNING_FRAGMENTS.get(), 4)
                .spiritCost(300)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_thunder_portal_frame");

        // =====================================================================
        //  深海祭品（普通）—— 原两个工作台配方之一
        //     主材料：金苹果    副物品：深海精华 ×4 + 深海之眼 ×4
        //     精神力 500，20 秒，产出 1 个
        // =====================================================================
        HunlingRitualRecipeBuilder.create(Items.GOLDEN_APPLE, ModItems.DEEP_SEA_OFFERINGS.get())
                .ingredient(ModItems.DEEPSEA_JINGHUA.get(), 4)
                .ingredient(ModItems.EYE_DEEP_SEA.get(), 4)
                .spiritCost(500)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_deep_sea_offering");

        // =====================================================================
        //  深海祭品（附魔）—— 结构完全一样，只有主材料换成附魔金苹果、产出 ×10
        //     这就是「掺合成多个物品」的写法：create(ItemLike, new ItemStack(..., 10))
        // =====================================================================
        HunlingRitualRecipeBuilder.create(Items.ENCHANTED_GOLDEN_APPLE,
                        new ItemStack(ModItems.DEEP_SEA_OFFERINGS.get(), 10))
                .ingredient(ModItems.DEEPSEA_JINGHUA.get(), 4)
                .ingredient(ModItems.EYE_DEEP_SEA.get(), 4)
                .spiritCost(500)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_deep_sea_offering_enchanted");


        // =====================================================================
        //  高阶聚灵基柱（原「高阶基石柱」工作台配方 → 已删）
        //     主材料：中阶聚灵基柱
        //     副物品：红火块 ×6 + 顶级魂环存储核心 ×1 + 沉银块 ×1
        //     精神力 700，30 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModBlocks.SPIRIT_GATHERING_STONE_1.get(),
                        ModBlocks.SPIRIT_GATHERING_STONE_2.get())
                .ingredient(ModBlocks.RED_FIRE_BLOCK.get(), 6)
                .ingredient(ModItems.TOP_HUNHUAN_STORAGE_CORE.get(), 1)
                .ingredient(ModBlocks.SUNKEN_SILVER_BLOCK.get(), 1)
                .spiritCost(700)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_high_spirit_pillar");

        // =====================================================================
        //  四阶魂环储存器（原工作台配方 → 已删）
        //     主材料：三级魂环收纳器
        //     副物品：沉银锭 ×4 + 红火锭 ×3 + 高阶魂环存储核心 ×1
        //     精神力 500，20 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.HUNHUAN_STORAGE_THREE.get(),
                        ModItems.HUNHUAN_STORAGE_FOUR.get())
                .ingredient(ModItems.SUNKEN_SILVER_INGOT.get(), 4)
                .ingredient(ModItems.RED_FIRE_INGOT.get(), 3)
                .ingredient(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get(), 1)
                .spiritCost(500)
                .duration(DURATION_20S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_hunhuan_storage_four");

        // =====================================================================
        //  五阶魂环储存器（原工作台配方 → 已删）
        //     主材料：四级魂环收纳器
        //     副物品：冷心钢锭 ×4 + 沉银锭 ×3 + 顶级魂环存储核心 ×1
        //     精神力 800，30 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.HUNHUAN_STORAGE_FOUR.get(),
                        ModItems.HUNHUAN_STORAGE_FIVE.get())
                .ingredient(ModItems.COLD_HEARTED_STEEL_INGOT.get(), 4)
                .ingredient(ModItems.SUNKEN_SILVER_INGOT.get(), 3)
                .ingredient(ModItems.TOP_HUNHUAN_STORAGE_CORE.get(), 1)
                .spiritCost(800)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_hunhuan_storage_five");

        // =====================================================================
        //  顶级丹炉核心（原「顶级核心」工作台配方 → 已删）
        //     主材料：高级丹炉核心
        //     副物品：沉银锭 ×4 + 星辰石 ×4
        //     精神力 800，30 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.HIGH_LEVEL_HEXIN.get(),
                        ModItems.TOP_LEVEL_HEXIN.get())
                .ingredient(ModItems.SUNKEN_SILVER_INGOT.get(), 4)
                .ingredient(ModItems.STARLIGHT_STONE.get(), 4)
                .spiritCost(800)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_top_liandan_core");

        // =====================================================================
        //  顶级魂环存储核心（原工作台配方 → 已删）
        //     主材料：刻刀（原配方用的是 ENGRAVING_KNIFE 标签，铁/钻石刻刀都行；
        //             仪式的主材料只能指定单一物品，这里先认「铁制刻刀」）
        //     副物品：星辰石 ×4 + 高阶魂环存储核心 ×4
        //     精神力 800，30 秒
        //     ⚠️ 刻刀在仪式里会被直接消耗掉（当作一次性材料），不会退还
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.IRON_ENGRAVING_KNIFE.get(),
                        ModItems.TOP_HUNHUAN_STORAGE_CORE.get())
                .ingredient(ModItems.STARLIGHT_STONE.get(), 4)
                .ingredient(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get(), 4)
                .spiritCost(800)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_top_hunhuan_core");

        HunlingRitualRecipeBuilder.create(ModItems.DIAMOND_ENGRAVING_KNIFE.get(),
                        ModItems.TOP_HUNHUAN_STORAGE_CORE.get())
                .ingredient(ModItems.STARLIGHT_STONE.get(), 4)
                .ingredient(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get(), 4)
                .spiritCost(800)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_top_hunhuan_core_diamone");

        // =====================================================================
        //  顶级御寒魂导器（原工作台配方 → 已删）
        //     主材料：顶级魂环存储核心
        //     副物品：沉银锭 ×4 + 星辰石 ×4
        //     精神力 1000，30 秒
        // =====================================================================
        HunlingRitualRecipeBuilder.create(ModItems.TOP_HUNHUAN_STORAGE_CORE.get(),
                        ModItems.TOP_COLD_PROTECTION.get())
                .ingredient(ModItems.SUNKEN_SILVER_INGOT.get(), 4)
                .ingredient(ModItems.STARLIGHT_STONE.get(), 4)
                .spiritCost(1000)
                .duration(DURATION_30S)
                .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_top_cold_protection");

        // =====================================================================
        //  召唤生物（测试配方，保留参考）
        //     3 腐肉摆基座 + 1 绿宝石当必需品，150 精神力 / 10 秒，召一只年限 1000 的僵尸
        //     ⚠️ 生物 ID 在仪式启动前会校验，写错/模组缺失时会拒绝开始
        //     ⚠️ 第三个参数是 JEI 展示物（一般写刷怪蛋），可以和实体 id 不一致
        // =====================================================================
        HunlingSpawnRitualRecipeBuilder.create(Items.EMERALD, "minecraft:zombie", Items.ZOMBIE_SPAWN_EGG)
                .ingredient(Items.ROTTEN_FLESH, 3)
                .spiritCost(150)
                .duration(200)
                .nianxian(520)
                .save(pWriter, KlMain.MOD_ID + ":hunling_spawn_zombie");

        // =====================================================================
        //  高级写法参考（注释掉，需要时取消注释改参数）
        // =====================================================================
        // —— 带副产物：消耗水桶返还空桶
        // HunlingRitualRecipeBuilder.create(Items.DIAMOND, Items.GOLD_BLOCK)
        //         .ingredient(Items.WATER_BUCKET, 2, Items.BUCKET)
        //         .ingredient(Items.GOLD_INGOT, 4)
        //         .spiritCost(5000)
        //         .duration(600)
        //         .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_gold");
        //
        // —— 严格 NBT：只认带特定 NBT 的那一件（主材料也要用 ItemStack 构造）
        // ItemStack specialCatalyst = new ItemStack(Items.NETHER_STAR);
        // specialCatalyst.getOrCreateTag().putString("KunlunRitual", "moon");
        // HunlingRitualRecipeBuilder.create(specialCatalyst, new ItemStack(Items.BEACON))
        //         .ingredient(Items.AMETHYST_SHARD, 4)
        //         .spiritCost(20000)
        //         .duration(2400)
        //         .strictNbt()
        //         .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_beacon");
    }
}
