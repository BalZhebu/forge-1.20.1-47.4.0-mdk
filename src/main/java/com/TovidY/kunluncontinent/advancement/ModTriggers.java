package com.TovidY.kunluncontinent.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class ModTriggers {
    public static final LevelTrigger LEVEL_TRIGGER = new LevelTrigger();
    public static void register() {
        CriteriaTriggers.register(LEVEL_TRIGGER);
    }
}
