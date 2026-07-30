package com.TovidY.kunluncontinent.screen.playernpc;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * NPC 对话文本库管理类
 */
public class NpcDialogTextLibrary {

    private static final Random RANDOM = new Random();

    // 默认/通用随机台词列表（每项可包含多行文本，用 \n 换行）
    private static final List<String> GENERAL_DIALOGS = Arrays.asList(
            "阁下光临，有何贵干？\n切磋点到为止，切勿伤了和气。",
            "昆仑山下藏龙卧虎，阁下行事还需多加小心。\n不知今日找我有何要事？",
            "路漫漫其修远兮，道友修行为何如此匆忙？\n不妨停下脚步坐会儿。",
            "若想在这片大陆立足，光凭武力可不行。\n你需要更多的机缘与悟性。",
            "最近这附近不太平，不少魂兽躁动不安。\n阁下出门在外，切记安全第一。"
    );

    /**
     * 从通用文本库中随机获取一条台词
     */
    public static String getRandomGeneralDialog() {
        return GENERAL_DIALOGS.get(RANDOM.nextInt(GENERAL_DIALOGS.size()));
    }

    /**
     * 如果你以后想根据 NPC 类型/皮肤等获取特定台词，可以扩展此方法
     */
    public static String getRandomDialogForNpc(int skinIndex) {
        // 预留扩展入口，目前默认返回通用台词
        return getRandomGeneralDialog();
    }
}
