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
                                int tier = 1;
                                if (state.getBlock() instanceof LiandanluBlock ldb) {
                                    tier = ldb.getTier();
                                }
                                return new LiandanluBlockEntity(pos, state, tier);
                            },
                            ModBlocks.LIANDANLU1.get(),
                            ModBlocks.LIANDANLU2.get(),
                            ModBlocks.LIANDANLU3.get(),
                            ModBlocks.LIANDANLU4.get(), // 补上 4 阶
                            ModBlocks.LIANDANLU5.get(), // 补上 5 阶
                            ModBlocks.LIANDANLU6.get(), // 补上 6 阶
                            ModBlocks.LIANDANLU7.get(), // 补上 7 阶
                            ModBlocks.LIANDANLU8.get(), // 补上 8 阶
                            ModBlocks.LIANDANLU9.get()  // 补上 9 阶
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
