package com.TovidY.kunluncontinent.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class ModTriggers {
    // 实例化你的等级触发器
    public static final LevelTrigger LEVEL_TRIGGER = new LevelTrigger();

    public static void register() {
        CriteriaTriggers.register(LEVEL_TRIGGER);
    }
}
