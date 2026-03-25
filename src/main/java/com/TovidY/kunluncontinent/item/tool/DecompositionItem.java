package com.TovidY.kunluncontinent.item.tool;

import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DecompositionItem extends Item {
    private final int decompositionLevel; // 分解等级（1~5）
    private static final Map<Integer, Integer> DECOMPOSITION_RANGE = Map.of(
            1, 3,  // 等级1: 3格
            2, 5,  // 等级2: 5格
            3, 8,  // 等级3: 8格
            4, 15, // 等级4: 15格
            5, 20  // 等级5: 20格
    );

    private static final Map<UUID, Long> firstClickTimeMap = new HashMap<>();
    private static final long CLICK_INTERVAL = 1000;

    public DecompositionItem(Properties properties, int decompositionLevel) {
        super(properties);
        this.decompositionLevel = Math.min(5, Math.max(1, decompositionLevel));
    }

    public int getDecompositionRange() {
        return DECOMPOSITION_RANGE.getOrDefault(decompositionLevel, 3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.getAbilities().mayBuild) {
            CompoundTag tag = stack.getOrCreateTag();
            boolean isActive = tag.getBoolean("decomposition_active");
            if (!isActive) {
                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();
                if (!firstClickTimeMap.containsKey(playerId)) {
                    firstClickTimeMap.put(playerId, currentTime);
                    player.sendSystemMessage(Component.literal("再次右键以确认开启自动分解（防误触）")
                            .withStyle(ChatFormatting.YELLOW));
                    return InteractionResultHolder.success(stack);
                }
                long firstClickTime = firstClickTimeMap.get(playerId);
                if (currentTime - firstClickTime <= CLICK_INTERVAL) {
                    tag.putBoolean("decomposition_active", true);
                    player.sendSystemMessage(Component.translatable("自动分解已开启 (半径: %s格)", getDecompositionRange())
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    player.sendSystemMessage(Component.literal("操作超时，请重新右键两次开启")
                            .withStyle(ChatFormatting.RED));
                }
                firstClickTimeMap.remove(playerId);
            } else {
                tag.putBoolean("decomposition_active", false);
                player.sendSystemMessage(Component.literal("自动分解已关闭")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @SubscribeEvent
    public static void onPlayerChangeItem(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (!(mainHand.getItem() instanceof DecompositionItem) &&
                !(offHand.getItem() instanceof DecompositionItem)) {
            firstClickTimeMap.remove(player.getUUID());
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        for (ItemStack stack : new ItemStack[]{mainHand, offHand}) {
            if (stack.getItem() instanceof DecompositionItem decomposer) {
                CompoundTag tag = stack.getOrCreateTag();
                if (!tag.getBoolean("decomposition_active")) continue;
                if (player.level().getGameTime() % 10 != 0) continue;
                int range = decomposer.getDecompositionRange();
                AABB area = new AABB(
                        player.getX() - range, player.getY() - 5, player.getZ() - range,
                        player.getX() + range, player.getY() + 5, player.getZ() + range
                );
                List<HunhuanEntity> hunhuans = player.level().getEntitiesOfClass(
                        HunhuanEntity.class, area,
                        e -> e.isAlive() && !e.isVehicle()
                );
                hunhuans.forEach(hunhuan -> {
                    hunhuan.livetime += 1000;
                    player.level().playSound(
                            null, hunhuan.getX(), hunhuan.getY(), hunhuan.getZ(),
                            SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.7F, 1.0F
                    );
                });
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("分解半径: %s格", getDecompositionRange())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("副手可用")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("右键空气开启/关闭自动分解")
                .withStyle(ChatFormatting.WHITE));

    }
}