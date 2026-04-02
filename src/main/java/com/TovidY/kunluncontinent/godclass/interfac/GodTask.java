package com.TovidY.kunluncontinent.godclass.interfac;

public class GodTask {
    public final GodTaskType type;
    public final String targetId;   // 比如 "minecraft:zombie" 或 "gongji"
    public final int requiredCount; // 需求量
    public final String description; // 面板显示的文字

    public GodTask(GodTaskType type, String targetId, int requiredCount, String description) {
        this.type = type;
        this.targetId = targetId;
        this.requiredCount = requiredCount;
        this.description = description;
    }
}
