package com.TovidY.kunluncontinent.datagen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, KlMain.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {

        //物品类
        add(ModItems.GRAY_IRON_INGOT.get(),"§8灰铁锭");
        add(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get(),"§6云纹铜");

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

        //方块类
        add(ModBlocks.CULTIVATION_PLATFORM.get(),"修炼台");
        add(ModBlocks.GRAY_IRON_ORE.get(),"灰铁矿");
        add(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(),"云纹铜矿");
        add(ModBlocks.PUTUAN_BLOCK.get(),"蒲团");
        add("putuan.xiulian.finish","§c你感到浑身清爽，但打坐时间太长你感到有被心魔入侵的风险，出去走走吧");

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

        //药水类
        add(ModEffects.ARMOR_PIERCING.get(),"§9破甲");
        add(ModEffects.SCORCHING.get(),"§c灼烧");
        add(ModEffects.DIZZINESS.get(),"§6眩晕");

    }
}
