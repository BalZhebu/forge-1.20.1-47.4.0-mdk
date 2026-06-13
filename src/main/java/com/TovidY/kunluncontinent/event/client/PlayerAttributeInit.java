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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;


//设置玩家初始属性
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerAttributeInit {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {
            if (!attributes.isInitialized()) {
                if (attributes.isBrandNewConfig()) {
                    attributes.initDefaultAttributes();
                    ItemStack book = new ItemStack(ModItems.GUIDE_BOOK.get());
                    if (!player.getInventory().add(book)) {
                        player.drop(book, false);
                    }
                }
                attributes.setInitialized(true);
            }

            syncMaxHealthToPlayer(player, attributes.getMaxshengming());
            syncAllAttributesToClient(player, attributes);
        });
    }

    public static void syncMaxHealthToPlayer(Player player, float targetMaxHealth) {
        if (player == null) return;
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null && (float) maxHealthAttr.getBaseValue() != targetMaxHealth) {
            maxHealthAttr.setBaseValue(targetMaxHealth);
        }
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