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

package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.network.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.function.Supplier;

// 同步实体属性数据的网络包
public class SPacketEntityAttribute {

  public static final HashMap<Integer, CompoundTag> monsterHashMapCapability = new HashMap();
  private int entityId;
  private CompoundTag nbt;

  public SPacketEntityAttribute(int entityId, CompoundTag nbt) {
    this.entityId = entityId;
    this.nbt = nbt;
  }

  public static void encode(SPacketEntityAttribute msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeNbt(msg.nbt);
  }

  public static SPacketEntityAttribute decode(FriendlyByteBuf buf) {
    return new SPacketEntityAttribute(buf.readInt(), buf.readNbt());
  }

    public static void handle(SPacketEntityAttribute msg, Supplier<NetworkEvent.Context> ctx) {
        // 客户端逻辑统一在 ClientPacketHandlers（本类不能出现 Minecraft/ClientLevel）
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.entityAttribute(msg.entityId, msg.nbt)));
        ctx.get().setPacketHandled(true);
    }
}
