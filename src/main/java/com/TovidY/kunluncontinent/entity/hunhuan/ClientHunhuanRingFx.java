package com.TovidY.kunluncontinent.entity.hunhuan;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端：给正在吸收魂环的玩家挂上一枚魂环。
 *
 * <p><b>为什么不需要自定义网络包？</b>因为原版引擎已经把需要的信息同步给所有客户端了：
 * 玩家"正骑在魂环实体上"是乘客同步（{@link net.minecraft.client.multiplayer.ClientPacketListener}），
 * 魂环实体的年限是 {@code HunhuanEntity} 的同步数据（{@code getNianxianSync()}）。
 * 所以每个客户端都能自己判断谁在吸魂环、该显示多大什么颜色 —— 别的玩家天然就能看到，
 * 不需要服务端额外发包，也就不会出现包丢了动画卡住的问题。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientHunhuanRingFx {

    /** 玩家实体 id -> 挂在他身上的那枚魂环。 */
    private static final Map<Integer, HunhuanRingParticle> RINGS = new HashMap<>();

    private ClientHunhuanRingFx() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            RINGS.clear();
            return;
        }

        for (Player player : level.players()) {
            // 骑在一个"还活着"的魂环实体上 = 正在吸收
            HunhuanEntity ring = player.getVehicle() instanceof HunhuanEntity h ? h : null;
            int playerId = player.getId();

            if (ring == null || ring.isRemoved() || ring.getNianxianSync() <= 0L) {
                // 不骑了（年限还没同步过来的那一两 tick 也先不生成，免得颜色不对）。
                // 引用直接丢掉就好：粒子自己会判断该播"收束飞入"还是"淡出"。
                RINGS.remove(playerId);
                continue;
            }

            HunhuanRingParticle particle = RINGS.get(playerId);
            if (particle == null || !particle.isAlive()) {
                HunhuanRingParticle spawned = HunhuanRingParticle.spawn(level, player, ring);
                if (spawned != null) {
                    RINGS.put(playerId, spawned);
                }
            }
        }

        // 玩家离开视野后粒子会自己淡出，这里顺手把失效引用清掉
        RINGS.entrySet().removeIf(entry -> !entry.getValue().isAlive());
    }
}
