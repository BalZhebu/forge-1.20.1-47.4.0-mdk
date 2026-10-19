package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.capability.CapabilityAttributeBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.concurrent.ThreadLocalRandom;

//各个年限生物的属性生成
public class MobAttributeCapability extends CapabilityAttributeBase implements INBTSerializable<CompoundTag> {
    private long nianxian;
    private boolean shenci;
    private float tempWugongModifier = 0f;
    private boolean isWeakened = false;

    /**
     * 生成这个能力时的「来源生物」实体类型 ID（如 {@code kunluncontinent:lone_bamboo}），空串 = 未知。
     *
     * <p><b>为什么需要它：</b>魂环实体是独立实体，它的能力是击杀瞬间从魂兽身上
     * 整份复制过来的（{@code KLivingDeathEvent#addHunhuanEntity}）。魂环本身拿不到
     * 原生物对象，所以「这个魂环来自谁」只能靠复制时顺手记一个 ID 带过来 ——
     * 特殊生物魂环（{@code special} 包）靠它判断该不该给特殊魂技。</p>
     */
    private String sourceType = "";

    public String getSourceType() { return sourceType; }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType == null ? "" : sourceType;
    }

    public MobAttributeCapability() {
        super();
        this.nianxian = 0;
    }

    public void initNianxian(long nianxian, Entity entity) {
        this.nianxian = nianxian;
        checkAndApplyWeakness(entity);
        updateAttributesByNianxian();
    }

    public void initNianxian(long nianxian) {
        this.nianxian = nianxian;
        updateAttributesByNianxian();
    }

    /**
     * 检查是否满足主世界新手保护条件（0,0 坐标 1000格内 & 年限 < 5000）
     */
    private void checkAndApplyWeakness(Entity entity) {
        if (entity != null && !entity.level().isClientSide()) {
            boolean isOverworld = entity.level().dimension() == Level.OVERWORLD;
            BlockPos pos = entity.blockPosition();
            double distanceSq = pos.getX() * (double) pos.getX() + pos.getZ() * (double) pos.getZ();
            // 满足条件：主世界 + 距离(0,0)小于等于1000格 (平方小于1000000) + 年限小于5000
            if (isOverworld && distanceSq <= 1000000.0D && this.nianxian < 5000) {
                this.isWeakened = true;
            }
        }
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
        // 如果触发了削弱，全属性乘以 0.35f (即削弱 65%)
        float weakenFactor = this.isWeakened ? 0.35f : 1.0f;

        this.setWugong(g * gMult * weakenFactor);
        this.setWufang((g / 3f) * weakenFactor);
        this.setMaxshengming(hpMult * g * weakenFactor);
        this.setShengming(hpBase * g * weakenFactor);
        this.setBaojilv(l * weakenFactor);
        this.setBaojishanghai((5f * l) * weakenFactor);
        this.setKangbao((3f * l) * weakenFactor);
        this.setXixue((l / 4f) * weakenFactor);
        this.setMingzhong(l * weakenFactor);
        this.setShanbi(l * weakenFactor);
        this.setWuchuan(l * weakenFactor);
    }

    /**
     * 挑战塔层数倍率 —— <b>只放大「生命 / 防御 / 攻击」三项核心属性</b>。
     *
     * <h3>为什么副属性绝对不能乘这个倍率</h3>
     * <ul>
     *   <li><b>闪避</b>：闪避判定是 {@code (闪避 − 命中) / (闪避 − 命中 + 100)}。
     *       闪避乘 12 后（500 → 6000），玩家必须把命中堆到同一量级才可能命中，
     *       否则就是全程 90%+ 闪避 —— 这不是"更难"，而是<b>根本打不到</b>。</li>
     *   <li><b>吸血</b>：吸血比例是 {@code x / (x + 100)}，本身就是饱和曲线。
     *       乘 12 后 55% → 93%，玩家好不容易磨掉的血一刀回满，等于物理免疫。</li>
     *   <li><b>暴击率 / 暴击伤害 / 抗暴 / 物穿 / 命中</b>同理：它们是<b>比率型</b>属性，
     *       乘倍率不是线性变强，而是直接跨过阈值，手感和平衡会当场崩掉。</li>
     * </ul>
     *
     * <p>所以层数难度只通过"血更厚、打得更痛、更耐打"这三项线性可读的数值表达；
     * 副属性保持年限原值 —— 它们本来就随年限成长，已经足够区分层数了。</p>
     *
     * <p><b>调用时机约束：</b>必须在 {@code initNianxian} 之后、
     * {@code MobTempAttributeManager} / 词条加成（壁垒、浑厚）之前调用 ——
     * 词条按放大后的基数再叠加，保证"本层核心×N"的语义一致。
     * 另外此时临时物攻加成（tempWugongModifier）必须仍为 0。</p>
     */
    public void applyAttributeMultiplier(float multiplier) {
        if (multiplier <= 0f || multiplier == 1f) return;
        // 核心三围：生命 / 防御 / 攻击（当前血量一起放大，保持满血开局）
        this.setWugong(this.getGongji() * multiplier);
        this.setWufang(this.getFangyu() * multiplier);
        this.setMaxshengming(this.getMaxshengming() * multiplier);
        this.setShengming(this.getShengming() * multiplier);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putLong("nianxian", nianxian);
        nbt.putBoolean("shenci", shenci);
        nbt.putFloat("tempWugongModifier", tempWugongModifier);
        nbt.putBoolean("isWeakened", isWeakened); // 保存削弱标记
        nbt.putString("towerSkillsTag", towerSkillsTag); // 挑战塔词条标签（同步给客户端画词条行）
        nbt.putString("sourceType", sourceType); // 来源生物实体类型 ID（特殊魂环判定用）
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        if (nbt.contains("nianxian")) this.nianxian = nbt.getLong("nianxian");
        if (nbt.contains("shenci")) this.shenci = nbt.getBoolean("shenci");
        if (nbt.contains("tempWugongModifier")) this.tempWugongModifier = nbt.getFloat("tempWugongModifier");
        if (nbt.contains("isWeakened")) this.isWeakened = nbt.getBoolean("isWeakened"); // 读取削弱标记
        this.towerSkillsTag = nbt.getString("towerSkillsTag");
        this.sourceType = nbt.getString("sourceType");
    }

    /** 挑战塔词条标签（仅用于客户端名牌第二行显示，空串 = 无词条）。 */
    private String towerSkillsTag = "";

    public String getTowerSkillsTag() { return towerSkillsTag; }
    public void setTowerSkillsTag(String tag) { this.towerSkillsTag = tag == null ? "" : tag; }

    // Getter and Setter
    public boolean isWeakened() { return isWeakened; }
    public void setWeakened(boolean weakened) { this.isWeakened = weakened; }

    public void addTempWugong(float value) { this.tempWugongModifier += value; }
    public void removeTempWugong(float value) { this.tempWugongModifier -= value; }

    @Override
    public float getGongji() { return super.getGongji() + this.tempWugongModifier; }

    public boolean isShenci() { return shenci; }
    public void setShenci(boolean shenci) { this.shenci = shenci; }

    public long getNianxian() { return nianxian; }
    public void setNianxian(long nianxian) {
        this.nianxian = nianxian;
        updateAttributesByNianxian();
    }
}