package com.TovidY.kunluncontinent.capability.itemattribute;

import com.TovidY.kunluncontinent.capability.CapabilityAttributeBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

import static com.TovidY.kunluncontinent.KlMain.random;

public class ItemAttributeCapability extends CapabilityAttributeBase implements INBTSerializable<CompoundTag> {
    private int nianxian;
    private String sourceName = ""; // 记录来源生物
    private List<String> activeAttributes = new ArrayList<>();

    public ItemAttributeCapability() {
        super();
    }

    public void toUpdateNianxian(int nianxian) {
        this.nianxian = nianxian;
        float g, l;
        if (nianxian < 100) {
            g = (10 + nianxian / 5.0f + random.nextInt(10)) * 0.1f;
            l = 1 + nianxian / 100f;
        } else if (nianxian < 1000) {
            g = (50 + nianxian / 10.0f + random.nextInt(50)) * 0.1f;
            l = 2 + nianxian / 1000f;
        } else if (nianxian < 10000) {
            g = (250 + nianxian / 20.0f + random.nextInt(250)) * 0.1f;
            l = 3 + nianxian / 10000f;
        } else if (nianxian < 100000) {
            g = (750 + nianxian / 67.0f + random.nextInt(750)) * 0.1f;
            l = 4 + nianxian / 100000f;
        } else if (nianxian < 1000000) {
            g = (3750 + nianxian / 133.0f + random.nextInt(3750)) * 0.1f;
            l = 5 + nianxian / 1000000f;
        } else {
            // 十万年以上
            g = (7750 + nianxian / 700.0f + random.nextInt(17750)) * 0.1f;
            l = 8 + nianxian / 10000000f;
        }

        // 2. 统一赋值逻辑 (避免重复写几百行)
        applyAttributes(g, l);
    }

    private void applyAttributes(float g, float l) {
        super.setWugong(g);
        super.setWufang(g / 3.0f);
        super.setWuchuan(g / 3.0f);
        super.setMaxshengming(10 * g);
        super.setShengming(10 * g);
        super.setBaojilv(l);
        super.setBaojishanghai(5 * l);
        super.setKangbao(3 * l);
        super.setXixue(l / 4.0f);
        super.setMingzhong(l);
        super.setShanbi(l);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT() != null ? super.serializeNBT() : new CompoundTag();
        nbt.putInt("nianxian", nianxian);
        nbt.putString("SourceName", sourceName);
        ListTag list = new ListTag();
        for (String attr : activeAttributes) {
            list.add(StringTag.valueOf(attr));
        }
        nbt.put("ActiveAttrs", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        super.deserializeNBT(nbt);
        this.sourceName = nbt.getString("SourceName");
        this.activeAttributes.clear();
        ListTag list = nbt.getList("ActiveAttrs", 8);
        for (int i = 0; i < list.size(); i++) {
            activeAttributes.add(list.getString(i));
        }
        this.nianxian = nbt.getInt("nianxian");
    }

    public void setSourceName(String name) { this.sourceName = name; }
    public String getSourceName() { return sourceName; }
    public List<String> getActiveAttributes() { return activeAttributes; }

    public int getNianxian() { return nianxian; }
    public void setNianxian(int nianxian) { this.nianxian = nianxian; }
}
