package com.TovidY.kunluncontinent.datagen;

import com.TovidY.kunluncontinent.KlMain;
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
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(),new ModRecipesProvider(packOutput));
        generator.addProvider(event.includeServer(),new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK))));

        BlockTagsProvider blockTagsProvider = generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(packOutput,lookupProvider,existingFileHelper));
        generator.addProvider(event.includeServer(),new ModItemTagsProvider(packOutput,lookupProvider,blockTagsProvider.contentsGetter(),existingFileHelper));

        generator.addProvider(event.includeClient(),new ModBlockStateProvider(packOutput,existingFileHelper));
        generator.addProvider(event.includeClient(),new ModItemModelsProvider(packOutput,existingFileHelper));
        generator.addProvider(event.includeClient(),new ModZhCnLangProvider(packOutput));

        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lookupProvider,
                new RegistrySetBuilder()
                        // 1. 注册矿物配置 (大小、替换目标)
                        .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrap)
                        // 2. 注册矿物放置 (高度、频率)
                        .add(Registries.PLACED_FEATURE, ModWorldGenProvider::placement)
                        // 3. 注册维度/生物群系绑定 (哪个矿在哪出现)
                        .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifierProvider::bootstrap),
                Set.of(KlMain.MOD_ID)));

    }
}
