package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.godclass.interfac.GodTaskType;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class GodRegistry {
    public static final Map<String, GodInfo> GODS = new HashMap<>();

    public static void init() {
        // ============================================================
        // 例子 1：海神 (Sea God) - 线性解锁，随考随给奖励
        // ============================================================
        register("sea_god", "海神")
                .setCumulative(true)

                .addTask(1, GodTaskType.ITEM_CONSUME, "minecraft:heart_of_the_sea", 15, "第一考：提交海洋之心（证明你对海洋的敬畏）")
                .addAttrReward(1, "maxshengming", 50000,"§b最大生命值提高：50000点")

                .addTask(2, GodTaskType.KILL, "minecraft:elder_guardian", 10, "第二考：击杀远古守卫者10只")
                .addAttrReward(2, "baojishanghai", 100,"§b暴击伤害提升100%")

                .addTask(3, GodTaskType.KILL, "minecraft:ender_dragon", 5, "第三考：击杀5只末影龙")
                .addAttrReward(3, "maxshengming", 85000.0f,"§b最大生命值提升：85000点")

                .addTask(4, GodTaskType.HUNHUAN_NIANXIAN, "any", 950000, "第五考：拥有一枚95万年以上魂环")
                .addAttrReward(4, "maxjingshenli", 3000.0f,"§b最大精神力提升：3000点")

                .addTask(5, GodTaskType.ATTRIBUTE, "maxshengming", 3500000, "第四考：最大生命值达到3500000以上")
                .addAttrReward(5, "dengji", 1,"§b当前等级+1")

                .addTask(6, GodTaskType.KILL, "kunluncontinent:demon_whale", 1, "第六考：击杀1只魔鲸")
                .addAttrReward(6, "gongji", 1500.0f,"§b攻击力提升：1500点")

                .addTask(7, GodTaskType.ITEM_CONSUME, "kunluncontinent:demon_whale_medal", 1, "第七考：提交一个深海勋章")
                .addAttrReward(7, "fangyu", 3000.0f,"§b防御提升：3000点")

                .addTask(8, GodTaskType.HUNHUAN_NIANXIAN, "any", 4000000, "第八考：拥有一枚400万年以上魂环")
                .addAttrReward(8, "maxshengming", 1000000.0f,"§b最大生命值提升：1000000点")

                .addTask(9, GodTaskType.ATTRIBUTE, "maxshengming", 8000000, "第九考：最大生命值达到8000000以上")
                .addAttrReward(9, "baojishanghai", 200,"§b暴击伤害提升：200%")

                .addFinalAttrReward("dengji",1f,"§b神位奖励：百级")
                .addFinalAttrReward("maxshengming", 1000000f, "§b神位奖励：生命值+100万")
                .addFinalAttrReward("fangyu", 5000f, "§b神位奖励：防御+5000点")
                .addFinalAttrReward("baojishanghai", 200f, "§b神位奖励：暴击伤害+200%")

        ;


        // ============================================================
        // 例子 2：修罗神 (Asura God) - 杀戮意志
        // ============================================================
        register("asura_god", "修罗神")

                .addTask(1, GodTaskType.KILL, "kunluncontinent:ice_crystal", 100, "第一考：击杀100只冰晶")
                .addAttrReward(1, "gongji", 1200.0f,"攻击力提升：1200")

                .addTask(2, GodTaskType.KILL, "kunluncontinent:snow_demon", 300, "第二考：击杀300只雪魔")
                .addAttrReward(2, "maxshengming", 15000.0f,"最大生命值提升：15000")

                .addTask(3, GodTaskType.KILL, "minecraft:warden", 30, "第三考：击杀30只循声守卫Boss")
                .addAttrReward(3, "gongji", 1800.0f,"攻击力提升：1800")

                .addTask(4, GodTaskType.KILL, "minecraft:wither", 20, "第四考：击杀20只凋灵Boss")
                .addAttrReward(4, "dengji", 1.0f,"等级提升：1")

                .addTask(5, GodTaskType.KILL, "minecraft:ender_dragon", 10, "第五考：击杀10只末影龙")
                .addAttrReward(5, "maxshengming", 2000.0f,"最大生命值提升：2000")

                .addTask(6, GodTaskType.KILL, "kunluncontinent:demon_whale", 1, "第六考：击杀1只魔鲸")
                .addAttrReward(6, "maxjingshenli", 2000.0f,"最大精神力提升：2000")

                .addTask(7, GodTaskType.HUNHUAN_NIANXIAN, "any", 950000, "第七考：拥有一枚95,0000年以上魂环")
                .addAttrReward(7, "baojishanghai", 100.0f,"暴击伤害提升：100%")

                .addTask(8, GodTaskType.HUNHUAN_NIANXIAN, "any", 5000000, "第八考：拥有一枚500,0000年以上魂环")
                .addAttrReward(8, "gongji", 10000.0f,"攻击力提升：10000")

                .addTask(9, GodTaskType.ATTRIBUTE, "gongji", 400000, "第九考：攻击力提升到40,0000以上")
                .addAttrReward(9, "baojishanghai", 300f,"§c暴击伤害提升：300%")

                .addFinalAttrReward("dengji",1f,"§c神位奖励：百级")
                .addFinalAttrReward("gongji", 80000f, "§c神位奖励：攻击力+8万")
                .addFinalAttrReward("fangyu", 3000f, "§c神位奖励：防御+3000点")
                .addFinalAttrReward("baojishanghai", 500f, "§c神位奖励：暴击伤害+500%")

        ;

        // ============================================================
        // 例子 3：天使神 (Angel God) - 神圣试炼，侧重属性提升
        // ============================================================
        register("angel_god", "天使神")
                // 第一考：神圣洗礼（随机任务池）
                .addTask(1, GodTaskType.ITEM_CHECK, "minecraft:golden_apple", 60, "第一考：提交60个金苹果")
//                .addTask(1, GodTaskType.ITEM_CHECK, "minecraft:golden_carrot", 1, "第一考：持有金胡萝卜（感悟光明）")
                .addAttrReward(1, "maxshengming", 100000.0f,"最大生命值提升：100000点")

                .addTask(2, GodTaskType.ITEM_CHECK, "minecraft:enchanted_golden_apple", 15, "第二考：提交15个附魔金苹果")
                .addAttrReward(2, "maxshengming", 100000.0f,"最大生命值提升：100000点")

                .addTask(3, GodTaskType.ATTRIBUTE, "maxshengming", 2000000, "第三考：最大生命值达到2000000以上")
                .addAttrReward(3, "gongji", 800.0f,"攻击力提升：800点")

                .addTask(4, GodTaskType.KILL, "minecraft:warden", 30, "第四考：击杀30只循声守卫（监守者）")
                .addItemReward(4, Items.ENCHANTED_GOLDEN_APPLE, 64,"§b附魔金苹果 x64")

                .addTask(5, GodTaskType.KILL, "minecraft:wither", 10, "第五考：击杀10只凋灵Boss")
                .addAttrReward(5, "maxjingshenli", 2500.0f,"最大精神力提升：2500")

                .addTask(6, GodTaskType.HUNHUAN_NIANXIAN, "any", 850000, "第六考：拥有一枚850000年以上魂环")
                .addAttrReward(6, "maxshengming", 500000.0f,"最大生命值提升：500000点")

                .addTask(7, GodTaskType.ITEM_CHECK, "kunluncontinent:demon_whale", 1, "第七考：击杀1只魔鲸")
                .addAttrReward(7, "maxjingshenli", 2500.0f,"最大精神力提升：2500点")

                .addTask(8, GodTaskType.HUNHUAN_NIANXIAN, "any", 3500000, "第八考：拥有一枚3500000年以上魂环")
                .addAttrReward(8, "baojishanghai", 200.0f,"暴击伤害提升：200%")

                .addTask(9, GodTaskType.ATTRIBUTE, "maxshengming", 8880000, "第九考：最大生命值达到888,0000以上")
                .addAttrReward(9, "gongji", 5000.0f,"攻击力提升：5000点")

                .addFinalAttrReward("dengji",1f,"§b神位奖励：百级")
                .addFinalAttrReward("maxshengming", 800000f, "§b神位奖励：生命值+80万")
                .addFinalAttrReward("fangyu", 5500f, "§b神位奖励：防御+5500点")
                .addFinalAttrReward("baojishanghai", 300.0f, "§b神位奖励：暴击伤害+300%")

                ;
    }

    private static GodInfo register(String id, String name) {
        GodInfo info = new GodInfo(id, name);
        GODS.put(id, info);
        return info;
    }
}