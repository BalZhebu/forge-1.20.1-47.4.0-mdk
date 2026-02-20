package com.TovidY.kunluncontinent.capability;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MonsterCapabilityAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.SPacketEntityAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 注册能力提供者
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)  // 改为 FORGE 总线
public class CapabilityRegistryHandler {
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        //玩家附加属性
        if (event.getObject() instanceof Player) {
            PlayerAttributeCapabilityProvider provider = new PlayerAttributeCapabilityProvider();
            event.addCapability(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "player_attribute"), provider);
        }
        // 为怪物附加属性
        if (entity instanceof Mob || entity instanceof HunhuanEntity) {
            MobAttributeCapabilityProvider provider = new MobAttributeCapabilityProvider();
            event.addCapability(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "mob_attribute"), provider);
            if (entity instanceof Mob mob) {
                monsterJoin(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if(entity==null)return;
        //魂环属性赋予
        if(entity instanceof HunhuanEntity hunhuan){
            hunhuanJoin(hunhuan);
        }
        //怪物属性赋予
        if (entity instanceof Mob monsterentity ) {
            monsterJoin(monsterentity);
        }
    }

    //怪物加入世界时赋予属性
    public static void monsterJoin(Mob entity){

        if(!entity.level().isClientSide){
            LazyOptional<MobAttributeCapability> capability = entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY);
            capability.ifPresent(playerCapability -> {
                if(playerCapability.getNianxian()==0){
                    MobAttributeCapability monsterAttributeCapability = MonsterCapabilityAPI.genMonsterCapability(entity);
                    playerCapability.deserializeNBT(monsterAttributeCapability.serializeNBT());
                    float maxshengming = playerCapability.getMaxshengming();
                    entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxshengming);
                    entity.setHealth(maxshengming);
                }
                if(entity.getCustomName() == null){
                    long nianxian = playerCapability.getNianxian();
                    if (nianxian>=10000000) {
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§c§l"+nianxian+"年"));
                    }else if (nianxian>=1000000){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§c"+nianxian+"年"));
                    }else if(nianxian>=100000){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§c"+nianxian+"年"));
                    }else if(nianxian>=10000){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§0"+nianxian+"年"));
                    }else if(nianxian>=1000){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§5"+nianxian+"年"));
                    }else if(nianxian>=100){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§e"+nianxian+"年"));
                    }else if(nianxian>=1){
                        entity.setCustomName(Component.translatable(entity.getDisplayName().getString()+"-----"+"§f"+nianxian+"年"));
                    }
                }
                SynsAPI.synsEntityAttribute(entity);
            });
        }else {
            CompoundTag compoundTag = SPacketEntityAttribute.monsterHashMapCapability.get(entity.getId());
            if(compoundTag!=null){
                entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    capability.deserializeNBT(compoundTag);
                });
            }
        }
    }

    //魂环加入世界时赋予属性
    public static void hunhuanJoin(HunhuanEntity entity){
        if(!entity.level().isClientSide){
            LazyOptional<MobAttributeCapability> capability = entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY);
            capability.ifPresent(monsterentity -> {
                SynsAPI.synsEntityAttribute(entity);

            });
        }else {
            CompoundTag compoundTag = SPacketEntityAttribute.monsterHashMapCapability.get(entity.getId());
            if(compoundTag!=null){
                entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    capability.deserializeNBT(compoundTag);

                    SPacketEntityAttribute.monsterHashMapCapability.remove(entity.getId());
                });
            }
        }
    }

    //玩家的同步属性
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (!target.level().isClientSide) {
            if (target instanceof Mob || target instanceof HunhuanEntity) {
                SynsAPI.synsEntityAttribute(target);
            }
        }
    }

}