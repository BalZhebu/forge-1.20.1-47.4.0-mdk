package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
//方块模型生成例如blockstates文件之类的，3D模型不需要写这里！
//3D模型需要手动写代码！
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output,KlMain.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.GRAY_IRON_ORE.get(), cubeAll(ModBlocks.GRAY_IRON_ORE.get()));
        simpleBlockWithItem(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(), cubeAll(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get()));



        // 自定义 3D 模型方块
        // 我们只生成 blockstate
        Block liandanlu = ModBlocks.LIANDANLU1.get();
        // 生成 blockstate
        simpleBlock(liandanlu, new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu1")));
        //生成item
        itemModels().withExistingParent("liandanlu1",
                ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "block/liandanlu1"));


    }
}
