package com.TovidY.kunluncontinent.block;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.klblock.PutuanBlock;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.function.Supplier;

//方块注册类
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KlMain.MOD_ID);

    public static final RegistryObject<Block> CULTIVATION_PLATFORM =
            registerBlock("cultivation_platform",()->
                    new Block(BlockBehaviour.Properties.of().strength(1.5f,3.0f).noOcclusion()));

    public static final RegistryObject<Block> GRAY_IRON_ORE =
            registerBlock("gray_iron_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)));

    public static final RegistryObject<Block> CLOUD_PATTERNED_BRONZE_ORE =
            registerBlock("cloud_patterned_bronze_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)));

    public static final RegistryObject<Block> PUTUAN_BLOCK =
            registerBlock("putuan_block",()->
                    new PutuanBlock(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> LIANDANLU1 =
            registerBlock("liandanlu1",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion()));

    public static final RegistryObject<Block> LIANDANLU2 =
            registerBlock("liandanlu2",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion()));

    public static final RegistryObject<Block> LIANDANLU3 =
            registerBlock("liandanlu3",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion()));


    //炼丹炉
    public static ArrayList<RegistryObject<Block>> LIANDANLULIST = new ArrayList<>();
    static{
        LIANDANLULIST.add(LIANDANLU1);
        LIANDANLULIST.add(LIANDANLU2);
        LIANDANLULIST.add(LIANDANLU3);
    }

    //mod矿物
    public static ArrayList<RegistryObject<Block>> MODORE = new ArrayList<>();
    static {
        MODORE.add(GRAY_IRON_ORE);
        MODORE.add(CLOUD_PATTERNED_BRONZE_ORE);
    }

    //mod方块
    public static ArrayList<RegistryObject<Block>> MODBLOCKS = new ArrayList<>();
    static {
        MODBLOCKS.add(CULTIVATION_PLATFORM);
        MODBLOCKS.add(PUTUAN_BLOCK);
    }

    private static <T extends Block> void registerBlockItems(String name,RegistryObject<T> block){
        ModItems.ITEMS.register(name,()->new BlockItem(block.get(),new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T>block){
        RegistryObject<T> blocks = BLOCKS.register(name,block);
        registerBlockItems(name,blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

}
