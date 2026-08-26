package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.mobattributes.HunhuanWeakener;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem.performUpgrade;

//各个方法的调用与使用

public class PlayerHunhuanAPI {

    private static final Random random = new Random();

    private static List<String> getGlobalWuhunList() {
        return PlayerAttributeCapability.wuhunListsnameall;
    }

    static void addWuHun(Player player) {
        List<String> globalList = getGlobalWuhunList();
        if (globalList == null || globalList.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[错误] 全局武魂池未初始化！请联系作者！"));
            return;
        }

        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if(capability1.isPresent()){
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);

            Object[] array = globalList.toArray();
            boolean b = true;

            int attempts = 0;
            while (b && attempts < 100){
                attempts++;
                String s = (String) array[random.nextInt(array.length)];
                List<MobAttributeCapability> monsterAttributeCapabilities = capability.getMonsterCapabilityLists().get(s);
                if(monsterAttributeCapabilities == null) {
                    capability.getMonsterCapabilityLists().put(s, new ArrayList<>());
                    capability.getWuhunSkillsMap().remove(s);
                    capability.getWuhunListsname().add(s);
                    capability.setHunhuankuaiguan(capability.getMonsterCapabilityLists().size()-1);
                    player.sendSystemMessage(Component.literal("成功觉醒武魂: " + s));
                    ((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(Component.literal("成功觉醒武魂: " + s)));
                    b = !b;
                    if(random.nextInt(5) == 0){
                        juexingShuangsheng(player);
                    }
                    Collections.sort(capability.getWuhunListsname());
                    SynsAPI.synsPlayerAttribute(player);
                }
            }
        }
    }

