package com.TovidY.kunluncontinent.network;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.network.client.CPacketOpenAttrubuteGUI;
import com.TovidY.kunluncontinent.network.client.CPacketQiehuanWuhun;
import com.TovidY.kunluncontinent.network.client.SyncShenciAttributesPacket;
import com.TovidY.kunluncontinent.network.client.SyncWuhunDataPacket;
import com.TovidY.kunluncontinent.network.server.*;
import com.TovidY.kunluncontinent.screen.attribute.skill.CPacketCycleSkill;
import com.TovidY.kunluncontinent.screen.attribute.skill.CPacketReleaseSkill;
import com.TovidY.kunluncontinent.screen.attribute.skill.CPacketSelectSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

//网络包注册器
public class NetworkHandler {

    private static final String PTC_VERSION = "1";

    public static SimpleChannel INSTANCE;

    private static int id = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(
                KlMain.MOD_ID, "main"))
                .networkProtocolVersion(() -> PTC_VERSION).clientAcceptedVersions(PTC_VERSION::equals)
                .serverAcceptedVersions(PTC_VERSION::equals).simpleChannel();
        //用于注册数据包

        //客户端
        register(CPacketOpenAttrubuteGUI.class, CPacketOpenAttrubuteGUI::encode, CPacketOpenAttrubuteGUI::decode, CPacketOpenAttrubuteGUI::handle);
        register(CPacketQiehuanWuhun.class, CPacketQiehuanWuhun::encode, CPacketQiehuanWuhun::decode, CPacketQiehuanWuhun::handle);
        register(SyncWuhunDataPacket.class, SyncWuhunDataPacket::encode, SyncWuhunDataPacket::decode, SyncWuhunDataPacket::handle);
        register(SyncShenciAttributesPacket.class, SyncShenciAttributesPacket::encode, SyncShenciAttributesPacket::decode, SyncShenciAttributesPacket::handle);
        register(CPacketSelectSkill.class, CPacketSelectSkill::encode, CPacketSelectSkill::decode, CPacketSelectSkill::handle);
        register(CPacketReleaseSkill.class, CPacketReleaseSkill::encode, CPacketReleaseSkill::decode, CPacketReleaseSkill::handle);
        register(CPacketCycleSkill.class, CPacketCycleSkill::encode, CPacketCycleSkill::decode, CPacketCycleSkill::handle);
        register(S2CCastingSyncPacket.class, S2CCastingSyncPacket::encode, S2CCastingSyncPacket::decode, S2CCastingSyncPacket::handle);

        //服务端
        register(PacketSyncPage.class, PacketSyncPage::encode, PacketSyncPage::decode, PacketSyncPage::handle);
        register(SPacketEntityAttribute.class, SPacketEntityAttribute::encode, SPacketEntityAttribute::decode, SPacketEntityAttribute::handle);
        register(SPacketSyncPlayerAttribute.class, SPacketSyncPlayerAttribute::encode, SPacketSyncPlayerAttribute::decode, SPacketSyncPlayerAttribute::handle);
        register(SPacketPlayerAttribute.class, SPacketPlayerAttribute::encode, SPacketPlayerAttribute::decode, SPacketPlayerAttribute::handle);
    }

    private static <M> void register(Class<M> messageType, BiConsumer<M, FriendlyByteBuf> encoder,
                                     Function<FriendlyByteBuf, M> decoder,
                                     BiConsumer<M, Supplier<NetworkEvent.Context>> messageConsumer) {
        INSTANCE.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}
