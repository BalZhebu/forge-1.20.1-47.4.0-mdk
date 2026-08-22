package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.capability.CapabilityAttributeBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.concurrent.ThreadLocalRandom;

//各个年限生物的属性生成

public class MobAttributeCapability extends CapabilityAttributeBase implements INBTSerializable<CompoundTag> {
    private long nianxian;
    private boolean shenci;
    private float tempWugongModifier = 0f;

    public MobAttributeCapability() {
        super();
        this.nianxian = 0;
    }

    /**
     * 仅在怪物首次生成/初始化年限时调用
     */

    public void initNianxian(long nianxian) {
        this.nianxian = nianxian;
        updateAttributesByNianxian();
    }

    private void updateAttributesByNianxian() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        float g;
        float l;
        float gMult = 1f;
        float hpMult = 10f;
        float hpBase = 10f;

        if (nianxian < 100) {
            g = 2 + (float) nianxian / 7 + random.nextInt(10);
            l = 5 + (float) nianxian / 20;
        } else if (nianxian < 1000) {
            g = 35 + (float) nianxian / 10 + random.nextInt(50);
            l = 15 + (float) nianxian / 1000 * 10;
            gMult = 2; hpMult = 10; hpBase = 10;
        } else if (nianxian < 10000) {
            g = 150 + (float) nianxian / 20 + random.nextInt(250);
            l = 20 + (float) nianxian / 10000 * 10;
            gMult = 2; hpMult = 25; hpBase = 10;
        } else if (nianxian < 100000) {
            g = 400 + (float) nianxian / 67 + random.nextInt(750);
            l = 40 + (float) nianxian / 100000 * 10;
            gMult = 3; hpMult = 40; hpBase = 10;
        } else if (nianxian < 1000000) {
            g = 888 + (float) nianxian / 133 + random.nextInt(3750);
            l = 50 + (float) nianxian / 1000000 * 10;
            gMult = 5; hpMult = 60; hpBase = 10;
        } else if (nianxian < 10000000) {
            g = 2560 + (float) nianxian / 500 + random.nextInt(15000);
            l = 100 + (float) nianxian / 10000000 * 20;
            gMult = 10; hpMult = 200; hpBase = 150;
        } else if (nianxian < 100000000) {
            g = 6400 + (float) nianxian / 1000 + random.nextInt(100000);
            l = 200 + (float) nianxian / 100000000 * 50;
            gMult = 25; hpMult = 500; hpBase = 400;
        } else {
            g = 15000 + (float) nianxian / 2000 + random.nextInt(500000);
            l = 500 + (float) nianxian / 1000000000 * 100;
            gMult = 60; hpMult = 1200; hpBase = 1000;
        }

        applyGrowth(g, l, gMult, hpMult, hpBase);
    }

    private void applyGrowth(float g, float l, float gMult, float hpMult, float hpBase) {
        this.setWugong(g * gMult);
        this.setWufang(g / 3f);
        this.setMaxshengming(hpMult * g);
        this.setShengming(hpBase * g);
        this.setBaojilv(l);
        this.setBaojishanghai(5f * l);
        this.setKangbao(3f * l);
        this.setXixue(l / 4f);
        this.setMingzhong(l);
        this.setShanbi(l);
        this.setWuchuan(l);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putLong("nianxian", nianxian);
        nbt.putBoolean("shenci", shenci);
        nbt.putFloat("tempWugongModifier", tempWugongModifier);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt); // 注意：父类 CapabilityAttributeBase 必须把计算好的攻击、血量等属性保存进 NBT，这样读档时才不会被重置！
        if (nbt.contains("nianxian")) this.nianxian = nbt.getLong("nianxian");
        if (nbt.contains("shenci")) this.shenci = nbt.getBoolean("shenci");
        if (nbt.contains("tempWugongModifier")) this.tempWugongModifier = nbt.getFloat("tempWugongModifier");
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

    public boolean isShenci() { return shenci; }
    public void setShenci(boolean shenci) { this.shenci = shenci; }

    public long getNianxian() { return nianxian; }
    public void setNianxian(long nianxian) {
        this.nianxian = nianxian;
        updateAttributesByNianxian();
    }
}