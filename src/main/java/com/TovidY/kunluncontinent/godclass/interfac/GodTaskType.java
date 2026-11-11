package com.TovidY.kunluncontinent.godclass.interfac;

public enum GodTaskType {
    KILL,          // 击杀生物
    ATTRIBUTE,     // 属性达标
    ITEM_CONSUME,  // 提交并扣除物品
    ITEM_CHECK,     // 仅检查物品不扣除
    HUNHUAN_NIANXIAN, //检查魂环年限
    /**
     * 事件考：<b>不通过「进度检查」按钮判定</b>，由代码在合适的时机直接判定。
     *
     * <p>用于 {@link com.TovidY.kunluncontinent.godclass.LoneBambooGodPath} 这类
     * 「吸收指定魂环」的考，以及需要状态机（试炼）的考。
     * 进度检查包遇到这个类型会提示"本考由事件自动判定"，不会误判成未通过。</p>
     */
    TRIAL
}
