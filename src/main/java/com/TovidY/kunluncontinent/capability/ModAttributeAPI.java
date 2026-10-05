package com.TovidY.kunluncontinent.capability;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.AttributePointSpec;
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
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;

import static com.TovidY.kunluncontinent.capability.hunhuanattributes.HunhuanAttributeHelper.getWuhunBonus;
import static com.TovidY.kunluncontinent.item.armor.ModArmorBaseItem.getLowTaozhuang;

/**
 * 统一属性 API，用于获取任何实体的属性
 * 整合了玩家属性和怪物属性逻辑
 */

public class ModAttributeAPI {

    /**
     * 取"玩家裸值 + 属性点加成"。
     *
     * <p><b>⚠️ 必须夹在裸值上、在其它加成相加之前调用</b>，不能在 getter 结尾对总和乘算 ——
     * 属性点是玩家自身修炼的收益，武魂 / 魂骨 / 装备 / 药水那些加成<b>不该被一起放大</b>。
     * 放到最后等于"开武魂后同样的 99 点收益翻十几倍"，实测 99 级 + 9 枚十万年魂环
     * 攻击力会从 66,123 变成 157,053。</p>
     *
     * <p>非玩家实体（怪物）原样返回，怪物的加点表不存在。</p>
     */
    private static float withPoints(Entity entity, String key, float nakedValue) {
        return AttributePointSpec.applyBonusByKey(key, nakedValue, entity);
    }

