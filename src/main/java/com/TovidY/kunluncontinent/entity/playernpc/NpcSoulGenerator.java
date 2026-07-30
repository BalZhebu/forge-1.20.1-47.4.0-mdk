package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.capability.mobattributes.HunhuanWeakener;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class NpcSoulGenerator {

    private static final Random RANDOM = new Random();

    private static final long[][] RING_YEAR_RANGES = {
            {1, 3000},          // 第1魂环
            {100, 8000},        // 第2魂环
            {1000, 30000},      // 第3魂环
            {10000, 50000},     // 第4魂环
            {40000, 80000},     // 第5魂环
            {70000, 200000},    // 第6魂环
            {80000, 500000},    // 第7魂环
            {90000, 800000},    // 第8魂环
            {100000, 2000000}   // 第9魂环
    };

    /**
     * 为 NPC 随机初始化魂师属性（武魂、等级、成长基础属性、魂环）
     */
    public static void initNpcSoulData(PlayerAttributeCapability npcCap, int level) {
        int finalLevel = Math.min(99, Math.max(1, level));
        npcCap.setDengji(finalLevel);
        applyNpcLevelGrowth(npcCap, finalLevel);

        List<String> allWuhuns = PlayerAttributeCapability.wuhunListsnameall;
        if (allWuhuns != null && !allWuhuns.isEmpty()) {
            String chosenWuhun = allWuhuns.get(RANDOM.nextInt(allWuhuns.size()));
            npcCap.getMonsterCapabilityLists().put(chosenWuhun, new ArrayList<>());
            npcCap.getWuhunListsname().add(chosenWuhun);
            npcCap.setHunhuankuaiguan(0);

            int ringCount = Math.min(9, finalLevel / 10);
            List<MobAttributeCapability> npcRings = npcCap.getMonsterCapabilityLists().get(chosenWuhun);

            for (int i = 0; i < ringCount; i++) {
                long minYear = RING_YEAR_RANGES[i][0];
                long maxYear = RING_YEAR_RANGES[i][1];
                long nianxian = minYear + (long) (RANDOM.nextDouble() * (maxYear - minYear));

                MobAttributeCapability rawRingCap = createBaseRingCapability(nianxian);

                MobAttributeCapability weakenedRing = HunhuanWeakener.weaken(rawRingCap);
                npcRings.add(weakenedRing);
            }

            recalculateNpcTotalStats(npcCap, chosenWuhun);
        }

        npcCap.setInitialized(true);
    }

    /**
     * 完美模拟 PlayerUpgradeSystem 的属性成长机制（裸体基础属性）
     */
    private static void applyNpcLevelGrowth(PlayerAttributeCapability cap, int targetLevel) {
        // 赋予基础的觉醒初始属性
        float hp = 100.0f;
        float atk = 15.0f;
        float def = 5.0f;
        float maxSpiritual = 100.0f;

        // 模拟从 1 级逐步突破升级到 targetLevel 的属性累加
        for (int lvl = 1; lvl <= targetLevel; lvl++) {
            hp += (lvl * 1.4f) * 0.7f;
            def += (lvl * 0.3f) * 0.7f;
            atk += (lvl * 0.5f) * 0.65f;
            maxSpiritual += (lvl * 2f) * 1.2f;
        }

        cap.setMaxshengming(hp);
        cap.setShengming(hp);
        cap.setGongji(atk);
        cap.setFangyu(def);
        cap.setMaxjingshenli(maxSpiritual);
        cap.setJingshenli(maxSpiritual);
    }

    /**
     * 依据年限构建真正的魂兽原生属性值（通过 initNianxian 触发官方公式）
     */
    private static MobAttributeCapability createBaseRingCapability(long nianxian) {
        MobAttributeCapability cap = new MobAttributeCapability();
        cap.initNianxian(nianxian);
        return cap;
    }

    /**
     * 【关键补充】：重新汇总 NPC 的“裸体基础属性 + 所有魂环附加属性”
     */
    private static void recalculateNpcTotalStats(PlayerAttributeCapability npcCap, String wuhunName) {
        List<MobAttributeCapability> rings = npcCap.getMonsterCapabilityLists().get(wuhunName);
        if (rings == null || rings.isEmpty()) return;

        // 以当前的裸体基础属性为基准
        float totalHp = npcCap.getMaxshengming();
        float totalAtk = npcCap.getGongji();
        float totalDef = npcCap.getFangyu();
        float totalBaojilv = npcCap.getBaojilv();
        float totalBaojishanghai = npcCap.getBaojishanghai();
        float totalKangbao = npcCap.getKangbao();
        float totalXixue = npcCap.getXixue();
        float totalMingzhong = npcCap.getMingzhong();
        float totalShanbi = npcCap.getShanbi();
        float totalWuchuan = npcCap.getWuchuan();

        // 遍历所有已装备的魂环，逐项累加属性
        for (MobAttributeCapability ring : rings) {
            totalHp += ring.getMaxshengming();
            totalAtk += ring.getGongji();
            totalDef += ring.getFangyu();
            totalBaojilv += ring.getBaojilv();
            totalBaojishanghai += ring.getBaojishanghai();
            totalKangbao += ring.getKangbao();
            totalXixue += ring.getXixue();
            totalMingzhong += ring.getMingzhong();
            totalShanbi += ring.getShanbi();
            totalWuchuan += ring.getWuchuan();
        }

        // 统一更新回 NPC 的主属性面板变量
        npcCap.setMaxshengming(totalHp);
        npcCap.setShengming(totalHp); // 保持满血状态
        npcCap.setGongji(totalAtk);
        npcCap.setFangyu(totalDef);
        npcCap.setBaojilv(totalBaojilv);
        npcCap.setBaojishanghai(totalBaojishanghai);
        npcCap.setKangbao(totalKangbao);
        npcCap.setXixue(totalXixue);
        npcCap.setMingzhong(totalMingzhong);
        npcCap.setShanbi(totalShanbi);
        npcCap.setWuchuan(totalWuchuan);
    }
}