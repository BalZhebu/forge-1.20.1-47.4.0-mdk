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

        //方块类
        add(ModBlocks.STONE_STAMP.get(), "挑战石碑");
        add(ModBlocks.SUMMON_TOWER.get(), "战斗石碑");
        add(ModBlocks.SPIRIT_GATHERING_ALTAR.get(),"§e聚灵台");
        add(ModBlocks.SPIRIT_GATHERING_STONE.get(),"空的聚灵基柱" );
        add(ModBlocks.SPIRIT_GATHERING_STONE_0.get(), "§5低阶聚灵基柱");
        add(ModBlocks.SPIRIT_GATHERING_STONE_1.get(), "§c中阶聚灵基柱");
        add(ModBlocks.SPIRIT_GATHERING_STONE_2.get(), "§e高阶聚灵基柱");

        add("spirit_gathering_stone_0_mees","放在聚灵台半径2格内");
        add("spirit_gathering_stone_1_mees","聚灵基柱出现粒子特效代表链接成功");
        add("spirit_gathering_stone_2_mees","每种等阶的聚灵基柱拥有不同的强度增幅");

        add(ModBlocks.GRAY_BLOCK.get(), "§7灰铁块");
        add(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get(), "§5云纹铜块");
        add(ModBlocks.RED_FIRE_BLOCK.get(),"§c赤火块");
        add(ModBlocks.SUNKEN_SILVER_BLOCK.get(), "§f沉银块");
        add(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get(), "§9寒心钢块");
        add(ModBlocks.RINSEI_BLOCK.get(), "§b凛晶块");

        //别忘了写分配技能的类
        //技能代码
        add(ModItems.SKILL_POHUN_1.get(), "§c《破魂枪》第一魂技[破魂枪]");
        add(ModItems.SKILL_POHUN_2.get(), "§c《破魂枪》第二魂技[枪芒]");
        add(ModItems.SKILL_POHUN_3.get(), "§c《破魂枪》第三魂技[破甲]");
        add(ModItems.SKILL_POHUN_4.get(), "§c《破魂枪》第四魂技[贯日]");
        add(ModItems.SKILL_POHUN_5.get(), "§c《破魂枪》第五魂技[碎星]");
        add(ModItems.SKILL_POHUN_6.get(), "§c《破魂枪》第六魂技[巨枪]");
        add(ModItems.SKILL_POHUN_7.get(), "§c《破魂枪》第七魂技[武魂真身]");
        add(ModItems.SKILL_POHUN_8.get(), "§c《破魂枪》第八魂技[裂空]");
        add(ModItems.SKILL_POHUN_9.get(), "§c《破魂枪》第九魂技[弑神]");

        add(ModItems.SKILL_BAHUANG_1.get(), "§c《八荒戟》第一魂技[八荒戟]");
        add(ModItems.SKILL_BAHUANG_2.get(), "§c《八荒戟》第二魂技[横扫]");
        add(ModItems.SKILL_BAHUANG_3.get(), "§c《八荒戟》第三魂技[劈山]");
        add(ModItems.SKILL_BAHUANG_4.get(), "§c《八荒戟》第四魂技[断江]");
        add(ModItems.SKILL_BAHUANG_5.get(), "§c《八荒戟》第五魂技[镇岳]");
        add(ModItems.SKILL_BAHUANG_6.get(), "§c《八荒戟》第六魂技[破军]");
        add(ModItems.SKILL_BAHUANG_7.get(), "§c《八荒戟》第七魂技[武魂真身]");
        add(ModItems.SKILL_BAHUANG_8.get(), "§c《八荒戟》第八魂技[降雷]");
        add(ModItems.SKILL_BAHUANG_9.get(), "§c《八荒戟》第九魂技[八荒寂灭]");

        add(ModItems.SKILL_LEIJINHU_1.get(), "§c《裂金虎》第一魂技[横爪]");
        add(ModItems.SKILL_LEIJINHU_2.get(), "§c《裂金虎》第二魂技[虎啸]");
        add(ModItems.SKILL_LEIJINHU_3.get(), "§c《裂金虎》第三魂技[裂石]");
        add(ModItems.SKILL_LEIJINHU_4.get(), "§c《裂金虎》第四魂技[碎金]");
        add(ModItems.SKILL_LEIJINHU_5.get(), "§c《裂金虎》第五魂技[扑杀]");
        add(ModItems.SKILL_LEIJINHU_6.get(), "§c《裂金虎》第六魂技[啸月]");
        add(ModItems.SKILL_LEIJINHU_7.get(), "§c《裂金虎》第七魂技[武魂真身]");
        add(ModItems.SKILL_LEIJINHU_8.get(), "§c《裂金虎》第八魂技[裂天]");
        add(ModItems.SKILL_LEIJINHU_9.get(), "§c《裂金虎》第九魂技[猛虎破界]");

        add(ModItems.SKILL_PANSHIJUYUAN_1.get(), "§c《磐石巨猿》第一魂技[重击]");
        add(ModItems.SKILL_PANSHIJUYUAN_2.get(), "§c《磐石巨猿》第二魂技[石肤]");
        add(ModItems.SKILL_PANSHIJUYUAN_3.get(), "§c《磐石巨猿》第三魂技[震地]");
        add(ModItems.SKILL_PANSHIJUYUAN_4.get(), "§c《磐石巨猿》第四魂技[搬山]");
        add(ModItems.SKILL_PANSHIJUYUAN_5.get(), "§c《磐石巨猿》第五魂技[撼地]");
        add(ModItems.SKILL_PANSHIJUYUAN_6.get(), "§c《磐石巨猿》第六魂技[裂地]");
        add(ModItems.SKILL_PANSHIJUYUAN_7.get(), "§c《磐石巨猿》第七魂技[武魂真身]");
        add(ModItems.SKILL_PANSHIJUYUAN_8.get(), "§c《磐石巨猿》第八魂技[撼岳]");
        add(ModItems.SKILL_PANSHIJUYUAN_9.get(), "§c《磐石巨猿》第九魂技[不周倾]");

        add("无技能","暂无技能描述");

        add("skill.pohunqiang.one.description", "消耗%s点精神力，凝结武魂实体化。");
        add("skill.pohunqiang.two.description", "消耗%s点精神力，释放枪芒：向前冲刺，对路径上的敌人造成伤害");
        add("skill.pohunqiang.three.description", "消耗%s点精神力，释放破甲：先前冲刺，对最近的单独一个敌人造成无视30%%防御力的真实伤害");
        add("skill.pohunqiang.four.description", "消耗%s点精神力，释放贯日：掷出长枪，化作一道流光贯穿直线上的所有敌人，最远可达20格以外。");
        add("skill.pohunqiang.five.description", "消耗%s点精神力，释放碎星：对自身15格范围内的所有实体造成持续性高额伤害，持续5秒");
        add("skill.pohunqiang.six.description", "消耗%s点精神力，释放巨枪：对距离玩家1格面前召唤巨枪落下来像陨石一样砸下来，造成巨额伤害");
        add("skill.pohunqiang.7.description", "消耗%s点精神力，释放武魂真身，全属性增幅自身");
        add("skill.pohunqiang.eight.description","消耗%s点精神力，释放裂空，将自身面前空气斩出虚空，让身处虚空处的敌人每秒收到伤害。");
        add("skill.pohunqiang.nine.description","消耗%s点精神力，释放弑神：对单独一个生物造成巨额伤害，并给对方附加虚弱2，同时有5%%的概率直接斩杀，并且，斩杀面对玩家时，玩家等级越高，斩杀概率越高");

        add("skill.bakuangji.one.description", "消耗%s点精神力，凝结武魂实体化。");
        add("skill.bahuangji.two.description", "消耗%s点精神力，释放横扫：对面前扇形范围敌人造成伤害");
        add("skill.bahuangji.three.description", "消耗%s点精神力，释放劈山：跳劈对范围敌人造成巨额伤害");
        add("skill.bahuangji.four.description", "消耗%s点精神力，释放断江：横向挥出，化作一道半月形光刃，宽4格可向前飞行10格。");
        add("skill.bahuangji.five.description", "消耗%s点精神力，释放镇岳：对玩家自身30格范围内的所有生物获得缓慢2的buff，并造成高额伤害");
        add("skill.bahuangji.six.description", "消耗%s点精神力，释放破军：强制玩家向前猛冲30格，对路径上敌人造成巨额伤害");
        add("skill.bahuangji.7.description", "消耗%s点精神力，释放武魂真身，全属性增幅自身");
        add("skill.bahuangji.eight.description","消耗%s点精神力，释放降雷，对自身范围50格内的所有敌人造成雷击伤害，无视高度。");
        add("skill.bahuangji.nine.description","消耗%s点精神力，释放八荒寂灭：对玩家为中心，向外扩散金色光环，光环所过之处生物受到高额伤害的同时获得5秒缓慢，共会发射10道光环，每一个光环释放时间间隔1.5秒，光环最远可达20格以外");

        add("skill.leijinhu.one.description", "消耗%s点精神力，释放横爪，可击打范围4格内的敌人");
        add("skill.liejinhu.two.description","消耗%s点精神力，释放虎啸：释放技能时对距离玩家越近的敌人造成虚弱和缓慢效果，对5格以外的敌人造成缓慢效果");
        add("skill.liejinhu.three.description","消耗%s点精神力，释放裂石：对范围敌人造成巨额伤害并附加缓慢");
        add("skill.leijinhu.four.description", "消耗%s点精神力，释放碎金：对面前3*3*3范围内的敌人造成大量伤害，可以破坏硬度较低的方块");
        add("skill.leijinhu.five.description", "消耗%s点精神力，释放扑杀：单体技能，对一个实体造成高额伤害的同时，有极低概率直接斩杀对方，斩杀特效不论对方实力强弱，触发后顷刻斩杀");
        add("skill.leijinhu.six.description", "消耗%s点精神力，释放啸月：在白天使用获得大日之威效果20秒，在夜晚使用获得邀月之华效果25秒。buff提供全属性加成。");
        add("skill.liejinhu.7.description", "消耗%s点精神力，释放武魂真身，全属性增幅自身");
        add("skill.leijinhu.eight.description","消耗%s点精神力，释放裂天：对自身半径20格范围内造成10次高额伤害。");
        add("skill.leijinhu.nine.description","消耗%s点精神力，释放猛虎破界：对10格内敌人造成巨额伤害的同时附加缓慢，虚弱，反胃时长15秒，并在攻击到的实体处破碎虚空，身处虚空的生物持续受到伤害");

        add("skill.panshijuyuan.one.description", "消耗%s点精神力，释放重击：可击打范围5格内的敌人并附加缓慢效果");
        add("skill.panshijuyuan.two.description","消耗%s点精神力，释放石肤：对自身附加30%%的防御力");
        add("skill.panshijuyuan.three.description","消耗%s点精神力，释放震地：对10格内敌人造成巨额伤害");
        add("skill.panshijuyuan.four.description", "消耗%s点精神力，释放搬山：给自身附加80%%的攻击力");
        add("skill.panshijuyuan.five.description", "消耗%s点精神力，释放撼地：给自身附加85%%的攻击力，70%%的防御力");
        add("skill.panshijuyuan.six.description", "消耗%s点精神力，释放裂地：强制跳起来5格以上高度，狠狠砸向地面，对范围20格内敌人造成高额伤害");
        add("skill.panshijuyuan.7.description", "消耗%s点精神力，释放武魂真身，全属性增幅自身");
        add("skill.panshijuyuan.eight.description","消耗%s点精神力，释放憾岳：对面自身前5格内敌人造成极高倍率的伤害");
        add("skill.panshijuyuan.nine.description","消耗%s点精神力，释放不周倾：对强制跳跃15格，落下后对周围100格范围里敌人造成恐怖伤害，并附加缓慢3，虚弱3，反胃，饥饿时长15秒");


        //锻造模版
        add(ModItems.RINSEI_FORGING_TEMPLATE.get(), "§b凛晶锻造模版");

        //引导书
        add(ModItems.GUIDE_BOOK.get(), "§b《昆仑大陆 · 引导书》");
        add("guide.kunlun.chapter1.content","§1§l《欢迎来到昆仑大陆！祝您玩的愉快 ~ 》\n[本引导书更新于’1.0‘版本]§r\n§c§l请各位玩家务必认真仔细查看，基本收录了正常游玩时遇到的所有常见攻略" +
                "\n§c以下为基础教程：§r" +
                "\n§l属性面板系统：§r默认按O键打开属性面板，在属性面板下按住Shift键可查询具体数值，例如玩家的攻击力为10000时属性面板会自动缩进变为1万，按下Shift即可查看具体数值。" +
                "\n§l玩家屏幕的GUI属性解释：§r玩家屏幕左上角UI四个属性条分别为：生命值(红色)，饱和度(橙色)，精神力(蓝色)，经验值(绿色)。经验条的左侧下面一点位置的数字代表是你当前的等级。注意！当玩家做出某种更新数值的举动（进入维度等）数值会变的异常，这是正常现象等待几秒即可恢复。" +
                "\n§l技能系统：§r默认长按R键可打开技能栏，按V键可释放选中的技能，按住R键时用鼠标滑动选择技能，鼠标移动到对应技能灰色滑块后松开R键即可选择技能，选择不松开R键的话技能栏右侧会出现技能的相关介绍，点按R可快速按顺序切换技能。魂技的威力关乎于吸收魂环的年限，年限越大魂技越强，消耗精神力越多。" +
                "\n§l觉醒武魂：§r玩家击杀生物、炼制丹药、用蒲团修炼，都可以获得经验，经验值满了后自动突破下一个等级，当玩家第一次突破成功时，将会觉醒武魂并分配玩家的天赋。" +
                "\n§l天赋系统：§r天赋在玩家觉醒武魂时出现，普通天赋最为常见，废物和天才天赋概率一致，是废物还是天才都在一念之间，不同的天赋会略微影响一点点数值，但后期均可通过努力提升。" +
                "\n§l武魂系统：§r默认按K键可打开武魂，打开武魂时会消耗精神力，打开武魂时可以吸收魂环。" +
                "\n§l魂环吸收:§r玩家每到10/20/30级等10的倍数的等级时，会锁定等级，必须吸收魂环才可突破下一阶段，吸收时需打开武魂后右键生物掉落的魂环坐上去后消耗精神力吸收魂环，精神力不足将停止吸收，吸收进度重置。" +
                "\n§l魂核系统：§r魂核是魂环分解后或者魂环消失后生成的产物，，会随着时间流逝消失，可用聚魂瓶收集起来。" +
                "\n§l重修&转世系统：§r玩家在主世界自然生成的地狱门遗迹可找到生成在地狱岩上的彼岸花，将彼岸花磨成粉可炼制忘川渡buff，喝下后找到重修遗迹，再自己身上有buff的情况下右键重修台就会遭受99道雷劫，但记住！雷劫不致命！请不要用任何手段恢复血量，因为重修需要玩家血量到达20以内才能成功转世，若99道天雷后你血量依旧健康，那么你的转世重修就会失败。重修会使玩家的武魂、魂环、属性、技能配置等全部重置，玩家拥有什么武魂就会给与对应的武魂果实。" +
                "\n§l重修遗迹：§r通过重修之眼可找到重修遗迹，重修之眼使用方法和末影之眼一致。" +
                "\n§l聚灵台阵：§r将聚灵台放置中心，半径2格也就是范围5*5*2的范围内放置聚魂基石柱可解锁其中的槽位，根据下方的强度达到每多少秒恢复能量值，最多可吃到8个聚魂基石柱的增幅，多余的根据等阶高的优先。" +
                "\n§l武魂果实：§r武魂果实将在玩家转生后会根据玩家拥有的武魂将武魂果实给予给玩家，吃下武魂果实即可觉醒对应的武魂，玩家最多觉醒3个武魂。" +
                "\n§l内丹系统：§r击杀不同年限的生物会掉落不同品质的内丹，内丹可以炼制经验丹药，不同等级的炼丹炉炼制速度不一样，例如1级炼丹炉炼制1级丹药需要10秒，而炼制9级丹药需要10秒的100倍的时间。" +
                "\n§l丹药品级系统：§r不同品质的内丹炼制出的丹药品质不一样，使用时的倍率不一样，详情自己炼制查看。炼制时可能出现丹渣品质的丹药，放入合成台一个丹渣品质丹药可合成一个丹渣，丹渣9个合成一个丹渣块，可减少炼制丹药时丹渣的概率。" +
                "\n§l年限生成系统：§r玩家距离世界坐标越远，生成高年限的生物概率越高，最远10000格以外，，到达一万格后，这个值将会到达极限。" +
                "\n§l魂骨系统：§r玩家在击杀生物时有极低概率掉落魂骨（击杀生物年限在1000万年以上时魂骨百分百掉落），魂骨是词条制，最低为1词条，最高为10词条。词条越多，概率越低。" +
                "\n§l蒲团修炼系统：§r玩家在前期难以修炼时可以用蒲团修炼，玩家在前期时修满一个周期（10分钟）可直升两级，修炼时玩家有10分钟修炼时间，时间结束后就会被强制停止修炼，可通过除不修炼的任何情况下都可以恢复这个修炼时间，提示：若玩家修炼时间已经结束，但仍然可以修炼，但这次修炼只会恢复精神力，而不会增加修为。" +
                "\n§l飞行系统：§r当玩家的最大精神力到达5000以上时，玩家将解锁飞行能力，飞行时精神力消耗加快，等级越高消耗越慢。注意！玩家若处于飞行状态则伤害削弱40%,但玩家等级超过70级时，这个限制会逐渐递减，最低可到10%" +
                "\n§l进阶维度系统：§r根据某些特殊生物掉落的物品可合成传送门框架，请查看传送门框架描述来搭建传送门结构，可前往生成高年限生物的维度。" +
                "\n§l特殊攻击系统：§r玩家攻击生物时有低概率触发特殊效果，例如撕裂、燃烧、震撼、湮灭、等特殊效果，特殊效果可增加玩家对生物造成的那次伤害并附加debuff，注意：怪物也可以对你造成这些特殊效果。" +
                "\n§l维度特色讲解：" +
                "\n§r1.极寒冰域：常年冰雪的维度。会生成最低万年，最高百万年的生物，玩家在维度内若没有御寒魂导器的话就会根据玩家自身的等级计算可以撑的时间，时间结束后你将会受到百分比的真实伤害。" +
                "\n§r2.万天雷域：常年下雨降雷的维度。会生成最低万年，最高百万年的生物，玩家在维度内需要电流防御魂器防御自身，否则将有较高概率遭受雷击，雷击会使玩家受到较高的百分比伤害。" +
                "\n§r3.占位符 + ");

        add(ModItems.RED_SPIDER_LILY_POTION.get(), "§c《忘川渡·彼岸花》");
        add(ModItems.EYE_TRANSFORMATION.get(), "§5重修之眼");
        add(ModItems.EXTREME_COLD.get(),"§b雪晶");

        add(ModItems.DEMONWHALE_SPAWN_EGG.get(),"§c魔鲸刷怪蛋");

        add(ModItems.ATTRIBUTE_BUTTON.get(),"属性面板");
        add(ModItems.SOUL_BONE_BUTTON.get(),"魂骨面板");
        add(ModItems.HUNHUAN_BUTTON.get(),"魂环面板");
        add(ModItems.SHENKAO_BUTTON.get(),"神考面板");

        add("item.kunluncontinent.guide_book.tooltip","§7第一次进入游戏即可获得");
        add("item.kunluncontinent.guide_book.tooltip1","§7请务必认真仔细查看");
        add("item.kunluncontinent.guide_book.tooltip2","§7建议不要弄丢");

        add("item.guyuancao_seeds.tooltip","会生成在大部分奖励箱中");
        add("tooltip.kunluncontinent.polar_ice_portal_block","需摆出地狱传送门的样式");

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
        add("advancements.kunluncontinent.gray_iron_ingot.title", "灰铁锭");
        add("advancements.kunluncontinent.gray_iron_ingot.description", "一切的开端");
        add("advancements.kunluncontinent.cloud_patterned_bronze_ingot.title", "云纹铜锭");
        add("advancements.kunluncontinent.cloud_patterned_bronze_ingot.description", "更好的灰铁锭");
        add("advancements.kunluncontinent.red_fire_ingot.title", "赤火锭");
        add("advancements.kunluncontinent.red_fire_ingot.description", "可在下界中找到~");
        add("advancements.kunluncontinent.sunken_silver_ingot.title", "沉银");
        add("advancements.kunluncontinent.sunken_silver_ingot.description", "可在末地中找到");
        add("advancements.kunluncontinent.rinsei_ingot.title", "凛晶");
        add("advancements.kunluncontinent.rinsei_ingot.description", "击杀雪魔后掉落~");
        add("advancements.kunluncontinent.cold_heated_steel_ingot.title", "寒心钢锭");
        add("advancements.kunluncontinent.cold_heated_steel_ingot.description", "可在极寒冰域中找到~！");
        add("advancements.kunluncontinent.ruby.title","红宝石");
        add("advancements.kunluncontinent.ruby.description","一切的开端");
        add("advancements.kunluncontinent.sapphire.title","蓝晶");
        add("advancements.kunluncontinent.sapphire.description","下界的器具矿物");
        add("advancements.kunluncontinent.amethyst.title","紫瑛");
        add("advancements.kunluncontinent.amethyst.description","末地中的宝贵矿石");
        add("advancements.kunluncontinent.starlight_stone.title","星辰石");
        add("advancements.kunluncontinent.starlight_stone.description","异界的宝贵矿石");


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
        add(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get(), "§8万雷天域框架");
        //传送门
        add(ModBlocks.POLAR_ICE_PORTAL.get(), "极寒冰域传送门");
        add(ModBlocks.THUNDER_REALM_PORTAL.get(), "万雷天域传送门");
        //打火石
        add(ModItems.EXTREME_COLD_SNOWFLAKE.get(), "§b极寒石");
        add(ModItems.THUNDERREALM_SNOWFLAKE.get(),"§8雷域石");

        //普通物品
        add(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get(), "§b雪晶碎片");
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
        add(ModItems.GUYUAN_DAN.get(), "§e归元丹");
        add(ModItems.FANQI_DAN.get(), "返气散");
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
        add(EntityInit.DEMON_WHALE.get(), "§c魔鲸");

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

        //物品类
        add(ModItems.DEEPSEA_JINGHUA.get(),"§b海洋精华");
        add("item.deepsea_jinghua.tooltip","击杀水生生物概率掉落");
        add("item.lightning_fragments.tooltip","在220格以上的高度放置避雷针");
        add("item.lightning_fragments.tooltip2","将凛晶扔在避雷针1格范围内");
        add("item.lightning_fragments.tooltip3","等待一段时间后就会变成闪电碎片");
        add(ModItems.EYE_DEEP_SEA.get(),"§b深海之眼");
        add(ModItems.DEEP_SEA_OFFERINGS.get(),"§b深海祭品");
        add(ModItems.DEMON_WHALE_MEDAL.get(),"§1§l深海勋章");
        add(ModItems.DEMON_WHALE_BADGE.get(), "§b魔鲸徽章");
        add(ModItems.LIGHTNING_FRAGMENTS.get(),"§e闪电碎片");

        //方块类
        add(ModBlocks.CULTIVATION_PLATFORM.get(),"重修台");
        add(ModBlocks.GRAY_IRON_ORE.get(),"灰铁矿");
        add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(),"云纹铜矿");
        add(ModBlocks.RED_FIRE_ORE.get(), "赤火矿");
        add(ModBlocks.SUNKEN_SILVER_ORE.get(), "沉银矿");
        add(ModBlocks.COLD_HEARTED_STEEL_ORE.get(), "寒心钢矿");

        add(ModBlocks.UNDERWATER_ALTAR.get(), "海底祭坛");

        add(ModBlocks.RUBY_ORE.get(), "深层红宝石原矿");
        add(ModBlocks.AMETHYST_ORE.get(),"紫瑛原矿");
        add(ModBlocks.SAPPHIRE_ORE.get(), "蓝晶原矿");
        add(ModBlocks.STARLIGHT_STONE_ORE.get(), "星辰石矿");

        add(ModBlocks.PUTUAN_BLOCK.get(),"蒲团");
        add("putuan.xiulian.finish","§c你感到浑身清爽，但打坐时间太长你感到有被心魔入侵的风险，出去走走吧");
        add(ModBlocks.DROSS_BLOCK.get(), "丹渣块");
        add("tooltip.kunluncontinent.dross_block","放入炼丹炉可明显减少破碎丹药概率");
        add(ModBlocks.SOUL_SOIL.get(),"魂土");
        add("item.demon_whale_badge.tooltip","击杀魔鲸必定掉落");

        //武魂果实
        add(ModItems.GUOSHI_POHUNQIANG.get(), "§c武魂果实 - 破魂枪");
        add(ModItems.GUOSHI_BAHUANGJI.get(), "§c武魂果实 - 八荒戟");
        add(ModItems.GUOSHI_LEIJINHU.get(), "§c武魂果实 - 裂金虎");
        add(ModItems.GUOSHI_PANSHIJUYUAN.get(), "§c武魂果实 - 磐石巨猿");
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
        add("成功吸收魂环","成功吸收魂环");

        //药水类
        add(ModEffects.ARMOR_PIERCING.get(),"§9破甲");
        add(ModEffects.SCORCHING.get(),"§c灼烧");
        add(ModEffects.DIZZINESS.get(),"§6眩晕");
        add(ModEffects.EXTREME_COLD.get(),"§d极寒");
        add(ModEffects.RED_SPIDER_LILY_POTION.get(),"§c忘川渡");
        add(ModEffects.STONE_SKIN.get(),"§6石肤");
        add(ModEffects.MOVING_MOUNTAINS.get(),"§d搬山");
        add(ModEffects.STONE_ARMOR.get(),"§c石铠");
        add(ModEffects.SUN_POWER.get(),"§e大日之威");
        add(ModEffects.POWER_OF_THE_MOON.get(),"§d邀月之华");

        add(ModEffects.THE_GAZE_OF_GOD.get(), "§e神之凝视");

        add(ModEffects.POHUNQIANG.get(),"§c破魂真身");
        add(ModEffects.BHUANGJI.get(),"§c八荒真身");
        add(ModEffects.LIEJINHU.get(),"§c裂虎真身");
        add(ModEffects.PANSHIJUYUAN.get(),"§c巨猿真身");

        //普通文字
        add("心神受损","你受到攻击，心神受损，被迫停止了修炼！");
        add("tooltip.kunlun.neidan_item","击杀不同年限生物概率掉落");
        add("tooltip.kunlun.neidan_item_tier","高品质内丹低概率掉落");
        add("tooltip.kunlun.instant_kill_sword.1","代码级秒杀：无视防御，强制抹除数据。");
        add("tooltip.kunlun.instant_kill_sword.2","世间万物，皆为定数；唯我一剑，可断因果。");
        add("tooltip.kunluncontinent.extreme_cold_snowflake1","需右键传送门内侧的面即可激活");
        add("tooltip.item.klitem.eyetf","§7可重复使用");
        add("item.red_spider_seeds.tooltip","§7生成在废弃地狱门附近的地狱岩上");
        add("gui.kunluncontinent.casting","§e正在施法中...");
        add("gui.kunluncontinent.cast_shifa","§a§l魂技释放成功！");
        add("item.rinsei_ingot.tooltip","击杀雪魔概率掉落");
        add("item.guyuancao_seeds.tooltip2","可以种在魂土上");
        add("tooltip.kunluncontinent.soul_soil","可通过聚魂瓶右键耕地转化");

        //适配你在看什么
        add(ModBlocks.RED_SPIDER_LILY_BLOCK.get(), "§c彼岸花");
        add(ModBlocks.GUYUANCAO_BLOCK.get(), "§c归元草");
        add(ModBlocks.FANQICAO_BLOCK.get(), "§b返气草");

        //草药
        add(ModItems.RED_SPIDER_LILY_ITEM.get(), "§c彼岸花");
        add(ModItems.GUYUANCAO_ITEM.get(), "§c归元草");
        add(ModItems.FANQICAO_ITEM.get(), "§b返气草");

        //种子
        add(ModItems.RED_SPIDER_SEEDS.get(), "§c彼岸花种子");
        add(ModItems.GUYUANCAO_SEEDS.get(), "§c归元草种子");
        add(ModItems.FANQICAO_SEEDS.get(), "§b返气草种子");

        add(ModItems.SOUL_GATHERING_BOTTLE_0.get(), "§a一阶聚魂瓶");
        add(ModItems.SOUL_GATHERING_BOTTLE_1.get(), "§e二阶聚魂瓶");
        add(ModItems.SOUL_GATHERING_BOTTLE_2.get(), "§5三阶聚魂瓶");
        add(ModItems.SOUL_GATHERING_BOTTLE_3.get(), "§7四阶聚魂瓶");
        add(ModItems.SOUL_GATHERING_BOTTLE_4.get(), "§c五§b阶§5聚§a魂§e瓶");

        add(ModItems.FIRST_DECOMPOSITION_GOSSIP.get(), "§a一阶魂环分解器");
        add(ModItems.TWO_DECOMPOSITION_GOSSIP.get(), "§e二阶魂环分解器");
        add(ModItems.THREE_DECOMPOSITION_GOSSIP.get(), "§5三阶魂环分解器");
        add(ModItems.FOUR_DECOMPOSITION_GOSSIP.get(), "§7四阶魂环分解器");
        add(ModItems.FIVE_DECOMPOSITION_GOSSIP.get(), "§c五§b阶§5魂§a环§e分§4解§a器");

        add(ModItems.FANGSHANHUNDAOQI_1.get(),"§a一阶电流防御魂器");
        add(ModItems.FANGSHANHUNDAOQI_2.get(),"§e二阶电流防御魂器");
        add(ModItems.FANGSHANHUNDAOQI_3.get(),"§5三阶电流防御魂器");
        add(ModItems.FANGSHANHUNDAOQI_4.get(),"§7四阶电流防御魂器");
        add(ModItems.FANGSHANHUNDAOQI_5.get(),"§c五§b阶§5电流防御魂器");
    }
}
