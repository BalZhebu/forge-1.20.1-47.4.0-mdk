/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

//武魂开关数据包
public class CPacketQiehuanWuhun {

  public static void encode(CPacketQiehuanWuhun msg, FriendlyByteBuf buf) {
  }

  public static CPacketQiehuanWuhun decode(FriendlyByteBuf buf) {
    return new CPacketQiehuanWuhun();
  }

  public static void handle(CPacketQiehuanWuhun msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      sender.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
        int hunhuankuaiguan = capability.getHunhuankuaiguan();
        int size = capability.getWuhunListsname().size();

        if(hunhuankuaiguan < size-1){
          capability.setHunhuankuaiguan(++hunhuankuaiguan);
          sender.connection.send(new ClientboundSetActionBarTextPacket(
                  Component.translatable("武魂已开启", capability.getWuhunListsname().get(hunhuankuaiguan))));
          PWRenderPlayerEvent.startOpenAnimation(sender);
        } else {
          capability.setHunhuankuaiguan(-1);
          sender.connection.send(new ClientboundSetActionBarTextPacket(
                  Component.translatable("武魂已关闭")));
          PWRenderPlayerEvent.sendCloseNotification(sender);
        }
        SynsAPI.synsPlayerAttribute(sender);
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