    static void juexingShuangsheng(Player player) {
        List<String> globalList = getGlobalWuhunList();
        if (globalList == null || globalList.isEmpty()) return;

        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if(capability1.isPresent()){
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);

            // 【修改点】改用 globalList
            Object[] array = globalList.toArray();
            String s = (String) array[random.nextInt(array.length)];
            addWuHun(player, s);
        }
    }

    public static void addWuHun(Player player, String name) {
        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if(capability1.isPresent()){
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);
            List<MobAttributeCapability> monsterAttributeCapabilities = capability.getMonsterCapabilityLists().get(name);
            if(monsterAttributeCapabilities ==null) {
                capability.getMonsterCapabilityLists().put(name,new ArrayList<>());
                capability.getWuhunListsname().add(name);
                capability.getWuhunSkillsMap().remove(name);
                capability.setHunhuankuaiguan(capability.getMonsterCapabilityLists().size()-1);
                player.sendSystemMessage(Component.literal("成功觉醒武魂: " + name));
                ((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(Component.literal("成功觉醒武魂: " + name)));
                SynsAPI.synsPlayerAttribute(player);
            }else {
                player.sendSystemMessage(Component.literal("觉醒失败，已拥有该武魂").withStyle(ChatFormatting.RED));
                ((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(Component.literal("觉醒失败，已拥有该武魂").withStyle(ChatFormatting.RED)));
            }
        }
    }

    static void addHunhuan(Player player, HunhuanEntity entity) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(monsterCap -> {
                List<String> wuhunNames = capability.getWuhunListsname();
                if (wuhunNames == null || wuhunNames.isEmpty()) {
                    player.sendSystemMessage(Component.literal("未开启或觉醒武魂"));
                    return;
                }
                int activeIndex = capability.getHunhuankuaiguan();
                if (activeIndex < 0 || activeIndex >= wuhunNames.size()) {
                    activeIndex = wuhunNames.size() - 1;
                    capability.setHunhuankuaiguan(activeIndex);
                }
                String activeName = wuhunNames.get(activeIndex);
                Map<String, List<MobAttributeCapability>> map = capability.getMonsterCapabilityLists();
                if (map == null) {
                    map = new HashMap<>();
                }
                List<MobAttributeCapability> listForActive = map.get(activeName);
                if (listForActive == null) {
                    listForActive = new ArrayList<>();
                    map.put(activeName, listForActive);
                }

                MobAttributeCapability weakenedCap = HunhuanWeakener.weaken(monsterCap);
                listForActive.add(weakenedCap);

                player.sendSystemMessage(Component.literal("成功吸收" + monsterCap.getNianxian() + "年魂环！"));
                SynsAPI.synsPlayerAttribute(player);
            });
        });
    }

    public static boolean isXishouHunhuan(ServerPlayer player, HunhuanEntity hunhuanEntity) {
        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if (capability1.isPresent()) {
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);
            int playerLevel = capability.getDengji();
            String currentWuhun = capability.getWuhunName();
            List<MobAttributeCapability> currentRingsList = capability.getMonsterCapabilityLists().get(currentWuhun);
            int currentRings = (currentRingsList != null) ? currentRingsList.size() : 0;
            int maxRingsInHistory = getMaxRings(capability);
            int allowedRingsByLevel = (playerLevel >= 199) ? 20 : (playerLevel / 10);
            if (playerLevel < 10 && playerLevel >= 1) allowedRingsByLevel = 1;
            if (currentRings >= allowedRingsByLevel) {
                player.sendSystemMessage(Component.literal("等级不足").withStyle(ChatFormatting.RED));
                return false;
            }
            if (currentRings >= maxRingsInHistory) {
                boolean isBreakthroughLevel = (playerLevel % 10 == 0 && playerLevel >= 10) || playerLevel == 199;
                if (!isBreakthroughLevel) {
                    player.sendSystemMessage(Component.literal("阶段等级不足，无法突破吸收").withStyle(ChatFormatting.YELLOW));
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static int getMaxRings(PlayerAttributeCapability cap) {
        int max = 0;
        Map<String, List<MobAttributeCapability>> map = cap.getMonsterCapabilityLists();
        if (map == null || map.isEmpty()) return 0;
        for (List<MobAttributeCapability> list : map.values()) {
            if (list != null && list.size() > max) max = list.size();
        }
        return max;
    }

    public static boolean forceXishouHunhuan(Player player, HunhuanEntity entity) {
        boolean absorbed = false;
        if (entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).isPresent()) {
            MobAttributeCapability capability = entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).resolve().get();
            double v1 = Math.log10(capability.getNianxian());
            double v = v1 * 10 + 10;

            if (entity.getExistenceTime() >= v) {
                addHunhuan(player, entity);
                entity.discard();
                absorbed = true;
            }
        }
        return absorbed;
    }

    public static boolean xishouHunhuan(Player player, HunhuanEntity entity) {
        boolean absorbed = false;
        if (entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).isPresent()) {
            MobAttributeCapability capability = entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).resolve().get();
            double v1 = Math.log10(capability.getNianxian());
            double v = v1 * 10 + 10;
            if (!capability.isShenci()) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability1 -> {
                    float jingshenli = (float) ((capability.getNianxian() / (v1 * v1 * 0.5)) / v);
                    capability1.setJingshenli(capability1.getJingshenli() - jingshenli);
                });
            }
            if (entity.getExistenceTime() >= v) {
                addHunhuan(player, entity);
                entity.discard();
                absorbed = true;
            }
        }
        return absorbed;
    }

    public static void addJingyan(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setJingyan(capability.getJingyan() + value);
            if (capability.getJingyan() >= capability.getMaxjingyan()) {
                 performUpgrade(player, capability);
            }
            if (capability.isConfigOpen(3)) {
                String formattedValue = String.format("%.2f", value);
                int currentExp = (int) capability.getJingyan();
                int maxExp = (int) capability.getMaxjingyan();
                player.sendSystemMessage(Component.literal("吸收经验成功: " + formattedValue + " 当前经验:§e" + currentExp + "/" + maxExp));
            }
        });
    }

    public static void addBaojilv(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setBaojilv(capability.getBaojilv()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addBaojishanhai(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setBaojishanghai(capability.getBaojishanghai()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addKangbao(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setKangbao(capability.getKangbao()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addShanbi(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setShanbi(capability.getShanbi()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addJingshenli(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setJingshenli(Math.min(capability.getJingshenli()+value,capability.getMaxjingshenli()));
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addMaxshengming(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setMaxshengming(capability.getMaxshengming()+value);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(capability.getMaxshengming());
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addMingzhong(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setMingzhong(capability.getMingzhong()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addShengming(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            float v = player.getHealth() + value;
            float maxHealth = player.getMaxHealth();
            player.setHealth(Math.min(v, maxHealth));
            syncPlayerAttributeToClient(player, capability);
        });
    }

    static void addWuchuan(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setWuchuan(capability.getWuchuan()+value);
        });
    }

    public static void addTupochenggonggailv(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setTupochenggonglv(capability.getTupochenggonglv()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addFangyu(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setFangyu(capability.getFangyu()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addGongji(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setGongji(capability.getGongji()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addXixue(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setXixue(capability.getXixue()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addMaxJingshenli(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setMaxjingshenli(capability.getMaxjingshenli()+value);
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void zhuansheng(PlayerAttributeCapability newplayerCapability, PlayerAttributeCapability oldItemCapability, ServerPlayer player) {
        if(newplayerCapability!=null&&oldItemCapability!=null){
            newplayerCapability.setGongji(newplayerCapability.getGongji()+oldItemCapability.getGongji()/20);
            newplayerCapability.setFangyu(newplayerCapability.getFangyu()+oldItemCapability.getFangyu()/20);
            newplayerCapability.setMaxshengming(newplayerCapability.getMaxshengming()+oldItemCapability.getMaxshengming()/20);
            newplayerCapability.setShengmingHuifu(newplayerCapability.getShengmingHuifu()+oldItemCapability.getShengmingHuifu()/20);
            newplayerCapability.setWuchuan(newplayerCapability.getWuchuan()+oldItemCapability.getWuchuan()/20);
            newplayerCapability.setKangbao(newplayerCapability.getKangbao()+oldItemCapability.getKangbao()/20);
            newplayerCapability.setMaxjingshenli((newplayerCapability.getMaxjingshenli()+oldItemCapability.getMaxjingshenli()/20));
            newplayerCapability.setJingshenli(0);
            newplayerCapability.setZhuanshengshu(newplayerCapability.getZhuanshengshu()+oldItemCapability.getZhuanshengshu()+1);
            newplayerCapability.getHunguInventory().deserializeNBT(oldItemCapability.getHunguInventory().serializeNBT());
            newplayerCapability.resetGodSystem();
            newplayerCapability.setCurrentTowerFloor(0);
            newplayerCapability.setTowerLastActiveTick(0);
            newplayerCapability.setTowerChallenging(false);
            newplayerCapability.getWuhunSkillsMap().clear();
            newplayerCapability.getWuhunListsname().clear();
            newplayerCapability.getMonsterCapabilityLists().clear();
            newplayerCapability.setInitialized(false);
            newplayerCapability.setHunhuankuaiguan(-1);
            SynsAPI.synsPlayerAttribute(player);
            player.setHealth(newplayerCapability.getMaxshengming());
        }
    }

    private static void syncPlayerAttributeToClient(ServerPlayer player, PlayerAttributeCapability capability) {
        CompoundTag nbtData = capability.serializeNBT();
        com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute packet =
                new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(player.getId(), nbtData);
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
        );
    }
}