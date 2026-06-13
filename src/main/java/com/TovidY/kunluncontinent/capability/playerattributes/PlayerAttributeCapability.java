package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapability;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.godclass.interfac.GodTask;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

//玩家属性

public class PlayerAttributeCapability implements ICapabilitySerializable<CompoundTag> {

    public static final UUID CASTING_SLOWDOWN_ID = UUID.fromString("7f369f4a-8e2b-4f9e-a0e4-522f1c305c6d");

    private int castingTick = 0;
    private int requiredCastTick = 0;
    private BaseSkillItem currentCastingSkill = null;

    private String godName = "";        // 当前进行的神位ID (如 "sea_god")，为空代表无神考
    private int currentStage = 1;       // 当前神考阶段 (1-9)
    private int taskProgress = 0;       // 当前任务的数值进度 (如杀怪数、提交数)
    private boolean isGod = false;      // 是否已完成封神

    private String[] assignedExams = new String[10]; // 存储1-9考抽中的任务描述
    private String[] assignedTargets = new String[10]; // 存储任务目标ID
    private int[] assignedCounts = new int[10]; // 存储需求数量
    private String[] assignedTypes = new String[10]; // 存储任务类型

    private boolean initialized = false;

    private int uiOffsetY = 0;

    // ==================== 【昆仑大陆·爬塔系统字段】 ====================
    private int currentTowerFloor = 0;      // 当前爬塔层数 (0代表第一层)
    private long towerLastActiveTick = 0;   // 爬塔防挂机最后活动Tick
    private boolean isTowerChallenging = false; // 是否处于爬塔挑战中

    private Map<String, BaseSkillItem[]> wuhunSkillsMap = new HashMap<>();
    private Map<String, Integer> selectedSkillIndexMap = new HashMap<>();

    private int xiulianTime = 600;
    private boolean usingAll = false;

    private Map<String, List<MobAttributeCapability>> monsterCapabilityLists = new HashMap<>();
    private List<String> wuhunListsname = new ArrayList<>();
    private int hunhuankuaiguan;

    private float shengming = 20.0f;  // 当前生命值，默认20.0
    private float maxshengming = 20.0f;  // 最大生命值，默认20.0
    private float jingshenli = 20.0f;
    private float maxjingshenli = 20.0f;
    private float mingzhong = 1.0f;
    private float fangyu = 1.0f;  // 防御力，默认0.0，减少所受伤害
    private float gongji = 1.0f;  // 攻击力，默认1.0，影响攻击伤害
    private float baojilv = 5.0f;  // 暴击率，默认5.0%，提高暴击几率
    private float baojishanghai = 150.0f;  // 暴击伤害，默认150.0%，暴击时的伤害加成
    private float xixue = 1.0f;  // 吸血，默认0.0%，暴击或攻击时回复生命
    private float shanbi = 1.0f;  // 闪避率，默认0.0%，闪避敌人攻击的几率
    private float kangbao = 1.0f;  // 抗暴，默认0.0%，减少被敌人暴击的几率
    private float jingyan = 0;  // 当前经验值，默认0
    private float wuchuan = 1;
    private int dengji = 0;  // 玩家等级，默认0
    private float maxjingyan = 20;  // 最大经验值，默认20，达到该经验后升级
    private float shengminghuifu = 1.0f;
    private float tupochenggonglv = 99.0f;  // 突破成功率，默认99%

    private String forcedTalent = "";
    private int xiantianTalent = 0;

    private int damageDisplayMode = 0;
    private long configFlags = 0xFFL;

    private int zhuanshengshu = 0;

