package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

import static com.TovidY.kunluncontinent.KlMain.random;
import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability.wuhunListsnameall;
import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem.performUpgrade;

//各个方法的调用与使用

public class PlayerHunhuanAPI {

    static void addWuHun(Player player) {
        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if(capability1.isPresent()){
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);
            Object[] array = wuhunListsnameall.toArray();
            boolean b = true;
            while (b){
                String s = (String) array[random.nextInt(array.length)];
                List<MobAttributeCapability> monsterAttributeCapabilities = capability.getMonsterCapabilityLists().get(s);
                if(monsterAttributeCapabilities ==null) {
                    capability.getMonsterCapabilityLists().put(s,new ArrayList<>());
                    capability.getWuhunSkillsMap().remove(s);
                    capability.getWuhunListsname().add(s);
                    capability.setHunhuankuaiguan(capability.getMonsterCapabilityLists().size()-1);
                    player.sendSystemMessage(Component.translatable("成功觉醒武魂",s));
                    ((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(Component.translatable("成功觉醒武魂",s)));
                    b=!b;
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
        LazyOptional<PlayerAttributeCapability> capability1 = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
        if(capability1.isPresent()){
            PlayerAttributeCapability capability = capability1.orElseThrow(RuntimeException::new);
            Object[] array = wuhunListsnameall.toArray();
            String s = (String) array[random.nextInt(array.length)];
            addWuHun(player,s);
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
                player.sendSystemMessage(Component.translatable("成功觉醒武魂",name));
                ((ServerPlayer)player).connection.send(new ClientboundSetTitleTextPacket(Component.translatable("成功觉醒武魂",name)));
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
                    player.sendSystemMessage(Component.translatable("未开启或觉醒武魂"));
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
                listForActive.add(monsterCap);
                player.sendSystemMessage(Component.translatable("成功吸收魂环", monsterCap.getNianxian()));
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
                player.sendSystemMessage(Component.translatable("等级不足").withStyle(ChatFormatting.RED));
                return false;
            }
            if (currentRings >= maxRingsInHistory) {
                boolean isBreakthroughLevel = (playerLevel % 10 == 0 && playerLevel >= 10) || playerLevel == 199;
                if (!isBreakthroughLevel) {
                    player.sendSystemMessage(Component.translatable("阶段等级").withStyle(ChatFormatting.YELLOW));
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

    // 强制吸收魂环方法
    public static boolean forceXishouHunhuan(Player player, HunhuanEntity entity) {
        boolean absorbed = false;
        if (entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).isPresent()) {
            MobAttributeCapability capability = entity.getCapability(MobAttributeCapabilityProvider .CAPABILITY).resolve().get();
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

    //50000
//    年限计算精神力消耗
//     （2*年限* （log10(年限)*10+10） * （log10(年限)*10+10）)/（log10(年限)*log(年限)）
    /*
    1.20.1版精神力消耗对照表
年限     精神力
160     65.8
640     162
1280    265
2560    440
5120    744
10240   1273
20480   2203
40960   3850
81920   6786
163840  12051
327680  21543
655360  38742
1310720 70047
2621440 12726
           */
    public static boolean xishouHunhuan(Player player, HunhuanEntity entity) {
        boolean absorbed = false;
        if (entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).isPresent()) {
            MobAttributeCapability capability = entity.getCapability(MobAttributeCapabilityProvider .CAPABILITY).resolve().get();
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
        });
        String formattedValue = String.format("%.2f", value);
        int currentExp = (int) ModAttributeAPI.getJingyan(player);
        int maxExp = (int) ModAttributeAPI.getMaxjingyan(player);
        player.sendSystemMessage(Component.translatable("吸收经验成功",
                formattedValue + " §e当前经验:" + currentExp + "/" + maxExp));
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
            
            // 同步属性到客户端
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
            
            // 同步属性到客户端
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addFangyu(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setFangyu(capability.getFangyu()+value);
            
            // 同步属性到客户端
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addGongji(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setGongji(capability.getGongji()+value);
            
            // 同步属性到客户端
            syncPlayerAttributeToClient(player, capability);
        });
    }

    public static void addXixue(ServerPlayer player, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            capability.setXixue(capability.getXixue()+value);
            
            // 同步属性到客户端
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
//            魂骨蓸
            newplayerCapability.getHunguInventory().deserializeNBT(oldItemCapability.getHunguInventory().serializeNBT());

            newplayerCapability.resetGodSystem();

            //技能转生重置
            newplayerCapability.getWuhunSkillsMap().clear();
            newplayerCapability.getWuhunListsname().clear();
            newplayerCapability.getMonsterCapabilityLists().clear();
            newplayerCapability.setInitialized(false);
            newplayerCapability.setHunhuankuaiguan(-1);

            NetworkHandler.sendToClient(new PacketSyncGodData(newplayerCapability), player);

            SynsAPI.synsPlayerAttribute(player);

            player.setHealth(newplayerCapability.getMaxshengming());
        }
    }

    /**
     * 同步玩家属性到客户端
     */
    private static void syncPlayerAttributeToClient(ServerPlayer player, PlayerAttributeCapability capability) {
        com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute packet = 
            new com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute(
                capability.getShengming(), capability.getMaxshengming(), capability.getJingshenli(), capability.getMaxjingshenli(),
                capability.getMingzhong(), capability.getFangyu(), capability.getGongji(), capability.getBaojilv(), capability.getBaojishanghai(),
                capability.getXixue(), capability.getShanbi(), capability.getKangbao(), capability.getJingyan(), capability.getDengji(), capability.getMaxjingyan(),
                    (int)capability.getWuchuan(),capability.getShengmingHuifu(),capability.getBoneOnlyStats()
            );
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}