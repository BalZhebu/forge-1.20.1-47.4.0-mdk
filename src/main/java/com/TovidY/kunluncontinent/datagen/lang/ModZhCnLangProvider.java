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


        //刻刀类
        add(ModItems.IRON_ENGRAVING_KNIFE.get(), "铁制刻刀");
        add(ModItems.DIAMOND_ENGRAVING_KNIFE.get(), "钻石制刻刀");
        //核心类
        add(ModItems.LOW_LEVEL_HEXIN.get(), "§7低级丹炉核心");
        add(ModItems.HIGH_LEVEL_HEXIN.get(), "§4顶级丹炉核心");
        add(ModItems.MIDDLE_LEVEL_HEXIN.get(), "§6中级丹炉核心");

        //矿石类
        add(ModItems.GRAY_IRON_INGOT.get(),"§8灰铁锭");
        add(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),"§6云纹铜");
        add(ModItems.RED_FIRE_INGOT.get(), "§c赤火锭");
        add(ModItems.RUBY.get(), "§c红宝石");

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
        add("tooltip.kunluncontinent.liandanlu","不同等阶的炼丹炉只不过是炼丹速度上的差异");

        //测试物品类
        add(ModItems.DANYAO_TEST.get(),"测试物品---经验+9999999");
        add(ModItems.DANYAO_JINGSHENLI.get(),"测试物品---精神力+100%");
        add(ModItems.TEST_SWORD.get(),"测试物品---剑");
        add(ModItems.DANYAO_DENGJI_JIA.get(),"测试物品---等级+1");
        add(ModItems.DANYAO_DENGJI_JIAN.get(),"测试物品---等级-1");

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

        //实体类
        add(EntityInit.HUNHUAN.get(),"§b魂环");
        add(EntityInit.HUNHE.get(),"§e魂核");

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

        //方块类
        add(ModBlocks.CULTIVATION_PLATFORM.get(),"修炼台");
        add(ModBlocks.GRAY_IRON_ORE.get(),"灰铁矿");
        add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(),"云纹铜矿");
        add(ModBlocks.RED_FIRE_ORE.get(), "赤火矿");
        add(ModBlocks.RUBY_ORE.get(), "深层红宝石原矿");
        add(ModBlocks.PUTUAN_BLOCK.get(),"蒲团");
        add("putuan.xiulian.finish","§c你感到浑身清爽，但打坐时间太长你感到有被心魔入侵的风险，出去走走吧");
        add(ModBlocks.DROSS_BLOCK.get(), "丹渣块");

        //JEI类
        add("liandalu","炼丹炉");

        //创造物品栏
        add("itemGroup.kunlun_tab","昆仑大陆");

        //按键
        add("attribute_mapping","玩家属性面板");
        add("kunluncontinent","昆仑大陆");
        add("kaiguan_mapping","武魂开关");

        //GUI
        add("gui.kunluncontinent.shift_hint","§e 提示：按住Shift键查看详细数值信息");

        //武魂类
        add("未开启或觉醒武魂", "§c未开启或觉醒武魂");
        add("成功觉醒武魂", "§c成功觉醒武魂§c%s");
        add("武魂已开启", "<<§c%s>> §a武魂已开启");
        add("武魂已关闭", "§a武魂已关闭");
        add("请开启武魂","§c请先开启武魂");
        add("阶段等级","§c请先升级到下一个等级阶段再吸收魂环（10的倍数）");
        add("需要吸收魂环","需要吸收魂环才能继续突破");

        //药水类
        add(ModEffects.ARMOR_PIERCING.get(),"§9破甲");
        add(ModEffects.SCORCHING.get(),"§c灼烧");
        add(ModEffects.DIZZINESS.get(),"§6眩晕");

        //普通文字
        add("心神受损","你受到攻击，心神受损，被迫停止了修炼！");
        add("tooltip.kunlun.neidan_item","击杀不同年限生物概率掉落");
        add("tooltip.kunlun.neidan_item_tier","内屏高品质低概率掉落");

    }
}
