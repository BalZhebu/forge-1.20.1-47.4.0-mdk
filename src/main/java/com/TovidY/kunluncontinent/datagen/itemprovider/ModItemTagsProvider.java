package com.TovidY.kunluncontinent.datagen.itemprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, KlMain.MOD_ID,existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ModTags.Items.DANYAO_DROSS)
                .add(ModItems.CHUYUAN_DAN.get())
                .add(ModItems.BAICAOLING_DAN.get())
                .add(ModItems.QIANHUABAO_DAN.get());
        this.tag(ModTags.Items.ENGRAVING_KNIFE)
                .add(ModItems.IRON_ENGRAVING_KNIFE.get())
                .add(ModItems.DIAMOND_ENGRAVING_KNIFE.get());
    }
}
