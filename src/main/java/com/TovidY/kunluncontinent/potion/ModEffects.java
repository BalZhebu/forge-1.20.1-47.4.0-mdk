package com.TovidY.kunluncontinent.potion;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.potion.bal.StrongSoulEffect;
import com.TovidY.kunluncontinent.potion.recipespotion.TheGazeOfGodEffect;
import com.TovidY.kunluncontinent.potion.skilleffect.*;
import com.TovidY.kunluncontinent.potion.skilleffect.wuhun.BhuanjiEffect;
import com.TovidY.kunluncontinent.potion.skilleffect.wuhun.LiejinhuEffect;
import com.TovidY.kunluncontinent.potion.skilleffect.wuhun.PanshijuYuanEffect;
import com.TovidY.kunluncontinent.potion.skilleffect.wuhun.PohunEffect;
import com.TovidY.kunluncontinent.potion.specialeffects.ArmorPiercingEffect;
import com.TovidY.kunluncontinent.potion.specialeffects.DizzinessEffect;
import com.TovidY.kunluncontinent.potion.specialeffects.RedSpiderEffect;
import com.TovidY.kunluncontinent.potion.specialeffects.ScorchingEffect;
import com.TovidY.kunluncontinent.potion.specialeffects.coldeffect.ExtremeColdEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

//注册效果
public class ModEffects {
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, KlMain.MOD_ID);

    public static final RegistryObject<MobEffect> ARMOR_PIERCING = REGISTER.register("armor_piercing", ArmorPiercingEffect::new);
    public static final RegistryObject<MobEffect> SCORCHING = REGISTER.register("scorching", ScorchingEffect::new);
    public static final RegistryObject<MobEffect> DIZZINESS = REGISTER.register("dizziness", DizzinessEffect::new);
    public static final RegistryObject<MobEffect> EXTREME_COLD = REGISTER.register("extreme_cold", ExtremeColdEffect::new);
    public static final RegistryObject<MobEffect> RED_SPIDER_LILY_POTION = REGISTER.register("red_spider_lily_potion", RedSpiderEffect::new);
    public static final RegistryObject<MobEffect> STONE_SKIN = REGISTER.register("stone_skin", StoneSkinEffect::new);
    public static final RegistryObject<MobEffect> MOVING_MOUNTAINS = REGISTER.register("moving_mountains", MovumtaEffect::new);
    public static final RegistryObject<MobEffect> STONE_ARMOR = REGISTER.register("stone_armor", StoneArmorEffect::new);
    public static final RegistryObject<MobEffect> SUN_POWER = REGISTER.register("sun_power", SunPowerEffect::new);
    public static final RegistryObject<MobEffect> POWER_OF_THE_MOON = REGISTER.register("power_of_the_moon", PowerOfTheMoonEffect::new);

    // 注册强魂 Buff (参数 1: 属性类别 Beneficial=正面/Harmful=负面/Neutral=中性, 参数 2: 颜色 16进制RGB)
    public static final RegistryObject<MobEffect> STRONG_SOUL = REGISTER.register("strong_soul",
            () -> new StrongSoulEffect(MobEffectCategory.BENEFICIAL, 0x9933FF));

    //武魂真身
    public static final RegistryObject<MobEffect> POHUNQIANG = REGISTER.register("pohunqiang", PohunEffect::new);
    public static final RegistryObject<MobEffect> BHUANGJI = REGISTER.register("bhuangji", BhuanjiEffect::new);
    public static final RegistryObject<MobEffect> LIEJINHU = REGISTER.register("liejinhu", LiejinhuEffect::new);
    public static final RegistryObject<MobEffect> PANSHIJUYUAN = REGISTER.register("panshijuyuan", PanshijuYuanEffect::new);

    //神之凝视
    public static final RegistryObject<MobEffect> THE_GAZE_OF_GOD = REGISTER.register("the_gaze_of_god", TheGazeOfGodEffect::new);

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
