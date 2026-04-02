package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncGodData {
    private final String godId;
    private final String displayName;
    private final int stage;
    private final int progress;
    private final boolean isGod;
    private final String desc;
    private final String target;
    private final int req;
    private final String type;
    private final String rewardDesc;

    public PacketSyncGodData(PlayerAttributeCapability cap) {
        this.godId = cap.getGodName();
        this.stage = cap.getCurrentStage();
        this.progress = cap.getGodTaskProgress();
        this.isGod = cap.isGod();

        GodInfo info = GodRegistry.GODS.get(this.godId);
        this.displayName = (info != null) ? info.getName() : "无";

        this.rewardDesc = (info != null) ? info.getRewardTooltip(this.stage) : "无";

        int current = this.stage;
        this.desc = (cap.getAssignedExams() != null && current < 10) ? cap.getAssignedExams()[current] : "";
        this.target = (cap.getAssignedTargets() != null && current < 10) ? cap.getAssignedTargets()[current] : "";
        this.req = (cap.getAssignedCounts() != null && current < 10) ? cap.getAssignedCounts()[current] : 0;
        this.type = (cap.getAssignedTypes() != null && current < 10) ? cap.getAssignedTypes()[current] : "";
    }

    public PacketSyncGodData(String godId, String displayName, int stage, int progress, boolean isGod,
                             String desc, String target, int req, String type, String rewardDesc) {
        this.godId = godId;
        this.displayName = displayName;
        this.stage = stage;
        this.progress = progress;
        this.isGod = isGod;
        this.desc = desc;
        this.target = target;
        this.req = req;
        this.type = type;
        this.rewardDesc = rewardDesc;
    }

    public static void encode(PacketSyncGodData msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.godId);
        buffer.writeUtf(msg.displayName);
        buffer.writeInt(msg.stage);
        buffer.writeInt(msg.progress);
        buffer.writeBoolean(msg.isGod);
        buffer.writeUtf(msg.desc != null ? msg.desc : "");
        buffer.writeUtf(msg.target != null ? msg.target : "");
        buffer.writeInt(msg.req);
        buffer.writeUtf(msg.type != null ? msg.type : "");
        buffer.writeUtf(msg.rewardDesc != null ? msg.rewardDesc : "无"); // 3. 必须写入 buffer
    }

    public static PacketSyncGodData decode(FriendlyByteBuf buffer) {
        return new PacketSyncGodData(
                buffer.readUtf(), // godId
                buffer.readUtf(), // displayName
                buffer.readInt(), buffer.readInt(),
                buffer.readBoolean(), buffer.readUtf(), buffer.readUtf(),
                buffer.readInt(), buffer.readUtf(),
                buffer.readUtf()  // 4. 必须读取 rewardDesc
        );
    }

    public static void handle(PacketSyncGodData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            GodClientData.update(msg.godId, msg.displayName, msg.stage, msg.progress,
                    msg.isGod, msg.desc, msg.target, msg.req, msg.type, msg.rewardDesc);
        });
        ctx.get().setPacketHandled(true);
    }
}