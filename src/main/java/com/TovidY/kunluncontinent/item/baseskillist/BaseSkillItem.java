package com.TovidY.kunluncontinent.item.baseskillist;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

public abstract class BaseSkillItem extends Item {
    public BaseSkillItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    public abstract float getBaseCost();
    public abstract float getDamageMultiplier();
    public abstract int getCastTime();
    public abstract int getCooldownTicks();

    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            float playerGongji = ModAttributeAPI.getGongji(player);
            float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;

            this.executeEffect(level, player, powerMultiplier, finalDamage);
        }
    }

    /**
     * NPC 释放技能专属入口（利用 FakePlayer 完美兼容现有 27 个技能类）
     */
    public void executeEffectForNpc(ServerLevel serverLevel, PlayerNpcEntity npc, float powerMultiplier, float finalDamage) {
        var fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
        fakePlayer.setPos(npc.getX(), npc.getY(), npc.getZ());
        fakePlayer.setXRot(npc.getXRot());
        fakePlayer.setYRot(npc.getYRot());
        var atkAttr = fakePlayer.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atkAttr != null) {
            atkAttr.setBaseValue(npc.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        this.executeEffect(serverLevel, fakePlayer, powerMultiplier, finalDamage);
    }

    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {

    }

    public String getDescriptionKey() {
        return this.getDescriptionId() + ".description";
    }

    public Component getDynamicDescription(float costMultiplier) {
        float finalCost = getBaseCost() * costMultiplier;
        return Component.translatable(getDescriptionKey(), String.format("%.1f", finalCost));
    }

    public float getPowerMultiplier(long nianxian) {
        if (nianxian >= 100000000) return 50.0f;
        if (nianxian >= 10000000) return 25.0f;
        if (nianxian >= 1000000) return 15.0f;
        if (nianxian >= 100000) return 8.0f;
        if (nianxian >= 10000) return 4.0f;
        if (nianxian >= 1000) return 2.5f;
        if (nianxian >= 100) return 1.5f;
        return 1.0f;
    }

    public float getCostMultiplier(int nianxian) {
        if (nianxian <= 100) return 1.0f;
        float costFactor = (float) (1.0 + Math.log10(nianxian / 10.0) * 0.4);
        return Math.min(3.5f, costFactor);
    }

    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float finalCost = getBaseCost() * costMultiplier;
            cap.setJingshenli(Math.max(0, cap.getJingshenli() - finalCost));
        });
    }

    public void handleRelease(Level level, Player player, int nianxian) {
        float costMultiplier = getCostMultiplier(nianxian);
        float finalCost = getBaseCost() * costMultiplier;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getJingshenli() >= finalCost && !player.getCooldowns().isOnCooldown(this)) {
                float powerMultiplier = getPowerMultiplier(nianxian);
                float playerGongji = ModAttributeAPI.getGongji(player);
                float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;
                executeEffect(level, player, powerMultiplier, finalDamage);

                applyPenalty(player, costMultiplier);
                player.getCooldowns().addCooldown(this, getCooldownTicks());
                SynsAPI.synsPlayerAttribute(player);
            }
        });
    }
}