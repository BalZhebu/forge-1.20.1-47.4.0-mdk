package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.portal.polarice.PolarIcePortalBlock;
import com.TovidY.kunluncontinent.block.portal.polarice.ThunderRealmPortalBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
//方块模型生成例如blockstates文件之类的
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output,KlMain.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //矿石方块
        simpleBlockWithItem(ModBlocks.GRAY_IRON_ORE.get(), cubeAll(ModBlocks.GRAY_IRON_ORE.get()));
        simpleBlockWithItem(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(), cubeAll(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get()));
        simpleBlockWithItem(ModBlocks.RED_FIRE_ORE.get(), cubeAll(ModBlocks.RED_FIRE_ORE.get()));
        simpleBlockWithItem(ModBlocks.SUNKEN_SILVER_ORE.get(), cubeAll(ModBlocks.SUNKEN_SILVER_ORE.get()));
        simpleBlockWithItem(ModBlocks.COLD_HEARTED_STEEL_ORE.get(), cubeAll(ModBlocks.COLD_HEARTED_STEEL_ORE.get()));
        //矿石块
        simpleBlockWithItem(ModBlocks.GRAY_BLOCK.get(), cubeAll(ModBlocks.GRAY_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get(), cubeAll(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.RED_FIRE_BLOCK.get(), cubeAll(ModBlocks.RED_FIRE_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.SUNKEN_SILVER_BLOCK.get(), cubeAll(ModBlocks.SUNKEN_SILVER_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get(), cubeAll(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.RINSEI_BLOCK.get(), cubeAll(ModBlocks.RINSEI_BLOCK.get()));
        //宝石矿
        simpleBlockWithItem(ModBlocks.RUBY_ORE.get(), cubeAll(ModBlocks.RUBY_ORE.get()));
        simpleBlockWithItem(ModBlocks.AMETHYST_ORE.get(), cubeAll(ModBlocks.AMETHYST_ORE.get()));
        simpleBlockWithItem(ModBlocks.SAPPHIRE_ORE.get(), cubeAll(ModBlocks.SAPPHIRE_ORE.get()));
        simpleBlockWithItem(ModBlocks.STARLIGHT_STONE_ORE.get(), cubeAll(ModBlocks.STARLIGHT_STONE_ORE.get()));
        //丹渣块
        simpleBlockWithItem(ModBlocks.DROSS_BLOCK.get(), cubeAll(ModBlocks.DROSS_BLOCK.get()));
        //传送门方块
        simpleBlockWithItem(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get(), cubeAll(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get(), cubeAll(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get()));
        //魂土
        simpleBlockWithItem(ModBlocks.SOUL_SOIL.get(), cubeAll(ModBlocks.SOUL_SOIL.get()));
        //草药
        makeCrop((CropBlock) ModBlocks.RED_SPIDER_LILY_BLOCK.get(), "red_spider_lily");
        makeCrop((CropBlock) ModBlocks.GUYUANCAO_BLOCK.get(), "guyuancao");
        makeCrop((CropBlock) ModBlocks.FANQICAO_BLOCK.get(), "fanqicao");

        ModelFile logModel = models().withExistingParent("divine_realm_log", "block/cube_column")
                .texture("end", modLoc("block/divine_realm_log_top"))
                .texture("side", modLoc("block/divine_realm_log"));
        simpleBlockWithItem(ModBlocks.DIVINE_REALM_LOG.get(), logModel);
        simpleBlockWithItem(ModBlocks.DIVINE_REALM_PLANKS.get(), cubeAll(ModBlocks.DIVINE_REALM_PLANKS.get()));
        // ⭐ 树叶：改成原版 LeavesBlock 后**多了 3 个属性**
        //    （distance **1~7** / persistent / waterlogged），共 7×2×2 = 28 种状态。
        //    simpleBlockWithItem 只能覆盖默认状态 → 枯萎时 distance 变化会丢模型变紫黑，
        //    所以必须逐个 partialState 注册。全部指向同一个模型。
        //    renderType("cutout") 不能少：贴图带 alpha，漏了镂空处会变黑方块。
        ModelFile leavesModel = models().cubeAll("divine_realm_leaves",
                modLoc("block/divine_realm_leaves")).renderType("cutout");
        var leavesBuilder = getVariantBuilder(ModBlocks.DIVINE_REALM_LEAVES.get());
        for (int dist = 1; dist <= 7; dist++) {
            for (boolean persistent : new boolean[]{false, true}) {
                for (boolean waterlogged : new boolean[]{false, true}) {
                    leavesBuilder.partialState()
                            .with(BlockStateProperties.DISTANCE, dist)
                            .with(BlockStateProperties.PERSISTENT, persistent)
                            .with(BlockStateProperties.WATERLOGGED, waterlogged)
                            .modelForState().modelFile(leavesModel).addModel();
                }
            }
        }

        ModelFile saplingModel = models().cross("divine_realm_sapling",
                modLoc("block/divine_realm_sapling")).renderType("cutout");
        getVariantBuilder(ModBlocks.DIVINE_REALM_SAPLING.get())
                .partialState().with(BlockStateProperties.STAGE, 0)
                .modelForState().modelFile(saplingModel).addModel()
                .partialState().with(BlockStateProperties.STAGE, 1)
                .modelForState().modelFile(saplingModel).addModel();

        //传送门
        Block portalBlock = ModBlocks.POLAR_ICE_PORTAL.get();
        ModelFile portalModel = models().withExistingParent("polar_ice_portal", "block/block")
                .element()
                .from(0f, 0f, 6.01f)
                .to(16f, 16f, 9.99f)
                .face(Direction.NORTH).texture("#portal").end()
                .face(Direction.SOUTH).texture("#portal").end()
                .end()
                .texture("portal", modLoc("block/polar_ice_portal"))
                .texture("particle", modLoc("block/polar_ice_portal"));
        getVariantBuilder(portalBlock).forAllStates(state -> {
            Direction.Axis axis = state.getValue(PolarIcePortalBlock.AXIS);
            return ConfiguredModel.builder()
                    .modelFile(portalModel)
                    .rotationY(axis == Direction.Axis.X ? 0 : 90) // Z轴时旋转90度
                    .build();
        });

        Block thunderportalBlock = ModBlocks.THUNDER_REALM_PORTAL.get();
        ModelFile thunderportalModel = models().withExistingParent("thunder_realm_portal", "block/block")
                .element()
                .from(0f, 0f, 6.01f)
                .to(16f, 16f, 9.99f)
                .face(Direction.NORTH).texture("#portal").end()
                .face(Direction.SOUTH).texture("#portal").end()
                .end()
                .texture("portal", modLoc("block/thunder_realm_portal"))
                .texture("particle", modLoc("block/thunder_realm_portal"));
        getVariantBuilder(thunderportalBlock).forAllStates(state -> {
            Direction.Axis axis = state.getValue(ThunderRealmPortalBlock.AXIS);
            return ConfiguredModel.builder()
                    .modelFile(thunderportalModel)
                    .rotationY(axis == Direction.Axis.X ? 0 : 90) // Z轴时旋转90度
                    .build();
        });
//
//        // 自定义 3D 模型方块
//        // 我们只生成 blockstate
//        Block liandanlu1 = ModBlocks.LIANDANLU1.get();
//        // 生成 blockstate
//        simpleBlock(liandanlu1, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu1")));
        //生成item
        itemModels().withExistingParent("liandanlu1",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu1"));
//
//        Block liandanlu2 = ModBlocks.LIANDANLU2.get();
//        simpleBlock(liandanlu2, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu2")));
        itemModels().withExistingParent("liandanlu2",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu2"));
//
//        Block liandanlu3 = ModBlocks.LIANDANLU3.get();
//        simpleBlock(liandanlu3, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu3")));
        itemModels().withExistingParent("liandanlu3",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu3"));
//
//        Block liandanlu4 = ModBlocks.LIANDANLU4.get();
//        simpleBlock(liandanlu4, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu4")));
        itemModels().withExistingParent("liandanlu4",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu4"));
//
//        Block liandanlu5 = ModBlocks.LIANDANLU5.get();
//        simpleBlock(liandanlu5, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu5")));
        itemModels().withExistingParent("liandanlu5",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu5"));
//
//        Block liandanlu6 = ModBlocks.LIANDANLU6.get();
//        simpleBlock(liandanlu6, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu6")));

        itemModels().withExistingParent("liandanlu6",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu6"));
//
//        Block liandanlu7 = ModBlocks.LIANDANLU7.get();
//        simpleBlock(liandanlu7, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu7")));

        itemModels().withExistingParent("liandanlu7",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu7"));
//
//        Block liandanlu8 = ModBlocks.LIANDANLU8.get();
//        simpleBlock(liandanlu8, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu8")));

        itemModels().withExistingParent("liandanlu8",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu8"));
//
//        Block liandanlu9 = ModBlocks.LIANDANLU9.get();
//        simpleBlock(liandanlu9, new ModelFile.UncheckedModelFile(
//                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu9")));

        itemModels().withExistingParent("liandanlu9",
                new ResourceLocation(KlMain.MOD_ID, "block/liandanlu9"));

        Block putuan = ModBlocks.PUTUAN_BLOCK.get();
        simpleBlock(putuan, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/putuan_block")));
        itemModels().withExistingParent("putuan_block",
                new ResourceLocation(KlMain.MOD_ID, "block/putuan_block"));

        Block stonestamp = ModBlocks.STONE_STAMP.get();
        horizontalBlock(stonestamp, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/stone_stamp")));
        itemModels().withExistingParent("stone_stamp",
                new ResourceLocation(KlMain.MOD_ID, "block/stone_stamp"));

        Block summonstone = ModBlocks.SUMMON_TOWER.get();
        simpleBlock(summonstone, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/summon_tower")));
        itemModels().withExistingParent("summon_tower",
                new ResourceLocation(KlMain.MOD_ID, "block/summon_tower"));

        Block spirit = ModBlocks.SPIRIT_GATHERING_ALTAR.get();
        simpleBlock(spirit, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_altar")));
        itemModels().withExistingParent("spirit_gathering_altar",
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_altar"));

        Block stone = ModBlocks.SPIRIT_GATHERING_STONE.get();
        simpleBlock(stone, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone")));
        itemModels().withExistingParent("spirit_gathering_stone",
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone"));

        Block stone1 = ModBlocks.SPIRIT_GATHERING_STONE_0.get();
        simpleBlock(stone1, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_0")));
        itemModels().withExistingParent("spirit_gathering_stone_0",
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_0"));

        Block stone2 = ModBlocks.SPIRIT_GATHERING_STONE_1.get();
        simpleBlock(stone2, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_1")));
        itemModels().withExistingParent("spirit_gathering_stone_1",
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_1"));

        Block stone3 = ModBlocks.SPIRIT_GATHERING_STONE_2.get();
        simpleBlock(stone3, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_2")));
        itemModels().withExistingParent("spirit_gathering_stone_2",
                new ResourceLocation(KlMain.MOD_ID, "block/spirit_gathering_stone_2"));

        Block underwater = ModBlocks.UNDERWATER_ALTAR.get();
        simpleBlock(underwater, new ModelFile.UncheckedModelFile(
                new ResourceLocation(KlMain.MOD_ID, "block/underwater_alta")));
        itemModels().withExistingParent("underwater_alta",
                new ResourceLocation(KlMain.MOD_ID, "block/underwater_alta"));

    }

    private void makeCrop(CropBlock block, String name) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (int age = 0; age <= 3; age++) {
            ModelFile model = models().cross(name + "_stage" + age,
                    modLoc("block/" + name + "_stage" + age)).renderType("cutout");

            builder.partialState().with(BlockStateProperties.AGE_3, age)
                    .modelForState().modelFile(model).addModel();
        }
    }
}
