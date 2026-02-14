package com.TovidY.kunluncontinent.block;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
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
                    BlockEntityType.Builder.of(LiandanluBlockEntity::new,
                            ModBlocks.LIANDANLU1.get(),
                            ModBlocks.LIANDANLU2.get(),
                            ModBlocks.LIANDANLU3.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
