package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

//设置玩家初始属性
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerAttributeInit {

    @SubscribeEvent
    public static void onPlayerLogin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide) return;
            
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {

                    boolean isRespawn = attributes.isInitialized() || 
                                       attributes.getGongji() != 1.0f || 
                                       attributes.getMaxshengming() != 20.0f || 
                                       attributes.getFangyu() != 1.0f ||
                                        attributes.getJingyan() !=0.0f;

                    if (!attributes.isInitialized() && !isRespawn) {
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
                        attributes.setInitialized(true);
                    } else {
                        if (!attributes.isInitialized()) {
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
        SPacketSyncPlayerAttribute packet = new SPacketSyncPlayerAttribute(
            attr.getShengming(), attr.getMaxshengming(), attr.getJingshenli(), attr.getMaxjingshenli(),
            attr.getMingzhong(), attr.getFangyu(), attr.getGongji(), attr.getBaojilv(), attr.getBaojishanghai(),
            attr.getXixue(), attr.getShanbi(), attr.getKangbao(), attr.getJingyan(), attr.getDengji(), attr.getMaxjingyan()
                , (int)attr.getShengmingHuifu(), attr.getWuchuan()
        );
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}