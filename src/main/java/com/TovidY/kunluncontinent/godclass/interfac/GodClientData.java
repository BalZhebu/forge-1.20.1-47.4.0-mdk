package com.TovidY.kunluncontinent.godclass.interfac;

public class GodClientData {
    public static String godId = "";
    public static String godName = "";
    public static int currentStage = 1;
    public static int taskProgress = 0;
    public static boolean isGod = false;
    public static String currentDesc = "无任务";
    public static String currentTarget = "";
    public static int requiredCount = 0;
    public static String taskType = "";

    public static String rewardDesc = "无"; // 增加这个字段

    public static void update(String id, String name, int stage, int progress, boolean god, String desc, String target, int req, String type, String rDesc) {
        godId = id;
        godName = name;
        currentStage = stage;
        taskProgress = progress;
        isGod = god;
        currentDesc = desc;
        currentTarget = target;
        requiredCount = req;
        taskType = type;

        rewardDesc = rDesc;
    }
}