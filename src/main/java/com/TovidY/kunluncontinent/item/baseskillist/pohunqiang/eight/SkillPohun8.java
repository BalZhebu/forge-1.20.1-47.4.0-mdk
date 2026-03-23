package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.eight;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkillPohun8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 700f; }
    @Override public float getDamageMultiplier() { return 3.8f; }
    @Override public int getCastTime() { return 15; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.pohunqiang.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            player.displayClientMessage(Component.literal("§c§l第八魂技：裂空！"), true);
            for (int i = 0; i < 20; i += 3) {
                double px = player.getX() + look.x * i;
                double py = player.getY() + 1.0;
                double pz = player.getZ() + look.z * i;
                AreaEffectCloud voidCloud = new AreaEffectCloud(serverLevel, px, py, pz);
                voidCloud.setOwner(player);
                voidCloud.setRadius(2.0f);
                voidCloud.setDuration(200);
                voidCloud.setWaitTime(0);
                voidCloud.setRadiusPerTick(0);
                voidCloud.setParticle(ParticleTypes.SQUID_INK);
                voidCloud.addTag("PohunVoidZone");
                voidCloud.getPersistentData().putFloat("VoidDamage", finalDamage * 0.4f);

                serverLevel.addFreshEntity(voidCloud);
            }
        }
    }
}