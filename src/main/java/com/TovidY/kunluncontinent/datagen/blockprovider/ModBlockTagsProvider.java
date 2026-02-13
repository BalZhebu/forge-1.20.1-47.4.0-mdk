package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, KlMain.MOD_ID, existingFileHelper);
    }
    //该方法意思是将指定的矿物可以被什么工具所破坏
    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

        //锄子
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.PUTUAN_BLOCK.get());

        //镐子
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.GRAY_IRON_ORE.get())
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get())
                .add(ModBlocks.LIANDANLU1.get())
                .add(ModBlocks.LIANDANLU2.get())
                .add(ModBlocks.LIANDANLU3.get())
        ;
        //需要铁镐破坏
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.GRAY_IRON_ORE.get())
        ;
        //需要钻石镐
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get())
                .add(ModBlocks.LIANDANLU1.get())
                .add(ModBlocks.LIANDANLU2.get())
                .add(ModBlocks.LIANDANLU3.get())
        ;
    }
}
