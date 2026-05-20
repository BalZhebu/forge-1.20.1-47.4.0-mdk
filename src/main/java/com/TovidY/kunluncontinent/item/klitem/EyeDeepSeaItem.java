package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.eyetrans.EyeTransformationEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.List;

public class EyeDeepSeaItem extends Item {
    public EyeDeepSeaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "underwater_ruins"));

            var registry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var structureHolder = registry.get(structureKey);

            if (structureHolder.isPresent()) {
                var foundResult = serverLevel.getChunkSource().getGenerator()
                        .findNearestMapStructure(serverLevel, HolderSet.direct(structureHolder.get()),
                                player.blockPosition(), 500, false);

                if (foundResult != null) {
                    BlockPos foundPos = foundResult.getFirst();

                    EyeTransformationEntity eyeEntity = new EyeTransformationEntity(level, player.getX(), player.getY(0.5D), player.getZ());
                    eyeEntity.signalTo(foundPos, itemstack);
                    level.addFreshEntity(eyeEntity);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F);

                    player.getCooldowns().addCooldown(this, 100);

                    player.sendSystemMessage(Component.literal("§b深海之眼已指向远古遗迹..."));

                    return InteractionResultHolder.success(itemstack);
                }
            }

            player.sendSystemMessage(Component.literal("§c深海之眼毫无反应，附近似乎没有遗迹的波动。"));
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.item.klitem.eyetf"));
    }
}