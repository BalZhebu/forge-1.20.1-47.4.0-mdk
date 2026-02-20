package com.TovidY.kunluncontinent.capability;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.tool.ModSwordBaseItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.item.armor.ModArmorBaseItem;
import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

import static com.TovidY.kunluncontinent.capability.hunhuanattributes.HunhuanAttributeHelper.getWuhunBonus;

/**
 * 统一属性 API，用于获取任何实体的属性
 * 整合了玩家属性和怪物属性逻辑
 */

public class ModAttributeAPI {

    private static float getBoneBonus(Player player, String key) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getBoneOnlyStats().getOrDefault(key, 0f))
                .orElse(0f);
    }

    public static float getShengming(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getShengming).orElse(0f);
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getShengming).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getMaxshengming(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMaxshengming).orElse(0f);

            value += getWuhunBonus(player, MobAttributeCapability::getMaxshengming);

            value += getBoneBonus(player, "maxshengming");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getMaxshengming).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
            }

            Iterable<ItemStack> armorSlots = livingEntity.getArmorSlots();
            if(armorSlots!=null) {
                Boolean istaozhuang = true;
                for (ItemStack armorSlot : armorSlots) {
                    if (!armorSlot.isEmpty() && armorSlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem && armorSlot.getMaxDamage() - armorSlot.getDamageValue() > 1) {
                        value += shArmorBaseItem.setMaxshengming(armorSlot, value);
                    } else {
                        istaozhuang = false;
                    }
                }
                if (istaozhuang) {
                    ItemStack itemBySlot = ModArmorBaseItem.getLowTaozhuang(armorSlots);
                    if (!itemBySlot.isEmpty() && itemBySlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem) {
                        value += shArmorBaseItem.setMaxshengmingTaozhuang(itemBySlot, value);
                    }
                }
            }
        }
        return value;

    }

    public static float getGongji(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getGongji).orElse(0f);

            value += getWuhunBonus(player, MobAttributeCapability::getGongji);
            value += getBoneBonus(player, "gongji");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getGongji).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHand = livingEntity.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ModSwordBaseItem swordItem) {
                if (mainHand.getMaxDamage() - mainHand.getDamageValue() > 1) {
                    value += swordItem.getGongji(mainHand);
                }
            }
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
            }
        }
        return value;
    }

    public static float getFangyu(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 0f;

        float baseFangyu = 0f;

        if (living instanceof Player player) {
            baseFangyu = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getFangyu).orElse(0f);

            baseFangyu += getWuhunBonus(player, MobAttributeCapability::getFangyu);

            baseFangyu += getBoneBonus(player, "fangyu");

        } else if (living instanceof Mob mob) {
            baseFangyu = mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getFangyu).orElse(0f);
        }

        // 假设你的药水实例名为 ModEffects.ARMOR_PIERCING
        if (living.hasEffect(ModEffects.ARMOR_PIERCING.get())) {
            baseFangyu *= 0.7f; // 扣除 30%，即保留 70%
        }

        if(entity instanceof LivingEntity livingEntity){
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                if(mobEffectMobEffectInstanceEntry.getKey() instanceof PotionAttribute potionAttribute){
                    baseFangyu += potionAttribute.getWufang(livingEntity,mobEffectMobEffectInstanceEntry,baseFangyu);
                }
            }
            Iterable<ItemStack> armorSlots = livingEntity.getArmorSlots();
            if(armorSlots!=null){
                Boolean istaozhuang = true;
                for (ItemStack armorSlot : armorSlots) {
                    if(!armorSlot.isEmpty() && armorSlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem && armorSlot.getMaxDamage()-armorSlot.getDamageValue()>1){
                        baseFangyu +=shArmorBaseItem.setWufang(armorSlot , baseFangyu);
                    }else {
                        istaozhuang = false;
                    }
                }
                if(istaozhuang){
                    ItemStack itemBySlot = ModArmorBaseItem.getLowTaozhuang(armorSlots);
                    if(!itemBySlot.isEmpty() && itemBySlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem){
                        baseFangyu +=shArmorBaseItem.setWufangTaozhuang(itemBySlot , baseFangyu);
                    }
                }
            }
        }

        return baseFangyu;
    }

    public static float getShengminghuifu(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getShengmingHuifu).orElse(0f);
            value += getBoneBonus(player, "shengminghuifu");
        }

        if(entity instanceof LivingEntity livingEntity){
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                if(mobEffectMobEffectInstanceEntry.getKey() instanceof PotionAttribute potionAttribute){
                    value += potionAttribute.getShengminghuifu(livingEntity,mobEffectMobEffectInstanceEntry,value);
                }
            }
            Iterable<ItemStack> armorSlots = livingEntity.getArmorSlots();
            if(armorSlots!=null){
                Boolean istaozhuang = true;
                for (ItemStack armorSlot : armorSlots) {
                    if(!armorSlot.isEmpty() && armorSlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem && armorSlot.getMaxDamage()-armorSlot.getDamageValue()>1){
                    }else {
                        istaozhuang = false;
                    }
                }
                if(istaozhuang){
                    ItemStack itemBySlot = ModArmorBaseItem.getLowTaozhuang(armorSlots);
                    if(!itemBySlot.isEmpty() && itemBySlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem){
                        value +=shArmorBaseItem.setShengminghuifuTaozhuang(itemBySlot , value);
                    }
                }
            }
        }

        return value;
    }


    public static float getJingshenli(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getJingshenli).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getMaxjingshenli(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMaxjingshenli).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getMingzhong(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMingzhong).orElse(0f);
            value += getBoneBonus(player, "mingzhong");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getMingzhong).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getBaojilv(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getBaojilv).orElse(0f);
            value += getBoneBonus(player, "baojilv");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getBaojilv).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHand = livingEntity.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ModSwordBaseItem swordItem) {
                if (mainHand.getMaxDamage() - mainHand.getDamageValue() > 1) {
                    value += swordItem.getBaoji(mainHand);
                }
            }
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getBaojishanghai(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getBaojishanghai).orElse(0f);
            value += getBoneBonus(player, "baojishanghai");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getBaojishanghai).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHand = livingEntity.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ModSwordBaseItem swordItem) {
                if (mainHand.getMaxDamage() - mainHand.getDamageValue() > 1) {
                    value += swordItem.getBaojiShanghai(mainHand);
                }
            }
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getXixue(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getXixue).orElse(0f);
            value += getBoneBonus(player, "xixue");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getXixue).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHand = livingEntity.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ModSwordBaseItem swordItem) {
                if (mainHand.getMaxDamage() - mainHand.getDamageValue() > 1) {
                    value += swordItem.getXixue(mainHand);
                }
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getWuchuan(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getWuchuan).orElse(0f);
            value += getBoneBonus(player, "wuchuan");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getWuchuan).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHand = livingEntity.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ModSwordBaseItem swordItem) {
                if (mainHand.getMaxDamage() - mainHand.getDamageValue() > 1) {
                    value += swordItem.getWuchuan(mainHand);
                }
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getShanbi(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getShanbi).orElse(0f);
            value += getBoneBonus(player, "shanbi");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getShanbi).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getKangbao(Entity entity) {
        float value = 0;
        if (entity instanceof Player player) {
            value += player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getKangbao).orElse(0f);
            value += getBoneBonus(player, "kangbao");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getKangbao).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            // 考虑药水效果
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry : activeEffectsMap.entrySet()) {
                // 在这里可以添加对自定义药水效果的处理
            }
        }
        return value;
    }

    public static float getJingyan(Entity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getJingyan).orElse(0f);
        }
        // 玩家专属属性，怪物不适用
        return 0f;
    }

    public static int getDengji(Entity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getDengji).orElse(0);
        }
        // 玩家专属属性，怪物不适用
        return 0;
    }

    public static float getMaxjingyan(Entity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMaxjingyan).orElse(0f);
        }
        // 玩家专属属性，怪物不适用
        return 0f;
    }

    public static float getTupochenggonglv(Entity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getTupochenggonglv).orElse(50.0f);
        }
        // 玩家专属属性，怪物不适用
        return 50.0f;
    }

    public static void setTupochenggonglv(Entity entity, float value) {
        if (entity instanceof Player player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> cap.setTupochenggonglv(value));
        }
        // 玩家专属属性，怪物不适用
    }
    
    /**
     * 获取有效防御力，考虑破甲效果
     * @param entity 实体
     * @return 有效防御力
     */
    public static float getEffectiveFangyu(LivingEntity entity) {
        float baseFangyu = getFangyu(entity);
        
        // 检查是否有破甲效果
        if (entity.hasEffect(ModEffects.ARMOR_PIERCING.get())) {
            // 有破甲效果，减少30%防御力
            return baseFangyu * 0.7f;
        }
        
        return baseFangyu;
    }
}
