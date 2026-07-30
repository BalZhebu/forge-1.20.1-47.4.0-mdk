package com.TovidY.kunluncontinent.capability;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MonsterCapabilityAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
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
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityRegistryHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        // 1. 玩家附加属性
        if (entity instanceof Player) {
            PlayerAttributeCapabilityProvider provider = new PlayerAttributeCapabilityProvider();
            event.addCapability(new ResourceLocation(KlMain.MOD_ID, "player_attribute"), provider);
        }
        // 2. 为怪物附加属性 (只附加能力，移除 monsterJoin 的过早调用)
        if (entity instanceof Mob || entity instanceof HunhuanEntity) {
            MobAttributeCapabilityProvider provider = new MobAttributeCapabilityProvider();
            event.addCapability(new ResourceLocation(KlMain.MOD_ID, "mob_attribute"), provider);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if (entity == null) return;

        // 魂环属性赋予
        if (entity instanceof HunhuanEntity hunhuan) {
            hunhuanJoin(hunhuan);
        }
        // 怪物属性赋予
        if (entity instanceof Mob monsterentity) {
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

    // 怪物/NPC 加入世界时赋予属性与头顶标签
    public static void monsterJoin(Mob entity) {

        if (!entity.level().isClientSide) {
            if (entity instanceof PlayerNpcEntity npc) {
                PlayerAttributeCapability npcCap = npc.getSoulCapability();
                if (npcCap != null) {
                    int level = npcCap.getDengji(); // 获取 NPC 等级
                    String wuhun = npcCap.getWuhunName(); // 获取 NPC 当前武魂

                    // 1. 设置头顶称号
                    if (wuhun != null) {
                        String title = getNpcTitleByLevel(level);
                        String colorPrefix = getNpcTitleColorPrefix(level);
                        String npcBaseName = npc.getName().getString();
                        Component newName = Component.literal(
                                npcBaseName + " [" + wuhun + "]-----" + colorPrefix + level + "级" + title
                        );
                        npc.setCustomName(newName);
                        npc.setCustomNameVisible(true);
                    }

                    // 2. 【核心修正】：从 npcCap 正确提取各项专属属性，防止全部错填为攻击力！
                    float npcHp = npcCap.getMaxshengming();
                    float npcAtk = npcCap.getGongji();
                    float npcDef = npcCap.getFangyu();
                    float npcBaojilv = npcCap.getBaojilv();
                    float npcBaojishanghai = npcCap.getBaojishanghai();
                    float npcKangbao = npcCap.getKangbao();
                    float npcXixue = npcCap.getXixue();
                    float npcMingzhong = npcCap.getMingzhong();
                    float npcShanbi = npcCap.getShanbi();
                    float npcWuchuan = npcCap.getWuchuan();
                    float npcShengmingHuifu = npcCap.getShengmingHuifu();

                    npc.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(mobCap -> {
                        // 2.1 血量与当前血量拉满
                        mobCap.setMaxshengming(npcHp);
                        mobCap.setShengming(npcHp); // 修正：设置当前血量为最大血量，不留 20 血漏洞

                        // 2.2 攻防数据正确映射
                        mobCap.setWugong(npcAtk);
                        mobCap.setfangyu(npcDef); // 修正：使用真实防御 (npcDef)

                        // 2.3 副属性一一对应赋值
                        mobCap.setBaojilv(npcBaojilv);             // 修正：暴击率
                        mobCap.setBaojishanghai(npcBaojishanghai); // 修正：暴击伤害
                        mobCap.setKangbao(npcKangbao);             // 修正：抗暴
                        mobCap.setXixue(npcXixue);                 // 修正：吸血
                        mobCap.setMingzhong(npcMingzhong);         // 修正：命中
                        mobCap.setShanbi(npcShanbi);               // 修正：闪避
                        mobCap.setWuchuan(npcWuchuan);             // 修正：武穿
                        mobCap.setShengminghuifu(npcShengmingHuifu); // 修正：生命恢复
                    });

                    // 3. 应用到 原生 MC 属性控制系统
                    var hpAttr = npc.getAttribute(Attributes.MAX_HEALTH);
                    if (hpAttr != null && npcHp > 0) {
                        hpAttr.setBaseValue(npcHp);
                        npc.setHealth(npcHp); // 实体真实血量拉满
                    }

                    var atkAttr = npc.getAttribute(Attributes.ATTACK_DAMAGE);
                    if (atkAttr != null && npcAtk > 0) {
                        atkAttr.setBaseValue(npcAtk);
                    }
                }

                // 4. 同步数据给客户端渲染
                SynsAPI.synsEntityAttribute(entity);
                return;
            }

            // ==================== 普通怪物处理逻辑 ====================
            LazyOptional<MobAttributeCapability> capability = entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY);
            capability.ifPresent(playerCapability -> {
                if (playerCapability.getNianxian() == 0) {
                    MobAttributeCapability monsterAttributeCapability = MonsterCapabilityAPI.genMonsterCapability(entity);
                    playerCapability.deserializeNBT(monsterAttributeCapability.serializeNBT());
                    float maxshengming = playerCapability.getMaxshengming();

                    var attributeInstance = entity.getAttribute(Attributes.MAX_HEALTH);
                    if (attributeInstance != null) {
                        attributeInstance.setBaseValue(maxshengming);
                        entity.setHealth(maxshengming);
                    }
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
                        net.minecraftforge.fml.util.ObfuscationReflectionHelper.setPrivateValue(
                                Mob.class, entity, false, "persistenceRequired"
                        );
                    } catch (Exception e) {
                        System.out.println("无法反射设置怪物的 persistenceRequired 属性");
                        e.printStackTrace();
                    }
                }
                SynsAPI.synsEntityAttribute(entity);
            });
        } else {
            CompoundTag compoundTag = SPacketEntityAttribute.monsterHashMapCapability.get(entity.getId());
            if (compoundTag != null) {
                entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    capability.deserializeNBT(compoundTag);
                });
            }
        }
    }

    /**
     * 根据等级返回斗罗大陆对应的魂师称号
     */
    public static String getNpcTitleByLevel(int level) {
        if (level >= 99) return "极限斗罗";
        if (level >= 90) return "封号斗罗";
        if (level >= 80) return "魂斗罗";
        if (level >= 70) return "魂圣";
        if (level >= 60) return "魂帝";
        if (level >= 50) return "魂王";
        if (level >= 40) return "魂宗";
        if (level >= 30) return "魂尊";
        if (level >= 20) return "大魂师";
        if (level >= 10) return "魂师";
        return "魂士";
    }

    /**
     * 根据等级段展示不同稀有度的颜色代码
     */
    public static String getNpcTitleColorPrefix(int level) {
        if (level >= 99) return "§6§l"; // 金色加粗 (极限斗罗)
        if (level >= 90) return "§c§l"; // 红色加粗 (封号斗罗)
        if (level >= 70) return "§c";   // 红色 (魂圣/魂斗罗)
        if (level >= 50) return "§5";   // 紫色 (魂王/魂帝)
        if (level >= 30) return "§9";   // 蓝色 (魂尊/魂宗)
        if (level >= 10) return "§e";   // 黄色 (魂师/大魂师)
        return "§f";                    // 白色 (魂士)
    }

    // 魂环加入世界时赋予属性
    public static void hunhuanJoin(HunhuanEntity entity) {
        if (!entity.level().isClientSide) {
            entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(monsterentity -> {
                SynsAPI.synsEntityAttribute(entity);
            });
        } else {
            CompoundTag compoundTag = SPacketEntityAttribute.monsterHashMapCapability.get(entity.getId());
            if (compoundTag != null) {
                entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    capability.deserializeNBT(compoundTag);
                    SPacketEntityAttribute.monsterHashMapCapability.remove(entity.getId());
                });
            }
        }
    }

    // 玩家数据同步：涵盖开始追踪、玩家自己刷出来、切换维度等全场景
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (!target.level().isClientSide) {
            if (target instanceof Mob || target instanceof HunhuanEntity) {
                SynsAPI.synsEntityAttribute(target);
            }
        }
    }

    // 补全玩家自身跨维度或死后复活时的属性同步（非常重要）
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            SynsAPI.synsEntityAttribute(player);
        }
    }
}