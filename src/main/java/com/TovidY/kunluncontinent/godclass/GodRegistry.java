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

                .addTask(2, GodTaskType.ATTRIBUTE, "maxshengming", 3500000, "第二考：最大生命值达到3500000以上")
                .addAttrReward(2, "dengji", 1,"§b当前等级+1")

                .addTask(3, GodTaskType.HUNHUAN_NIANXIAN, "any", 950000, "第三考：拥有一枚95万年以上魂环")
                .addAttrReward(3, "maxjingshenli", 3000.0f,"§b最大精神力提升：3000点")

                .addTask(4, GodTaskType.KILL, "kunluncontinent:demon_whale", 1, "第四考：击杀1只魔鲸")
                .addAttrReward(4, "gongji", 1500.0f,"§b攻击力提升：1500点")

                .addTask(5, GodTaskType.ITEM_CONSUME, "kunluncontinent:demon_whale_medal", 1, "第五考：提交一个深海勋章")
                .addAttrReward(5, "fangyu", 3000.0f,"§b防御提升：3000点")
        ;


        // ============================================================
        // 例子 2：修罗神 (Asura God) - 杀戮意志，积攒奖励至第九考
        // ============================================================
        register("asura_god", "修罗神")
                .addTask(1, GodTaskType.KILL, "minecraft:zombie", 100, "第一考：击杀100只僵尸")
                .addAttrReward(1, "gongji", 10.0f,"攻击力提升：10")

                .addTask(2, GodTaskType.KILL, "minecraft:wither_skeleton", 20, "第二考：击杀20只凋灵骷髅")
                .addAttrReward(2, "gongji", 20.0f,"攻击力提升：20")

                .addTask(3, GodTaskType.KILL, "minecraft:wither", 1, "第三考：击杀一只凋灵BOSS")
                .addAttrReward(3, "gongji", 50.0f,"攻击力提升：50");


        // ============================================================
        // 例子 3：天使神 (Angel God) - 神圣试炼，侧重属性提升
        // ============================================================
        register("angel_god", "天使神")
                // 第一考：神圣洗礼（随机任务池）
                .addTask(1, GodTaskType.ITEM_CHECK, "minecraft:glistering_melon_slice", 1, "第一考：持有闪烁的西瓜（感悟生命）")
                .addTask(1, GodTaskType.ITEM_CHECK, "minecraft:golden_carrot", 1, "第一考：持有金胡萝卜（感悟光明）")
                .addAttrReward(1, "maxshengming", 10.0f,"最大生命值提升：10")

                // 第二考：属性试炼
                .addTask(2, GodTaskType.ATTRIBUTE, "gongji", 100, "第二考：攻击力达到100")
                .addAttrReward(2, "baojilv", 5.0f,"暴击率提升：5")

                // 第三考：驱逐黑暗
                .addTask(3, GodTaskType.KILL, "minecraft:phantom", 15, "第三考：击杀15只幻翼（驱逐梦魇）")
                .addItemReward(3, Items.TOTEM_OF_UNDYING, 1,"§b不死图腾 x1")
                .addCommandReward(3, "say %player% 通过了天使第三考，圣光永存！");
    }

    private static GodInfo register(String id, String name) {
        GodInfo info = new GodInfo(id, name);
        GODS.put(id, info);
        return info;
    }
}