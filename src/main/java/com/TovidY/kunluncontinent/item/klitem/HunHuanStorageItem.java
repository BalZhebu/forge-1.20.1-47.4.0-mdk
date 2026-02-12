package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

//魂环收纳器
public class HunHuanStorageItem extends Item {
    private static final String STORED_HUNHUAN_TAG = "StoredHunhuan";
    private final int minYear;
    private final int maxYear;

    public HunHuanStorageItem(Properties pProperties, int minYear, int maxYear) {
        super(pProperties);
        this.minYear = minYear;
        this.maxYear = maxYear;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag stackTag = stack.getOrCreateTag();
            if (stackTag.contains(STORED_HUNHUAN_TAG)) {
                return releaseStoredHunhuan(level, player, stack);
            } else {
                return tryAbsorbHunhuan(level, player, stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private InteractionResultHolder<ItemStack> tryAbsorbHunhuan(Level level, Player player, ItemStack stack) {
        AABB searchArea = new AABB(player.position(), player.position()).inflate(5.0);
        List<Entity> entities = level.getEntities(player, searchArea);

        for (Entity entity : entities) {
            if (entity instanceof HunhuanEntity hunhuan) {
                Long nianxian = hunhuan.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                        .map(MobAttributeCapability::getNianxian)
                        .orElse(0L);

                if (nianxian < minYear || nianxian > maxYear) {
                    player.sendSystemMessage(Component.literal("该收纳器的等级无法承受魂环的能量")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(stack);
                }

                CompoundTag hunhuanData = new CompoundTag();
                hunhuan.saveWithoutId(hunhuanData);
                CompoundTag stackTag = stack.getOrCreateTag();
                stackTag.put(STORED_HUNHUAN_TAG, hunhuanData);
                syncItemStackToClient(player, stack);
                hunhuan.discard();

                player.sendSystemMessage(Component.literal("成功收纳魂环！年限: ")
                        .append(Component.literal(String.format("%,d", nianxian)).withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GREEN));

                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private InteractionResultHolder<ItemStack> releaseStoredHunhuan(Level level, Player player, ItemStack stack) {
        CompoundTag stackTag = stack.getOrCreateTag();
        CompoundTag hunhuanData = stackTag.getCompound(STORED_HUNHUAN_TAG);
        EntityType<?> type = EntityType.by(hunhuanData).orElse(EntityInit.HUNHUAN.get());

        Entity entity = type.create(level);
        if (entity instanceof HunhuanEntity newHunhuan) {
            newHunhuan.load(hunhuanData);
            newHunhuan.setPos(player.position().add(0, 1, 0));

            if (level.addFreshEntity(newHunhuan)) {
                stackTag.remove(STORED_HUNHUAN_TAG);
                syncItemStackToClient(player, stack);
                int nianxian = getNianxianFromNBT(hunhuanData);
                player.sendSystemMessage(Component.literal("魂环已被释放！年限: ")
                        .append(Component.literal(String.format("%,d", nianxian)).withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GREEN));
                return InteractionResultHolder.success(stack);
            } else {
                player.sendSystemMessage(Component.literal("释放魂环失败！请联系腐竹！").withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    private int getNianxianFromNBT(CompoundTag nbt) {
        if (nbt.contains("ForgeCaps")) {
            CompoundTag forgeCaps = nbt.getCompound("ForgeCaps");
            if (forgeCaps.contains("kunluncontinent:mob_attribute")) {
                CompoundTag monsterAttr = forgeCaps.getCompound("kunluncontinent:mob_attribute");
                if (monsterAttr.contains("nianxian")) {
                    return monsterAttr.getInt("nianxian");
                }
            }
        }
        return 0;
    }

    private void syncItemStackToClient(Player player, ItemStack stack) {
        player.getInventory().setChanged();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                    -2, 0, player.getInventory().selected, stack));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.literal("可收纳" + minYear + "~" + maxYear + "年魂环").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("右键魂环实体收纳，再次右键释放").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("每个收纳器只能存储一个魂环").withStyle(ChatFormatting.GRAY));

        CompoundTag stackTag = stack.getOrCreateTag();
        if (stackTag.contains(STORED_HUNHUAN_TAG)) {
            CompoundTag hunhuanData = stackTag.getCompound(STORED_HUNHUAN_TAG);
            int nianxian = getNianxianFromNBT(hunhuanData);
            tooltip.add(Component.literal("已收纳魂环年限: " + nianxian + "年").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("当前没有收纳魂环").withStyle(ChatFormatting.RED));
        }
    }
}
