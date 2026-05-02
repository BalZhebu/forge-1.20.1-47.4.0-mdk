package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

//设置玩家初始属性
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerAttributeInit {

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {
                    boolean isNotInitialized = !attributes.isInitialized();
                    boolean isBrandNew = attributes.getGongji() == 1.0f &&
                            attributes.getMaxshengming() == 20.0f &&
                            attributes.getDengji() == 0;
                    if (isNotInitialized && isBrandNew) {
                        attributes.setShengming(20.0f);
                        attributes.setMaxshengming(20.0f);
                        attributes.setJingshenli(20.0f);
                        attributes.setMaxjingshenli(20.0f);
                        attributes.setMingzhong(1.0f);
                        attributes.setFangyu(1.0f);
                        attributes.setGongji(1.0f);
                        attributes.setBaojilv(5.0f);
                        attributes.setBaojishanghai(150.0f);
                        attributes.setXixue(1.0f);
                        attributes.setShanbi(1.0f);
                        attributes.setKangbao(1.0f);
                        attributes.setJingyan(0.0f);
                        attributes.setDengji(0);
                        attributes.setMaxjingyan(20.0f);
                        attributes.setShengmingHuifu(1.0f);
                        attributes.setWuchuan(1.0f);
                        attributes.setXiulianTime(600);
                        ItemStack book = new ItemStack(ModItems.GUIDE_BOOK.get());
                        if (!serverPlayer.getInventory().add(book)) {
                            serverPlayer.drop(book, false);
                        }
                        attributes.setInitialized(true);
                    } else {
                        if (isNotInitialized) {
                            attributes.setInitialized(true);
                        }
                    }
                    syncMaxHealthToPlayer(player, attributes.getMaxshengming());
                    syncAllAttributesToClient(serverPlayer, attributes);
                });
            }
        }
    }

    public static void syncMaxHealthToPlayer(Player player, float targetMaxHealth) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;
        double currentMaxHealth = maxHealthAttr.getBaseValue();
        maxHealthAttr.setBaseValue(targetMaxHealth);
    }

    private static void syncAllAttributesToClient(ServerPlayer player, PlayerAttributeCapability attr) {
        CompoundTag nbtData = attr.serializeNBT();
        com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute packet =
                new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(player.getId(), nbtData);
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
        );
    }

}