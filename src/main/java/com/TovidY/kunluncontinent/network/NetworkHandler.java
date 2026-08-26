package com.TovidY.kunluncontinent.network;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.network.client.*;
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
        INSTANCE = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(
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

        INSTANCE.registerMessage(99, PacketChangeCamera.class, PacketChangeCamera::toBytes, PacketChangeCamera::new, PacketChangeCamera::handle);


        register(C2SNpcDialogActionPacket.class,C2SNpcDialogActionPacket::encode,C2SNpcDialogActionPacket::new , C2SNpcDialogActionPacket::handle);
        register(SyncNpcWuhunPacket.class,SyncNpcWuhunPacket::encode,SyncNpcWuhunPacket::decode, SyncNpcWuhunPacket::handle);
        register(S2COpenNpcDialogPacket.class,S2COpenNpcDialogPacket::encode,S2COpenNpcDialogPacket::new , S2COpenNpcDialogPacket::handle);
        // 售卖按钮交互包（客户端→服务端）
        register(C2SNpcSellPacket.class, C2SNpcSellPacket::encode, C2SNpcSellPacket::new, C2SNpcSellPacket::handle);

        register(PacketXiulianChoice.class,PacketXiulianChoice::encode,PacketXiulianChoice::decode,PacketXiulianChoice::handle);
        register(PacketOpenXiulianScreen.class,PacketOpenXiulianScreen::encode,PacketOpenXiulianScreen::new,PacketOpenXiulianScreen::handle);
                //服务端
        register(PacketSyncPage.class, PacketSyncPage::encode, PacketSyncPage::decode, PacketSyncPage::handle);
        register(SPacketEntityAttribute.class, SPacketEntityAttribute::encode, SPacketEntityAttribute::decode, SPacketEntityAttribute::handle);
        register(SPacketPlayerAttribute.class, SPacketPlayerAttribute::encode, SPacketPlayerAttribute::decode, SPacketPlayerAttribute::handle);
        register(PacketChangeDisplayMode.class,PacketChangeDisplayMode::encode,PacketChangeDisplayMode::decode,PacketChangeDisplayMode::handle);
        register(PacketToggleConfig.class, PacketToggleConfig::encode,PacketToggleConfig::decode,PacketToggleConfig::handle);

        register(PacketPlayGodRitualEffect.class, PacketPlayGodRitualEffect::encode, PacketPlayGodRitualEffect::decode, PacketPlayGodRitualEffect::handle);

        register(PacketSyncTowerTimer.class, PacketSyncTowerTimer::encode, PacketSyncTowerTimer::decode, PacketSyncTowerTimer::handle);

        register(PacketUpdateUIOffset.class, PacketUpdateUIOffset::toBytes, PacketUpdateUIOffset::new, PacketUpdateUIOffset::handle);



        // 服务端发给客户端（同步数据）
        register(PacketSyncGodData.class, PacketSyncGodData::encode, PacketSyncGodData::decode, PacketSyncGodData::handle);

        // 客户端发给服务端（点击检测按钮）
        register(C2SCheckTaskPacket.class, C2SCheckTaskPacket::encode, C2SCheckTaskPacket::decode, C2SCheckTaskPacket::handle);

        INSTANCE.registerMessage(id, S2CSkillNotifyPacket.class, S2CSkillNotifyPacket::encode, S2CSkillNotifyPacket::decode, S2CSkillNotifyPacket::handle);





    }

    public static void sendToServer(Object packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
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
