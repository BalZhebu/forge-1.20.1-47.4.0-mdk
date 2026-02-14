package com.TovidY.kunluncontinent.block;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.block.klblock.LiandanluBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KlMain.MOD_ID);

    // 注册炼丹炉
    public static final RegistryObject<BlockEntityType<LiandanluBlockEntity>> LIANDANLU_BE =
            BLOCK_ENTITIES.register("liandanlu_be", () ->
                    BlockEntityType.Builder.of((pos, state) -> {
                                // 自动根据 Block 的 tier 来创建对应的 Entity
                                int tier = 1;
                                if (state.getBlock() instanceof LiandanluBlock ldb) {
                                    tier = ldb.getTier();
                                }
                                return new LiandanluBlockEntity(pos, state, tier);
                            },
                            ModBlocks.LIANDANLU1.get(),ModBlocks.LIANDANLU2.get(), ModBlocks.LIANDANLU3.get() // 绑定所有炉子方块
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
