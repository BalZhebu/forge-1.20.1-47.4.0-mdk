package com.TovidY.kunluncontinent.datagen.lang;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.common.Mod;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, KlMain.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        //技能代码
        add(ModItems.SKILL_POHUN_1.get(), "§c《破魂枪》第一魂技[破魂枪]");

        add(ModItems.SKILL_BAHUANG_1.get(), "§c《八荒戟》第一魂技[八荒戟]");

        add(ModItems.SKILL_LEIJINHU_1.get(), "§c《裂金虎》第一魂技[横爪]");

        add(ModItems.SKILL_PANSHIJUYUAN_1.get(), "§c《磐石巨猿》第一魂技[重击]");

        add("无技能","暂无技能描述");

        add("skill.pohunqiang.one.description", "消耗%s点精神力，凝结武魂实体化。");
        add("skill.bakuangji.one.description", "消耗%s点精神力，凝结武魂实体化。");
        add("skill.leijinhu.one.description", "消耗%s点精神力，横爪，可击打范围4格内的敌人");
        add("skill.panshijuyuan.one.description", "消耗%s点精神力，重击，可击打范围5格内的敌人并附加缓慢效果");

        //锻造模版
        add(ModItems.RINSEI_FORGING_TEMPLATE.get(), "§b凛晶锻造模版");


        //引导书
        add(ModItems.GUIDE_BOOK.get(), "§b《昆仑大陆 · 引导书》");
        add("guide.kunlun.chapter1.content","§1§l《欢迎来到昆仑大陆！祝您玩的愉快 ~ 》\n[本引导书更新于’1.0‘版本]§r\n§c§l请各位玩家务必认真仔细查看，基本收录了正常游玩时遇到的所有常见攻略" +
                "\n§c以下为基础教程：§r" +
                "\n§l属性面板系统：§r默认按O键打开属性面板，在属性面板下按住Shift键可查询具体数值，例如玩家的攻击力为10000时属性面板会自动缩进变为1万，按下Shift即可查看具体数值。" +
                "\n§l技能系统：§r默认长按按R键可打开技能栏，按V键可释放选中的技能，技能栏会显示玩家当前拥有的技能，用鼠标滑动选择技能，鼠标移动到对应灰色滑块后松开R键即可选择技能，选择不松开R键的话技能栏中心会出现技能的相关介绍，点按R可快速按顺序切换技能。" +
                "\n§l觉醒武魂：§r玩家击杀生物、炼制丹药、用蒲团修炼，都可以获得经验，经验值满了后自动突破下一个等级，当玩家第一次突破成功时，将会觉醒武魂并分配玩家的天赋。" +
                "\n§l天赋系统：§r天赋在玩家觉醒武魂时出现，普通天赋最为常见，废物和天才天赋概率一致，是废物还是天才都在一念之间，不同的天赋会略微影响一点点数值，但后期均可通过努力提升。" +
                "\n§l武魂系统：§r默认按K键可打开武魂，打开武魂时会消耗精神力，打开武魂时可以吸收魂环。" +
                "\n§l魂环吸收:§r玩家每到10/20/30级等10的倍数的等级时，会锁定等级，必须吸收魂环才可突破下一阶段，吸收时需打开武魂后右键生物掉落的魂环坐上去后消耗精神力吸收魂环，精神力不足将停止吸收，吸收进度重置。" +
                "\n§l重修&转世系统：§r玩家在主世界自然生成的地狱门遗迹可找到生成在地狱岩上的彼岸花，将彼岸花磨成粉可炼制忘川渡buff，喝下后找到重修遗迹，再自己身上有buff的情况下右键重修台就会遭受99道雷劫，但记住！雷劫不致命！请不要用任何手段恢复血量，因为重修需要玩家血量到达20以内才能成功转世，若99道天雷后你血量依旧健康，那么，你的转世重修就会失败。" +
                "\n§l重修遗迹：§r通过重修之眼可找到重修遗迹，重修之眼使用方法和末影之眼一致。" +
                "\n§l武魂果实：§r武魂果实将在玩家转生后会根据玩家拥有的武魂将武魂果实给予给玩家，吃下武魂果实即可觉醒对应的武魂，玩家最多觉醒3个武魂。" +
                "\n§l内丹系统：§r击杀不同年限的生物会掉落不同品质的内丹，内丹可以炼制经验丹药，不同等级的炼丹炉炼制速度不一样，例如1级炼丹炉炼制1级丹药需要10秒，而炼制9级丹药需要10秒的100倍的时间。" +
                "\n§l丹药品级系统：§r不同品质的内丹炼制出的丹药品质不一样，使用时的倍率不一样，详情自己炼制查看。炼制时可能出现丹渣品质的丹药，放入合成台一个丹渣品质丹药可合成一个丹渣，丹渣9个合成一个丹渣块，可减少炼制丹药时丹渣的概率。" +
                "\n§l年限生成系统：§r玩家距离世界坐标越远，生成高年限的生物概率越高，最远10000格以外，，到达一万格后，这个值将会到达极限。" +
                "\n§l魂骨系统：§r玩家在击杀生物时有极低概率掉落魂骨，魂骨是词条制，最低为1词条，最高为10词条。词条越多，概率越低。" +
                "\n§l蒲团修炼系统：§r玩家在前期难以修炼时可以用蒲团修炼，玩家在前期时修满一个周期（10分钟）可直升两级，修炼时玩家有10分钟周期限制，10分钟后不准修炼，可通过除不修炼的任何情况下都可以恢复这个修炼时间，提示：若玩家修炼时间已满，但仍然可以修炼，但这次修炼只会恢复精神力，而不会增加修为。" +
                "\n§l飞行系统：§r当玩家的最大精神力到达5000并等级大于25级时，玩家将解锁飞行能力，飞行时精神力消耗加快，等级越高消耗越慢。" +
                "\n§l进阶维度系统：§r根据某些特殊生物掉落的物品可合成传送门框架，请查看传送门框架描述来搭建传送门结构，可前往生成高年限生物的维度。" +
                "\n§l特殊攻击系统：§r玩家攻击生物时有低概率触发特殊效果，例如撕裂、燃烧、震撼、湮灭、等特殊效果，特殊效果可增加玩家对生物造成的那次伤害并附加debuff，注意：怪物也可以对你造成这些特殊效果。" +
                "\n§l玩家屏幕的GUI属性解释：§r左上角GUI的红色部分的代表玩家血量，血量条右侧体力代表玩家的饱食度，下面浅青色代表玩家的精神力，物品栏上方中间多边形内空白处的数值为玩家当前等级（当玩家进入世界、或更新事件时这个数值会异变，这是正常现象，通常几秒即可恢复），物品栏上方黄色进度条为经验条，满了后自动突破，注意：数值在玩家做出某种事件更替时会出现异常变动，这是正常现象，稍等几秒即可恢复。" +
                "\n§l维度特色讲解：" +
                "\n§r1.极寒冰域：会生成最低万年，最高百万年的生物，玩家在维度内若没有御寒魂导器的话就会根据玩家自身的等级计算可以撑的时间，时间一到你将会受到真实伤害的百分比伤害。" +
                "\n§r2.占位符 +" +
                "\n§r3.占位符 + ");

        add(ModItems.RED_SPIDER_LILY_POTION.get(), "§c《忘川渡·彼岸花》");
        add(ModItems.EYE_TRANSFORMATION.get(), "§5重修之眼");

        add("item.kunluncontinent.guide_book.tooltip","§7第一次进入游戏即可获得");
        add("item.kunluncontinent.guide_book.tooltip1","§7请务必认真仔细查看");
        add("item.kunluncontinent.guide_book.tooltip2","§7建议不要弄丢");

        //成就类
        add("advancements.kunluncontinent.root.title", "初入世界");
        add("advancements.kunluncontinent.root.description", "踏入昆仑大陆的第一步。");
        add("adv.kunlun.lvl10.title", "魂师");
        add("adv.kunlun.lvl10.desc", "吸收一个魂环来到魂师行类吧");
        add("adv.kunlun.lvl20.title", "大魂师");
        add("adv.kunlun.lvl20.desc", "相比你已经学会了如何吸收魂环了吧");
        add("adv.kunlun.lvl30.title", "魂尊");
        add("adv.kunlun.lvl30.desc", "你觉得你能够吸收一个千年魂环了吗=-=？");
        add("adv.kunlun.lvl40.title", "魂宗");
        add("adv.kunlun.lvl40.desc", "你觉得你能够吸收一个万年魂环了吗=-=？");
        add("adv.kunlun.lvl50.title", "魂王");
        add("adv.kunlun.lvl50.desc", "'像你这个年纪都能吸收十万年魂环了'");
        add("adv.kunlun.lvl60.title", "魂帝");
        add("adv.kunlun.lvl60.desc", "属性质的飞跃！");
        add("adv.kunlun.lvl70.title", "魂圣");
        add("adv.kunlun.lvl70.desc", "你觉得你能够吸收一个百万年魂环了吗=-=？");
        add("adv.kunlun.lvl80.title", "魂斗罗");
        add("adv.kunlun.lvl80.desc", "潜力高运气好的是不是拿到神位了呢~");
        add("adv.kunlun.lvl90.title", "封号斗罗");
        add("adv.kunlun.lvl90.desc", "是时候去拿到神位传承了！");
        add("adv.kunlun.lvl99.title", "极限斗罗");
        add("adv.kunlun.lvl99.desc", "是时候去封神了！");
        add("adv.kunlun.liandanlu1.title","一级炼丹炉");
        add("adv.kunlun.liandanlu1.desc","内丹品质高丹炉品质低炼丹速度越慢，反之炼丹速度更快");
        add("adv.kunlun.liandanlu2.title","二级炼丹炉！");
        add("adv.kunlun.liandanlu2.desc","建议使用更高级炼丹炉");
        add("adv.kunlun.liandanlu3.title","三级炼丹炉！");
        add("adv.kunlun.liandanlu3.desc","可以让炼丹速度加快！");
        add("adv.kunlun.liandanlu4.title","四级炼丹炉！");
        add("adv.kunlun.liandanlu4.desc","但若用低于等阶炼丹炉炼制高阶内丹，炼制速度将会提高10000%");
        add("adv.kunlun.liandanlu5.title","五级炼丹炉！");
        add("adv.kunlun.liandanlu5.desc","内丹品质高丹炉品质低炼丹速度越慢，反之炼丹速度更快");
        add("adv.kunlun.liandanlu6.title","六级炼丹炉！");
        add("adv.kunlun.liandanlu6.desc","内丹品质高丹炉品质低炼丹速度越慢，反之炼丹速度更快");
        add("adv.kunlun.liandanlu7.title","七级炼丹炉！");
        add("adv.kunlun.liandanlu7.desc","材料的难以获取");
        add("adv.kunlun.liandanlu8.title","八级炼丹炉！");
        add("adv.kunlun.liandanlu8.desc","高效的炼丹");
        add("adv.kunlun.liandanlu9.title","§c九§b阶§5炼§a丹§e炉！");
        add("adv.kunlun.liandanlu9.desc","材料要求极高实用性确不高");

        //魂骨类
        add(ModItems.SOUL_BEAST_SKULL.get(), "§c§k------§r §e《魂骨 · 头骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_BREASTBONE.get(), "§c§k------§r §e《魂骨 · 躯干骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_LEFT_HAND_BONE.get(), "§c§k------§r §e《魂骨 · 左臂骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_RIGHT_HAND_BONE.get(), "§c§k------§r §e《魂骨 · 右臂骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_LEFT_LEG_BONE.get(), "§c§k------§r §e《魂骨 · 左腿骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_RIGHT_LEG_BONE.get(), "§c§k------§r §e《魂骨 · 右腿骨》 §r§c§k------");
        add(ModItems.SOUL_BEAST_EXTERNAL_APPENDAGES.get(), "§c§k------§r §e《魂骨 · 外附骨》 §r§c§k------");

        //刻刀类
        add(ModItems.IRON_ENGRAVING_KNIFE.get(), "铁制刻刀");
        add(ModItems.DIAMOND_ENGRAVING_KNIFE.get(), "钻石制刻刀");
        //核心类
        add(ModItems.LOW_LEVEL_HEXIN.get(), "§7低级丹炉核心");
        add(ModItems.HIGH_LEVEL_HEXIN.get(), "§4高级丹炉核心");
        add(ModItems.MIDDLE_LEVEL_HEXIN.get(), "§6中级丹炉核心");
        add(ModItems.TOP_LEVEL_HEXIN.get(), "§e§l顶级丹炉核心");
        //核心
        add(ModItems.LOW_HUNHUAN_STORAGE_CORE.get(), "§7低级核心");
        add(ModItems.HIGH_HUNHUAN_STORAGE_CORE.get(), "§4高级核心");
        add(ModItems.MIDDLE_HUNHUAN_STORAGE_CORE.get(), "§6中级核心");
        add(ModItems.TOP_HUNHUAN_STORAGE_CORE.get(), "§e§l顶级核心");

        //伤害源
        add("death.attack.extreme_cold","%1$s 被极寒永久冰封了...");
        add("death.attack.extreme_cold.player","%1$s 在极寒中化作了永恒的冰雕");
        add("death.attack.extreme_cold.item","%1$s 逃离 %2$s 时被极寒永久冰封了...");

        //传送门方块
        add(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get(), "§b极寒冰域传送门框架");
        //传送门
        add(ModBlocks.POLAR_ICE_PORTAL.get(), "极寒冰域传送门");
        //打火石
        add(ModItems.EXTREME_COLD_SNOWFLAKE.get(), "§b极寒雪晶");

        //普通物品
        add(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get(), "§b极寒雪晶碎片");
        add(ModItems.POHUNQIANG.get(), "§c破魂枪");
        add(ModItems.BAHUANGJI.get(), "§c八荒戟");

        //矿石类
        add(ModItems.GRAY_IRON_INGOT.get(),"§8灰铁锭");
        add(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),"§6云纹铜");
        add(ModItems.RED_FIRE_INGOT.get(), "§c赤火锭");
        add(ModItems.SUNKEN_SILVER_INGOT.get(), "§f沉银");
        add(ModItems.COLD_HEARTED_STEEL_INGOT.get(), "§7寒心钢锭");
        add(ModItems.RINSEI_INGOT.get(), "§b凛晶");

        add(ModItems.RUBY.get(), "§c红宝石");
        add(ModItems.AMETHYST.get(), "§5紫瑛");
        add(ModItems.SAPPHIRE.get(), "§6蓝晶");
        add(ModItems.STARLIGHT_STONE.get(), "§7星辰石");

        //内丹类
        add(ModItems.NEIDAN1.get(),"一阶内丹");
        add(ModItems.NEIDAN2.get(),"二阶内丹");
        add(ModItems.NEIDAN3.get(),"三阶内丹");
        add(ModItems.NEIDAN4.get(),"四阶内丹");
        add(ModItems.NEIDAN5.get(),"五阶内丹");
        add(ModItems.NEIDAN6.get(),"六阶内丹");
        add(ModItems.NEIDAN7.get(),"七阶内丹");
        add(ModItems.NEIDAN8.get(),"八阶内丹");
        add(ModItems.NEIDAN9.get(),"九阶内丹");


        //炼丹炉
        add(ModBlocks.LIANDANLU1.get(),"一阶炼丹炉");
        add(ModBlocks.LIANDANLU2.get(),"二阶炼丹炉");
        add(ModBlocks.LIANDANLU3.get(),"三阶炼丹炉");
        add(ModBlocks.LIANDANLU4.get(),"四阶炼丹炉");
        add(ModBlocks.LIANDANLU5.get(),"五阶炼丹炉");
        add(ModBlocks.LIANDANLU6.get(),"六阶炼丹炉");
        add(ModBlocks.LIANDANLU7.get(),"七阶炼丹炉");
        add(ModBlocks.LIANDANLU8.get(),"八阶炼丹炉");
        add(ModBlocks.LIANDANLU9.get(),"§c九§b阶§5炼§a丹§e炉");
        add("tooltip.kunluncontinent.liandanlu","不同等阶的炼丹炉只不过是炼丹速度上的差异");

        //测试物品类
        add(ModItems.DANYAO_TEST.get(),"测试物品---经验+999999999");
        add(ModItems.DANYAO_JINGSHENLI.get(),"测试物品---精神力+100%");
        add(ModItems.TEST_SWORD.get(),"测试物品---剑");
        add(ModItems.DANYAO_DENGJI_JIA.get(),"测试物品---等级+1");
        add(ModItems.DANYAO_DENGJI_JIAN.get(),"测试物品---等级-1");
        add(ModItems.INSTANT_KILL_SWORD.get(),"测试物品---天道裁决剑");
        add(ModItems.TEST_ZHUANSHENG.get(),"测试物品---转生物品");

        //魂环收纳器
        add(ModItems.HUNHUAN_STORAGE_ONE.get(),"一级魂环收纳器");
        add(ModItems.HUNHUAN_STORAGE_TWO.get(),"二级魂环收纳器");
        add(ModItems.HUNHUAN_STORAGE_THREE.get(),"三级魂环收纳器");
        add(ModItems.HUNHUAN_STORAGE_FOUR.get(),"四级魂环收纳器");
        add(ModItems.HUNHUAN_STORAGE_FIVE.get(),"五级魂环收纳器");

        //丹药类
        add(ModItems.CHUYUAN_DAN.get(),"初元丹");
        add(ModItems.BAICAOLING_DAN.get(),"百草灵丹");
        add(ModItems.QIANHUABAO_DAN.get(),"千华宝丹");
        add(ModItems.WANPOXUAN_DAN.get(), "万魄玄丹");
        add(ModItems.SHIFANGJIE_DAN.get(), "十方劫丹");
        add(ModItems.HUANYUANYIQI_DAN.get(), "混元一气丹");
        add(ModItems.TAIXUPOWANG_DAN.get(), "太虚破妄丹");
        add(ModItems.QIANWANXINGCHEN_DAN.get(), "千万星辰丹");
        add(ModItems.YIZAICHUANGSHENG_DAN.get(), "亿载长生丹");
        add(ModItems.DROSS.get(),"丹渣");
        add("吸收经验成功","§2吸收成功,经验：§e+%s");
        add("生命", "生命：%s/%s");
        add("最大生命", "最大生命：+%s");
        add("获得生命", "生命：+%s");
        add("攻击力", "攻击力：+%s");
        add("防御力", "防御力：+%s");
        add("暴击伤害", "暴击伤害：+%s");
        add("暴击率", "暴击率：+%s");
        add("突破成功率", "突破成功率：+%s");
        add("抗暴", "抗暴：+%s");
        add("吸血", "吸血：+%s");
        add("生命恢复", "生命恢复：+%s");
        add("命中", "命中：+%s");
        add("闪避", "闪避：+%s");
        add("经验", "经验：%s/%s");
        add("体力", "体力：%s/%s");
        add("获得经验", "经验：+%s");
        add("等级", "等级：%s");
        add("精神力", "精神力：%s/%s");
        add("获得精神力", "精神力：+%s");
        add("获得最大精神力", "最大精神力：+%s");
        add("最大使用次数", "最大使用次数：%s");

        //工具类
        add(ModItems.GRAY_IRON_SWORD.get(),"§8灰铁剑");
        add(ModItems.GRAY_IRON_PICKAXE.get(),"§8灰铁镐");
        add(ModItems.GRAY_IRON_AXE.get(),"§8灰铁斧");
        add(ModItems.GRAY_IRON_SHOVEL.get(),"§8灰铁铲");
        add(ModItems.GRAY_IRON_HOE.get(),"§8灰铁锄");

        add(ModItems.CLOUD_PATTERNED_BRONZE_SWORD.get(),"§6云纹铜剑");
        add(ModItems.CLOUD_PATTERNED_BRONZE_PICKAXE.get(),"§6云纹铜镐");
        add(ModItems.CLOUD_PATTERNED_BRONZE_AXE.get(),"§6云纹铜斧");
        add(ModItems.CLOUD_PATTERNED_BRONZE_SHOVEL.get(),"§6云纹铜铲");
        add(ModItems.CLOUD_PATTERNED_BRONZE_HOE.get(),"§6云纹铜锄");

        add(ModItems.RED_FIRE_SWORD.get(), "§c赤火剑");
        add(ModItems.RED_FIRE_PICKAXE.get(), "§c赤火镐");
        add(ModItems.RED_FIRE_AXE.get(), "§c赤火斧");
        add(ModItems.RED_FIRE_SHOVEL.get(), "§c赤火铲");
        add(ModItems.RED_FIRE_HOE.get(), "§c赤火锄");

        add(ModItems.SUNKEN_SILVER_SWORD.get(), "§f沉银剑");
        add(ModItems.SUNKEN_SILVER_PICKAXE.get(), "§f沉银镐");
        add(ModItems.SUNKEN_SILVER_AXE.get(), "§f沉银斧");
        add(ModItems.SUNKEN_SILVER_SHOVEL.get(), "§f沉银铲");
        add(ModItems.SUNKEN_SILVER_HOE.get(), "§f沉银锄");

        add(ModItems.COLD_HEARTED_STEEL_SWORD.get(), "§7寒心钢剑");
        add(ModItems.COLD_HEARTED_STEEL_PICKAXE.get(), "§7寒心钢镐");
        add(ModItems.COLD_HEARTED_STEEL_AXE.get(), "§7寒心钢斧");
        add(ModItems.COLD_HEARTED_STEEL_SHOVEL.get(), "§7寒心钢铲");
        add(ModItems.COLD_HEARTED_STEEL_HOE.get(), "§7寒心钢锄");

        add(ModItems.RINSEI_SWORD.get(), "§b凛晶剑");
        add(ModItems.RINSEI_PICKAXE.get(), "§b凛晶镐");
        add(ModItems.RINSEI_AXE.get(), "§b凛晶斧");
        add(ModItems.RINSEI_SHOVEL.get(), "§b凛晶铲");
        add(ModItems.RINSEI_HOE.get(), "§b凛晶锄");

        //生物蛋
        add(ModItems.ICE_CRYSTAL_SPAWN_EGG.get(), "冰晶刷怪蛋");
        add(ModItems.SNOW_DEMON_SPAWN_EGG.get(), "雪魔刷怪蛋");

        //实体类
        add(EntityInit.HUNHUAN.get(),"§b魂环");
        add(EntityInit.HUNHE.get(),"§e魂核");
        add(EntityInit.ICE_CRYSTAL.get(), "§b冰晶");
        add(EntityInit.ICE_SHARD.get(), "§b冰凌");
        add(EntityInit.SNOW_DEMON.get(),"§b雪魔");

        //装备类
        add(ModItems.GRAY_IRON_HELMET.get(),"§8灰铁头盔");
        add(ModItems.GRAY_IRON_CHESTPLATE.get(),"§8灰铁胸甲");
        add(ModItems.GRAY_IRON_LEGGINGS.get(),"§8灰铁护腿");
        add(ModItems.GRAY_IRON_BOOTS.get(),"§8灰铁靴子");

        add(ModItems.CLOUD_PATTERNED_BRONZE_HELMET.get(),"§6云纹铜头盔");
        add(ModItems.CLOUD_PATTERNED_BRONZE_CHESTPLATE.get(),"§6云纹铜胸甲");
        add(ModItems.CLOUD_PATTERNED_BRONZE_LEGGINGS.get(),"§6云纹铜护腿");
        add(ModItems.CLOUD_PATTERNED_BRONZE_BOOTS.get(),"§6云纹铜靴子");

        add(ModItems.RED_FIRE_HELMET.get(), "§c赤火头盔");
        add(ModItems.RED_FIRE_CHESTPLATE.get(), "§c赤火胸甲");
        add(ModItems.RED_FIRE_LEGGINGS.get(), "§c赤火护腿");
        add(ModItems.RED_FIRE_BOOTS.get(), "§c赤火靴子");

        add(ModItems.SUNKEN_SILVER_HELMET.get(), "§f沉银头盔");
        add(ModItems.SUNKEN_SILVER_CHESTPLATE.get(), "§f沉银胸甲");
        add(ModItems.SUNKEN_SILVER_LEGGINGS.get(), "§f沉银护腿");
        add(ModItems.SUNKEN_SILVER_BOOTS.get(), "§f沉银靴子");

        add(ModItems.COLD_HEARTED_STEEL_HELMET.get(), "§7寒心钢头盔");
        add(ModItems.COLD_HEARTED_STEEL_CHESTPLATE.get(), "§7寒心钢胸甲");
        add(ModItems.COLD_HEARTED_STEEL_LEGGINGS.get(), "§7寒心钢护腿");
        add(ModItems.COLD_HEARTED_STEEL_BOOTS.get(), "§7寒心钢靴子");

        add(ModItems.RINSEI_HELMET.get(), "§b凛晶头盔");
        add(ModItems.RINSEI_CHESTPLATE.get(), "§b凛晶胸甲");
        add(ModItems.RINSEI_LEGGINGS.get(), "§b凛晶护腿");
        add(ModItems.RINSEI_BOOTS.get(), "§b凛晶靴子");

        //方块类
        add(ModBlocks.CULTIVATION_PLATFORM.get(),"重修台");
        add(ModBlocks.GRAY_IRON_ORE.get(),"灰铁矿");
        add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(),"云纹铜矿");
        add(ModBlocks.RED_FIRE_ORE.get(), "赤火矿");
        add(ModBlocks.SUNKEN_SILVER_ORE.get(), "沉银矿");
        add(ModBlocks.COLD_HEARTED_STEEL_ORE.get(), "寒心钢矿");

        add(ModBlocks.RUBY_ORE.get(), "深层红宝石原矿");
        add(ModBlocks.AMETHYST_ORE.get(),"紫瑛原矿");
        add(ModBlocks.SAPPHIRE_ORE.get(), "蓝晶原矿");
        add(ModBlocks.STARLIGHT_STONE_ORE.get(), "星辰石矿");

        add(ModBlocks.PUTUAN_BLOCK.get(),"蒲团");
        add("putuan.xiulian.finish","§c你感到浑身清爽，但打坐时间太长你感到有被心魔入侵的风险，出去走走吧");
        add(ModBlocks.DROSS_BLOCK.get(), "丹渣块");
        add("tooltip.kunluncontinent.dross_block","放入炼丹炉\n可明显减少破碎丹药概率");

        //武魂果实
        add(ModItems.GUOSHI_POHUNQIANG.get(), "§c武魂果实 - 破魂枪");
        add(ModItems.GUOSHI_BAHUANGJI.get(), "§c武魂果实 - 八荒戟");
        add(ModItems.GUOSHI_LEIJINHU.get(), "§c武魂果实 - 裂金虎");
        add(ModItems.GUOSHI_PANSHIJUYUAN.get(), "§c武魂果实 - 磐石巨像");
        add("服用后觉醒武魂", "§2服用后觉醒%s武魂");

        //JEI类
        add("liandalu","炼丹炉");

        //创造物品栏
        add("itemGroup.kunlun_tab","昆仑大陆");

        //御寒魂导器
        add(ModItems.LOW_COLD_PROTECTION.get(), "低阶御寒魂导器");
        add(ModItems.HIGH_COLD_PROTECTION.get(), "高阶御寒魂导器");
        add(ModItems.MID_COLD_PROTECTION.get(), "中阶御寒魂导器");
        add(ModItems.TOP_COLD_PROTECTION.get(), "顶阶御寒魂导器");

        //按键
        add("attribute_mapping","玩家属性面板");
        add("kunluncontinent","昆仑大陆");
        add("kaiguan_mapping","武魂开关");
        add("key.kunlun.skill_wheel","技能选择面板");
        add("key.kunlun.release_skill","释放技能");

        //GUI
        add("gui.kunluncontinent.shift_hint","§e 提示：按住Shift键查看详细数值信息");

        //武魂类
        add("未开启或觉醒武魂", "§c未开启或觉醒武魂");
        add("成功觉醒武魂", "§c成功觉醒武魂§c%s");
        add("武魂已开启", "<<§c%s>> §a武魂已开启");
        add("武魂已关闭", "§a武魂已关闭");
        add("请开启武魂","§c请先开启武魂");
        add("阶段等级","§c请先升级到下一个等级阶段再吸收魂环（10的倍数）");
        add("等级不足","等级不足！当前等级无法承载更多魂环。");
        add("需要吸收魂环","需要吸收魂环才能继续突破");

        //药水类
        add(ModEffects.ARMOR_PIERCING.get(),"§9破甲");
        add(ModEffects.SCORCHING.get(),"§c灼烧");
        add(ModEffects.DIZZINESS.get(),"§6眩晕");
        add(ModEffects.EXTREME_COLD.get(),"§d极寒");
        add(ModEffects.RED_SPIDER_LILY_POTION.get(),"§c忘川渡");

        //普通文字
        add("心神受损","你受到攻击，心神受损，被迫停止了修炼！");
        add("tooltip.kunlun.neidan_item","击杀不同年限生物概率掉落");
        add("tooltip.kunlun.neidan_item_tier","高品质内丹低概率掉落");
        add("tooltip.kunlun.instant_kill_sword.1","代码级秒杀：无视防御，强制抹除数据。");
        add("tooltip.kunlun.instant_kill_sword.2","世间万物，皆为定数；唯我一剑，可断因果。");
        add("tooltip.kunluncontinent.extreme_cold_snowflake1","需右键最底下传送门框架");
        add("tooltip.kunluncontinent.extreme_cold_snowflake2","从左往右的第二个方块最上面的一面激活传送门");
        add("tooltip.item.klitem.eyetf","§7可重复使用");
        add("item.red_spider_seeds.tooltip","§7生成在废弃地狱门附近的地狱岩上");

        //草药
        add(ModItems.RED_SPIDER_LILY_ITEM.get(), "§c彼岸花");

        //种子
        add(ModItems.RED_SPIDER_SEEDS.get(), "§c彼岸花种子");
    }
}
