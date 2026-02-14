package com.TovidY.kunluncontinent.datagen.oredatagen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
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

public class ModBiomeModifierProvider {
    // 定义 Key 的位置
    public static final ResourceKey<BiomeModifier> ADD_GRAY_IRON_ORE = createKey("add_gray_iron_ore");
    public static final ResourceKey<BiomeModifier> ADD_BRONZE_ORE = createKey("add_bronze_ore");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // --- 灰铁矿：生成在主世界 ---
        context.register(ADD_GRAY_IRON_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD), // 维度/生物群系限定
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenProvider.createPlaceKey(ModBlocks.GRAY_IRON_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES // 生成阶段：地下矿石
        ));

        // --- 云纹铜矿：生成在主世界 (以后若改下界，把下行换成 BiomeTags.IS_NETHER) ---
        context.register(ADD_BRONZE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenProvider.createPlaceKey(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get()))),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, name));
    }
}
