package com.TovidY.kunluncontinent.event.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 客户端侧「谁正在吟唱」的实时账本。
 *
 * <h3>为什么不能用单个静态字段</h3>
 * <p>原来只有一对 {@code remainingTicks/maxTicks}，只够服务「自己看自己的进度条」。
 * 现在要在<b>任意玩家</b>身上摆出结印姿势，就必须知道每个实体的状态，
 * 所以服务端把吟唱广播给 {@code TRACKING_ENTITY_AND_SELF}，这里按 entityId 记账。</p>
 *
 * <p>倒计时在客户端自己走：服务端只在开始 / 结束时各发一个包，中间不再同步。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class CastClientState {

    /** 一个正在吟唱的目标。 */
    public static final class Cast {
        public final int maxTicks;
        public int remainingTicks;

        Cast(int maxTicks) {
            this.maxTicks = Math.max(1, maxTicks);
            this.remainingTicks = this.maxTicks;
        }
    }

    private static final Map<Integer, Cast> ACTIVE = new HashMap<>();

    private CastClientState() {
    }

    public static void start(int entityId, int totalTicks) {
        ACTIVE.put(entityId, new Cast(totalTicks));
    }

    /** 吟唱收尾（唱完 / 被打断 / 死亡 / 换武魂都走这里）。 */
    public static void stop(int entityId) {
        ACTIVE.remove(entityId);
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    public static Cast get(int entityId) {
        return ACTIVE.get(entityId);
    }

    public static boolean isCasting(int entityId) {
        Cast cast = ACTIVE.get(entityId);
        return cast != null && cast.remainingTicks > 0;
    }

    /** 每客户端 tick 调用：推进倒计时并清理走完的条目。 */
    public static void tick() {
        for (Iterator<Map.Entry<Integer, Cast>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
            Cast cast = it.next().getValue();
            if (--cast.remainingTicks <= 0) {
                it.remove();
            }
        }
    }
}
