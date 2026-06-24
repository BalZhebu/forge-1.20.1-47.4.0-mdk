package com.TovidY.kunluncontinent.datagen.advancement;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.LevelTrigger;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
                    .display(ModItems.HUNHUAN_BUTTON.get(),
                            Component.translatable("advancements.kunluncontinent.root.title"),
                            Component.translatable("advancements.kunluncontinent.root.description"),
                            ResourceLocation.tryParse("minecraft:textures/gui/advancements/backgrounds/stone.png"),
                            FrameType.TASK, true, true, false)
                    .addCriterion("on_join", PlayerTrigger.TriggerInstance.tick())
                    .save(saver,new ResourceLocation(KlMain.MOD_ID, "main/root"), existingFileHelper);

            Advancement rubyAdvancement = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModItems.RUBY.get(),
                            Component.translatable("advancements.kunluncontinent.ruby.title"),
                            Component.translatable("advancements.kunluncontinent.ruby.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.RUBY.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_ruby"), existingFileHelper);

            Advancement sapphireAdvancement = Advancement.Builder.advancement()
                    .parent(rubyAdvancement)
                    .display(
                            ModItems.SAPPHIRE.get(),
                            Component.translatable("advancements.kunluncontinent.sapphire.title"),
                            Component.translatable("advancements.kunluncontinent.sapphire.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_sapphire", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.SAPPHIRE.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_sapphire"), existingFileHelper);

            Advancement amethystAdvancement = Advancement.Builder.advancement()
                    .parent(sapphireAdvancement)
                    .display(
                            ModItems.AMETHYST.get(),
                            Component.translatable("advancements.kunluncontinent.amethyst.title"),
                            Component.translatable("advancements.kunluncontinent.amethyst.description"),
                            null,
                            FrameType.CHALLENGE, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.AMETHYST.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_amethyst"), existingFileHelper);

            Advancement starlightstoneAdvancement = Advancement.Builder.advancement()
                    .parent(amethystAdvancement)
                    .display(
                            ModItems.STARLIGHT_STONE.get(),
                            Component.translatable("advancements.kunluncontinent.starlight_stone.title"),
                            Component.translatable("advancements.kunluncontinent.starlight_stone.description"),
                            null,
                            FrameType.GOAL, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_starlight_stone", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.STARLIGHT_STONE.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_starlight_stone"), existingFileHelper);


            Advancement grayIronAdvancement = Advancement.Builder.advancement()
                    .parent(root) // 设置父成就（可选）
                    .display(
                            ModItems.GRAY_IRON_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.gray_iron_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.gray_iron_ingot.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.GRAY_IRON_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot"), existingFileHelper);

            Advancement grayIronAdvancement2 = Advancement.Builder.advancement()
                    .parent(grayIronAdvancement) // 设置父成就（可选）
                    .display(
                            ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.cloud_patterned_bronze_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.cloud_patterned_bronze_ingot.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot2"), existingFileHelper);

            Advancement grayIronAdvancement3 = Advancement.Builder.advancement()
                    .parent(grayIronAdvancement2) // 设置父成就（可选）
                    .display(
                            ModItems.RED_FIRE_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.red_fire_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.red_fire_ingot.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.RED_FIRE_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot3"), existingFileHelper);

            Advancement grayIronAdvancement4 = Advancement.Builder.advancement()
                    .parent(grayIronAdvancement3) // 设置父成就（可选）
                    .display(
                            ModItems.SUNKEN_SILVER_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.sunken_silver_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.sunken_silver_ingot.description"),
                            null,
                            FrameType.TASK, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.SUNKEN_SILVER_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot4"), existingFileHelper);

            Advancement grayIronAdvancement5 = Advancement.Builder.advancement()
                    .parent(grayIronAdvancement4) // 设置父成就（可选）
                    .display(
                            ModItems.COLD_HEARTED_STEEL_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.cold_heated_steel_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.cold_heated_steel_ingot.description"),
                            null,
                            FrameType.CHALLENGE, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.COLD_HEARTED_STEEL_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot5"), existingFileHelper);

            Advancement grayIronAdvancement6 = Advancement.Builder.advancement()
                    .parent(grayIronAdvancement5) // 设置父成就（可选）
                    .display(
                            ModItems.RINSEI_INGOT.get(),
                            Component.translatable("advancements.kunluncontinent.rinsei_ingot.title"),
                            Component.translatable("advancements.kunluncontinent.rinsei_ingot.description"),
                            null,
                            FrameType.CHALLENGE, // 成就框类型：TASK(普通), CHALLENGE(挑战), GOAL(目标)
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_gray_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(
                            ModItems.RINSEI_INGOT.get()
                    ))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/get_gray_iron_ingot6"), existingFileHelper);


            // 1. 检查获得特定物品/方块的成就
            Advancement obtainItem = Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            ModBlocks.LIANDANLU1.get(),
                            Component.translatable("adv.kunlun.liandanlu1.title"),
                            Component.translatable("adv.kunlun.liandanlu1.desc"),
                            null,
                            FrameType.TASK, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU1.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu1"), existingFileHelper);

            // 1. 检查获得特定物品/方块的成就
            Advancement liandanlu2 = Advancement.Builder.advancement()
                    .parent(obtainItem)
                    .display(
                            ModBlocks.LIANDANLU2.get(),
                            Component.translatable("adv.kunlun.liandanlu2.title"),
                            Component.translatable("adv.kunlun.liandanlu2.desc"),
                            null,
                            FrameType.TASK, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU2.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu2"), existingFileHelper);

            Advancement liandanlu3 = Advancement.Builder.advancement()
                    .parent(liandanlu2)
                    .display(
                            ModBlocks.LIANDANLU3.get(),
                            Component.translatable("adv.kunlun.liandanlu3.title"),
                            Component.translatable("adv.kunlun.liandanlu3.desc"),
                            null,
                            FrameType.TASK, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU3.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu3"), existingFileHelper);

            Advancement liandanlu4 = Advancement.Builder.advancement()
                    .parent(liandanlu3)
                    .display(
                            ModBlocks.LIANDANLU4.get(),
                            Component.translatable("adv.kunlun.liandanlu4.title"),
                            Component.translatable("adv.kunlun.liandanlu4.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU4.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu4"), existingFileHelper);

            Advancement liandanlu5 = Advancement.Builder.advancement()
                    .parent(liandanlu4)
                    .display(
                            ModBlocks.LIANDANLU5.get(),
                            Component.translatable("adv.kunlun.liandanlu5.title"),
                            Component.translatable("adv.kunlun.liandanlu5.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )

                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU5.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu5"), existingFileHelper);

            Advancement liandanlu6 = Advancement.Builder.advancement()
                    .parent(liandanlu5)
                    .display(
                            ModBlocks.LIANDANLU6.get(),
                            Component.translatable("adv.kunlun.liandanlu6.title"),
                            Component.translatable("adv.kunlun.liandanlu6.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )

                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU6.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu6"), existingFileHelper);

            Advancement liandanlu7 = Advancement.Builder.advancement()
                    .parent(liandanlu6)
                    .display(
                            ModBlocks.LIANDANLU7.get(),
                            Component.translatable("adv.kunlun.liandanlu7.title"),
                            Component.translatable("adv.kunlun.liandanlu7.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )

                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU7.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu7"), existingFileHelper);

            Advancement liandanlu8 = Advancement.Builder.advancement()
                    .parent(liandanlu7)
                    .display(
                            ModBlocks.LIANDANLU8.get(),
                            Component.translatable("adv.kunlun.liandanlu8.title"),
                            Component.translatable("adv.kunlun.liandanlu8.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )

                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU8.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu8"), existingFileHelper);

            Advancement liandanlu9 = Advancement.Builder.advancement()
                    .parent(liandanlu8)
                    .display(
                            ModBlocks.LIANDANLU9.get(),
                            Component.translatable("adv.kunlun.liandanlu9.title"),
                            Component.translatable("adv.kunlun.liandanlu9.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIANDANLU9.get()))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/liandanlu9"), existingFileHelper);

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
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_10"), existingFileHelper);

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
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_20"), existingFileHelper);

            Advancement level30 = Advancement.Builder.advancement()
                    .parent(level20)
                    .display(
                            ModItems.QIANHUABAO_DAN.get(),
                            Component.translatable("adv.kunlun.lvl30.title"),
                            Component.translatable("adv.kunlun.lvl30.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(30))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_30"), existingFileHelper);

            Advancement level40 = Advancement.Builder.advancement()
                    .parent(level30)
                    .display(
                            ModItems.WANPOXUAN_DAN.get(),
                            Component.translatable("adv.kunlun.lvl40.title"),
                            Component.translatable("adv.kunlun.lvl40.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(40))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_40"), existingFileHelper);

            Advancement level50 = Advancement.Builder.advancement()
                    .parent(level40)
                    .display(
                            ModItems.SHIFANGJIE_DAN.get(),
                            Component.translatable("adv.kunlun.lvl50.title"),
                            Component.translatable("adv.kunlun.lvl50.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(50))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_50"), existingFileHelper);

            Advancement level60 = Advancement.Builder.advancement()
                    .parent(level50)
                    .display(
                            ModItems.HUANYUANYIQI_DAN.get(),
                            Component.translatable("adv.kunlun.lvl60.title"),
                            Component.translatable("adv.kunlun.lvl60.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(60))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_60"), existingFileHelper);

            Advancement level70 = Advancement.Builder.advancement()
                    .parent(level60)
                    .display(
                            ModItems.TAIXUPOWANG_DAN.get(),
                            Component.translatable("adv.kunlun.lvl70.title"),
                            Component.translatable("adv.kunlun.lvl70.desc"),
                            null,
                            FrameType.GOAL, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(70))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_70"), existingFileHelper);

            Advancement level80 = Advancement.Builder.advancement()
                    .parent(level70)
                    .display(
                            ModItems.QIANWANXINGCHEN_DAN.get(),
                            Component.translatable("adv.kunlun.lvl80.title"),
                            Component.translatable("adv.kunlun.lvl80.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(80))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_80"), existingFileHelper);

            Advancement level90 = Advancement.Builder.advancement()
                    .parent(level80)
                    .display(
                            ModItems.YIZAICHUANGSHENG_DAN.get(),
                            Component.translatable("adv.kunlun.lvl90.title"),
                            Component.translatable("adv.kunlun.lvl90.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(90))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_90"), existingFileHelper);

            Advancement level99 = Advancement.Builder.advancement()
                    .parent(level90)
                    .display(
                            ModItems.YIZAICHUANGSHENG_DAN.get(),
                            Component.translatable("adv.kunlun.lvl99.title"),
                            Component.translatable("adv.kunlun.lvl99.desc"),
                            null,
                            FrameType.CHALLENGE, true, true, false
                    )
                    .addCriterion("reached_lvl", LevelTrigger.Instance.levelReached(99))
                    .save(saver, new ResourceLocation(KlMain.MOD_ID, "main/level_99"), existingFileHelper);
        }
    }
}