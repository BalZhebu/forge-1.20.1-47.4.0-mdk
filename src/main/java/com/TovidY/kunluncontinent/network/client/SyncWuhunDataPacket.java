package com.TovidY.kunluncontinent.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

//武魂数据同步包
public class SyncWuhunDataPacket {
    private final UUID playerUUID;
    private final List<Integer> wuhunNianxianList;
    private final boolean isPlayingAnimation;
    private final long animationStartTime;
    /** 该玩家的"魂环开启方式"样式下标，让别人的客户端也按同一个样式播放展开动画。 */
    private final int animStyle;

    /**
     * 每个环来自哪个神位，与 {@link #wuhunNianxianList} 一一对应：非神赐环写空串。
     *
     * <p>客户端只能从这里知道该用哪张贴图（海神 {@code haishenhunhuan.png}，修罗/天使待补）：
     * 玩家自己的 capability 虽有 {@code getShenciGod()}，但<b>别人的</b> capability 不在这边，
     * 只能靠这个包带过来。</p>
     */
    private final List<String> shenciGodList;

    /**
     * 该玩家是否已成神（{@code cap.isGod()}）。
     *
     * <p>成神后第 10 环起要走顶点渐变，而客户端拿不到别人的 capability，
     * 所以必须由服务端带过来（渲染缓存在 {@code EntityWuhunCache#divine}）。</p>
     */
    private final boolean divine;

    /** 老签名（不带神赐标记，全部按普通环处理）。 */
    public SyncWuhunDataPacket(UUID playerUUID, List<Integer> wuhunNianxianList,
                               boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        this(playerUUID, wuhunNianxianList, Collections.emptyList(), false, isPlayingAnimation, animationStartTime, animStyle);
    }

    /** 不带成神标记的签名（渲染端会当成未成神）。 */
    public SyncWuhunDataPacket(UUID playerUUID, List<Integer> wuhunNianxianList, List<String> shenciGodList,
                               boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        this(playerUUID, wuhunNianxianList, shenciGodList, false, isPlayingAnimation, animationStartTime, animStyle);
    }

    public SyncWuhunDataPacket(UUID playerUUID, List<Integer> wuhunNianxianList, List<String> shenciGodList,
                               boolean divine, boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        this.playerUUID = playerUUID;
        this.wuhunNianxianList = wuhunNianxianList != null ? wuhunNianxianList : Collections.emptyList();
        this.shenciGodList = shenciGodList != null ? shenciGodList : Collections.emptyList();
        this.divine = divine;
        this.isPlayingAnimation = isPlayingAnimation;
        this.animationStartTime = animationStartTime;
        this.animStyle = animStyle;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public List<Integer> getWuhunNianxianList() {
        return wuhunNianxianList;
    }

    public List<String> getShenciGodList() {
        return shenciGodList;
    }

    public boolean isPlayingAnimation() {
        return isPlayingAnimation;
    }

    public long getAnimationStartTime() {
        return animationStartTime;
    }

    public int getAnimStyle() {
        return animStyle;
    }

    public boolean isDivine() {
        return divine;
    }

    public static void encode(SyncWuhunDataPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.playerUUID);
        List<Integer> list = pkt.wuhunNianxianList != null ? pkt.wuhunNianxianList : Collections.emptyList();
        buf.writeInt(list.size());
        for (int nianxian : list) {
            buf.writeInt(nianxian);
        }
        // 神赐标记：与年限列表等长，逐位一个神位 id（空串 = 普通环）
        List<String> flags = pkt.shenciGodList != null ? pkt.shenciGodList : Collections.emptyList();
        buf.writeInt(flags.size());
        for (String godId : flags) {
            buf.writeUtf(godId == null ? "" : godId);
        }
        buf.writeBoolean(pkt.isPlayingAnimation);
        buf.writeLong(pkt.animationStartTime);
        buf.writeInt(pkt.animStyle);
        // 成神标记：第 10 环起用顶点渐变
        buf.writeBoolean(pkt.divine);
    }

    public static SyncWuhunDataPacket decode(FriendlyByteBuf buf) {
        UUID playerUUID = buf.readUUID();
        int size = buf.readInt();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readInt());
        }
        int flagSize = buf.readInt();
        List<String> flags = new ArrayList<>(flagSize);
        for (int i = 0; i < flagSize; i++) {
            flags.add(buf.readUtf());
        }
        boolean isPlayingAnimation = buf.readBoolean();
        long animationStartTime = buf.readLong();
        int animStyle = buf.readInt();
        boolean divine = buf.readBoolean();
        return new SyncWuhunDataPacket(playerUUID, list, flags, divine, isPlayingAnimation, animationStartTime, animStyle);
    }

    public static void handle(SyncWuhunDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        // 客户端逻辑统一在 ClientPacketHandlers：本类不能出现 Minecraft/ClientLevel，
        // 否则专用服务器在注册包头时会加载客户端类并崩服。
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.syncWuhunData(
                        pkt.playerUUID, pkt.wuhunNianxianList, pkt.shenciGodList, pkt.divine,
                        pkt.isPlayingAnimation, pkt.animationStartTime, pkt.animStyle)));
        ctx.get().setPacketHandled(true);
    }
}
