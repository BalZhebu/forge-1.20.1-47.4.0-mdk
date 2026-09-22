package com.TovidY.kunluncontinent.block;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.klblock.*;
import com.TovidY.kunluncontinent.block.portal.polarice.PolarIcePortalBlock;
import com.TovidY.kunluncontinent.block.portal.polarice.ThunderRealmPortalBlock;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.tower.block.StoneStampBlock;
import com.TovidY.kunluncontinent.tower.block.SummonTowerBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

//方块注册类
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KlMain.MOD_ID);

    public static final RegistryObject<Block> CULTIVATION_PLATFORM =
            registerBlock("cultivation_platform",()->
                    new CultivationPlatformBlock(BlockBehaviour.Properties.of().strength(1.5f,3.0f).noOcclusion()));

    public static final RegistryObject<Block> GRAY_IRON_ORE =
            registerBlock("gray_iron_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.cultivation_platform.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> CLOUD_PATTERNED_BRONZE_ORE =
            registerBlock("cloud_patterned_bronze_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.cloud_patterned_bronze_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> RED_FIRE_ORE =
            registerBlock("red_fire_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.red_fire_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> SUNKEN_SILVER_ORE =
            registerBlock("sunken_silver_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.sunken_silver_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> COLD_HEARTED_STEEL_ORE =
            registerBlock("cold_hearterd_steel_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.cold_hearterd_steel_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> SPIRIT_GATHERING_ALTAR =
            registerBlock("spirit_gathering_altar",()->
                    new SpiritGatheringAltherBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE).noOcclusion()));

    public static final RegistryObject<Block> SPIRIT_GATHERING_STONE =
            registerBlock("spirit_gathering_stone",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE).noOcclusion()){
                protected static final VoxelShape SHAPE = Stream.of(Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D), Block.box(5.0D, 2.0D, 5.0D, 11.0D, 14.0D, 11.0D), Block.box(4.0D, 14.0D, 2.0D, 12.0D, 15.0D, 4.0D), Block.box(4.0D, 14.0D, 12.0D, 12.0D, 15.0D, 14.0D), Block.box(2.0D, 14.0D, 4.0D, 4.0D, 15.0D, 12.0D), Block.box(12.0D, 14.0D, 4.0D, 14.0D, 15.0D, 12.0D))
                        .reduce(Shapes::or).get();
                @Override public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
                {return SHAPE;}});

    public static final RegistryObject<Block> SPIRIT_GATHERING_STONE_0 =
            registerBlock("spirit_gathering_stone_0",()->
                    new SpiritGatheringStoneBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE).noOcclusion(),0));

    public static final RegistryObject<Block> SPIRIT_GATHERING_STONE_1 =
            registerBlock("spirit_gathering_stone_1",()->
                    new SpiritGatheringStoneBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE).noOcclusion(),1));

    public static final RegistryObject<Block> SPIRIT_GATHERING_STONE_2 =
            registerBlock("spirit_gathering_stone_2",()->
                    new SpiritGatheringStoneBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE).noOcclusion(),2));

    public static final RegistryObject<Block> RUBY_ORE =
            registerBlock("ruby_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
                            pTooltip.add(Component.translatable("tooltip.ruby_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> AMETHYST_ORE =
            registerBlock("amethyst_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
                            pTooltip.add(Component.translatable("tooltip.amethyst_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> SAPPHIRE_ORE =
            registerBlock("sapphire_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
                            pTooltip.add(Component.translatable("tooltip.sapphire_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> STARLIGHT_STONE_ORE =
            registerBlock("starlight_stone_ore",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
                            pTooltip.add(Component.translatable("tooltip.starlight_stone_ore.tooltip").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    //魂土
    public static final RegistryObject<Block> SOUL_SOIL = registerBlock("soul_soil", () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)){
        @Override
        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
            pTooltip.add(Component.translatable("tooltip.kunluncontinent.soul_soil").withStyle(ChatFormatting.DARK_GRAY));
        }
    });

    //草药
    public static final RegistryObject<Block> RED_SPIDER_LILY_BLOCK = BLOCKS.register("red_spider_lily_block", () -> new KLCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion(), () -> Blocks.NETHERRACK, ModItems.RED_SPIDER_SEEDS,10));
    public static final RegistryObject<Block> GUYUANCAO_BLOCK = BLOCKS.register("guyuancao_block", () -> new KLCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion(), ModBlocks.SOUL_SOIL, ModItems.GUYUANCAO_SEEDS,10));

    public static final RegistryObject<Block> FANQICAO_BLOCK = BLOCKS.register("fanqicao_block", () -> new KLCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion(), ModBlocks.SOUL_SOIL, ModItems.FANQICAO_SEEDS,10));

    public static final RegistryObject<Block> DROSS_BLOCK =
            registerBlock("dross_block",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.kunluncontinent.dross_block").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    public static final RegistryObject<Block> PUTUAN_BLOCK =
            registerBlock("putuan_block",()->
                    new PutuanBlock(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> LIANDANLU1 =
            registerBlock("liandanlu1",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),1));

    public static final RegistryObject<Block> LIANDANLU2 =
            registerBlock("liandanlu2",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),2));

    public static final RegistryObject<Block> LIANDANLU3 =
            registerBlock("liandanlu3",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),3));

    public static final RegistryObject<Block> LIANDANLU4 =
            registerBlock("liandanlu4",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),4));

    public static final RegistryObject<Block> LIANDANLU5 =
            registerBlock("liandanlu5",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),5));

    public static final RegistryObject<Block> LIANDANLU6 =
            registerBlock("liandanlu6",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),6));

    public static final RegistryObject<Block> LIANDANLU7 =
            registerBlock("liandanlu7",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),7));

    public static final RegistryObject<Block> LIANDANLU8 =
            registerBlock("liandanlu8",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),8));

    public static final RegistryObject<Block> LIANDANLU9 =
            registerBlock("liandanlu9",()->
                    new LiandanluBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion(),9));

    //传送门框架
    public static final RegistryObject<Block> POLAR_ICE_PORTAL_BLOCK =
            registerBlock("polar_ice_portal_block",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.kunluncontinent.polar_ice_portal_block").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });
    public static final RegistryObject<Block> THUNDER_REALM_PORTAL_BLOCK =
            registerBlock("thunder_realm_portal_block",()->
                    new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)){
                        @Override
                        public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                            pTooltip.add(Component.translatable("tooltip.kunluncontinent.polar_ice_portal_block").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });

    //海底祭坛
    public static final RegistryObject<Block> UNDERWATER_ALTAR = registerBlock("underwater_alta", UnderwaterAltarBlock::new);

    //传送门方块
    public static final RegistryObject<Block> POLAR_ICE_PORTAL =
            registerBlock("polar_ice_portal", PolarIcePortalBlock::new);
    public static final RegistryObject<Block> THUNDER_REALM_PORTAL =
            registerBlock("thunder_realm_portal", ThunderRealmPortalBlock::new);

    //石碑
    public static final RegistryObject<Block> STONE_STAMP =
            registerBlock("stone_stamp", () ->
                    new StoneStampBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));

    //召唤塔
    public static final RegistryObject<Block> SUMMON_TOWER = registerBlock("summon_tower", () ->
            new SummonTowerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK).noOcclusion().noLootTable()));

    //灰铁块
    public static final RegistryObject<Block> GRAY_BLOCK = registerBlock("gray_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.COAL_BLOCK)));

    //云纹铜块
    public static final RegistryObject<Block> CLOUD_PATTERNED_BRONZE_BLOCK = registerBlock("cloud_patterned_bronze_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    //赤火块
    public static final RegistryObject<Block> RED_FIRE_BLOCK = registerBlock("red_fire_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    //沉银块
    public static final RegistryObject<Block> SUNKEN_SILVER_BLOCK = registerBlock("sunken_silver_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)));

    //寒心钢块
    public static final RegistryObject<Block> COLD_HEARTED_STEEL_BLOCK = registerBlock("cold_hearterd_steel_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    //凛晶块
    public static final RegistryObject<Block> RINSEI_BLOCK = registerBlock("rinsei_block", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    //块
    public static ArrayList<RegistryObject<Block>> BLOCKLIST = new ArrayList<>();
    static{
        BLOCKLIST.add(GRAY_BLOCK);
        BLOCKLIST.add(CLOUD_PATTERNED_BRONZE_BLOCK);
        BLOCKLIST.add(RED_FIRE_BLOCK);
        BLOCKLIST.add(SUNKEN_SILVER_BLOCK);
        BLOCKLIST.add(COLD_HEARTED_STEEL_BLOCK);
        BLOCKLIST.add(RINSEI_BLOCK);
    }

    //炼丹炉
    public static ArrayList<RegistryObject<Block>> LIANDANLULIST = new ArrayList<>();
    static{
        LIANDANLULIST.add(LIANDANLU1);
        LIANDANLULIST.add(LIANDANLU2);
        LIANDANLULIST.add(LIANDANLU3);
        LIANDANLULIST.add(LIANDANLU4);
        LIANDANLULIST.add(LIANDANLU5);
        LIANDANLULIST.add(LIANDANLU6);
        LIANDANLULIST.add(LIANDANLU7);
        LIANDANLULIST.add(LIANDANLU8);
        LIANDANLULIST.add(LIANDANLU9);
    }

    //mod矿物
    public static ArrayList<RegistryObject<Block>> MODORE = new ArrayList<>();
    static {
        MODORE.add(GRAY_IRON_ORE);
        MODORE.add(CLOUD_PATTERNED_BRONZE_ORE);
        MODORE.add(RED_FIRE_ORE);
        MODORE.add(SUNKEN_SILVER_ORE);
        MODORE.add(COLD_HEARTED_STEEL_ORE);

        MODORE.add(RUBY_ORE);
        MODORE.add(AMETHYST_ORE);
        MODORE.add(SAPPHIRE_ORE);
        MODORE.add(STARLIGHT_STONE_ORE);
    }

    //mod方块
    public static ArrayList<RegistryObject<Block>> MODBLOCKS = new ArrayList<>();
    static {
        MODBLOCKS.add(CULTIVATION_PLATFORM);
        MODBLOCKS.add(PUTUAN_BLOCK);
        MODBLOCKS.add(SPIRIT_GATHERING_ALTAR);
        MODBLOCKS.add(SPIRIT_GATHERING_STONE);
        MODBLOCKS.add(SPIRIT_GATHERING_STONE_0);
        MODBLOCKS.add(SPIRIT_GATHERING_STONE_1);
        MODBLOCKS.add(SPIRIT_GATHERING_STONE_2);
        MODBLOCKS.add(DROSS_BLOCK);
        MODBLOCKS.add(SOUL_SOIL);
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
