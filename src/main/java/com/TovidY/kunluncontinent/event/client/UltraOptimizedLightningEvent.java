package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.TovidY.kunluncontinent.worldgen.ModDimensions.POLAR_ICE_REALM_TYPE;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UltraOptimizedLightningEvent {

    private static final int CHANCE_DENOMINATOR = 200;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || event.level.getGameTime() % 20 != 0) return;
        ServerLevel level = (ServerLevel) event.level;
        if (!level.dimensionTypeId().equals(POLAR_ICE_REALM_TYPE)) return;
        if (level.random.nextInt(CHANCE_DENOMINATOR) != 0) return;
        for (ServerPlayer player : level.players()) {
            BlockPos pPos = player.blockPosition();
            AABB searchArea = new AABB(pPos).inflate(20);
            List<ItemEntity> diamonds = level.getEntitiesOfClass(ItemEntity.class, searchArea,
                    item -> item.getItem().is(ModItems.RINSEI_INGOT.get()) && item.getY() > 210
            );
            if (diamonds.isEmpty()) continue;
            for (ItemEntity diamond : diamonds) {
                BlockPos itemPos = diamond.blockPosition();
                Iterable<BlockPos> nearbyBlocks = BlockPos.betweenClosed(itemPos.offset(-2, -2, -2), itemPos.offset(2, 1, 2));
                for (BlockPos targetPos : nearbyBlocks) {
                    if (level.getBlockState(targetPos).is(Blocks.LIGHTNING_ROD)) {
                        triggerLightning(level, targetPos, diamond);
                        return;
                    }
                }
            }
        }
    }

    private static void triggerLightning(ServerLevel level, BlockPos pos, ItemEntity diamond) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            lightning.setVisualOnly(false);
            level.addFreshEntity(lightning);
            ItemStack stack = diamond.getItem();
            ItemEntity star = new ItemEntity(level, diamond.getX(), diamond.getY(), diamond.getZ(),
                    new ItemStack(ModItems.LIGHTNING_FRAGMENTS.get(), stack.getCount()));

            star.setGlowingTag(true);
            star.setInvulnerable(true);
            star.setDeltaMovement(0, 0.2, 0);

            level.addFreshEntity(star);
            diamond.discard();

            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0F, 1.0F);
        }
    }
}
