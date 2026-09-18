package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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

            // ---- 特效：沿视线撕开一道虚空裂隙（暗核 + 两侧锯齿撕口）----
            ParticleFx fx = ParticleFx.of(level, player);
            Vec3 origin = player.position().add(0, 1.0, 0);
            Vec3 riftEnd = origin.add(look.scale(20));
            double rot = serverLevel.getGameTime() * 0.4;

            if (fx != null) {
                fx.budget(1800);
                // 裂隙主轴：暗物质核心 + 外层空间撕裂
                fx.line(ParticleTypes.REVERSE_PORTAL, origin, riftEnd, 0.35, 0.12, 0.0, Vec3.ZERO);
                fx.line(ParticleTypes.SQUID_INK, origin, riftEnd, 0.8, 0.35, 0.0, Vec3.ZERO);
                // 裂隙两侧的锯齿状撕口
                Vec3 side = ParticleFx.ortho(look);
                for (int s = -1; s <= 1; s += 2) {
                    Vec3 offset = side.scale(s * 1.1);
                    fx.lightning(ParticleTypes.SCULK_SOUL, origin.add(offset), riftEnd.add(offset), 26, 0.55, rot + s);
                }
                // 末端空间塌缩
                fx.sphere(ParticleTypes.REVERSE_PORTAL, riftEnd, 1.6, 40, 1.0, 0.08);
                fx.dot(ParticleTypes.FLASH, riftEnd);
            }

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

                // 每个虚空节点套一圈反向封印环（打散环 → 断裂的封印感）
                if (fx != null) {
                    Vec3 node = new Vec3(px, py, pz);
                    fx.dashedRing(ParticleTypes.REVERSE_PORTAL, node, ParticleFx.Axis.Y, 1.9, 10, 0.4, rot + i * 0.25, 0.06);
                    fx.dashedRing(ParticleTypes.SCULK_SOUL, node, ParticleFx.Axis.X, 1.9, 10, 0.4, -rot - i * 0.25, 0.06);
                }
            }
        }
    }
}
