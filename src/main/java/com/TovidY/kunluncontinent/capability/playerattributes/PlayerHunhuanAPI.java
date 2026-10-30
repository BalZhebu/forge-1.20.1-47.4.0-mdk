package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.HunhuanWeakener;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.godclass.GodRank;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.ZhenshenState;
import com.TovidY.kunluncontinent.screen.attribute.skill.SkillBatchRelease;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.SyncWuhunDataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
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

    /** 双生武魂概率（%）：一次觉醒仪式的第一个武魂之后，再白得一个。 */
    public static final int DUO_WUHUN_CHANCE_PERCENT = 6;

    /** 三生武魂概率（%）：<b>成功拿到第二武魂之后</b>再掷一次，中了就再白得一个。 */
    public static final int TRIPLE_WUHUN_CHANCE_PERCENT = 20;

    /**
     * 武魂精华最多能预定几个 = 一届觉醒最多能出几个武魂（3）。
     *
     * <p>吃满 3 枚 → 觉醒时直接是「三生武魂」，而且到顶了，不再追加、也不再抽双生。
     * 玩家总武魂数上限同样是 3（见 {@code WuhunguoshiItem} 的拦截）。</p>
     */
    public static final int MAX_PENDING_ESSENCES = 3;

    /**
     * <b>觉醒仪式</b>：决定这次觉醒出哪些武魂，并真的加到玩家身上。
     *
     * <h3>四个步骤</h3>
     * <ol>
     *   <li><b>武魂精华预定先占位</b>（0~3 个）—— 精华的唯一作用就是「定下这次觉醒的前几个武魂」，
     *       定完之后<b>后续抽签照常</b>：吃 1 枚也能接着抽双生/三生；吃满 3 枚直接就是三生，不再追加；</li>
     *   <li>一个都没定下 → 抽第一个：<b>10% 天使</b>（独立概率，不在平均池里），否则池子等概率；</li>
     *   <li>只有 1 个且不是天使 → 掷 <b>6% 双生</b>，抽第二个（<b>排除已拥有</b>，可以抽到天使）；</li>
     *   <li>已经 2 个且都不含天使 → 掷 <b>20% 三生</b>，再抽第三个（同样排除已拥有，也可以抽到天使）。</li>
     * </ol>
     *
     * <p><b>天使一旦出现在本次觉醒里就停</b>（不再追加双生/三生）—— 它是「独一无二」的，
     * 一次觉醒只会出它一个。注意这不影响它作为第二/第三武魂被抽到（前两个都不是它时照样能抽中）。</p>
     *
     * <p>一次觉醒最多出 <b>3 个</b>武魂 —— 到 3 个就停，之后不会再有觉醒。</p>
     *
     * @return 本次实际觉醒的武魂名（顺序 = 提示顺序）；空表 = 一个都没觉醒
     */
    public static List<String> awakenAtRitual(ServerPlayer player, PlayerAttributeCapability cap) {
        List<String> result = new ArrayList<>();

        // ① 精华预定：先把「定下的」放进去
        List<String> pending = new ArrayList<>(cap.getPendingWuhunEssences());
        if (!pending.isEmpty()) {
            cap.clearPendingWuhunEssences();
            for (String name : pending) {
                if (result.size() >= MAX_PENDING_ESSENCES) {
                    break;
                }
                if (cap.getWuhunListsname().contains(name)) {
                    continue;   // 中间已经通过别的途径拿到这个武魂了
                }
                addWuHun(player, name);
                if (cap.getWuhunListsname().contains(name)) {
                    result.add(name);
                }
            }
        }

        // ② 一个都没定下 → 抽第一个：天使 10% → 否则池子等概率
        if (result.isEmpty()) {
            String first = rollOneWuhun(player, cap, Collections.emptyList());
            if (first == null) {
                player.sendSystemMessage(Component.literal(
                        "§c[错误] 没有可觉醒的武魂（全局武魂池为空或已被抽干），请联系作者！"));
                return result;
            }
            addWuHun(player, first);
            if (!cap.getWuhunListsname().contains(first)) {
                return result;
            }
            result.add(first);
        }

        // ③ 双生：6%，只在「当前恰好 1 个、且不是天使」时掷
        if (result.size() == 1
                && !Wuhunname.tianshi.equals(result.get(0))
                && player.getRandom().nextInt(100) < DUO_WUHUN_CHANCE_PERCENT) {
            String second = rollOneWuhun(player, cap, new ArrayList<>(cap.getWuhunListsname()));
            if (second != null) {
                addWuHun(player, second);
                if (cap.getWuhunListsname().contains(second)) {
                    result.add(second);
                }
            }
        }

        // ④ 三生：20%，必须「已经成功拿到 2 个」且两个都不是天使时才再掷一次
        if (result.size() == 2
                && !result.contains(Wuhunname.tianshi)
                && player.getRandom().nextInt(100) < TRIPLE_WUHUN_CHANCE_PERCENT) {
            String third = rollOneWuhun(player, cap, new ArrayList<>(cap.getWuhunListsname()));
            if (third != null) {
                addWuHun(player, third);
                if (cap.getWuhunListsname().contains(third)) {
                    result.add(third);
                }
            }
        }

        // 天使的「开局限定」资格：只有在本仪式（0 级觉醒）里拿到天使，
        // 才会在吸收第一枚魂环时补那 10 级。吃精华 / 命令拿到的不算 —— 见 TianshiWuhun。
        if (result.contains(Wuhunname.tianshi)) {
            cap.setTianshiStartAwaken(true);
        }

        SynsAPI.synsPlayerAttribute(player);
        return result;
    }

    /**
     * 抽一个武魂（<b>只抽不加</b>）。
     *
     * @param exclude 本次不能抽到的名字（双生抽第二个时传「已拥有」）
     * @return 抽到的武魂名；{@code null} = 没得抽（池子被排除光了）
     */
    private static String rollOneWuhun(ServerPlayer player, PlayerAttributeCapability cap, List<String> exclude) {
        if (TianshiWuhun.rollIndependentAngel(player, cap, exclude)) {
            return Wuhunname.tianshi;
        }
        List<String> pool = new ArrayList<>();
        for (String name : getGlobalWuhunList()) {
            if (!exclude.contains(name) && !cap.getWuhunListsname().contains(name)) {
                pool.add(name);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(random.nextInt(pool.size()));
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
                AchievementAPI.onAwakenWuhun(player);
                SynsAPI.synsPlayerAttribute(player);
                // 天使武魂：等级已经很高时才觉醒（武魂精华 / 双生）→ 立刻补发天使神神考
                if (player instanceof ServerPlayer sp) {
                    TianshiWuhun.maybeGrantTrial(sp, capability);
                }
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
                AchievementAPI.onAbsorbHunhuan(player, monsterCap.getNianxian(), listForActive.size());
                SynsAPI.synsPlayerAttribute(player);

                // 天使武魂：第一枚魂环到手后补发剩下的 10 级（内部幂等）
                if (player instanceof ServerPlayer sp) {
                    TianshiWuhun.onRingAbsorbed(sp, capability);
                }
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
            int allowedRingsByLevel;
            if (playerLevel >= 199) {
                allowedRingsByLevel = 20;
            } else if (playerLevel >= GodRank.MIN_LEVEL) {
                // 100 级以后按「神祇称谓段」放开：120 级可吸到第 11 枚（用来升 121），
                // 121~139 级额度仍是 11 → 要等 140 级才能吸第 12 枚。
                allowedRingsByLevel = GodRank.requiredRingsForLevel(playerLevel + 1);
                if (allowedRingsByLevel < 0) {
                    allowedRingsByLevel = playerLevel / 10;   // 兜底，理论上不会走到
                }
            } else {
                allowedRingsByLevel = playerLevel / 10;
                if (playerLevel < 10 && playerLevel >= 1) allowedRingsByLevel = 1;
            }
            if (currentRings >= allowedRingsByLevel) {
                player.sendSystemMessage(Component.literal("等级不足").withStyle(ChatFormatting.RED));
                return false;
            }
            if (currentRings >= maxRingsInHistory) {
                // 只有「段末」才允许吸环跨段（120 / 140 / 160 / 180 / 195）；
                // 100 级以下沿用原来的「每 10 级一个突破点」。
                boolean isBreakthroughLevel = playerLevel >= GodRank.MIN_LEVEL
                        ? GodRank.isStageBoundary(playerLevel)
                        : (playerLevel % 10 == 0 && playerLevel >= 10);
                if (!isBreakthroughLevel && playerLevel != 199) {
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
                    // 扣精神力时同样用"加点后的上限"做下限钳制，
                    // 否则加点后上限>裸上限时，扣到裸上限就会变成负数
                    float mpMax = ModAttributeAPI.getMaxjingshenli((ServerPlayer) player);
                    capability1.setJingshenli(
                            Math.max(0f, Math.min(capability1.getJingshenli() - jingshenli, mpMax)));
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
            // ⚠️ 上限必须走 ModAttributeAPI（**含加点加成**），不能直接读
            //    capability.getMaxjingshenli() —— 那是裸字段，不含"凝神"加点，
            //    会把回复量钳在裸上限上，导致点了精神力却"回不满"。
            float cap = ModAttributeAPI.getMaxjingshenli(player);
            capability.setJingshenli(Math.min(capability.getJingshenli() + value, cap));
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

            // 银行余额：转生的写法是 oldCap.deserializeNBT(newCap.serializeNBT())，
            // 新 cap 这三个字段是 0 → 不显式拷回，玩家一转生整副家产就被清零。
            // （钱是账号资产，不属于"修为"，不该跟着转生重置。）
            newplayerCapability.setBankGold(oldItemCapability.getBankGold());
            newplayerCapability.setBankSilver(oldItemCapability.getBankSilver());
            newplayerCapability.setBankCopper(oldItemCapability.getBankCopper());

            // ==================== 属性点：清空并按总数 10%~30% 折算继承 ====================
            int spentPoints = oldItemCapability.getAllocatedPointsTotal();
            int inherited = AttributePoints.inheritOnZhuansheng(newplayerCapability, oldItemCapability);

            // ==================== 武魂永久基础属性：按 5% 打折保留（绝不保留全部） ====================
            WuhunPermanent.zhuanshengKeep(newplayerCapability, oldItemCapability);

            newplayerCapability.resetGodSystem();
            newplayerCapability.setCurrentTowerFloor(0);
            newplayerCapability.setTowerLastActiveTick(0);
            newplayerCapability.setTowerChallenging(false);
            newplayerCapability.getWuhunSkillsMap().clear();
            newplayerCapability.getWuhunListsname().clear();
            newplayerCapability.getMonsterCapabilityLists().clear();
            newplayerCapability.setInitialized(false);
            newplayerCapability.setHunhuankuaiguan(-1);

            // =====================================================================
            // 以下字段**不写进 NBT**，靠 newCap 覆盖 oldCap 是带不走的，必须直接清 oldCap。
            // （这就是"转生后有些数据没重置"的根因：存档覆盖只覆盖序列化过的东西。）
            // =====================================================================
            // 吟唱状态：清技能引用、槽位、进度，并摘掉 -70% 移速修饰符
            oldItemCapability.stopCasting(player);
            // 每个武魂"选中第几技能槽"（运行时手感状态，不序列化）
            oldItemCapability.clearSelectedSkillSlots();
            // 强制天赋：不清的话转生重新觉醒会被上一次的 roll 结果顶掉
            oldItemCapability.setForcedTalent("");
            // 调试开关（public 字段，同样不序列化）
            oldItemCapability.debugIgnoreTianfu = false;
            oldItemCapability.debugForceSuccess = false;

            // 武魂真身：状态存在 player.persistentData（KL_ZhenshenActive / Effect / Wuhun），
            // 不在存档覆盖范围里 → 不主动关的话，转生后还会挂着真身增益和 20 倍维持费。
            if (ZhenshenState.isActive(player)) {
                ZhenshenState.deactivate(player);
            }
            // 一键释放队列里可能还有没放完的槽位（新的武魂/技能表已经不认识了）
            SkillBatchRelease.cancel(player.getUUID());

            // 魂骨属性快照：魂骨背包是整份带过来的，但 boneOnlyStats 被新存档的空表覆盖成空了，
            // 不重算的话转生后魂骨全部失效（要重新穿一次才恢复）。
            PlayerAttributeCapability.collectBoneAttributes(newplayerCapability);

            // 武魂永久属性的"本次已结算"标记挂在 capability 对象上（WeakHashMap），
            // 不清掉的话转生后第一次按等级结算会被跳过，永久属性不再增长。
            WuhunPermanent.clearSettledFlag(oldItemCapability);
            WuhunPermanent.clearSettledFlag(newplayerCapability);

            // 当前生命值字段跟最大生命对齐（玩家实体血量在下面 setHealth 里设）
            newplayerCapability.setShengming(newplayerCapability.getMaxshengming());

            // 成就：第一次转生 / 第二次转生（次数已经 +1 过了）
            AchievementAPI.onReincarnate(player, newplayerCapability.getZhuanshengshu());

            SynsAPI.synsPlayerAttribute(player);
            player.setHealth(newplayerCapability.getMaxshengming());

            final int finalSpent = spentPoints;
            final int finalInherited = inherited;
            player.sendSystemMessage(Component.literal("§f属性点已重置：§7投入 §e" + finalSpent + " §7点 → §b继承 §e" + finalInherited + " §7点"));
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

    /**
     * 把某个玩家当前的魂环年限列表广播给同维度所有玩家。
     *
     * <p><b>为什么必须广播：</b>客户端的魂环渲染读的是 {@code PWRenderPlayerEvent} 里
     * 按玩家 UUID 缓存的一份快照（{@code entityWuhunCacheMap}），那份缓存只在
     * 「登录 / 换维度 / 手动开关魂环」时刷新。所以**魂环列表一变就得广播一次**，
     * 否则新魂环不会立刻出现 —— 必须手动关开一次魂环才显示出来。</p>
     *
     * @param playAnimation 是否让魂环逐环"显现"。
     *                      吸收完成时传 {@code false}，免得已有的魂环陪着重播一遍出场动画。
     */
    public static void broadcastWuhunRings(ServerPlayer player, boolean playAnimation) {
        if (player == null) {
            return;
        }
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            List<Integer> nianxianList = new ArrayList<>();
            // 与 nianxianList 一一对应：这枚环来自哪个神位（空串 = 普通环，客户端据此选神环贴图）
            List<String> shenciGodList = new ArrayList<>();
            if (capability.getWuhunList() != null) {
                for (MobAttributeCapability wuhun : capability.getWuhunList()) {
                    if (wuhun != null) {
                        nianxianList.add((int) wuhun.getNianxian());
                        shenciGodList.add(wuhun.getShenciGodOrDefault());
                    }
                }
            }
            long startTime = playAnimation ? player.level().getGameTime() : 0L;
            SyncWuhunDataPacket packet = new SyncWuhunDataPacket(
                    player.getUUID(), nianxianList, shenciGodList, capability.isGod(),
                    playAnimation, startTime, capability.getHunhuanOpenAnim());
            for (ServerPlayer target : ((ServerLevel) player.level()).getPlayers(p -> true)) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), packet);
            }
        });
    }
}