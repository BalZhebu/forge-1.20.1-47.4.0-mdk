package com.TovidY.kunluncontinent.network;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.server.SPacketEntityAttribute;
import com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.PacketDistributor;

// 同步属性接口
public interface SynsAPI {
    static void synsPlayerAttribute(Entity entity){
        if(entity instanceof ServerPlayer livingEntity){
            float maxshengming = ModAttributeAPI.getMaxshengming(livingEntity);
            if(livingEntity.getMaxHealth() != maxshengming){
                livingEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxshengming);
            }

            entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {

                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), new SPacketPlayerAttribute(entity.getId(),capability.serializeNBT()));
            });
        }
    }
    static void synsEntityAttribute(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    float myMaxHP = capability.getMaxshengming();
                    if (myMaxHP > 0) {
                        maxHealthAttr.setBaseValue(myMaxHP);
                        if (livingEntity.tickCount < 5) {
                            livingEntity.setHealth(myMaxHP);
                        }
                    }
                }
                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                        new SPacketEntityAttribute(entity.getId(), capability.serializeNBT()));
            });
        }
    }
}
