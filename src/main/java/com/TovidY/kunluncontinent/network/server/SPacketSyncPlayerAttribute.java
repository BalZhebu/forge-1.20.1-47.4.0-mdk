package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
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
    private final float wuchuan;
    private final float shengminghuifu;

    private final Map<String, Float> boneOnlyStats;


    public SPacketSyncPlayerAttribute(
        float shengming, float maxshengming, float jingshenli, float maxjingshenli,
        float mingzhong, float fangyu, float gongji, float baojilv, float baojishanghai,
        float wuchuan, float shengminghuifu, float xixue, float shanbi, float kangbao,
        float jingyan, int dengji, float maxjingyan,Map<String, Float> boneOnlyStats
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
        this.wuchuan = wuchuan;
        this.shengminghuifu = shengminghuifu;
        this.boneOnlyStats = boneOnlyStats;

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
        buf.writeFloat(msg.wuchuan);
        buf.writeFloat(msg.shengminghuifu);

        buf.writeInt(msg.boneOnlyStats.size());
        msg.boneOnlyStats.forEach((key, val) -> {
            buf.writeUtf(key);
            buf.writeFloat(val);
        });
    }

    public static SPacketSyncPlayerAttribute decode(FriendlyByteBuf buf) {
        // 按顺序读取
        float shengming = buf.readFloat();
        float maxshengming = buf.readFloat();
        float jingshenli = buf.readFloat();
        float maxjingshenli = buf.readFloat();
        float mingzhong = buf.readFloat();
        float fangyu = buf.readFloat();
        float gongji = buf.readFloat();
        float baojilv = buf.readFloat();
        float baojishanghai = buf.readFloat();
        float xixue = buf.readFloat();
        float shanbi = buf.readFloat();
        float kangbao = buf.readFloat();
        float jingyan = buf.readFloat();
        int dengji = buf.readInt();
        float maxjingyan = buf.readFloat();
        float wuchuan = buf.readFloat();
        float shengminghuifu = buf.readFloat();

        int size = buf.readInt();
        Map<String, Float> boneMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            boneMap.put(buf.readUtf(), buf.readFloat());
        }

        return new SPacketSyncPlayerAttribute(
                shengming, maxshengming, jingshenli, maxjingshenli,
                mingzhong, fangyu, gongji, baojilv, baojishanghai,
                wuchuan, shengminghuifu, xixue, shanbi, kangbao,
                jingyan, dengji, maxjingyan, boneMap
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
                    attr.setWuchuan(msg.wuchuan);
                    attr.setShengmingHuifu(msg.shengminghuifu);

                    attr.getBoneOnlyStats().clear();
                    attr.getBoneOnlyStats().putAll(msg.boneOnlyStats);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
