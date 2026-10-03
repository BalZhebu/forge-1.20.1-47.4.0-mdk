package com.TovidY.kunluncontinent.item.baseskillist;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 由 {@link SkillSpec} 驱动的魂技物品 —— 一个类吃下所有"声明式魂技"。
 *
 * <p>新增一条魂技的完整流程：</p>
 * <ol>
 *   <li>在 {@code ModItems} 里写一条 {@code skillVariant("skill_xx_1b", 图标来源, new SkillSpec(...))}；</li>
 *   <li>在 {@code SkillLibrary} 对应槽位的池子里加上这个 RegistryObject；</li>
 *   <li>在 {@code ModZhCnLangProvider} 加名字（add(Item, 名字)）和描述（descKey）。</li>
 * </ol>
 * 图标、模型、存档、随机挑选全部自动处理，不再需要为每条魂技建一个类和一张贴图。
 */
public class LambdaSkillItem extends BaseSkillItem {

    private final SkillSpec spec;

    public LambdaSkillItem(SkillSpec spec) {
        super();
        this.spec = spec;
    }

    public SkillSpec spec() {
        return spec;
    }

    @Override
    public float getBaseCost() {
        return spec.baseCost();
    }

    @Override
    public float getDamageMultiplier() {
        return spec.damageMultiplier();
    }

    @Override
    public int getCastTime() {
        return spec.castTime();
    }

    @Override
    public int getCooldownTicks() {
        return spec.cooldownTicks();
    }

    @Override
    public String getDescriptionKey() {
        return spec.descKey();
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        spec.effect().execute(serverLevel, serverPlayer, powerMultiplier, finalDamage);
    }
}
