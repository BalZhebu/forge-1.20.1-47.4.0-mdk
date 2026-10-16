package com.TovidY.kunluncontinent.special.lonebamboo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * 撑杆跳的「客户端可见状态」。
 *
 * <p>服务端释放撑杆跳时会发一个 {@code S2CPoleVaultPacket}，这里记下
 * <b>谁在什么时间起跳、从哪个点起跳</b>，渲染层据此画手臂姿势与竹竿。</p>
 *
 * <p><b>为什么不从速度反推：</b>「竖直速度 > 阈值」这种判定没法区分撑杆跳和普通跳跃 /
 * 跳跃提升药水，会把普通起跳也画成撑杆跳。所以老老实实发一个包。</p>
 *
 * <p>状态是瞬时的（几秒），<b>不写存档</b> —— 也就不会出现「重进世界后竹竿还在」这种脏状态。</p>
 */
public final class PoleVaultClientState {

    /** 一次撑杆跳的完整记录。 */
    private record Vault(long startTick, long endTick, Vec3 launch) {

        boolean isActive(long now) {
            return now <= endTick;
        }
    }

    private static final Map<Integer, Vault> ACTIVE = new HashMap<>();

    private PoleVaultClientState() {
    }

    /** 收到服务端包时调用（客户端线程）。 */
    public static void start(int entityId, int durationTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        Vec3 launch = entity != null ? entity.position() : Vec3.ZERO;
        long now = level.getGameTime();
        ACTIVE.put(entityId, new Vault(now, now + durationTicks, launch));
    }

    /** 这个实体此刻是否在撑杆跳。顺带清理过期条目。 */
    public static boolean isVaulting(Entity entity) {
        return lookup(entity) != null;
    }

    /** 起跳点（世界坐标）；不在撑杆跳状态时返回 {@code null}。 */
    public static Vec3 launchPos(Entity entity) {
        Vault vault = lookup(entity);
        return vault == null ? null : vault.launch();
    }

    /**
     * 本次动作的推进度，{@code 0 → 1}。
     * <p>用于淡出之类的收尾表现；已经结束时返回 1。</p>
     */
    public static float progress(Entity entity, float partialTick) {
        Vault vault = lookup(entity);
        ClientLevel level = Minecraft.getInstance().level;
        if (vault == null || level == null) {
            return 1.0F;
        }
        long span = Math.max(1L, vault.endTick - vault.startTick);
        float elapsed = (level.getGameTime() + partialTick) - vault.startTick;
        return Math.max(0.0F, Math.min(1.0F, elapsed / span));
    }

    private static Vault lookup(Entity entity) {
        if (entity == null || !entity.level().isClientSide()) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Vault vault = ACTIVE.get(entity.getId());
        if (vault == null) {
            return null;
        }
        if (!vault.isActive(level.getGameTime())) {
            ACTIVE.remove(entity.getId());
            return null;
        }
        return vault;
    }
}
