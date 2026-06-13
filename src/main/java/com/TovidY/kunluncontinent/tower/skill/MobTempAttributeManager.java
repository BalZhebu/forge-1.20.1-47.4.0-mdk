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

    private static class TempTask {
        final Mob mob;
        final float amount;
        int remainingTicks;

        TempTask(Mob mob, float amount, int seconds) {
            this.mob = mob;
            this.amount = amount;
            this.remainingTicks = seconds * 20;
        }
    }

    private static final List<TempTask> TASKS = new ArrayList<>();

    /**
     * 外部唯一注入入口：让某只怪临时增加物攻
     */
    public static void applyTempWugong(Mob mob, float amount, int durationSeconds) {
        if (mob == null || !mob.isAlive()) return;

        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.addTempWugong(amount);
            TASKS.add(new TempTask(mob, amount, durationSeconds));
            SynsAPI.synsEntityAttribute(mob);
        });
    }

    /**
     * 全局每Tick监听：时间到了自动剥离属性
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) return;

        Iterator<TempTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            TempTask task = iterator.next();

            // 如果怪物死掉了或者被清除了，直接从队列移除
            if (task.mob == null || !task.mob.isAlive() || task.mob.isRemoved()) {
                iterator.remove();
                continue;
            }

            task.remainingTicks--;
            if (task.remainingTicks <= 0) {
                // 【时间寿终正寝】：无缝剥离加成！
                task.mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.removeTempWugong(task.amount);
                    SynsAPI.synsEntityAttribute(task.mob); // 属性恢复，再次同步
                });
                iterator.remove(); // 销毁任务
            }
        }
    }
}
