package com.TovidY.kunluncontinent.item.baseskillist.juyuan.two;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillPanshijuyuan2 extends BaseSkillItem {
    public SkillPanshijuyuan2() {
        super();
    }

    @Override
    public int getCastTime() {
        return 0; // 瞬发技能，强化自身
    }

    @Override
    public int getCooldownTicks() {
        return 400; // 20秒冷却 (20 * 20)
    }

    @Override
    public float getDamageMultiplier() {
        return 0f;
    }

    @Override
    public float getBaseCost() {
        return 100f;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.two.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.STONE_SKIN.get(), 200, 0));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);

            player.displayClientMessage(Component.literal("§6§l第二魂技：石肤！"), true);
        }
    }
}
