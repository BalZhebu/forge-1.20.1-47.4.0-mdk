package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
//方块模型生成例如blockstates文件之类的
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output,KlMain.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.GRAY_IRON_ORE.get(), cubeAll(ModBlocks.GRAY_IRON_ORE.get()));
        simpleBlockWithItem(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(), cubeAll(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get()));
        simpleBlockWithItem(ModBlocks.DROSS_BLOCK.get(), cubeAll(ModBlocks.DROSS_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.RED_FIRE_ORE.get(), cubeAll(ModBlocks.RED_FIRE_ORE.get()));
        simpleBlockWithItem(ModBlocks.SUNKEN_SILVER_ORE.get(), cubeAll(ModBlocks.SUNKEN_SILVER_ORE.get()));

        simpleBlockWithItem(ModBlocks.RUBY_ORE.get(), cubeAll(ModBlocks.RUBY_ORE.get()));
        simpleBlockWithItem(ModBlocks.AMETHYST_ORE.get(), cubeAll(ModBlocks.AMETHYST_ORE.get()));

        // 自定义 3D 模型方块
        // 我们只生成 blockstate
        Block liandanlu1 = ModBlocks.LIANDANLU1.get();
        // 生成 blockstate
        simpleBlock(liandanlu1, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu1")));
        //生成item
        itemModels().withExistingParent("liandanlu1",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu1"));

        Block liandanlu2 = ModBlocks.LIANDANLU2.get();
        simpleBlock(liandanlu2, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu2")));
        itemModels().withExistingParent("liandanlu2",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu2"));

        Block liandanlu3 = ModBlocks.LIANDANLU3.get();
        simpleBlock(liandanlu3, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu3")));
        itemModels().withExistingParent("liandanlu3",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu3"));

        Block liandanlu4 = ModBlocks.LIANDANLU4.get();
        simpleBlock(liandanlu4, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu4")));
        itemModels().withExistingParent("liandanlu4",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu4"));

        Block liandanlu5 = ModBlocks.LIANDANLU5.get();
        simpleBlock(liandanlu5, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu5")));
        itemModels().withExistingParent("liandanlu5",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu5"));

        Block liandanlu6 = ModBlocks.LIANDANLU6.get();
        simpleBlock(liandanlu6, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu6")));
        itemModels().withExistingParent("liandanlu6",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu6"));

        Block liandanlu7 = ModBlocks.LIANDANLU7.get();
        simpleBlock(liandanlu7, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu7")));
        itemModels().withExistingParent("liandanlu7",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu7"));

        Block liandanlu8 = ModBlocks.LIANDANLU8.get();
        simpleBlock(liandanlu8, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu8")));
        itemModels().withExistingParent("liandanlu8",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu8"));

        Block liandanlu9 = ModBlocks.LIANDANLU9.get();
        simpleBlock(liandanlu9, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu9")));
        itemModels().withExistingParent("liandanlu9",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu9"));

        Block putuan = ModBlocks.PUTUAN_BLOCK.get();
        simpleBlock(putuan, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/putuan_block")));
        itemModels().withExistingParent("putuan_block",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/putuan_block"));

    }
}
