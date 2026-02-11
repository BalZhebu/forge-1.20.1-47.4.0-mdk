package com.TovidY.kunluncontinent.datagen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
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
    }
}
