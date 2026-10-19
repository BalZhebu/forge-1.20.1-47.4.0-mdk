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

    /**
     * 【回春】词条的登记表：怪物 → 词条等级。
     *
     * <p>用 {@link java.util.WeakHashMap} 而不是普通 Map：怪物被卸载/丢弃后
     * key 自动失效，不需要手动清理，也不会长期持有实体引用导致内存泄漏。</p>
     */
    private static final java.util.Map<Mob, Integer> REJUVENATE = new java.util.WeakHashMap<>();

    /** 【回春】的回复间隔（tick）：4 秒一跳。 */
    private static final int REJUVENATE_INTERVAL = 80;

    /** 登记一条【回春】词条（由 {@code TowerSkillPool} 在注入时调用）。 */
    public static void registerRejuvenate(Mob mob, int level) {
        if (mob == null) return;
        REJUVENATE.put(mob, Math.max(1, Math.min(TowerSkillPool.MAX_LEVEL, level)));
    }

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
                // 概率类属性走加算（层数倍率只作用于生命/防御/攻击，副属性不会被倍率放大）
                // 目前只有「虚影」用 SHANBI，另外两个是给后续词条预留的挂点。
                case SHANBI:
                    cap.setShanbi(cap.getShanbi() + amount);
                    break;
                case MINGZHONG:
                    cap.setMingzhong(cap.getMingzhong() + amount);
                    break;
                case BAOJILV:
                    cap.setBaojilv(cap.getBaojilv() + amount);
                    break;
            }
        });
    }

    /**
     * 全局每Tick监听
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 【回春】与属性任务互不相干：即使一只怪身上只有回春（TASKS 里没有它），也必须被 tick 到，
        // 所以这一行必须在下面的 TASKS.isEmpty() 早退之前。
        tickRejuvenate();

        if (TASKS.isEmpty()) return;

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

    /**
     * 【回春】被动词条的周期回复。
     *
     * <p>直接读 {@code tickCount % 80} 而不是给每只怪维护一个倒计时：
     * 不需要额外状态，也不会因为怪物中途被 unload 而漏拍。
     * 死亡/丢弃的怪会在下一次遍历时从 WeakHashMap 里摘掉。</p>
     */
    private static void tickRejuvenate() {
        if (REJUVENATE.isEmpty()) return;

        Iterator<java.util.Map.Entry<Mob, Integer>> iterator = REJUVENATE.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<Mob, Integer> entry = iterator.next();
            Mob mob = entry.getKey();
            if (mob == null || mob.isRemoved() || !mob.isAlive()) {
                iterator.remove();
                continue;
            }
            if (mob.tickCount % REJUVENATE_INTERVAL != 0) continue;
            if (mob.getHealth() >= mob.getMaxHealth()) continue;

            var cap = mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).resolve().orElse(null);
            float maxHp = cap != null ? cap.getMaxshengming() : mob.getMaxHealth();
            // 每跳恢复最大生命的 0.8% × 等级系数（10 级约 1.5%）
            mob.heal(maxHp * 0.008f * TowerSkillPool.levelMultiplier(entry.getValue()));
            if (cap != null) cap.setShengming(mob.getHealth());

            if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                        6, 0.3D, 0.4D, 0.3D, 0.0D);
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