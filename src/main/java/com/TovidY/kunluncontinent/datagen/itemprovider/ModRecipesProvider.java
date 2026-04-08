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
    public static final List<ItemLike> RED_FIRE = List.of(ModBlocks.RED_FIRE_ORE.get());
    public static final List<ItemLike> SUNKEN_SILVER = List.of(ModBlocks.SUNKEN_SILVER_ORE.get());
    public static final List<ItemLike> COLD_HEARTED_STEEL = List.of(ModBlocks.COLD_HEARTED_STEEL_ORE.get());

    //宝石矿
    public static final List<ItemLike> RUBY_ORE = List.of(ModBlocks.RUBY_ORE.get());
    public static final List<ItemLike> SAPPHIRE_ORE = List.of(ModBlocks.SAPPHIRE_ORE.get());
    public static final List<ItemLike> AMETHYST_ORE = List.of(ModBlocks.AMETHYST_ORE.get());
    public static final List<ItemLike> STARLIGHT_STONE_ORE = List.of(ModBlocks.STARLIGHT_STONE_ORE.get());

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        oreSmelting(pWriter,GRAY_IRON,RecipeCategory.MISC,ModItems.GRAY_IRON_INGOT.get(),0.25F,200,"gray_iron");
        oreBlasting(pWriter,GRAY_IRON,RecipeCategory.MISC,ModItems.GRAY_IRON_INGOT.get(),0.25F,100,"gray_iron");
        oreSmelting(pWriter,CLOUD_PATTERNED_BRONZE,RecipeCategory.MISC,ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),0.30F,400,"cloud_patterned_bronze");
        oreBlasting(pWriter,CLOUD_PATTERNED_BRONZE,RecipeCategory.MISC,ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),0.30F,200,"cloud_patterned_bronze");
        oreSmelting(pWriter,RED_FIRE,RecipeCategory.MISC,ModItems.RED_FIRE_INGOT.get(),0.35F,400,"red_fire");
        oreBlasting(pWriter,RED_FIRE,RecipeCategory.MISC,ModItems.RED_FIRE_INGOT.get(),0.35F,200,"red_fire");
        oreSmelting(pWriter,SUNKEN_SILVER,RecipeCategory.MISC,ModItems.SUNKEN_SILVER_INGOT.get(),0.35F,800,"sunken_silver");
        oreBlasting(pWriter,SUNKEN_SILVER,RecipeCategory.MISC,ModItems.SUNKEN_SILVER_INGOT.get(),0.35F,400,"sunken_silver");
        oreSmelting(pWriter,COLD_HEARTED_STEEL,RecipeCategory.MISC,ModItems.COLD_HEARTED_STEEL_INGOT.get(),0.45F,1000,"cold_hearterd_steel");
        oreBlasting(pWriter,COLD_HEARTED_STEEL,RecipeCategory.MISC,ModItems.COLD_HEARTED_STEEL_INGOT.get(),0.45F,800,"cold_hearterd_steel");
        oreSmelting(pWriter,RUBY_ORE,RecipeCategory.MISC,ModItems.RUBY.get(),0.50F,1000,"ruby");
        oreBlasting(pWriter,RUBY_ORE,RecipeCategory.MISC,ModItems.RUBY.get(),0.50F,800,"ruby");
        oreSmelting(pWriter,SAPPHIRE_ORE,RecipeCategory.MISC,ModItems.SAPPHIRE.get(),0.50F,400,"sapphire");
        oreBlasting(pWriter,SAPPHIRE_ORE,RecipeCategory.MISC,ModItems.SAPPHIRE.get(),0.50F,200,"sapphire");
        oreSmelting(pWriter,AMETHYST_ORE,RecipeCategory.MISC,ModItems.AMETHYST.get(),0.50F,600,"amethyst");
        oreBlasting(pWriter,AMETHYST_ORE,RecipeCategory.MISC,ModItems.AMETHYST.get(),0.50F,400,"amethyst");
        oreSmelting(pWriter,STARLIGHT_STONE_ORE,RecipeCategory.MISC,ModItems.STARLIGHT_STONE.get(),0.50F,800,"starlight_stone");
        oreBlasting(pWriter,STARLIGHT_STONE_ORE,RecipeCategory.MISC,ModItems.STARLIGHT_STONE.get(),0.50F,400,"starlight_stone");

        //聚魂瓶合成
        SpecialRecipeBuilder.special(ModRecipes.BOTTLE_REFILL_SERIALIZER.get())
                .save(pWriter, KlMain.MOD_ID + ":bottle_refill");

        //丹药炼制代码
        LiandanRecipeBuilder.create(ModItems.NEIDAN1.get(), ModItems.CHUYUAN_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "chuyuan_dan_from_neidan1"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN2.get(), ModItems.BAICAOLING_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "baicaoling_dan_from_neidan2"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN3.get(), ModItems.QIANHUABAO_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "qianhuabao_dan_from_neidan3"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN4.get(), ModItems.WANPOXUAN_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "wanpoxuan_dan_from_neidan4"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN5.get(), ModItems.SHIFANGJIE_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "shifangjie_dan_from_neidan5"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN6.get(), ModItems.HUANYUANYIQI_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "huanyuanyiqi_dan_from_neidan6"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN7.get(), ModItems.TAIXUPOWANG_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "taixupowang_dan_from_neidan7"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN8.get(), ModItems.QIANWANXINGCHEN_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "qianwanxingchen_dan_from_neidan8"));
        LiandanRecipeBuilder.create(ModItems.NEIDAN9.get(), ModItems.YIZAICHUANGSHENG_DAN.get(), 400)
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "yizhaichuangsheng_dan_from_neidan9"));

        LiandanRecipeBuilder.create(ModItems.GUYUANCAO_ITEM.get(), ModItems.GUYUAN_DAN.get(), 200)
                .special()
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "guyuan_dan_from"));

        LiandanRecipeBuilder.create(ModItems.FANQICAO_ITEM.get(), ModItems.FANQI_DAN.get(), 150)
                .special()
                .save(pWriter,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "fanqi_dan_from"));

        //重修之眼的配方
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EYE_TRANSFORMATION.get())
                .pattern("ZXZ")
                .pattern("XOX")
                .pattern("ZXZ")
                .define('X', ModItems.RED_FIRE_INGOT.get())
                .define('Z', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('O', Items.ENDER_EYE)
                .unlockedBy("has_eye", has(ModItems.EYE_TRANSFORMATION.get()))
                .save(pWriter);

        //一阶魂环分解器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FIRST_DECOMPOSITION_GOSSIP.get())
                .pattern("ZXZ")
                .pattern("XOX")
                .pattern("ZXZ")
                .define('X', ModItems.GRAY_IRON_INGOT.get())
                .define('Z', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('O', ModItems.SOUL_GATHERING_BOTTLE_0.get())
                .unlockedBy("has_decomposer_0", has(ModItems.FIRST_DECOMPOSITION_GOSSIP.get()))
                .save(pWriter);

        //二阶魂环分解器
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.FIRST_DECOMPOSITION_GOSSIP.get()),
                        Ingredient.of(ModItems.LOW_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.TWO_DECOMPOSITION_GOSSIP.get()
                )
                .unlocks("has_kunluncontinent_ecomposition_gossip_1", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RUBY.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_ecomposition_gossip_1"));

        //三阶魂环分解器
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.TWO_DECOMPOSITION_GOSSIP.get()),
                        Ingredient.of(ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.THREE_DECOMPOSITION_GOSSIP.get()
                )
                .unlocks("has_kunluncontinent_ecomposition_gossip_2", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.SAPPHIRE.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_ecomposition_gossip_2"));

        //四阶魂环分解器
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.THREE_DECOMPOSITION_GOSSIP.get()),
                        Ingredient.of(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.FOUR_DECOMPOSITION_GOSSIP.get()
                )
                .unlocks("has_kunluncontinent_ecomposition_gossip_3", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.AMETHYST.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_ecomposition_gossip_3"));

        //五阶魂环分解
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()),
                        Ingredient.of(ModItems.FOUR_DECOMPOSITION_GOSSIP.get()),
                        Ingredient.of(ModItems.TOP_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.FIVE_DECOMPOSITION_GOSSIP.get()
                )
                .unlocks("has_kunluncontinent_ecomposition_gossip_4", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.STARLIGHT_STONE.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_ecomposition_gossip_4"));

        //一阶聚魂瓶
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOUL_GATHERING_BOTTLE_0.get())
                .pattern("ZXZ")
                .pattern("XOX")
                .pattern("ZXZ")
                .define('X', ModItems.GRAY_IRON_INGOT.get())
                .define('Z', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('O', Items.GLASS_BOTTLE)
                .unlockedBy("has_soul_gathering_bottle_0", has(ModItems.SOUL_GATHERING_BOTTLE_0.get()))
                .save(pWriter);

        //二级聚魂瓶
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.SOUL_GATHERING_BOTTLE_0.get()),
                        Ingredient.of(ModItems.LOW_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.SOUL_GATHERING_BOTTLE_1.get()
                )
                .unlocks("has_kunluncontinent_soul_gathering_bottle_1", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RUBY.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_soul_gathering_bottle_1"));

        //三阶聚魂瓶
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.SOUL_GATHERING_BOTTLE_1.get()),
                        Ingredient.of(ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.SOUL_GATHERING_BOTTLE_2.get()
                )
                .unlocks("has_kunluncontinent_soul_gathering_bottle_2", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.SAPPHIRE.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_soul_gathering_bottle_2"));

        //四阶聚魂瓶
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.SOUL_GATHERING_BOTTLE_2.get()),
                        Ingredient.of(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.SOUL_GATHERING_BOTTLE_3.get()
                )
                .unlocks("has_kunluncontinent_soul_gathering_bottle_3", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.AMETHYST.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_soul_gathering_bottle_3"));

        //五阶聚魂瓶
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()),
                        Ingredient.of(ModItems.SOUL_GATHERING_BOTTLE_3.get()),
                        Ingredient.of(ModItems.TOP_HUNHUAN_STORAGE_CORE.get()),
                        RecipeCategory.MISC,
                        ModItems.SOUL_GATHERING_BOTTLE_4.get()
                )
                .unlocks("has_kunluncontinent_soul_gathering_bottle_4", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.STARLIGHT_STONE.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_soul_gathering_bottle_4"));


        //锻造模版
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RINSEI_FORGING_TEMPLATE.get(),3)
                .pattern("IAI")
                .pattern("IBI")
                .pattern("III")
                .define('I', ModItems.RINSEI_INGOT.get())
                .define('A', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .define('B', ModItems.EXTREME_COLD.get())
                .unlockedBy("has_rinsei_forging)", has(ModItems.RINSEI_FORGING_TEMPLATE.get()))
                .save(pWriter);

        //丹渣
        SpecialRecipeBuilder.special(ModRecipes.DROSS_CONVERSION_SERIALIZER.get())
                .save(pWriter, KlMain.MOD_ID + ":dross_conversion");

        //引导书
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GUIDE_BOOK.get())
                .pattern("IX")
                .define('I', Items.BOOK)
                .define('X', ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_guide_book", has(ModItems.GUIDE_BOOK.get()))
                .save(pWriter);

        //极寒打火石
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTREME_COLD_SNOWFLAKE.get())
                .pattern("III")
                .pattern("IAI")
                .pattern("III")
                .define('A', Items.FLINT_AND_STEEL)
                .define('I', ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get())
                .unlockedBy("has_extreme_cold_snowflake_peif", has(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get()))
                .save(pWriter);

        //冰晶
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EXTREME_COLD.get())
                .pattern("II")
                .pattern("II")
                .define('I', ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get())
                .unlockedBy("has_extreme_cold_snowflak", has(ModItems.EXTREME_COLD.get()))
                .save(pWriter);

        //极寒冰晶框架
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())
                .pattern("III")
                .pattern("IWI")
                .pattern("III")
                .define('W', ModItems.EXTREME_COLD.get())
                .define('I', Items.SNOW_BLOCK)
                .unlockedBy("has_polar_ice_portal_block_peifang)", has(ModItems.EXTREME_COLD.get()))
                .save(pWriter);

        //低阶魂环存储核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_HUNHUAN_STORAGE_CORE.get())
                .pattern(" R ")
                .pattern("RKR")
                .pattern(" R ")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.RUBY.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //中阶魂环存储核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.SAPPHIRE.get())
                .define('X', ModItems.LOW_HUNHUAN_STORAGE_CORE.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //高阶魂环存储核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HIGH_HUNHUAN_STORAGE_CORE.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.AMETHYST.get())
                .define('X', ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //顶级魂环存储核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TOP_HUNHUAN_STORAGE_CORE.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.STARLIGHT_STONE.get())
                .define('X', ModItems.HIGH_HUNHUAN_STORAGE_CORE.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //一阶魂环储存器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.HUNHUAN_STORAGE_ONE.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', ModItems.GRAY_IRON_INGOT.get())
                .define('X', ModItems.LOW_HUNHUAN_STORAGE_CORE.get())
                .define('Z', Items.IRON_INGOT)
                .define('Y', ModItems.NEIDAN1.get())
                .unlockedBy("has_hunhuan_storage", has(ModItems.HUNHUAN_STORAGE_ONE.get()))
                .save(pWriter);

        //二阶魂环储存器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.HUNHUAN_STORAGE_TWO.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('X', ModItems.LOW_HUNHUAN_STORAGE_CORE.get())
                .define('Z', ModItems.GRAY_IRON_INGOT.get())
                .define('Y', ModItems.HUNHUAN_STORAGE_ONE.get())
                .unlockedBy("has_hunhuan_storage", has(ModItems.HUNHUAN_STORAGE_TWO.get()))
                .save(pWriter);

        //三阶魂环储存器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.HUNHUAN_STORAGE_THREE.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', ModItems.RED_FIRE_INGOT.get())
                .define('X', ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get())
                .define('Z', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('Y', ModItems.HUNHUAN_STORAGE_TWO.get())
                .unlockedBy("has_hunhuan_storage", has(ModItems.HUNHUAN_STORAGE_THREE.get()))
                .save(pWriter);

        //四阶魂环储存器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.HUNHUAN_STORAGE_FOUR.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('X', ModItems.HIGH_HUNHUAN_STORAGE_CORE.get())
                .define('Z', ModItems.RED_FIRE_INGOT.get())
                .define('Y', ModItems.HUNHUAN_STORAGE_THREE.get())
                .unlockedBy("has_hunhuan_storage", has(ModItems.HUNHUAN_STORAGE_FOUR.get()))
                .save(pWriter);

        //五阶魂环储存器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.HUNHUAN_STORAGE_FIVE.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('X', ModItems.TOP_HUNHUAN_STORAGE_CORE.get())
                .define('Z', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('Y', ModItems.HUNHUAN_STORAGE_FOUR.get())
                .unlockedBy("has_hunhuan_storage", has(ModItems.HUNHUAN_STORAGE_FIVE.get()))
                .save(pWriter);

        //低阶核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_LEVEL_HEXIN.get())
                .pattern("RRR")
                .pattern("RKR")
                .pattern("RRR")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('R', ModItems.RUBY.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //中阶核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIDDLE_LEVEL_HEXIN.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', Ingredient.of(ModTags.Items.ENGRAVING_KNIFE))
                .define('X', ModItems.RUBY.get())
                .define('R', ModItems.SAPPHIRE.get())
                .unlockedBy("has_knife", has(ModTags.Items.ENGRAVING_KNIFE))
                .save(pWriter);

        //高阶核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HIGH_LEVEL_HEXIN.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.MIDDLE_LEVEL_HEXIN.get())
                .define('X', ModItems.AMETHYST.get())
                .define('R', ModItems.RED_FIRE_INGOT.get())
                .unlockedBy("has_knife", has(ModItems.HIGH_LEVEL_HEXIN.get()))
                .save(pWriter);

        //顶级核心
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TOP_LEVEL_HEXIN.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.HIGH_LEVEL_HEXIN.get())
                .define('X', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('R', ModItems.STARLIGHT_STONE.get())
                .unlockedBy("has_knife", has(ModItems.TOP_LEVEL_HEXIN.get()))
                .save(pWriter);

        //低阶御寒魂导器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOW_COLD_PROTECTION.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.LOW_HUNHUAN_STORAGE_CORE.get())
                .define('X', Items.REDSTONE_BLOCK)
                .define('R', ModItems.GRAY_IRON_INGOT.get())
                .unlockedBy("has_cold_protections", has(ModItems.LOW_COLD_PROTECTION.get()))
                .save(pWriter);

        //中阶御寒魂导器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MID_COLD_PROTECTION.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get())
                .define('X', ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())
                .define('R', ModItems.SAPPHIRE.get())
                .unlockedBy("has_cold_protections", has(ModItems.MID_COLD_PROTECTION.get()))
                .save(pWriter);

        //高阶御寒魂导器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HIGH_COLD_PROTECTION.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.HIGH_HUNHUAN_STORAGE_CORE.get())
                .define('X', ModItems.RED_FIRE_INGOT.get())
                .define('R', ModItems.AMETHYST.get())
                .unlockedBy("has_cold_protections", has(ModItems.HIGH_COLD_PROTECTION.get()))
                .save(pWriter);

        //顶级御寒魂导器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TOP_COLD_PROTECTION.get())
                .pattern("XRX")
                .pattern("RKR")
                .pattern("XRX")
                .define('K', ModItems.TOP_HUNHUAN_STORAGE_CORE.get())
                .define('X', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('R', ModItems.STARLIGHT_STONE.get())
                .unlockedBy("has_cold_protections", has(ModItems.TOP_COLD_PROTECTION.get()))
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

        //三阶炼丹炉
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.LIANDANLU3.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', Blocks.OBSIDIAN)
                .define('X', ModItems.LOW_LEVEL_HEXIN.get())
                .define('Z', ModItems.RED_FIRE_INGOT.get())
                .define('Y', ModBlocks.LIANDANLU2.get())
                .unlockedBy("has_liandanlu3", has(ModBlocks.LIANDANLU3.get()))
                .save(pWriter);

        //四阶炼丹炉
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.LIANDANLU4.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', Blocks.OBSIDIAN)
                .define('X', ModItems.MIDDLE_LEVEL_HEXIN.get())
                .define('Z', ModItems.SUNKEN_SILVER_INGOT.get())
                .define('Y', ModBlocks.LIANDANLU3.get())
                .unlockedBy("has_liandanlu4", has(ModBlocks.LIANDANLU4.get()))
                .save(pWriter);

        //五阶炼丹炉
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.LIANDANLU5.get())
                .pattern("#Y#")
                .pattern("ZXZ")
                .pattern("#Z#")
                .define('#', Blocks.OBSIDIAN)
                .define('X', ModItems.MIDDLE_LEVEL_HEXIN.get())
                .define('Z', ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('Y', ModBlocks.LIANDANLU4.get())
                .unlockedBy("has_liandanlu5", has(ModBlocks.LIANDANLU5.get()))
                .save(pWriter);

        //六级炼丹炉（锻造配方）
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.MIDDLE_LEVEL_HEXIN.get()),
                        Ingredient.of(ModBlocks.LIANDANLU5.get()),
                        Ingredient.of(ModItems.RINSEI_INGOT.get()),
                        RecipeCategory.MISC,
                        ModBlocks.LIANDANLU6.get().asItem())
                .unlocks("has_liandanlu6", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "liandanlu6_smithing"));

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

        //赤火矿
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.RED_FIRE_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("M")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.RED_FIRE_PICKAXE.get())
                .pattern("###")
                .pattern(" M ")
                .pattern(" M ")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.RED_FIRE_AXE.get())
                .pattern("##")
                .pattern("#M")
                .pattern(" M")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.RED_FIRE_SHOVEL.get())
                .pattern("#")
                .pattern("M")
                .pattern("M")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.RED_FIRE_HOE.get())
                .pattern("##")
                .pattern(" M")
                .pattern(" M")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.RED_FIRE_HELMET.get())
                .pattern("###")
                .pattern("# #")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.RED_FIRE_CHESTPLATE.get())
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.RED_FIRE_LEGGINGS.get())
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.RED_FIRE_BOOTS.get())
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.RED_FIRE_INGOT.get())
                .unlockedBy("has_red_fire_ingot", has(ModItems.RED_FIRE_INGOT.get()))
                .save(pWriter);

        //沉银
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.SUNKEN_SILVER_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("M")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.SUNKEN_SILVER_PICKAXE.get())
                .pattern("###")
                .pattern(" M ")
                .pattern(" M ")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.SUNKEN_SILVER_AXE.get())
                .pattern("##")
                .pattern("#M")
                .pattern(" M")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.SUNKEN_SILVER_SHOVEL.get())
                .pattern("#")
                .pattern("M")
                .pattern("M")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.SUNKEN_SILVER_HOE.get())
                .pattern("##")
                .pattern(" M")
                .pattern(" M")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .define('M', Items.STICK)
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.SUNKEN_SILVER_HELMET.get())
                .pattern("###")
                .pattern("# #")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.SUNKEN_SILVER_CHESTPLATE.get())
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.SUNKEN_SILVER_LEGGINGS.get())
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.SUNKEN_SILVER_BOOTS.get())
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_sunken_silver_ingot", has(ModItems.SUNKEN_SILVER_INGOT.get()))
                .save(pWriter);

        //寒心钢
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.COLD_HEARTED_STEEL_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("M")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('M', ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.COLD_HEARTED_STEEL_PICKAXE.get())
                .pattern("###")
                .pattern(" M ")
                .pattern(" M ")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('M', ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.COLD_HEARTED_STEEL_AXE.get())
                .pattern("##")
                .pattern("#M")
                .pattern(" M")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('M', ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.COLD_HEARTED_STEEL_SHOVEL.get())
                .pattern("#")
                .pattern("M")
                .pattern("M")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('M', ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.COLD_HEARTED_STEEL_HOE.get())
                .pattern("##")
                .pattern(" M")
                .pattern(" M")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .define('M', ModItems.SUNKEN_SILVER_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.COLD_HEARTED_STEEL_HELMET.get())
                .pattern("###")
                .pattern("# #")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.COLD_HEARTED_STEEL_CHESTPLATE.get())
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.COLD_HEARTED_STEEL_LEGGINGS.get())
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT,ModItems.COLD_HEARTED_STEEL_BOOTS.get())
                .pattern("# #")
                .pattern("# #")
                .define('#',ModItems.COLD_HEARTED_STEEL_INGOT.get())
                .unlockedBy("has_hearted_steel_ingot", has(ModItems.COLD_HEARTED_STEEL_INGOT.get()))
                .save(pWriter);

        //凛晶
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_SWORD.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.COMBAT, // 配方分类
                        ModItems.RINSEI_SWORD.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing1")); // 保存路径

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_PICKAXE.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.COMBAT, // 配方分类
                        ModItems.RINSEI_PICKAXE.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing2"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_AXE.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.TOOLS, // 配方分类
                        ModItems.RINSEI_AXE.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing3"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_SHOVEL.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.TOOLS, // 配方分类
                        ModItems.RINSEI_SHOVEL.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing4"));
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_HOE.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.TOOLS, // 配方分类
                        ModItems.RINSEI_HOE.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing5"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_HELMET.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.TOOLS, // 配方分类
                        ModItems.RINSEI_HELMET.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing6"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_CHESTPLATE.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.COMBAT, // 配方分类
                        ModItems.RINSEI_CHESTPLATE.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing7"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()), // 锻造模板
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_LEGGINGS.get()), // 基础武器（材料武器）
                        Ingredient.of(ModItems.RINSEI_INGOT.get()), // 消耗的锭
                        RecipeCategory.COMBAT, // 配方分类
                        ModItems.RINSEI_LEGGINGS.get() // 输出的结果武器
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build())) // 解锁条件：获得该锭时解锁配方册
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing8"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.RINSEI_FORGING_TEMPLATE.get()),
                        Ingredient.of(ModItems.COLD_HEARTED_STEEL_BOOTS.get()),
                        Ingredient.of(ModItems.RINSEI_INGOT.get()),
                        RecipeCategory.COMBAT,
                        ModItems.RINSEI_BOOTS.get()
                )
                .unlocks("has_kunluncontinent_rinsei_duanzao", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.RINSEI_INGOT.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kunluncontinent_weapon_smithing9"));
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