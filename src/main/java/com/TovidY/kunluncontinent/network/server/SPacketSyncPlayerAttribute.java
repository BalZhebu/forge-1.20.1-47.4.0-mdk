package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端同步玩家属性数据包
 */

public class SPacketSyncPlayerAttribute {
    private final float shengming;
    private final float maxshengming;
    private final float jingshenli;
    private final float maxjingshenli;
    private final float mingzhong;
    private final float fangyu;
    private final float gongji;
    private final float baojilv;
    private final float baojishanghai;
    private final float xixue;
    private final float shanbi;
    private final float kangbao;
    private final float jingyan;
    private final int dengji;
    private final float maxjingyan;

    public SPacketSyncPlayerAttribute(
        float shengming, float maxshengming, float jingshenli, float maxjingshenli,
        float mingzhong, float fangyu, float gongji, float baojilv, float baojishanghai,
        float xixue, float shanbi, float kangbao, float jingyan, int dengji, float maxjingyan
    ) {
        this.shengming = shengming;
        this.maxshengming = maxshengming;
        this.jingshenli = jingshenli;
        this.maxjingshenli = maxjingshenli;
        this.mingzhong = mingzhong;
        this.fangyu = fangyu;
        this.gongji = gongji;
        this.baojilv = baojilv;
        this.baojishanghai = baojishanghai;
        this.xixue = xixue;
        this.shanbi = shanbi;
        this.kangbao = kangbao;
        this.jingyan = jingyan;
        this.dengji = dengji;
        this.maxjingyan = maxjingyan;
    }

    public static void encode(SPacketSyncPlayerAttribute msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.shengming);
        buf.writeFloat(msg.maxshengming);
        buf.writeFloat(msg.jingshenli);
        buf.writeFloat(msg.maxjingshenli);
        buf.writeFloat(msg.mingzhong);
        buf.writeFloat(msg.fangyu);
        buf.writeFloat(msg.gongji);
        buf.writeFloat(msg.baojilv);
        buf.writeFloat(msg.baojishanghai);
        buf.writeFloat(msg.xixue);
        buf.writeFloat(msg.shanbi);
        buf.writeFloat(msg.kangbao);
        buf.writeFloat(msg.jingyan);
        buf.writeInt(msg.dengji);
        buf.writeFloat(msg.maxjingyan);
    }

    public static SPacketSyncPlayerAttribute decode(FriendlyByteBuf buf) {
        return new SPacketSyncPlayerAttribute(
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readInt(), buf.readFloat()
        );
    }

    public static void handle(SPacketSyncPlayerAttribute msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    attr.setShengming(msg.shengming);
                    attr.setMaxshengming(msg.maxshengming);
                    attr.setJingshenli(msg.jingshenli);
                    attr.setMaxjingshenli(msg.maxjingshenli);
                    attr.setMingzhong(msg.mingzhong);
                    attr.setFangyu(msg.fangyu);
                    attr.setGongji(msg.gongji);
                    attr.setBaojilv(msg.baojilv);
                    attr.setBaojishanghai(msg.baojishanghai);
                    attr.setXixue(msg.xixue);
                    attr.setShanbi(msg.shanbi);
                    attr.setKangbao(msg.kangbao);
                    attr.setJingyan(msg.jingyan);
                    attr.setDengji(msg.dengji);
                    attr.setMaxjingyan(msg.maxjingyan);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
