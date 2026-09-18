package com.TovidY.kunluncontinent.item.neidanitems;

public enum NeidanQuality {
    FAN("凡品", 6000),   // 权重 6000
    LIANG("良品", 2500),  // 权重 2500
    SHANG("上品", 1000),  // 权重 1000
    ZHEN("珍品", 400),    // 权重 400
    JUE("绝品", 97),      // 权重 97
    XIAN("仙品", 3);      // 权重 3

    public final String name;
    public final int baseWeight;

    NeidanQuality(String name, int baseWeight) {
        this.name = name;
        this.baseWeight = baseWeight;
    }
}