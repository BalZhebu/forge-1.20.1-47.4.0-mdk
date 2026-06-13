package com.TovidY.kunluncontinent.tower.floor;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TowerFloorRegistry {

    public static class FloorData {
        public final List<MonsterConfig> monsters;
        public final int timeLimitSeconds;
        public final long baseNianxian;

        public FloorData(List<MonsterConfig> monsters, int timeLimitSeconds, long baseNianxian) {
            this.monsters = monsters;
            this.timeLimitSeconds = timeLimitSeconds;
            this.baseNianxian = baseNianxian;
        }
    }

    private static final Map<Integer, FloorData> REGISTRY = new HashMap<>();

    private static ResourceLocation klMob(String path) {
        return new ResourceLocation("kunluncontinent", path);
    }

    static {
        // ---------------------------------------------------------------------
        // 写法一：原样不动，老关卡纯完全随机 2 个词条
        // ---------------------------------------------------------------------
        registerFloor(0, 800000L, 60,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        // ---------------------------------------------------------------------
        // 写法二：混合模式。固定带【不死】，另外再让系统不重复随机抽取 2 个
        // ---------------------------------------------------------------------
        registerFloor(4, 3000L, 120,
                new MonsterConfig(klMob("liejinhu"), 2, "不死")
        );

        // ---------------------------------------------------------------------
        // 写法三：【绝杀：0 随机纯自定义词条怪物】
        // 尾随随机数填 0，后面直接无限写死你要的词条名字。系统绝对不去随机池里摸鱼，只加载你指定的！
        // ---------------------------------------------------------------------
        registerFloor(9, 8000L, 180,
                new MonsterConfig(klMob("baitiaojinshe"), 0, "不死", "狂暴", "反震", "金身")
        );

        // 哪怕你想指定 10 个纯手写词条、0 随机，也只需要在后面用逗号一直往下追加字符串就行了！
    }

    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds, MonsterConfig... monsters) {
        REGISTRY.put(floorIndex, new FloorData(List.of(monsters), timeLimitSeconds, nianxian));
    }

    public static FloorData getFloorData(int floor) {
        return REGISTRY.getOrDefault(floor, REGISTRY.get(0));
    }
}