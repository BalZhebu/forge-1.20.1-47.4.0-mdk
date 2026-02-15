package com.TovidY.kunluncontinent.datagen.advancement;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.LevelTrigger;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends ForgeAdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ModAdvancements()));
    }

    public static class ModAdvancements implements AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
            Advancement root = Advancement.Builder.advancement()
                    .display(ModItems.IRON_ENGRAVING_KNIFE.get(),
                            Component.translatable("advancements.kunluncontinent.root.title"),
                            Component.translatable("advancements.kunluncontinent.root.description"),
                            ResourceLocation.tryParse("minecraft:textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK, true, true, false)
                    .addCriterion("on_join", PlayerTrigger.TriggerInstance.tick())
                    .save(saver,ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "main/root"), existingFileHelper);

            Advancement level10 = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            //成就图标
                            ModItems.CHUYUAN_DAN.get(),
                            Component.translatable("adv.kunlun.lvl10.title"),
                            Component.translatable("adv.kunlun.lvl10.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(10))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "main/level_10"), existingFileHelper);

            Advancement level20 = Advancement.Builder.advancement()
                    .parent(level10)
                    .display(
                            ModItems.BAICAOLING_DAN.get(),
                            Component.translatable("adv.kunlun.lvl20.title"),
                            Component.translatable("adv.kunlun.lvl20.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(20))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "main/level_20"), existingFileHelper);

            Advancement level30 = Advancement.Builder.advancement()
                    .parent(level10)
                    .display(
                            ModItems.QIANHUABAO_DAN.get(),
                            Component.translatable("adv.kunlun.lvl30.title"),
                            Component.translatable("adv.kunlun.lvl30.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(30))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "main/level_30"), existingFileHelper);
        }
    }
}