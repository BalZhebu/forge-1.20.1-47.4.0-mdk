package com.TovidY.kunluncontinent.tower.skill;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.TovidY.kunluncontinent.KlMain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobTempAttributeManager {

    private static final java.util.Set<Mob> ACTIVATED_BINSI = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    private static class TempTask {
        final Mob mob;
        final MobAttributeType type; // 改变的属性类型
        final float amount;          // 改变的数值（正数为加，负数为减）
        int remainingTicks;
        final boolean isPermanent;   // 是否为永久生效（比如爬塔怪物的被动）

        // 限时任务构造器（如你原本的攻击加成）
        TempTask(Mob mob, MobAttributeType type, float amount, int seconds) {
            this.mob = mob;
            this.type = type;
            this.amount = amount;
            this.remainingTicks = seconds * 20;
            this.isPermanent = false;
        }

        // 永久任务构造器（如爬塔常驻被动）
        TempTask(Mob mob, MobAttributeType type, float amount) {
            this.mob = mob;
            this.type = type;
            this.amount = amount;
            this.remainingTicks = 0;
            this.isPermanent = true;
        }
    }

    public static void applyTempWugong(Mob mob, float amount, int durationSeconds) {
        // 直接转发给新写的高级通用方法，指定属性类型为 WUGONG 即可！
        applyTempAttribute(mob, MobAttributeType.WUGONG, amount, durationSeconds);
    }

    private static final List<TempTask> TASKS = new ArrayList<>();

    /**
     * 1. 外部唯一注入入口：原有的限时属性修改（兼容你原本的临时物攻）
     */
    public static void applyTempAttribute(Mob mob, MobAttributeType type, float amount, int durationSeconds) {
        if (mob == null || !mob.isAlive()) return;

        // 修改属性值并记录任务
        modifyAttributeValue(mob, type, amount);
        TASKS.add(new TempTask(mob, type, amount, durationSeconds));
        SynsAPI.synsEntityAttribute(mob);
    }

    /**
     * 2. 【全新注入入口】：永久属性修改（专门用于爬塔怪物的常驻被动，死后随怪物自动销毁）
     */

    public static void applyPermanentAttribute(Mob mob, MobAttributeType type, float amount) {
        if (mob == null || !mob.isAlive()) return;

        modifyAttributeValue(mob, type, amount);
        TASKS.add(new TempTask(mob, type, amount));
        SynsAPI.synsEntityAttribute(mob);
    }

    private static void modifyAttributeValue(Mob mob, MobAttributeType type, float amount) {
        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            switch (type) {
                case WUGONG:
                    cap.setWugong(cap.getGongji() + amount);
                    break;
                case WUFANG:
                    cap.setWufang(cap.getFangyu() + amount);
                    break;
                case MAX_SHENGMING:
                float newMax = cap.getMaxshengming() + amount;
                cap.setMaxshengming(newMax);
                if (amount > 0) {
                    cap.setShengming(cap.getShengming() + amount);
                } else {
                    cap.setShengming(Math.min(cap.getShengming(), newMax));
                }
                break;
            }
        });
    }

    /**
     * 全局每Tick监听
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) return;

        Iterator<TempTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            TempTask task = iterator.next();

            // 如果怪物死掉了或者被清除了，直接从队列移除（永久任务和限时任务都会在这里优雅死掉，绝不残留垃圾数据）
            if (task.mob == null || !task.mob.isAlive() || task.mob.isRemoved()) {
                iterator.remove();
                continue;
            }

            checkAndTriggerBinSi(task.mob);

            // 如果是爬塔常驻的被动，不走倒计时，继续保留
            if (task.isPermanent) {
                continue;
            }

            // 限时任务倒计时
            task.remainingTicks--;
            if (task.remainingTicks <= 0) {
                // 限时到了：反向扣除属性恢复原状
                modifyAttributeValue(task.mob, task.type, -task.amount);
                SynsAPI.synsEntityAttribute(task.mob);
                iterator.remove();
            }
        }
    }

    private static void checkAndTriggerBinSi(Mob mob) {
        if (mob == null || !mob.isAlive()) return;
        if (mob.getPersistentData().getBoolean("Skill_BinSi_Available") && !ACTIVATED_BINSI.contains(mob)) {
            if (mob.getHealth() <= (mob.getMaxHealth() * 0.10F)) {
                ACTIVATED_BINSI.add(mob);
                TowerSkillPool.ShieldActiveSkillNotify(mob, "濒死");
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    float currentGongji = cap.getGongji();
                    applyPermanentAttribute(mob, MobAttributeType.WUGONG, currentGongji);
                });
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                        Integer.MAX_VALUE, 1, false, true
                ));
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        net.minecraft.sounds.SoundEvents.WITHER_HURT,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 0.7F);
                if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                            25, 0.3D, 0.5D, 0.3D, 0.1D);
                }
            }
        }
    }

}