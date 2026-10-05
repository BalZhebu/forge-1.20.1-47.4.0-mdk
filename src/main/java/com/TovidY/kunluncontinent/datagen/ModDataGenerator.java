package com.TovidY.kunluncontinent.datagen;

import com.TovidY.kunluncontinent.Init.ModDamageTypes;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.datagen.advancement.ModAdvancementProvider;
import com.TovidY.kunluncontinent.datagen.blockprovider.ModBlockLootTablesProvider;
import com.TovidY.kunluncontinent.datagen.blockprovider.ModBlockStateProvider;
import com.TovidY.kunluncontinent.datagen.blockprovider.ModBlockTagsProvider;
import com.TovidY.kunluncontinent.datagen.itemprovider.ModItemModelsProvider;
import com.TovidY.kunluncontinent.datagen.itemprovider.ModItemTagsProvider;
import com.TovidY.kunluncontinent.datagen.itemprovider.ModRecipesProvider;
import com.TovidY.kunluncontinent.datagen.lang.ModZhCnLangProvider;
import com.TovidY.kunluncontinent.datagen.oredatagen.ModBiomeModifierProvider;
import com.TovidY.kunluncontinent.datagen.oredatagen.ModStructureProvider;
import com.TovidY.kunluncontinent.datagen.oredatagen.ModWorldGenOreProvider;
import com.TovidY.kunluncontinent.datagen.worldgenprovider.ModBiomeModifiers;
import com.TovidY.kunluncontinent.datagen.worldgenprovider.ModGlobalLootModifierProvider;
import com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
//数据生成
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            // ⭐ 注意：同一个注册表只能 `.add()` 一次（重复 add 会报
            // "Multiple entries with same key"），所以下面用 lambda 把神界
            // 和原有维度的bootstrap 串在同一次调用里。
            .add(Registries.BIOME, context -> {
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider.bootstrapBiome(context);
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialWorldGen.bootstrapCelestialBiome(context);
            })
            .add(Registries.DIMENSION_TYPE, context -> {
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider.bootstrapType(context);
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialWorldGen.bootstrapCelestialType(context);
            })
            .add(Registries.LEVEL_STEM, context -> {
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider.bootstrapStem(context);
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialWorldGen.bootstrapCelestialStem(context);
            })
            // 神界的岛屿地形噪声（独立注册表，可以单独add）
            .add(Registries.NOISE_SETTINGS, com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialWorldGen::bootstrapCelestialNoise)
            // ⚠️ 同一个注册表只能 .add() 一次，所以把神界树和矿物合并在一次调用里
            .add(Registries.CONFIGURED_FEATURE, context -> {
                ModWorldGenOreProvider.bootstrap(context);
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialTreeFeature.bootstrap(context);
            })
            .add(Registries.PLACED_FEATURE, context -> {
                ModWorldGenOreProvider.placement(context);
                com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialTreeFeature.placement(context);
            })
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
            .add(Registries.STRUCTURE, ModStructureProvider::bootstrapStructure)
            .add(Registries.STRUCTURE_SET, ModStructureProvider::bootstrapStructureSet)
            .add(Registries.TEMPLATE_POOL, ModStructureProvider::bootstrapPools)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                ModBiomeModifierProvider.bootstrap(context);
                ModBiomeModifiers.bootstrap(context);
            });

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModGlobalLootModifierProvider(packOutput, KlMain.MOD_ID));

        // 基础 Provider
        generator.addProvider(event.includeServer(), new ModRecipesProvider(packOutput));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK))));

        // 标签 Provider
        BlockTagsProvider blockTagsProvider = generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        // 客户端渲染相关 Provider
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModZhCnLangProvider(packOutput));

        // 成就
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(packOutput, lookupProvider, existingFileHelper));

        // 包含了维度和矿物
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, BUILDER, Set.of(KlMain.MOD_ID)));
    }
}