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
            .add(Registries.BIOME, com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider::bootstrapBiome)
            .add(Registries.DIMENSION_TYPE, com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider::bootstrapType)
            .add(Registries.LEVEL_STEM, com.TovidY.kunluncontinent.datagen.worldgenprovider.ModWorldGenProvider::bootstrapStem)
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenOreProvider::bootstrap)
            .add(Registries.PLACED_FEATURE, ModWorldGenOreProvider::placement)
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