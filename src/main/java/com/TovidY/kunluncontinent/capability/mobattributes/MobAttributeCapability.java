package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.capability.CapabilityAttributeBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

//各个年限生物的属性生成

public class MobAttributeCapability extends CapabilityAttributeBase implements INBTSerializable<CompoundTag> {
    private static final Random RANDOM = new Random();
    private long nianxian;
    private boolean shenci;

    private float tempWugongModifier = 0f;

    public MobAttributeCapability() {
        super();
        this.nianxian = 0;
    }

    public void initNianxian(long nianxian) {
        this.nianxian = nianxian;
        updateAttributesByNianxian();
    }

    private void updateAttributesByNianxian() {
        if (nianxian < 100) {
            float g = 2 + (float) nianxian / 10 + RANDOM.nextInt(10);
            float l = 5 + (float) nianxian / 20;
            applyBaseGrowth(g, l);
        } else if (nianxian < 1000) {
            float g = 35 + (float) nianxian / 10 + RANDOM.nextInt(50);
            float l = 15 + (float) nianxian / 1000 * 10;
            applyGrowth(g, l, 2, 10, 10);
        } else if (nianxian < 10000) {
            float g = 150 + (float) nianxian / 20 + RANDOM.nextInt(250);
            float l = 20 + (float) nianxian / 10000 * 10;
            applyGrowth(g, l, 2, 25, 10);
        } else if (nianxian < 100000) {
            float g = 400 + (float) nianxian / 67 + RANDOM.nextInt(750);
            float l = 40 + (float) nianxian / 100000 * 10;
            applyGrowth(g, l, 3, 40, 10);
        } else if (nianxian < 1000000) {
            float g = 888 + (float) nianxian / 133 + RANDOM.nextInt(3750);
            float l = 50 + (float) nianxian / 1000000 * 10;
            applyGrowth(g, l, 5, 60, 10);
        } else if (nianxian < 10000000) {
            float g = 2560 + (float) nianxian / 500 + RANDOM.nextInt(15000);
            float l = 100 + (float) nianxian / 10000000 * 20;
            applyGrowth(g, l, 10, 200, 150);
        } else if (nianxian < 100000000) {
            float g = 6400 + (float) nianxian / 1000 + RANDOM.nextInt(100000);
            float l = 200 + (float) nianxian / 100000000 * 50;
            applyGrowth(g, l, 25, 500, 400);
        } else {
            float g = 15000 + (float) nianxian / 2000 + RANDOM.nextInt(500000);
            float l = 500 + (float) nianxian / 1000000000 * 100;
            applyGrowth(g, l, 60, 1200, 1000);
        }
    }

    private void applyBaseGrowth(float g, float l) {
        this.setWugong(g);
        this.setWufang(g / 3);
        this.setMaxshengming(10 * g);
        this.setShengming(10 * g);
        this.setBaojilv(l);
        this.setBaojishanghai(5 * l);
        this.setKangbao(3 * l);
        this.setXixue(l / 4);
        this.setMingzhong(l);
        this.setShanbi(l);
        this.setWuchuan(l);
    }

    private void applyGrowth(float g, float l, float gMult, float hpMult, float hpBase) {
        this.setWugong(g * gMult);
        this.setWufang(g / 3);
        this.setMaxshengming(hpMult * g);
        this.setShengming(hpBase * g);
        this.setBaojilv(l);
        this.setBaojishanghai(5 * l);
        this.setKangbao(3 * l);
        this.setXixue(l / 4);
        this.setMingzhong(l);
        this.setShanbi(l);
        this.setWuchuan(l);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putLong("nianxian", nianxian);
        nbt.putBoolean("shenci", shenci);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.nianxian = nbt.getLong("nianxian");
        this.shenci = nbt.getBoolean("shenci");
    }

    public void addTempWugong(float value) {
        this.tempWugongModifier += value;
    }

    public void removeTempWugong(float value) {
        this.tempWugongModifier -= value;
    }

    @Override
    public float getGongji() {
        return super.getGongji() + this.tempWugongModifier;
    }

    public boolean isShenci() {
        return shenci;
    }

    public void setShenci(boolean shenci) {
        this.shenci = shenci;
    }


    public long getNianxian() { return nianxian; }

    public void setNianxian(long nianxian) { this.nianxian = nianxian; }


}
