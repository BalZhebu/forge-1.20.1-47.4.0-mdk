package com.TovidY.kunluncontinent.datagen.itemprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.ModTags;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipeBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

//配方生成

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder  {
    public ModRecipesProvider(PackOutput pOutput) {
        super(pOutput);
    }

    public static final List<ItemLike> GRAY_IRON = List.of(ModBlocks.GRAY_IRON_ORE.get());
    public static final List<ItemLike> CLOUD_PATTERNED_BRONZE = List.of(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get());

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        oreSmelting(pWriter,GRAY_IRON,RecipeCategory.MISC,ModItems.GRAY_IRON_INGOT.get(),0.25F,200,"gray_iron");
        oreBlasting(pWriter,GRAY_IRON,RecipeCategory.MISC,ModItems.GRAY_IRON_INGOT.get(),0.25F,100,"gray_iron");
        oreSmelting(pWriter,CLOUD_PATTERNED_BRONZE,RecipeCategory.MISC,ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),0.30F,400,"cloud_patterned_bronze");
        oreBlasting(pWriter,CLOUD_PATTERNED_BRONZE,RecipeCategory.MISC,ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),0.30F,200,"cloud_patterned_bronze");

        //丹药炼制代码
        LiandanRecipeBuilder.create(ModItems.NEIDAN1.get(), ModItems.CHUYUAN_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "chuyuan_dan_from_neidan1"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN2.get(), ModItems.BAICAOLING_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "baicaoling_dan_from_neidan2"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN3.get(), ModItems.QIANHUABAO_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "qianhuabao_dan_from_neidan3"));

        //丹渣
        SpecialRecipeBuilder.special(ModRecipes.DROSS_CONVERSION_SERIALIZER.get())
                .save(pWriter, KlMain.MOD_ID + ":dross_conversion");

        //低阶核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_LEVEL_HEXIN.get())
                .pattern(" R ")
                .pattern("RKR")
                .pattern(" R ")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.RUBY.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //一阶炼丹炉
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.LIANDANLU1.get())
                .pattern("#Z#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', Blocks.OBSIDIAN)
                .define('X', ModItems.LOW_LEVEL_HEXIN.get())
                .define('Z', ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_liandanlu1", has(ModBlocks.LIANDANLU1.get()))
                .save(pWriter);

        //二阶炼丹炉
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.LIANDANLU2.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', Blocks.OBSIDIAN)
                .define('X', ModItems.LOW_LEVEL_HEXIN.get())
                .define('Z', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('Y', ModBlocks.LIANDANLU1.get())
                .unlockedBy("has_liandanlu2", has(ModBlocks.LIANDANLU2.get()))
                .save(pWriter);

        //丹渣块
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DROSS_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.DROSS.get()) // 这里直接使用丹渣物品
                .unlockedBy("has_dross_item", has(ModItems.DROSS.get()))
                .save(pWriter);

        //铁刻刀
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.IRON_ENGRAVING_KNIFE.get())
                .pattern(" ##")
                .pattern(" M#")
                .pattern("M  ")
                .define('#', Items.IRON_INGOT)
                .define('M', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(pWriter);

        //钻石刻刀
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.DIAMOND_ENGRAVING_KNIFE.get())
                .pattern(" ##")
                .pattern(" M#")
                .pattern("M  ")
                .define('#', Items.DIAMOND)
                .define('M', Items.STICK)
                .unlockedBy("has_diamond_ingot", has(Items.DIAMOND))
                .save(pWriter);

        //蒲团
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.PUTUAN_BLOCK.get())
                .pattern("# #")
                .pattern(" # ")
                .define('#', Items.HAY_BLOCK)
                .unlockedBy("has_putuan_block", has(ModBlocks.PUTUAN_BLOCK.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.GRAY_IRON_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("M")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.GRAY_IRON_PICKAXE.get())
                .pattern("###")
                .pattern(" M ")
                .pattern(" M ")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.GRAY_IRON_AXE.get())
                .pattern("##")
                .pattern("#M")
                .pattern(" M")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.GRAY_IRON_SHOVEL.get())
                .pattern("#")
                .pattern("M")
                .pattern("M")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.GRAY_IRON_HOE.get())
                .pattern("##")
                .pattern(" M")
                .pattern(" M")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.GRAY_IRON_HELMET.get())
                .pattern("###")
                .pattern("# #")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.GRAY_IRON_CHESTPLATE.get())
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.GRAY_IRON_LEGGINGS.get())
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.GRAY_IRON_BOOTS.get())
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_gray_iron_ingot", has(ModItems.GRAY_IRON_INGOT.get()))
                .save(pWriter);
        //云纹铜
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.CLOUD_PATTERNED_BRONZE_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("M")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.CLOUD_PATTERNED_BRONZE_PICKAXE.get())
                .pattern("###")
                .pattern(" M ")
                .pattern(" M ")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.CLOUD_PATTERNED_BRONZE_AXE.get())
                .pattern("##")
                .pattern("#M")
                .pattern(" M")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.CLOUD_PATTERNED_BRONZE_SHOVEL.get())
                .pattern("#")
                .pattern("M")
                .pattern("M")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.CLOUD_PATTERNED_BRONZE_HOE.get())
                .pattern("##")
                .pattern(" M")
                .pattern(" M")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.CLOUD_PATTERNED_BRONZE_HELMET.get())
                .pattern("###")
                .pattern("# #")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.CLOUD_PATTERNED_BRONZE_CHESTPLATE.get())
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.CLOUD_PATTERNED_BRONZE_LEGGINGS.get())
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.CLOUD_PATTERNED_BRONZE_BOOTS.get())
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .unlockedBy("has_cloud_patterned_bronze_ingot", has(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()))
                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder
                    .generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer, KlMain.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}






