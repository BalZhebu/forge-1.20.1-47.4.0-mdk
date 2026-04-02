package com.TovidY.kunluncontinent.datagen.oredatagen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import static com.TovidY.kunluncontinent.datagen.oredatagen.ModWorldGenOreProvider.RED_SPIDER_LILY_PLACED;

//矿物的生成
public class ModBiomeModifierProvider {
    // 定义 Key 的位置
    public static final ResourceKey<BiomeModifier> ADD_GRAY_IRON_ORE = createKey("add_gray_iron_ore");
    public static final ResourceKey<BiomeModifier> ADD_RUBY_ORE = createKey("add_ruby_ore");
    public static final ResourceKey<BiomeModifier> ADD_BRONZE_ORE = createKey("add_bronze_ore");

    public static final ResourceKey<BiomeModifier> ADD_RED_FIRE = createKey("add_red_fire_ore");
    public static final ResourceKey<BiomeModifier> ADD_AMETHYST_ORE = createKey("add_amethyst_ore");
    public static final ResourceKey<BiomeModifier> ADD_SAPPHIRE_ORE = createKey("add_sapphire_ore");
    public static final ResourceKey<BiomeModifier> ADD_STARLIGHT_STONE_ORE = createKey("add_starlight_stone_ore");

    public static final ResourceKey<BiomeModifier> ADD_SUNKEN_SILVER_ORE = createKey("add_sunken_silver_ore");
    public static final ResourceKey<BiomeModifier> ADD_COLD_HEARTED_STEEL_ORE = createKey("add_cold_hearter_stone_ore");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // --- 灰铁矿：生成在主世界 ---
        context.register(ADD_GRAY_IRON_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD), // 维度/生物群系限定
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.GRAY_IRON_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES // 生成阶段：地下矿石
        ));
        //红宝石矿
        context.register(ADD_RUBY_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD), // 维度/生物群系限定
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.RUBY_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES // 生成阶段：地下矿石
        ));

        // --- 云纹铜矿：生成在主世界 (以后若改下界，把下行换成 BiomeTags.IS_NETHER) ---
        context.register(ADD_BRONZE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //赤火矿
        context.register(ADD_RED_FIRE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.RED_FIRE_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //蓝晶
        context.register(ADD_SAPPHIRE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.SAPPHIRE_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //沉银矿
        context.register(ADD_SUNKEN_SILVER_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.SUNKEN_SILVER_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //冷心钢矿
        context.register(ADD_COLD_HEARTED_STEEL_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.COLD_HEARTED_STEEL_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //紫水晶矿
        context.register(ADD_AMETHYST_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.AMETHYST_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //星辰石
        context.register(ADD_STARLIGHT_STONE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.STARLIGHT_STONE_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        //彼岸花
        context.register(ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "add_red_spider_lily")),
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(RED_SPIDER_LILY_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                ));
    }

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, name));
    }
}
