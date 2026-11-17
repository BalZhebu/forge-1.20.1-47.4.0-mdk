package com.TovidY.kunluncontinent.screen.playernpc;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * NPC 对话文本库管理类
 */
public class NpcDialogTextLibrary {

    private static final Random RANDOM = new Random();

    private static final List<String> GENERAL_DIALOGS = Arrays.asList(
            "阁下光临，有何贵干？\n切磋点到为止，切勿伤了和气。",
            "昆仑山下藏龙卧虎，阁下行事还需多加小心。\n不知今日找我有何要事？",
            "路漫漫其修远兮，道友修行为何如此匆忙？\n不妨停下脚步坐会儿。",
            "若想在这片大陆立足，光凭武力可不行。\n你需要更多的机缘与悟性。",
            "最近这附近不太平，不少魂兽躁动不安。\n阁下出门在外，切记安全第一。",
            "我一定要打通挑战塔获得祂的瞥视！\n唉？阁下有什么事吗？",
            "这雪下了不知多少年了。\n听说极寒冰域深处，藏着不为人知的秘密。",
            "等级不是重点，别一直盯着等级看。\n可真正拉开差距的，唯有高年限魂环。",
            "阁下身上的气息……不一般呐。\n看来是刚经历过一场生死搏杀？",
            "挑战塔上的那些存在，都不好惹。\n修为不够的话，最好还是别往上闯。",
            "我年轻时也曾想过封号斗罗。\n如今只想在这片大陆下，安稳过日子。",
            "精神力这东西，用光了要慢慢养。\n别把自己逼得太紧，会伤到根基的。",
            "附近的魂兽最近躁得很。\n连我这把老骨头，都能听见它们夜里嚎叫。",
            "宝物有缘者得之，强求不来。\n不过你若真要买，我这儿倒是有些存货。",
            "别看我这样，我当年也进过挑战塔。\n当然……只爬到第一层。",
            "宁愿修炼卡十级百年，打定根基提升精神力。\n也不愿简单的吸收一个十年魂环毁掉根基。",
            "修行一途，最忌心浮气躁。\n静下心来打坐片刻，兴许比苦战一整天有用。",
            "神，到底存在吗？\n或许吧，或许不吧。",
            "因为太怕痛把属性点全部点在防御上了。\n不信你跟我练练？",
            "听说了吗？隔壁村有个叫‘老王’的。\n听说他见过挑战塔第55层遇到了史无前例的千万年魂兽！",
            "我偷看了作者的大纲。\n里面写了唯一的两个千万年魂兽！好像是两条龙？"
    );

    /**
     * 【一键解决】获取随机台词，并自动按 \n 拆分成多行列表
     */
    public static String getRandomGeneralDialog() {
        return GENERAL_DIALOGS.get(RANDOM.nextInt(GENERAL_DIALOGS.size()));
    }
}