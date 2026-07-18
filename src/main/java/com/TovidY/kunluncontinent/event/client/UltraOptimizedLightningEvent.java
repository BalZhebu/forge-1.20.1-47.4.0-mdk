package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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

    // ---- 触发概率与判定参数集中管理 ----
    private static final int CHANCE_DENOMINATOR = 200;   // 每 20 tick 有 1/200 概率触发判定
    private static final int SEARCH_RADIUS = 20;         // 以玩家为中心的物品搜索半径
    private static final int MIN_TRIGGER_HEIGHT = 210;   // 只有高于此 Y 坐标的临星铁锭才会被雷劈中

    // 避雷针搜索范围（以掉落物为中心的偏移量）
    private static final Vec3i LIGHTNING_ROD_SEARCH_MIN = new Vec3i(-2, -2, -2);
    private static final Vec3i LIGHTNING_ROD_SEARCH_MAX = new Vec3i(2, 1, 2);

    private static final float LIGHTNING_SOUND_VOLUME = 2.0F;
    private static final float LIGHTNING_SOUND_PITCH = 1.0F;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.level.isClientSide
                || event.level.getGameTime() % 20 != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) event.level;
        if (!level.dimensionTypeId().equals(POLAR_ICE_REALM_TYPE)) return;
        if (level.random.nextInt(CHANCE_DENOMINATOR) != 0) return;

        for (ServerPlayer player : level.players()) {
            if (tryStrikeNearPlayer(level, player)) {
                // 保留原逻辑：一次判定全局只触发一次雷击
                return;
            }
        }
    }

    /**
     * 尝试在指定玩家周围寻找“临星铁锭掉落物 + 附近避雷针”的组合并触发雷击。
     * @return 是否成功触发了一次雷击
     */
    private static boolean tryStrikeNearPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        AABB searchArea = new AABB(playerPos).inflate(SEARCH_RADIUS);

        List<ItemEntity> targetIngots = level.getEntitiesOfClass(ItemEntity.class, searchArea,
                item -> item.getItem().is(ModItems.RINSEI_INGOT.get()) && item.getY() > MIN_TRIGGER_HEIGHT
        );
        if (targetIngots.isEmpty()) return false;

        for (ItemEntity ingot : targetIngots) {
            BlockPos ingotPos = ingot.blockPosition();
            Iterable<BlockPos> lightningRodSearchArea = BlockPos.betweenClosed(
                    ingotPos.offset(LIGHTNING_ROD_SEARCH_MIN.getX(), LIGHTNING_ROD_SEARCH_MIN.getY(), LIGHTNING_ROD_SEARCH_MIN.getZ()),
                    ingotPos.offset(LIGHTNING_ROD_SEARCH_MAX.getX(), LIGHTNING_ROD_SEARCH_MAX.getY(), LIGHTNING_ROD_SEARCH_MAX.getZ())
            );

            for (BlockPos rodPos : lightningRodSearchArea) {
                if (level.getBlockState(rodPos).is(Blocks.LIGHTNING_ROD)) {
                    triggerLightning(level, rodPos, ingot);
                    return true;
                }
            }
        }
        return false;
    }

    private static void triggerLightning(ServerLevel level, BlockPos pos, ItemEntity ingot) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) return;

        lightning.moveTo(Vec3.atBottomCenterOf(pos));
        lightning.setVisualOnly(false);
        level.addFreshEntity(lightning);

        ItemStack ingotStack = ingot.getItem();
        ItemEntity fragment = new ItemEntity(level, ingot.getX(), ingot.getY(), ingot.getZ(),
                new ItemStack(ModItems.LIGHTNING_FRAGMENTS.get(), ingotStack.getCount()));
        fragment.setGlowingTag(true);
        fragment.setInvulnerable(true);
        fragment.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(fragment);

        ingot.discard();

        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,
                LIGHTNING_SOUND_VOLUME, LIGHTNING_SOUND_PITCH);
    }
}