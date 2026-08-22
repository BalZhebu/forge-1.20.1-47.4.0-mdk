package com.TovidY.kunluncontinent.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

//基础属性面板

public class CapabilityAttributeBase implements INBTSerializable<CompoundTag> {

/*基础属性
生命          最大生命
物攻          物防
暴击伤害       暴击率
真伤          物穿
抗暴
吸血          生命回复
命中          闪避
       */
    private float shengming;
    private float maxshengming;
    private float gongji;
    private float fangyu;
    private float baojishanghai;
    private float baojilv;
    private float kangbao;
    private float xixue;
    private float mingzhong;
    private float shanbi;
    private float jingshenli;
    private float maxjingshenli;
    private float wuchuan;
    private float shengminghuifu;

    public CapabilityAttributeBase(){
        this.shengming = 20;
        this.maxshengming = 20;
        this.gongji = 1;
        this.fangyu = 1;
        this.baojishanghai = 1;
        this.baojilv = 1;
        this.kangbao = 1;
        this.xixue = 0;
        this.mingzhong = 1;
        this.shanbi = 1;
        this.jingshenli = 20;
        this.maxjingshenli = 20;
        this.wuchuan = 1;
        this.shengminghuifu = 1;
    }

    public CapabilityAttributeBase(float shengming, float maxshengming, float gongji, float wufang, float baojishanghai, float baojilv, float jingshenli, float maxjingshenli, float kangbao, float xixue, float mingzhong, float shanbi,float shengminghuifu,float wuchuan) {
        this.shengming = shengming;
        this.maxshengming = maxshengming;
        this.jingshenli = jingshenli;
        this.maxjingshenli = maxjingshenli;
        this.gongji = gongji;
        this.fangyu = wufang;
        this.baojishanghai = baojishanghai;
        this.baojilv = baojilv;
        this.kangbao = kangbao;
        this.xixue = xixue;
        this.mingzhong = mingzhong;
        this.shanbi = shanbi;
        this.wuchuan = wuchuan;
        this.shengminghuifu = shengminghuifu;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("wugong", gongji);
        nbt.putFloat("wufang", fangyu);
        nbt.putFloat("baojishanghai", baojishanghai);
        nbt.putFloat("baojilv", baojilv);
        nbt.putFloat("kangbao", kangbao);
        nbt.putFloat("xixue", xixue);
        nbt.putFloat("minghzong", mingzhong);
        nbt.putFloat("shanbi", shanbi);
        nbt.putFloat("shengming", shengming);
        nbt.putFloat("maxshengming", maxshengming);
        nbt.putFloat("jingshenli", jingshenli);
        nbt.putFloat("maxjingshenli", maxjingshenli);
        nbt.putFloat("wuchuan", wuchuan);
        nbt.putFloat("shengminghuifu", shengminghuifu);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("wugong")) this.gongji = nbt.getFloat("wugong");
        if (nbt.contains("wufang")) this.fangyu = nbt.getFloat("wufang");
        if (nbt.contains("baojishanghai")) this.baojishanghai = nbt.getFloat("baojishanghai");
        if (nbt.contains("baojilv")) this.baojilv = nbt.getFloat("baojilv");
        if (nbt.contains("kangbao")) this.kangbao = nbt.getFloat("kangbao");
        if (nbt.contains("xixue")) this.xixue = nbt.getFloat("xixue");
        if (nbt.contains("minghzong")) this.mingzhong = nbt.getFloat("minghzong");
        if (nbt.contains("shanbi")) this.shanbi = nbt.getFloat("shanbi");
        if (nbt.contains("shengming")) this.shengming = nbt.getFloat("shengming");
        if (nbt.contains("maxshengming")) this.maxshengming = nbt.getFloat("maxshengming");
        if (nbt.contains("jingshenli")) this.jingshenli = nbt.getFloat("jingshenli");
        if (nbt.contains("maxjingshenli")) this.maxjingshenli = nbt.getFloat("maxjingshenli");
        if (nbt.contains("wuchuan")) this.wuchuan = nbt.getFloat("wuchuan");
        if (nbt.contains("shengminghuifu")) this.shengminghuifu = nbt.getFloat("shengminghuifu");
    }

    public float getShengminghuifu() {
        return shengminghuifu;
    }
    public void setShengminghuifu(float shengminghuifu) {
        this.shengminghuifu = shengminghuifu;
    }

    public float getShengming() {
        return shengming;
    }

    public void setShengming(float shengming) {
        this.shengming = shengming;
    }

    public float getMaxshengming() {
        return maxshengming;
    }

    public void setMaxshengming(float maxshengming) {
        this.maxshengming = maxshengming;
    }

    public float getGongji() { return gongji; }
    public void setGongji(float gongji) { this.gongji = gongji; }
    public void setWugong(float gongji) { this.gongji = gongji; } // 别名，对应API

    public float getWuchuan() { return wuchuan; }
    public void setWuchuan(float wuchuan) { this.wuchuan = wuchuan; }

    public float getFangyu() { return fangyu; }
    public void setfangyu(float fangyu) { this.fangyu = fangyu; }
    public void setWufang(float fangyu) { this.fangyu = fangyu; } // 别名

    public float getBaojishanghai() {
        return baojishanghai;
    }

    public void setBaojishanghai(float baojishanghai) {
        this.baojishanghai = baojishanghai;
    }

    public float getBaojilv() {
        return baojilv;
    }

    public void setBaojilv(float baojilv) {
        this.baojilv = baojilv;
    }

    public float getKangbao() {
        return kangbao;
    }

    public void setKangbao(float kangbao) {
        this.kangbao = kangbao;
    }

    public float getXixue() {
        return xixue;
    }

    public void setXixue(float xixue) {
        this.xixue = xixue;
    }

    public float getMingzhong() {
        return mingzhong;
    }

    public void setMingzhong(float mingzhong) {
        this.mingzhong = mingzhong;
    }

    public float getShanbi() {
        return shanbi;
    }

    public void setShanbi(float shanbi) {
        this.shanbi = shanbi;
    }
}