    private static float getBoneBonus(Player player, String key) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getBoneOnlyStats().getOrDefault(key, 0f))
                .orElse(0f);
    }

    /**
     * 取"武魂永久基础属性"（关武魂后依然生效的那部分）。
     *
     * <p>与魂骨一样按能力取，NPC 没有这个概念 → 返回 0。</p>
     */
    private static float getPermanentBonus(Player player, String key) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getWuhunPermanentStats().getOrDefault(key, 0f))
                .orElse(0f);
    }

    /**
     * NPC 的属性统一入口。
     *
     * <p><b>为什么 NPC 要单独一条路</b>：{@code PlayerNpcEntity} 继承的是 {@code PathfinderMob}，
     * 所以下面 11 个 getter 的 {@code instanceof Mob} 分支全都会命中它 →
     * 去读 {@link MobAttributeCapability}。但 NPC 的属性其实存在
     * {@code soulCapability}（{@link PlayerAttributeCapability}，和玩家同一个类）里，
     * <b>从不写MobAttributeCapability</b> → 战斗里 NPC 的攻防暴闪全读成 0。</p>
     *
     * <p>所以这里在Mob 分支<b>之前</b>拦一道：命中 NPC 就直接返回
     * 「soulCapability 裸值 + 魂骨加成」，武魂加成走原魂环链路自动生效。</p>
     *
     * @param key裸值 getter（"gongji" / "fangyu" / …）
     * @param boneKey 魂骨加成在 {@code boneOnlyStats} 里的键（一般与 key 相同）
     * @return 命中的话返回 NPC 属性；<b>没命中返回 {@link #NO_NPC}</b> 让调用方继续原逻辑
     */
    private static final float NO_NPC = Float.NaN;

    private static float npcAttr(Entity entity, String key,
                                 java.util.function.Function<PlayerAttributeCapability, Float> rawGetter,
                                 String boneKey) {
        if (!(entity instanceof com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity npc)) {
            return NO_NPC;
        }
        PlayerAttributeCapability soul = npc.getSoulCapability();
        float raw = rawGetter.apply(soul);
        float bone = soul.getBoneOnlyStats().getOrDefault(boneKey, 0f);
        return raw + bone;
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
        return value;
    }

    /**
     * 取"属性面板上显示的那个数值"。
     *
     * <p>面板（{@code AttributeScreen}）里除了生命之外，用的都是本类下面那些聚合 getter；
     * 而**生命一栏显示的就是 {@code player.getMaxHealth()}** —— 即
     * {@link #getMaxshengming(Entity)} 的结果同步进原版 MAX_HEALTH 之后、
     * 再叠上原版属性修正的最终值。</p>
     *
     * <p>⚠️ 数值类考核（神考）必须用这个方法，**不要**用
     * {@code PlayerAttributeCapability.getMaxshengming()}：
     * 那只是玩家自身的裸值，不含魂环 / 魂骨 / 装备 / 药水等外部加成，
     * 会导致"面板上明明达标了却过不了考核"。</p>
     *
     * @param attrKey 与神考任务里写的属性键一致：maxshengming / gongji / fangyu / ...
     */
    public static float getPanelAttributeValue(Player player, String attrKey) {
        if (player == null || attrKey == null) {
            return 0f;
        }
        // 生命：面板显示的就是 MAX_HEALTH 的最终值（含一切加成）
        if ("maxshengming".equals(attrKey)) {
            return player.getMaxHealth();
        }
        return switch (attrKey) {
            case "gongji" -> getGongji(player);
            case "fangyu" -> getFangyu(player);
            case "baojilv" -> getBaojilv(player);
            case "baojishanghai" -> getBaojishanghai(player);
            case "shengminghuifu" -> getShengminghuifu(player);
            case "xixue" -> getXixue(player);
            case "shanbi" -> getShanbi(player);
            case "mingzhong" -> getMingzhong(player);
            case "wuchuan" -> getWuchuan(player);
            case "kangbao" -> getKangbao(player);
            default -> 0f;
        };
    }

    public static float getMaxshengming(Entity entity) {
        float npc = npcAttr(entity, "maxshengming",
                PlayerAttributeCapability::getMaxshengming, "maxshengming");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "maxshengming", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMaxshengming).orElse(0f));

            value += getWuhunBonus(player, MobAttributeCapability::getMaxshengming);

            value += getBoneBonus(player, "maxshengming");
            value += getPermanentBonus(player, "maxshengming");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getMaxshengming).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getMaxshengming(livingEntity, entry, value);
                }
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
                    ItemStack itemBySlot = getLowTaozhuang(armorSlots);
                    if (!itemBySlot.isEmpty() && itemBySlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem) {
                        value += shArmorBaseItem.setMaxshengmingTaozhuang(itemBySlot, value);
                    }
                }
            }
        }
        return value;

    }

    public static float getGongji(Entity entity) {
        float npc = npcAttr(entity, "gongji",
                PlayerAttributeCapability::getGongji, "gongji");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "gongji", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getGongji).orElse(0f));

            value += getWuhunBonus(player, MobAttributeCapability::getGongji);
            value += getBoneBonus(player, "gongji");
            value += getPermanentBonus(player, "gongji");
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
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                MobEffect effect = entry.getKey();
                if (effect instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getWugong(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getFangyu(Entity entity) {
        float npc = npcAttr(entity, "fangyu",
                PlayerAttributeCapability::getFangyu, "fangyu");
        if (!Float.isNaN(npc)) return npc;

        if (!(entity instanceof LivingEntity living)) return 0f;

        float baseFangyu = 0f;

        if (living instanceof Player player) {
            baseFangyu = withPoints(living, "fangyu", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getFangyu)
                    .orElse(0f));

            baseFangyu += getWuhunBonus(player, MobAttributeCapability::getFangyu);
            baseFangyu += getBoneBonus(player, "fangyu");
            baseFangyu += getPermanentBonus(player, "fangyu");

        } else if (living instanceof Mob mob) {
            baseFangyu = mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getFangyu)
                    .orElse(0f);
        }

        if (living.hasEffect(ModEffects.ARMOR_PIERCING.get())) {
            baseFangyu *= 0.7f;
        }

        Map<MobEffect, MobEffectInstance> activeEffectsMap =
                living.getActiveEffectsMap();

        for (Map.Entry<MobEffect, MobEffectInstance> entry
                : activeEffectsMap.entrySet()) {

            if (entry.getKey() instanceof PotionAttribute potionAttribute) {
                baseFangyu += potionAttribute.getWufang(
                        living,
                        entry,
                        baseFangyu
                );
            }
        }

        Iterable<ItemStack> armorSlots = living.getArmorSlots();

        if (armorSlots != null) {
            boolean isFullArmor = true;

            for (ItemStack armorSlot : armorSlots) {
                if (armorSlot.isEmpty()
                        || !(armorSlot.getItem() instanceof ModArmorBaseItem)
                        || armorSlot.getMaxDamage() - armorSlot.getDamageValue() <= 1) {

                    isFullArmor = false;
                    break;
                }
            }

            if (isFullArmor) {
                ItemStack lowArmor = getLowTaozhuang(armorSlots);

                if (!lowArmor.isEmpty()
                        && lowArmor.getItem() instanceof ModArmorBaseItem shArmorBaseItem) {

                    baseFangyu += shArmorBaseItem.setWufangTaozhuang(
                            lowArmor,
                            baseFangyu
                    );
                }
            }
        }

        return baseFangyu;
    }

    public static float getShengminghuifu(Entity entity) {
        float npc = npcAttr(entity, "shengminghuifu",
                PlayerAttributeCapability::getShengmingHuifu, "shengminghuifu");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "shengminghuifu", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getShengmingHuifu).orElse(0f));
            value += getBoneBonus(player, "shengminghuifu");
            value += getPermanentBonus(player, "shengminghuifu");
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
                    ItemStack itemBySlot = getLowTaozhuang(armorSlots);
                    if(!itemBySlot.isEmpty() && itemBySlot.getItem() instanceof ModArmorBaseItem shArmorBaseItem){
                        value +=shArmorBaseItem.setShengminghuifuTaozhuang(itemBySlot , value);
                    }
                }
            }
        }

        return value;
    }


    public static float getJingshenli(Entity entity) {
        if (entity instanceof Player player) {
            return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getJingshenli).orElse(0f);
        }
        return 0f;
    }

    public static float getMaxjingshenli(Player player) {
        float value = 0;
        LazyOptional<PlayerAttributeCapability> capability = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if (capability.isPresent()) {
            PlayerAttributeCapability playerAttributeCapability = capability.orElseThrow(RuntimeException::new);
            value += playerAttributeCapability.getMaxjingshenli();
        }
        return value;
    }

    public static float getMingzhong(Entity entity) {
        float npc = npcAttr(entity, "mingzhong",
                PlayerAttributeCapability::getMingzhong, "mingzhong");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "mingzhong", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getMingzhong).orElse(0f));
            value += getBoneBonus(player, "mingzhong");
            value += getPermanentBonus(player, "mingzhong");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getMingzhong).orElse(0f);
        }

        if (entity instanceof LivingEntity livingEntity) {
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getMinghzong(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getBaojilv(Entity entity) {
        float npc = npcAttr(entity, "baojilv",
                PlayerAttributeCapability::getBaojilv, "baojilv");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "baojilv", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getBaojilv).orElse(0f));
            value += getBoneBonus(player, "baojilv");
            value += getPermanentBonus(player, "baojilv");
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
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getBaojilv(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getBaojishanghai(Entity entity) {
        float npc = npcAttr(entity, "baojishanghai",
                PlayerAttributeCapability::getBaojishanghai, "baojishanghai");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "baojishanghai", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getBaojishanghai).orElse(0f));
            value += getBoneBonus(player, "baojishanghai");
            value += getPermanentBonus(player, "baojishanghai");
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
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getBaojishanghai(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getXixue(Entity entity) {
        float npc = npcAttr(entity, "xixue",
                PlayerAttributeCapability::getXixue, "xixue");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "xixue", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getXixue).orElse(0f));
            value += getBoneBonus(player, "xixue");
            value += getPermanentBonus(player, "xixue");
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
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getXixue(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getWuchuan(Entity entity) {
        float npc = npcAttr(entity, "wuchuan",
                PlayerAttributeCapability::getWuchuan, "wuchuan");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "wuchuan", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getWuchuan).orElse(0f));
            value += getBoneBonus(player, "wuchuan");
            value += getPermanentBonus(player, "wuchuan");
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
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getWuchuan(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getShanbi(Entity entity) {
        float npc = npcAttr(entity, "shanbi",
                PlayerAttributeCapability::getShanbi, "shanbi");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "shanbi", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getShanbi).orElse(0f));
            value += getBoneBonus(player, "shanbi");
            value += getPermanentBonus(player, "shanbi");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getShanbi).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getShanbi(livingEntity, entry, value);
                }
            }
        }
        return value;
    }

    public static float getKangbao(Entity entity) {
        float npc = npcAttr(entity, "kangbao",
                PlayerAttributeCapability::getKangbao, "kangbao");
        if (!Float.isNaN(npc)) return npc;

        float value = 0;
        if (entity instanceof Player player) {
            value += withPoints(entity, "kangbao", player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(PlayerAttributeCapability::getKangbao).orElse(0f));
            value += getBoneBonus(player, "kangbao");
            value += getPermanentBonus(player, "kangbao");
        }
        if (entity instanceof Mob mob) {
            value += mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getKangbao).orElse(0f);
        }
        
        if (entity instanceof LivingEntity livingEntity) {
            Map<MobEffect, MobEffectInstance> activeEffectsMap = livingEntity.getActiveEffectsMap();
            for (Map.Entry<MobEffect, MobEffectInstance> entry : activeEffectsMap.entrySet()) {
                if (entry.getKey() instanceof PotionAttribute potionAttr) {
                    value += potionAttr.getKangbao(livingEntity, entry, value);
                }
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
        return getFangyu(entity);
    }
}