//    配方类别，决定在配方书中显示的位置
//    可选值：
//    RecipeCategory.BUILDING_BLOCKS - 建筑方块
//    RecipeCategory.DECORATIONS - 装饰方块
//    RecipeCategory.REDSTONE - 红石
//    RecipeCategory.TRANSPORTATION - 运输
//    RecipeCategory.MISC - 杂项（默认）
//    RecipeCategory.FOOD - 食物
//    RecipeCategory.TOOLS - 工具
//    RecipeCategory.COMBAT - 战斗

//      oreSmelting(
//            pWriter,                    // 参数1：RecipeOutput 对象
//            GRAY_IRON,                 // 参数2：输入物品
//            RecipeCategory.MISC,        // 参数3：配方类别
//            ModItems.GRAY_IRON_INGOT.get(), // 参数4：输出物品
//    0.25F,                      // 参数5：经验值
//            200,                        // 参数6：熔炼时间（刻）
//            "gray_iron"                 // 参数7：配方ID
//            );
//// 基础语法
//oreSmelting(pWriter, 输入物品, 配方类别, 输出物品, 经验值, 时间, "分组");
//
//    // 完整示例   熔炉
//    oreSmelting(pWriter,
//                Ingredient.of(ModItems.GRAY_IRON_ORE.get()), // 输入：灰色铁矿石
//    RecipeCategory.MISC,                         // 类别：杂项
//            ModItems.GRAY_IRON_INGOT.get(),              // 输出：灰色铁锭
//            0.7F,                                        // 经验：0.7点
//            200,                                         // 时间：200刻（10秒）
//            "gray_iron");                                // 分组：gray_iron

// 高炉
//oreBlasting(pWriter, 输入物品, 配方类别, 输出物品, 经验值, 时间, "分组");
//
//        // 完整示例
//        oreBlasting(pWriter,
//                    Ingredient.of(ModItems.GRAY_IRON_ORE.get()),
//        RecipeCategory.MISC,
//        ModItems.GRAY_IRON_INGOT.get(),
//    0.7F,      // 经验相同
//            100,       // 时间减半（高炉特性）
//            "gray_iron");

//// 熔炉烹饪
//foodSmelting(pWriter, 输入食物, 配方类别, 输出食物, 经验值, 时间, "分组");
//
//        // 完整示例
//        foodSmelting(pWriter,
//                     Ingredient.of(ModItems.RAW_MYSTIC_MEAT.get()),
//        RecipeCategory.FOOD,                        // 类别：食物
//        ModItems.COOKED_MYSTIC_MEAT.get(),
//    0.35F,                                      // 经验：0.35点
//            200,
//            "mystic_meat");

//// 烟熏炉烹饪
//foodSmoking(pWriter, 输入食物, 配方类别, 输出食物, 经验值, 时间, "分组");
//
//        // 完整示例
//        foodSmoking(pWriter,
//                    Ingredient.of(ModItems.RAW_MYSTIC_MEAT.get()),
//        RecipeCategory.FOOD,
//        ModItems.COOKED_MYSTIC_MEAT.get(),
//    0.35F,
//            100,       // 时间减半
//            "mystic_meat");

//// 营火烹饪
//campfireCooking(pWriter, 输入食物, 配方类别, 输出食物, 经验值, 时间, "分组");
//
//        // 完整示例
//        campfireCooking(pWriter,
//                        Ingredient.of(ModItems.RAW_MYSTIC_MEAT.get()),
//        RecipeCategory.FOOD,
//        ModItems.COOKED_MYSTIC_MEAT.get(),
//    0.35F,
//            600,       // 时间更长（30秒），但不需要燃料
//            "mystic_meat");

//// 2*2合成，指4个木板合成工作台
//twoByTwoPacker(pWriter, 配方类别, 输出方块, 输入材料);
//
//        // 完整示例：4个铁锭→铁块
//        twoByTwoPacker(pWriter,
//                       RecipeCategory.BUILDING_BLOCKS,
//                       Items.IRON_BLOCK,
//                       Items.IRON_INGOT);

////  3x3打包
//threeByThreePacker(pWriter, 配方类别, 输出方块, 输入材料);
//
//        // 完整示例：9个钻石→钻石块
//        threeByThreePacker(pWriter,
//                           RecipeCategory.BUILDING_BLOCKS,
//                           Items.DIAMOND_BLOCK,
//                           Items.DIAMOND);\

////  有序合成
//shaped(pWriter, 输出物品, 字符映射表, "模式行1", "模式行2", "模式行3");
//
//        // 完整示例：木镐配方
//        shaped(pWriter,
//               Items.WOODEN_PICKAXE,           // 输出：木镐
//               define('#', Items.OAK_PLANKS),  // # = 橡木木板
//        define('|', Items.STICK),       // | = 木棍
//            "###",                          // 第一行
//            " | ",                          // 第二行
//            " | ");                         // 第三行