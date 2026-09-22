package com.TovidY.kunluncontinent.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class ModTriggers {
    public static final LevelTrigger LEVEL_TRIGGER = new LevelTrigger();

    /** 通用成就触发器：整套成就共用，靠事件 ID 区分。 */
    public static final KunlunTrigger KUNLUN_TRIGGER = new KunlunTrigger();

    public static void register() {
        CriteriaTriggers.register(LEVEL_TRIGGER);
        CriteriaTriggers.register(KUNLUN_TRIGGER);
    }
}
