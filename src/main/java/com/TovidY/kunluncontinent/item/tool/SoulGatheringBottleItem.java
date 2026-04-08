package com.TovidY.kunluncontinent.item.tool;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.entity.hunhe.HunheEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulGatheringBottleItem extends Item implements Nengliang {
    private final int absorbRangeLevel;
    private int maxnengliang = 1200;

    private static final Map<Integer, Integer> RANGE_MAP = Map.of(
            1, 5, 2, 8, 3, 10, 4, 15, 5, 20, 6, 13, 7, 15, 8, 20, 9, 25
    );
    private static final int HEIGHT_RANGE = 5;

    public SoulGatheringBottleItem(Properties properties, int absorbRangeLevel) {
        super(properties);
        this.absorbRangeLevel = Math.min(9, Math.max(1, absorbRangeLevel));
    }

    public int getAbsorbRange() {
        return RANGE_MAP.getOrDefault(absorbRangeLevel, 3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            boolean isAutoAbsorbing = !tag.getBoolean("auto_absorb");
            tag.putBoolean("auto_absorb", isAutoAbsorbing);

            player.displayClientMessage(Component.literal(
                    isAutoAbsorbing ? "§a自动吸收开启 (半径:" + getAbsorbRange() + ")" : "§c自动吸收关闭"
            ), true);

            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof FarmBlock && getNengliang(player, stack) >= 20) {
            if (!level.isClientSide) {
                setNengliang(player, stack, getNengliang(player, stack) - (level.random.nextInt(15) + 13));
                level.setBlock(pos, ModBlocks.SOUL_SOIL.get().defaultBlockState(), 11);
            }
            player.playSound(SoundEvents.HOE_TILL, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public void performAbsorb(Player player, ItemStack stack) {
        if (player.level().isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean("auto_absorb")) return;
        if (getNengliang(player, stack) >= maxnengliang) return;
        if (player.level().getGameTime() % 10 != 0) return;
        int range = getAbsorbRange();
        AABB area = player.getBoundingBox().inflate(range, HEIGHT_RANGE, range);
        List<HunheEntity> hunheEntities = player.level().getEntitiesOfClass(HunheEntity.class, area);
        if (!hunheEntities.isEmpty()) {
            hunheEntities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            HunheEntity nearest = hunheEntities.get(0);
            float val = nearest.getValue();
            setNengliang(player, stack, getNengliang(player, stack) + (int) val);
            nearest.discard();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.2F);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        checkAndAbsorb(player, player.getMainHandItem());
        checkAndAbsorb(player, player.getOffhandItem());
    }

    private static void checkAndAbsorb(Player player, ItemStack stack) {
        if (stack.getItem() instanceof SoulGatheringBottleItem bottle) {
            bottle.performAbsorb(player, stack);
        }
    }

    @Override
    public void setNengliang(Player player, ItemStack itemStack, int value) {
        CompoundTag tag = itemStack.getOrCreateTag();
        int finalVal = Math.max(0, Math.min(value, maxnengliang));
        tag.putInt("sh_nengliang", finalVal);

        if (player != null && finalVal >= maxnengliang && !player.level().isClientSide) {
            player.displayClientMessage(Component.literal("§6聚魂瓶能量已注满"), true);
        }
    }

    @Override
    public int getNengliang(Player player, ItemStack itemStack) {
        return itemStack.hasTag() ? itemStack.getTag().getInt("sh_nengliang") : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int current = getNengliang(null, stack);
        tooltip.add(Component.literal("能量存储: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(current + " / " + maxnengliang).withStyle(ChatFormatting.AQUA)));

        boolean isAutoOn = stack.getOrCreateTag().getBoolean("auto_absorb");
        tooltip.add(Component.literal("自动模式: " + (isAutoOn ? "§a开启" : "§c关闭")));
        tooltip.add(Component.literal("吸收范围: §6" + getAbsorbRange() + "格"));
        tooltip.add(Component.literal("§7[右键空气切换自动模式]"));
    }


    public SoulGatheringBottleItem setMaxnengliang(int maxnengliang) {
        this.maxnengliang = maxnengliang;
        return this;
    }
}