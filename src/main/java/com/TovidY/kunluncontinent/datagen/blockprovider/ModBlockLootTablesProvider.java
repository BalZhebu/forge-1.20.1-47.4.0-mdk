package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
//用于检查方块有没有写战利品列表，若排除方块则在方块后加入.noLootTable
public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.CULTIVATION_PLATFORM.get());
        dropSelf(ModBlocks.GRAY_IRON_ORE.get());
        dropSelf(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get());
        dropSelf(ModBlocks.PUTUAN_BLOCK.get());
        dropSelf(ModBlocks.DROSS_BLOCK.get());
        dropSelf(ModBlocks.LIANDANLU1.get());
        dropSelf(ModBlocks.LIANDANLU2.get());
        dropSelf(ModBlocks.LIANDANLU3.get());
    }

    //该方法是矿石类，挖矿石会掉落更多矿物，将原本的block方块替换成该方法即可
    //用于直接爆出锭
    protected LootTable.Builder createCopperOreLikeDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock, LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
