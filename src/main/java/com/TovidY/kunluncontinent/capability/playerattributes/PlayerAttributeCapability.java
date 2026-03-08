package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapability;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

    private int castingTick = 0;
    private int requiredCastTick = 0;
    private BaseSkillItem currentCastingSkill = null;

    // 初始化标志，用于判断是否是第一次创建角色
    private boolean initialized = false;

    private Map<String, BaseSkillItem[]> wuhunSkillsMap = new HashMap<>();
    private Map<String, Integer> selectedSkillIndexMap = new HashMap<>();

    private int xiulianTime = 600;
    private boolean usingAll = false;

    private static final String DATA_VERSION_TAG = "DataVersion";
    private static final int CURRENT_DATA_VERSION = 2;

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

    private int zhuanshengshu = 0;

    private final ItemStackHandler hunguInventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
        }
    };

    private final Map<String, Float> boneOnlyStats = new HashMap<>();
    public Map<String, Float> getBoneOnlyStats() { return boneOnlyStats; }


    public PlayerAttributeCapability(){
        super();
        this.hunhuankuaiguan = 0;
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

        // --- 核心修复：保存武魂名称的原始顺序 ---
        ListTag wuhunOrderTag = new ListTag();
        for (String name : wuhunListsname) {
            wuhunOrderTag.add(StringTag.valueOf(name));
        }
        tag.put("WuhunOrderList", wuhunOrderTag);

        // 保存每个武魂的具体魂环数据
        for (Map.Entry<String, List<MobAttributeCapability>> entry : monsterCapabilityLists.entrySet()) {
            String wuhunName = entry.getKey();
            tag.putBoolean("iswuhun" + wuhunName, true);
            int ringIndex = 0;
            for (MobAttributeCapability ringCap : entry.getValue()) {
                tag.put(wuhunName + ringIndex, ringCap.serializeNBT());
                ringIndex++;
            }
        }

        // 保存其他数据
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

        if (nbt.contains("HunguSlots")) {
            hunguInventory.deserializeNBT(nbt.getCompound("HunguSlots"));
        }
        if (nbt.contains("WuhunSkillsData")) {
            CompoundTag allSkillsTag = nbt.getCompound("WuhunSkillsData");
            this.wuhunSkillsMap.clear();
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
                this.wuhunSkillsMap.put(wuhunName, skills);
            }
        }
        this.monsterCapabilityLists.clear();
        this.wuhunListsname.clear();
        if (nbt.contains("WuhunOrderList")) {
            ListTag orderList = nbt.getList("WuhunOrderList", 8);
            for (int i = 0; i < orderList.size(); i++) {
                String name = orderList.getString(i);
                loadWuhunData(nbt, name);
            }
        } else {
            for (String name : wuhunListsnameall) {
                if (nbt.getBoolean("iswuhun" + name)) {
                    loadWuhunData(nbt, name);
                }
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

    public void refreshBoneAttributes(Player player) {
        // 仅仅刷新 Map 缓存，不碰任何 this.gongji 等主字段！
        this.boneOnlyStats.clear();

        for (int i = 0; i < 7; i++) {
            ItemStack stack = hunguInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    List<String> active = attr.getActiveAttributes();
                    // 仅把魂骨属性累加进 Map
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

    // 辅助工具：根据字符串获取属性值
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

    public float getBaojishanghai() { return baojishanghai; }
    public void setBaojishanghai(float baojishanghai) { this.baojishanghai = baojishanghai; }

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

    public void stopCasting() {
        this.currentCastingSkill = null;
        this.castingTick = 0;
        this.requiredCastTick = 0;
    }

    private Map<String, Long> skillCooldowns = new HashMap<>();

    public long getSkillLastUsedTime(String wuhun, int slot) {
        return skillCooldowns.getOrDefault(wuhun + "_" + slot, 0L);
    }

    public void setSkillLastUsedTime(String wuhun, int slot, long time) {
        skillCooldowns.put(wuhun + "_" + slot, time);
    }



    public Map<String, List<MobAttributeCapability>> getMonsterCapabilityLists() {
        return monsterCapabilityLists;
    }

    public List<String> getWuhunListsname() {
        return wuhunListsname;
    }


}