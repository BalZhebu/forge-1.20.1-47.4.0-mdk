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
        // 写法一：原样不动，老关卡纯完全随机 1 个词条
        // ---------------------------------------------------------------------
        registerFloor(0, 888888L, 30,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 1)
        );

        registerFloor(1, 900000L, 30,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(2, 920000L, 45,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(3, 940000L, 40,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 3)
        );

        registerFloor(4, 960000L, 60,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 4,"泥沼","恐惧")
        );

        registerFloor(5, 970000L, 60,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(6, 988888L, 60,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 3)
        );

        registerFloor(7, 990000L, 60,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(8, 999999L, 50,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 3)
        );

        registerFloor(9, 1000001L, 80,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 4,"狂暴","恐惧","瞬移")
        );

        registerFloor(10, 1100000L, 50,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(11, 1200000L, 45,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 3)
        );

        registerFloor(12, 1300000L, 40,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 2)
        );

        registerFloor(13, 1400000L, 30,
                new MonsterConfig(new ResourceLocation("minecraft", "zombie"), 3)
        );

        // ---------------------------------------------------------------------
        // 写法三：【绝杀：0 随机纯自定义词条怪物】
        // 尾随随机数填 0，后面直接无限写死你要的词条名字。系统绝对不去随机池里摸鱼，只加载你指定的！
        // ---------------------------------------------------------------------
//        registerFloor(9, 8000L, 180,
//                new MonsterConfig(klMob("baitiaojinshe"), 0, "不死", "狂暴", "反震", "金身")
//        );

        // 哪怕你想指定 10 个纯手写词条、0 随机，也只需要在后面用逗号一直往下追加字符串就行了！
    }

    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds, MonsterConfig... monsters) {
        REGISTRY.put(floorIndex, new FloorData(List.of(monsters), timeLimitSeconds, nianxian));
    }

    public static FloorData getFloorData(int floor) {
        return REGISTRY.getOrDefault(floor, REGISTRY.get(0));
    }
}