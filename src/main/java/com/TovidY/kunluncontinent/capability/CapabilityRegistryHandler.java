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
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

// 注册能力提供者

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)  // 改为 FORGE 总线
public class CapabilityRegistryHandler {
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        //玩家附加属性
        if (event.getObject() instanceof Player) {
            PlayerAttributeCapabilityProvider provider = new PlayerAttributeCapabilityProvider();
            event.addCapability(new ResourceLocation(KlMain.MOD_ID, "player_attribute"), provider);
        }
        // 为怪物附加属性
        if (entity instanceof Mob || entity instanceof HunhuanEntity) {
            MobAttributeCapabilityProvider provider = new MobAttributeCapabilityProvider();
            event.addCapability(new ResourceLocation(KlMain.MOD_ID, "mob_attribute"), provider);
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

    @SubscribeEvent
    public static void onEntityTransform(LivingConversionEvent.Post event) {
        if (event.getOutcome() instanceof Mob newMob) {
            event.getEntity().getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(oldCap -> {
                newMob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
            newMob.setCustomName(null);
            monsterJoin(newMob);
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
                long nianxian = playerCapability.getNianxian();
                if (nianxian > 0) {
                    String rawName = entity.getType().getDescription().getString();
                    String colorPrefix = "§f";
                    if (nianxian >= 10000000) colorPrefix = "§c§l";
                    else if (nianxian >= 100000) colorPrefix = "§c";
                    else if (nianxian >= 10000) colorPrefix = "§0";
                    else if (nianxian >= 1000) colorPrefix = "§5";
                    else if (nianxian >= 100) colorPrefix = "§e";
                    Component newName = Component.literal(rawName + "-----" + colorPrefix + nianxian + "年");
                    entity.setCustomName(newName);

                    try {
                        Field persistenceField = Mob.class.getDeclaredField("persistenceRequired");
                        persistenceField.setAccessible(true);
                        persistenceField.set(entity, false);
                    } catch (Exception e) {
                        e.printStackTrace();
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