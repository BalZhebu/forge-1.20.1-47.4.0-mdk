package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
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

        //铲子
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.SOUL_SOIL.get());

        //锄子
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.PUTUAN_BLOCK.get())
                .add(ModBlocks.DROSS_BLOCK.get());

        //镐子
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.GRAY_IRON_ORE.get())
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get())
                .add(ModBlocks.RED_FIRE_ORE.get())
                .add(ModBlocks.SUNKEN_SILVER_ORE.get())
                .add(ModBlocks.RUBY_ORE.get())
                .add(ModBlocks.AMETHYST_ORE.get())
                .add(ModBlocks.SAPPHIRE_ORE.get())
                .add(ModBlocks.STARLIGHT_STONE_ORE.get())
                .add(ModBlocks.COLD_HEARTED_STEEL_ORE.get())
                .add(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get())
                .add(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())
                .add(ModBlocks.LIANDANLU1.get())
                .add(ModBlocks.LIANDANLU2.get())
                .add(ModBlocks.LIANDANLU3.get())
                .add(ModBlocks.LIANDANLU4.get())
                .add(ModBlocks.LIANDANLU5.get())
                .add(ModBlocks.LIANDANLU6.get())
                .add(ModBlocks.LIANDANLU7.get())
                .add(ModBlocks.LIANDANLU8.get())
                .add(ModBlocks.LIANDANLU9.get())
                .add(ModBlocks.STONE_STAMP.get())
                //矿物块
                .add(ModBlocks.GRAY_BLOCK.get())
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get())
                .add(ModBlocks.RED_FIRE_BLOCK.get())
                .add(ModBlocks.SUNKEN_SILVER_BLOCK.get())
                .add(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get())
                .add(ModBlocks.RINSEI_BLOCK.get())

        ;
        //需要铁镐破坏
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.GRAY_IRON_ORE.get())
                .add(ModBlocks.RUBY_ORE.get())
                .add(ModBlocks.GRAY_BLOCK.get())
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get())
        ;
        //需要钻石镐
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get())
                .add(ModBlocks.STONE_STAMP.get())
                .add(ModBlocks.SAPPHIRE_ORE.get())
                .add(ModBlocks.RED_FIRE_ORE.get())
                .add(ModBlocks.LIANDANLU1.get())
                .add(ModBlocks.LIANDANLU2.get())
                .add(ModBlocks.LIANDANLU3.get())
                .add(ModBlocks.LIANDANLU4.get())
                .add(ModBlocks.LIANDANLU5.get())
                .add(ModBlocks.LIANDANLU6.get())
                .add(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())
                .add(ModBlocks.RED_FIRE_BLOCK.get())
                .add(ModBlocks.SUNKEN_SILVER_BLOCK.get())
                .add(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get())
                .add(ModBlocks.RINSEI_BLOCK.get())
        ;

        tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                .add(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get())
                .add(ModBlocks.AMETHYST_ORE.get())
                .add(ModBlocks.STARLIGHT_STONE_ORE.get())
                .add(ModBlocks.SUNKEN_SILVER_ORE.get())
                .add(ModBlocks.COLD_HEARTED_STEEL_ORE.get())
                .add(ModBlocks.LIANDANLU7.get())
                .add(ModBlocks.LIANDANLU8.get())
                .add(ModBlocks.LIANDANLU9.get())
        ;
    }
}