    private final ItemStackHandler hunguInventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
        }
    };

    public boolean debugIgnoreTianfu = false;
    public boolean debugForceSuccess = false;

    private final Map<String, Float> boneOnlyStats = new HashMap<>();
    public Map<String, Float> getBoneOnlyStats() { return boneOnlyStats; }


    public PlayerAttributeCapability(){
        super();
        this.hunhuankuaiguan = 0;

        this.assignedExams = new String[10];
        this.assignedTargets = new String[10];
        this.assignedCounts = new int[10];
        this.assignedTypes = new String[10];
        this.godName = ""; // 默认为空
    }

    public PlayerAttributeCapability(int hunhuankuaiguan) {
        this.hunhuankuaiguan = hunhuankuaiguan;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Initialized", initialized);
        tag.putFloat("Shengming", shengming);
        tag.putFloat("MaxShengming", maxshengming);
        tag.putFloat("Jingshenli", jingshenli);
        tag.putFloat("Maxjingshenli", maxjingshenli);
        tag.putFloat("Mingzhong", mingzhong);
        tag.putFloat("Fangyu", fangyu);
        tag.putFloat("Gongji", gongji);
        tag.putFloat("Baojilv", baojilv);
        tag.putFloat("Baojishanghai", baojishanghai);
        tag.putFloat("Xixue", xixue);
        tag.putFloat("Shanbi", shanbi);
        tag.putFloat("Kangbao", kangbao);
        tag.putFloat("Jingyan", jingyan);
        tag.putFloat("Wuchuan", wuchuan);
        tag.putInt("Dengji", dengji);
        tag.putFloat("MaxJingyan", maxjingyan);
        tag.putFloat("TupoChenggonglv", tupochenggonglv);
        tag.putFloat("ShengmingHuifu", shengminghuifu);
        tag.putInt("xiulianTime", this.xiulianTime);
        tag.putBoolean("usingAll", this.usingAll);
        tag.put("HunguSlots", hunguInventory.serializeNBT());
        tag.putInt("Zhuanshengshu", zhuanshengshu);
        tag.putInt("Hunhuankuaiguan", hunhuankuaiguan);

        tag.putInt("DamageDisplayMode", damageDisplayMode);
        tag.putLong("configFlags", configFlags);

        tag.putInt("UiOffsetY", this.uiOffsetY);

        // 保存神祇系统数据
        tag.putString("GodName", godName);
        tag.putInt("CurrentGodStage", currentStage);
        tag.putInt("GodTaskProgress", taskProgress);
        tag.putBoolean("IsGod", isGod);

        tag.putInt("XiantianTalent", this.xiantianTalent);

        // 保存爬塔系统数据
        tag.putInt("CurrentTowerFloor", currentTowerFloor);
        tag.putLong("TowerLastActiveTick", towerLastActiveTick);
        tag.putBoolean("IsTowerChallenging", isTowerChallenging);

        // 在 serializeNBT 中增加：
        ListTag taskTag = new ListTag();
        for (int i = 1; i <= 9; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("desc", assignedExams[i] != null ? assignedExams[i] : "");
            entry.putString("target", assignedTargets[i] != null ? assignedTargets[i] : "");
            entry.putInt("count", assignedCounts[i]);
            entry.putString("type", assignedTypes[i] != null ? assignedTypes[i] : "");
            taskTag.add(entry);
        }
        tag.put("GodTasks", taskTag);

        ListTag wuhunOrderTag = new ListTag();
        for (String name : wuhunListsname) {
            wuhunOrderTag.add(StringTag.valueOf(name));
        }
        tag.put("WuhunOrderList", wuhunOrderTag);

        for (Map.Entry<String, List<MobAttributeCapability>> entry : monsterCapabilityLists.entrySet()) {
            String wuhunName = entry.getKey();
            tag.putBoolean("iswuhun" + wuhunName, true);
            int ringIndex = 0;
            for (MobAttributeCapability ringCap : entry.getValue()) {
                tag.put(wuhunName + ringIndex, ringCap.serializeNBT());
                ringIndex++;
            }
        }
        CompoundTag boneTag = new CompoundTag();
        for (Map.Entry<String, Float> entry : boneOnlyStats.entrySet()) {
            boneTag.putFloat(entry.getKey(), entry.getValue());
        }
        tag.put("BoneOnlyStats", boneTag);

        CompoundTag allSkillsTag = new CompoundTag();
        for (Map.Entry<String, BaseSkillItem[]> entry : wuhunSkillsMap.entrySet()) {
            CompoundTag singleWuhunTag = new CompoundTag();
            BaseSkillItem[] skills = entry.getValue();
            for (int i = 0; i < 9; i++) {
                if (skills[i] != null) {
                    singleWuhunTag.putString("Skill_" + i, ForgeRegistries.ITEMS.getKey(skills[i]).toString());
                }
            }
            allSkillsTag.put(entry.getKey(), singleWuhunTag);
        }
        tag.put("WuhunSkillsData", allSkillsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.initialized = nbt.getBoolean("Initialized");
        this.shengming = nbt.getFloat("Shengming");
        this.maxshengming = nbt.getFloat("MaxShengming");
        this.jingshenli = nbt.getFloat("Jingshenli");
        this.maxjingshenli = nbt.getFloat("Maxjingshenli");
        this.mingzhong = nbt.getFloat("Mingzhong");
        this.fangyu = nbt.getFloat("Fangyu");
        this.gongji = nbt.getFloat("Gongji");
        this.baojilv = nbt.getFloat("Baojilv");
        this.baojishanghai = nbt.getFloat("Baojishanghai");
        this.xixue = nbt.getFloat("Xixue");
        this.shanbi = nbt.getFloat("Shanbi");
        this.wuchuan = nbt.getFloat("Wuchuan");
        this.kangbao = nbt.getFloat("Kangbao");
        this.jingyan = nbt.getFloat("Jingyan");
        this.dengji = nbt.getInt("Dengji");
        this.maxjingyan = nbt.getFloat("MaxJingyan");
        this.tupochenggonglv = nbt.getFloat("TupoChenggonglv");
        this.shengminghuifu = nbt.getFloat("ShengmingHuifu");
        this.zhuanshengshu = nbt.getInt("Zhuanshengshu");
        this.xiulianTime = nbt.getInt("xiulianTime");
        this.usingAll = nbt.getBoolean("usingAll");
        this.hunhuankuaiguan = nbt.getInt("Hunhuankuaiguan");

        this.damageDisplayMode = nbt.getInt("DamageDisplayMode");

        if (nbt.contains("UiOffsetY")) {
            this.uiOffsetY = nbt.getInt("UiOffsetY");
        }

        if (nbt.contains("configFlags")) {
            this.configFlags = nbt.getLong("configFlags");
        } else {
            this.configFlags = 0xFFL;
        }

        // 读取神祇系统数据
        this.godName = nbt.getString("GodName");
        this.currentStage = nbt.getInt("CurrentGodStage");
        this.taskProgress = nbt.getInt("GodTaskProgress");
        this.isGod = nbt.getBoolean("IsGod");

        if (nbt.contains("XiantianTalent")) {
            this.xiantianTalent = nbt.getInt("XiantianTalent");
        }

        // 读取爬塔系统数据
        this.currentTowerFloor = nbt.getInt("CurrentTowerFloor");
        this.towerLastActiveTick = nbt.getLong("TowerLastActiveTick");
        this.isTowerChallenging = nbt.getBoolean("IsTowerChallenging");

        // 在 deserializeNBT 中增加：
        if (nbt.contains("GodTasks")) {
            ListTag taskTag = nbt.getList("GodTasks", 10);
            for (int i = 0; i < taskTag.size() && i < 9; i++) {
                CompoundTag entry = taskTag.getCompound(i);
                assignedExams[i+1] = entry.getString("desc");
                assignedTargets[i+1] = entry.getString("target");
                assignedCounts[i+1] = entry.getInt("count");
                assignedTypes[i+1] = entry.getString("type");
            }
        }

        if (nbt.contains("HunguSlots")) {
            hunguInventory.deserializeNBT(nbt.getCompound("HunguSlots"));
        }

        if (nbt.contains("WuhunOrderList") || nbt.contains("iswuhun" + wuhunListsnameall.get(0))) {
            List<String> tempNames = new ArrayList<>();
            Map<String, List<MobAttributeCapability>> tempMonsters = new HashMap<>();
            if (nbt.contains("WuhunOrderList")) {
                ListTag orderList = nbt.getList("WuhunOrderList", 8);
                for (int i = 0; i < orderList.size(); i++) {
                    String name = orderList.getString(i);
                    tempNames.add(name);
                }
            }
        }
        if (nbt.contains("WuhunSkillsData")) {
            CompoundTag allSkillsTag = nbt.getCompound("WuhunSkillsData");
            Map<String, BaseSkillItem[]> tempSkillsMap = new HashMap<>(); // 临时 Map
            for (String wuhunName : allSkillsTag.getAllKeys()) {
                CompoundTag singleWuhunTag = allSkillsTag.getCompound(wuhunName);
                BaseSkillItem[] skills = new BaseSkillItem[9];
                for (int i = 0; i < 9; i++) {
                    if (singleWuhunTag.contains("Skill_" + i)) {
                        String registryName = singleWuhunTag.getString("Skill_" + i);
                        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(registryName));
                        if (item instanceof BaseSkillItem baseSkill) {
                            skills[i] = baseSkill;
                        }
                    }
                }
                tempSkillsMap.put(wuhunName, skills);
            }
            if (!tempSkillsMap.isEmpty()) {
                this.wuhunSkillsMap = tempSkillsMap;
            }
        }
        this.monsterCapabilityLists.clear();
        this.wuhunListsname.clear();
        if (nbt.contains("WuhunOrderList")) {
            ListTag orderList = nbt.getList("WuhunOrderList", 8);
            for (int i = 0; i < orderList.size(); i++) {
                loadWuhunData(nbt, orderList.getString(i));
            }
        }

        if (nbt.contains("BoneOnlyStats")) {
            CompoundTag boneTag = nbt.getCompound("BoneOnlyStats");
            this.boneOnlyStats.clear();
            for (String key : boneTag.getAllKeys()) {
                this.boneOnlyStats.put(key, boneTag.getFloat(key));
            }
        }
    }

    private void loadWuhunData(CompoundTag nbt, String name) {
        ArrayList<MobAttributeCapability> list = new ArrayList<>();
        int index = 0;
        while (nbt.contains(name + index)) {
            MobAttributeCapability ringCap = new MobAttributeCapability();
            ringCap.deserializeNBT(nbt.getCompound(name + index));
            list.add(ringCap);
            index++;
        }
        this.monsterCapabilityLists.put(name, list);
        if (!this.wuhunListsname.contains(name)) {
            this.wuhunListsname.add(name);
        }
    }

    public void setWuhunSkill(String wuhunName, int slot, BaseSkillItem skill) {
        BaseSkillItem[] skills = wuhunSkillsMap.computeIfAbsent(wuhunName, k -> new BaseSkillItem[9]);
        if (slot >= 0 && slot < 9) {
            skills[slot] = skill;
        }
    }

    public String getGodName() { return godName; }

    public void startGodExam(String godId) {
        this.godName = godId;
        this.currentStage = 1;
        this.taskProgress = 0;
        this.isGod = false;
    }

    public int getCurrentStage() { return currentStage; }

    public void nextGodStage() {
        if (this.currentStage < 9) {
            this.currentStage++;
            this.taskProgress = 0;
        } else {
            this.isGod = true;
        }
    }

    // ==================== 【昆仑大陆·爬塔系统 API】 ====================
    public int getCurrentTowerFloor() { return this.currentTowerFloor; }
    public void setCurrentTowerFloor(int floor) { this.currentTowerFloor = Math.max(0, floor); }

    public long getTowerLastActiveTick() { return this.towerLastActiveTick; }
    public void setTowerLastActiveTick(long tick) { this.towerLastActiveTick = tick; }

    public boolean isTowerChallenging() { return this.isTowerChallenging; }
    public void setTowerChallenging(boolean challenging) { this.isTowerChallenging = challenging; }

    public int getGodTaskProgress() { return taskProgress; }
    public void setGodTaskProgress(int progress) { this.taskProgress = progress; }
    public void addGodTaskProgress(int amount) { this.taskProgress += amount; }

    public boolean isGod() { return isGod; }

    public float getGodAttributeValue(String attrKey) {
        return switch (attrKey) {
            case "gongji" -> this.gongji;
            case "fangyu" -> this.fangyu;
            case "maxshengming" -> this.maxshengming;
            case "jingshenli" -> this.jingshenli;
            case "baojilv" -> this.baojilv;
            case "shanbi" -> this.shanbi;
            default -> 0f;
        };
    }

    public boolean isConfigOpen(int index) {
        return ((configFlags >> index) & 1) == 1;
    }

    public void toggleConfig(int index) {
        this.configFlags ^= (1L << index);
    }

    public void refreshBoneAttributes(Player player) {
        this.boneOnlyStats.clear();
        for (int i = 0; i < 7; i++) {
            ItemStack stack = hunguInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    List<String> active = attr.getActiveAttributes();
                    for (String key : active) {
                        float val = getAttrValueByKey(attr, key);
                        if (val > 0) {
                            boneOnlyStats.put(key, boneOnlyStats.getOrDefault(key, 0f) + val);
                        }
                    }
                });
            }
        }
        SynsAPI.synsPlayerAttribute(player);
    }


    // 当玩家开启神考时调用（比如右键祭坛或神的瞥视）
    public void initializeGodExam(Player player, String godId) {
        GodInfo info = GodRegistry.GODS.get(godId);
        if (info == null) return;
        this.godName = godId;
        this.currentStage = 1;
        this.taskProgress = 0;
        RandomSource random = player.level().random;
        for (int i = 1; i <= 9; i++) {
            GodTask task = info.getRandomTask(i, random);
            if (task != null) {
                this.assignedExams[i] = task.description;
                this.assignedTargets[i] = task.targetId;
                this.assignedCounts[i] = task.requiredCount;
                this.assignedTypes[i] = task.type.name();
            }
        }
    }

    public void checkTaskCompletion(Player player) {
        GodInfo info = GodRegistry.GODS.get(this.godName);
        if (info == null) return;
        info.executeRewards(this.currentStage, player);
        this.nextGodStage();

        if (this.isGod()) {
            player.sendSystemMessage(Component.literal("§6恭喜你，成就" + info.name + "之位！"));
        }
    }

    public String[] getAssignedExams() {
        return this.assignedExams;
    }

    public String[] getAssignedTargets() {
        return this.assignedTargets;
    }

    public int[] getAssignedCounts() {
        return this.assignedCounts;
    }

    public String[] getAssignedTypes() {
        return this.assignedTypes;
    }

    public boolean hasActiveTask() {
        return !godName.isEmpty() && currentStage >= 1 && currentStage <= 9;
    }

    private float getAttrValueByKey(ItemAttributeCapability attr, String key) {
        return switch (key) {
            case "gongji" -> attr.getGongji();
            case "fangyu" -> attr.getFangyu();
            case "maxshengming" -> attr.getMaxshengming();
            case "baojilv" -> attr.getBaojilv();
            case "baojishanghai" -> attr.getBaojishanghai();
            case "xixue" -> attr.getXixue();
            case "shanbi" -> attr.getShanbi();
            case "mingzhong" -> attr.getMingzhong();
            case "wuchuan" -> attr.getWuchuan();
            case "kangbao" -> attr.getKangbao();
            case "shengminghuifu" -> attr.getShengminghuifu();
            default -> 0f;
        };
    }

    public ItemStackHandler getHunguInventory() {
        return hunguInventory;
    }


    public void setForcedTalent(String talent) { this.forcedTalent = talent; }
    public String getForcedTalent() { return this.forcedTalent; }

    public void setXiantianTalent(int talent) { this.xiantianTalent = talent; }
    public int getXiantianTalent() { return this.xiantianTalent; }

    public int getXiulianTime() {
        return this.xiulianTime;
    }
    public void setXiulianTime(int time) {
        this.xiulianTime = Math.max(0, Math.min(600, time));
    }

    public boolean isUsingAll() {
        return this.usingAll;
    }
    public void setUsingAll(boolean usingAll) {
        this.usingAll = usingAll;
    }

    public int getHunhuankuaiguan() {
        return hunhuankuaiguan;
    }

    public void setHunhuankuaiguan(int hunhuankuaiguan) {
        this.hunhuankuaiguan = hunhuankuaiguan;
    }

    public List<MobAttributeCapability> getWuhunList() {
        if(wuhunListsname.size()-1<hunhuankuaiguan||hunhuankuaiguan<0)return null;
        return monsterCapabilityLists.get(wuhunListsname.get(hunhuankuaiguan));
    }

    public int getDamageDisplayMode() { return damageDisplayMode; }
    public void setDamageDisplayMode(int mode) { this.damageDisplayMode = mode; }

    public int getZhuanshengshu() {return zhuanshengshu;}
    public void setZhuanshengshu(int zhuanshengshu) {
        this.zhuanshengshu = zhuanshengshu;
    }

    public float getShengming() { return shengming; }
    public void setShengming(float shengming) { this.shengming = shengming; }

    public float getJingshenli() {
        return jingshenli;
    }

    public void setJingshenli(float jingshenli) { this.jingshenli = jingshenli; }

    public float getMaxjingshenli() { return maxjingshenli; }
    public void setMaxjingshenli(float maxjingshenli) { this.maxjingshenli = maxjingshenli; }

    public float getMingzhong() { return mingzhong; }
    public void setMingzhong(float mingzhong) { this.mingzhong = mingzhong; }

    public float getShengmingHuifu() { return shengminghuifu; }
    public void setShengmingHuifu(float shengminghuifu) { this.shengminghuifu = shengminghuifu; }

    public float getMaxshengming() { return maxshengming; }
    public void setMaxshengming(float maxshengming) { this.maxshengming = maxshengming; }

    public float getFangyu() { return fangyu; }
    public void setFangyu(float fangyu) { this.fangyu = fangyu; }

    public float getWuchuan() { return wuchuan; }
    public void setWuchuan(float wuchuan) { this.wuchuan = wuchuan; }

    public float getGongji() { return gongji; }
    public void setGongji(float gongji) { this.gongji = gongji; }

    public float getBaojilv() { return baojilv; }
    public void setBaojilv(float baojilv) { this.baojilv = baojilv; }

    public long getConfigFlags() { return configFlags; }
    public void setConfigFlags(long flags) { this.configFlags = flags; }

    public float getBaojishanghai() { return baojishanghai; }

    public void setBaojishanghai(float baojishanghai) {
        this.baojishanghai = baojishanghai;
    }

    public float getXixue() { return xixue; }
    public void setXixue(float xixue) { this.xixue = xixue; }

    public float getShanbi() { return shanbi; }
    public void setShanbi(float shanbi) { this.shanbi = shanbi; }

    public float getKangbao() { return kangbao; }
    public void setKangbao(float kangbao) { this.kangbao = kangbao; }

    public float getJingyan() { return jingyan; }
    public void setJingyan(float jingyan) { this.jingyan = jingyan; }

    public int getDengji() { return dengji; }
    public void setDengji(int dengji) { this.dengji = dengji; }

    public float getMaxjingyan() { return maxjingyan; }
    public void setMaxjingyan(float maxjingyan) { this.maxjingyan = maxjingyan; }
    
    public float getTupochenggonglv() { return tupochenggonglv; }
    public void setTupochenggonglv(float tupochenggonglv) { this.tupochenggonglv = tupochenggonglv; }
    
    public boolean isInitialized() { return initialized; }
    public void setInitialized(boolean initialized) { this.initialized = initialized; }

    public int getUiOffsetY() {
        return this.uiOffsetY;
    }

    public void setUiOffsetY(int value) {
        this.uiOffsetY = value;
    }

    public void startCasting(BaseSkillItem skill, int time) {
        this.currentCastingSkill = skill;
        this.requiredCastTick = time;
        this.castingTick = 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == PlayerAttributeCapabilityProvider.CAPABILITY) {
            return LazyOptional.of(() -> (T) this);
        }
        return LazyOptional.empty();
    }

    public Map<String, BaseSkillItem[]> getWuhunSkillsMap() {
        return wuhunSkillsMap;
    }

    public int getSelectedSkillSlot() {
        String currentWuhun = getWuhunName();
        if (currentWuhun == null) return 0;
        return selectedSkillIndexMap.getOrDefault(currentWuhun, 0);
    }

    public void setSelectedSkillSlot(int slot) {
        String currentWuhun = getWuhunName();
        if (currentWuhun != null && slot >= 0 && slot < 9) {
            selectedSkillIndexMap.put(currentWuhun, slot);
        }
    }

    public static ArrayList<String> wuhunListsnameall= new ArrayList<>();
    static {
        wuhunListsnameall.add(Wuhunname.pohunqiang);
        wuhunListsnameall.add(Wuhunname.bahuangji);
        wuhunListsnameall.add(Wuhunname.panshijuyuan);
        wuhunListsnameall.add(Wuhunname.liejinhu);

        Collections.sort(wuhunListsnameall);
    }

    public void resetGodSystem() {
        this.godName = "";           // 移除神位ID
        this.currentStage = 1;       // 回到第一考
        this.taskProgress = 0;       // 进度清零
        this.isGod = false;          // 剥夺神位状态
        this.assignedExams = new String[10];
        this.assignedTargets = new String[10];
        this.assignedCounts = new int[10];
        this.assignedTypes = new String[10];
    }

    public String getWuhunName() {
        if(wuhunListsname.size()-1<hunhuankuaiguan||hunhuankuaiguan<0)return null;
        return wuhunListsname.get(hunhuankuaiguan);
    }

    public BaseSkillItem getCurrentCastingSkill() {
        return this.currentCastingSkill;
    }

    public int getCastingTick() {
        return this.castingTick;
    }

    public void setCastingTick(int tick) {
        this.castingTick = tick;
    }

    public int getRequiredCastTick() {
        return this.requiredCastTick;
    }

    public void stopCasting(Player player) {
        this.currentCastingSkill = null;
        this.castingTick = 0;
        this.requiredCastTick = 0;

        removeCastingSlowdown(player);
    }

    public void removeCastingSlowdown(Player player) {
        if (player != null) {
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.hasModifier(CASTING_SLOWDOWN_MODIFIER)) {
                speedAttr.removeModifier(CASTING_SLOWDOWN_ID);
            }
        }
    }

    public boolean isBrandNewConfig() {
        return this.gongji == 1.0f && this.maxshengming == 20.0f && this.dengji == 0;
    }

    public void initDefaultAttributes() {
        this.shengming = 20.0f;
        this.maxshengming = 20.0f;
        this.jingshenli = 20.0f;
        this.maxjingshenli = 20.0f;
        this.mingzhong = 1.0f;
        this.fangyu = 1.0f;
        this.gongji = 1.0f;
        this.baojilv = 5.0f;
        this.baojishanghai = 150.0f;
        this.xixue = 1.0f;
        this.shanbi = 1.0f;
        this.kangbao = 1.0f;
        this.jingyan = 0.0f;
        this.dengji = 0;
        this.maxjingyan = 20.0f;
        this.shengminghuifu = 1.0f;
        this.wuchuan = 1.0f;
        this.xiulianTime = 600;
    }

    private Map<String, Long> skillCooldowns = new HashMap<>();

    public long getSkillLastUsedTime(String wuhun, int slot) {
        return skillCooldowns.getOrDefault(wuhun + "_" + slot, 0L);
    }

    public void setSkillLastUsedTime(String wuhun, int slot, long time) {
        skillCooldowns.put(wuhun + "_" + slot, time);
    }

    public static final AttributeModifier CASTING_SLOWDOWN_MODIFIER = new AttributeModifier(CASTING_SLOWDOWN_ID, "Casting skill slowdown", -0.7, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public Map<String, List<MobAttributeCapability>> getMonsterCapabilityLists() {
        return monsterCapabilityLists;
    }

    public List<String> getWuhunListsname() {
        return wuhunListsname;
    }


}