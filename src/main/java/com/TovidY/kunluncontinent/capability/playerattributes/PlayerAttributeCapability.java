package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

public class PlayerAttributeCapability implements ICapabilitySerializable<CompoundTag> {

    // 初始化标志，用于判断是否是第一次创建角色
    private boolean initialized = false;

    private static final String DATA_VERSION_TAG = "DataVersion";
    private static final int CURRENT_DATA_VERSION = 2;

    private Map<String, List<MobAttributeCapability>> monsterCapabilityLists = new HashMap<>();
    private List<String> wuhunListsname = new ArrayList<>();
    private int hunhuankuaiguan;

    // 玩家属性字段
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
    // Getter 和 Setter 方法


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
        tag.putBoolean("Initialized", initialized);  // 保存初始化标志
        tag.putFloat("Shengming", shengming);  // 当前生命值
        tag.putFloat("MaxShengming", maxshengming);  // 最大生命值
        tag.putFloat("Jingshenli",jingshenli);
        tag.putFloat("Maxjingshenli",maxjingshenli);
        tag.putFloat("Mingzhong",mingzhong);
        tag.putFloat("Fangyu", fangyu);  // 防御力
        tag.putFloat("Gongji", gongji);  // 攻击力
        tag.putFloat("Baojilv", baojilv);  // 暴击率
        tag.putFloat("Baojishanghai", baojishanghai);  // 暴击伤害
        tag.putFloat("Xixue", xixue);  // 吸血
        tag.putFloat("Shanbi", shanbi);  // 闪避率
        tag.putFloat("Kangbao", kangbao);  // 抗暴
        tag.putFloat("Jingyan", jingyan);  // 当前经验值
        tag.putFloat("Wuchuan", wuchuan);
        tag.putInt("Dengji", dengji);  // 玩家等级
        tag.putFloat("MaxJingyan", maxjingyan);  // 最大经验值
        tag.putFloat("TupoChenggonglv", tupochenggonglv);  // 突破成功率
        tag.putFloat("ShengmingHuifu", shengminghuifu);

        tag.putInt("Hunhuankuaiguan", hunhuankuaiguan);

        for (Map.Entry<String, List<MobAttributeCapability>> stringListEntry : monsterCapabilityLists.entrySet()) {
            tag.putBoolean("iswuhun"+stringListEntry.getKey(),true);
            int nameindex = 0;
            for (MobAttributeCapability monsterAttributeCapability : stringListEntry.getValue()) {
                CompoundTag compoundTag = monsterAttributeCapability.serializeNBT();
                tag.put(stringListEntry.getKey()+nameindex,compoundTag);
                nameindex++;
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

        int dataVersion = nbt.contains(DATA_VERSION_TAG) ? nbt.getInt(DATA_VERSION_TAG) : 1;

        this.initialized = nbt.getBoolean("Initialized");  // 读取初始化标志
        this.shengming = nbt.getFloat("Shengming");  // 当前生命值
        this.maxshengming = nbt.getFloat("MaxShengming");  // 最大生命值
        this.jingshenli = nbt.getFloat("Jingshenli");
        this.maxjingshenli = nbt.getFloat("Maxjingshenli");  // 修复拼写错误：shenli
        this.mingzhong = nbt.getFloat("Mingzhong");
        this.fangyu = nbt.getFloat("Fangyu");  // 防御力
        this.gongji = nbt.getFloat("Gongji");  // 攻击力
        this.baojilv = nbt.getFloat("Baojilv");  // 暴击率
        this.baojishanghai = nbt.getFloat("Baojishanghai");  // 暴击伤害
        this.xixue = nbt.getFloat("Xixue");  // 吸血
        this.shanbi = nbt.getFloat("Shanbi");  // 闪避率
        this.wuchuan = nbt.getFloat("Wuchuan");
        this.kangbao = nbt.getFloat("Kangbao");  // 抗暴
        this.jingyan = nbt.getFloat("Jingyan");  // 当前经验值
        this.dengji = nbt.getInt("Dengji");  // 玩家等级
        this.maxjingyan = nbt.getFloat("MaxJingyan");  // 最大经验值
        this.tupochenggonglv = nbt.getFloat("TupoChenggonglv");  // 突破成功率
        this.shengminghuifu = nbt.getFloat("ShengmingHuifu");

        this.hunhuankuaiguan = nbt.getInt("Hunhuankuaiguan");

        for (String s : wuhunListsnameall) {
            int nameindex = 0;
            Tag tag = nbt.get(s + nameindex);
            ArrayList<MobAttributeCapability> list = new ArrayList<>();
            while (tag != null){
                MobAttributeCapability monsterAttributeCapability = new MobAttributeCapability();
                monsterAttributeCapability.deserializeNBT((CompoundTag) tag);
                list.add(monsterAttributeCapability);
                nameindex++;
                tag = nbt.get(s + nameindex);
            }
            if(nbt.getBoolean("iswuhun"+s)){
                this.monsterCapabilityLists.put(s,list);

                this.wuhunListsname.add(s);
            }
        }
    }

    public void setForcedTalent(String talent) { this.forcedTalent = talent; }
    public String getForcedTalent() { return this.forcedTalent; }

    public void setXiantianTalent(int talent) { this.xiantianTalent = talent; }
    public int getXiantianTalent() { return this.xiantianTalent; }


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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        // 检查传入的 Capability 是否是我们的玩家属性 Capability
        if (cap == PlayerAttributeCapabilityProvider.CAPABILITY) {
            return LazyOptional.of(() -> (T) this); // 返回该实例本身作为能力提供者
        }
        return LazyOptional.empty(); // 如果不是我们期望的能力，返回空的 LazyOptional
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

    public Map<String, List<MobAttributeCapability>> getMonsterCapabilityLists() {
        return monsterCapabilityLists;
    }

    public List<String> getWuhunListsname() {
        return wuhunListsname;
    }



}