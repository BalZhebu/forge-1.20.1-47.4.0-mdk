package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.CPacketOpenAttrubuteGUI;
import com.TovidY.kunluncontinent.network.client.CPacketQiehuanWuhun;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class KeyMappingInit {

    //常量字段
    public static final KeyMapping ATTRIBUTE_MAPPING = new KeyMapping("attribute_mapping", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KlMain.MOD_ID);

    public static final KeyMapping KAIGUAN_MAPPING = new KeyMapping("kaiguan_mapping", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KlMain.MOD_ID);

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RegisterBindings {
        //用于注册按键
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(ATTRIBUTE_MAPPING);
            event.register(KAIGUAN_MAPPING);
        }
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class OnClientTick {

        @SubscribeEvent
        public static void onKeyPressed(InputEvent.Key event) {
            // 检查是否按下的按键是我们自定义的按键，并且玩家不在聊天框中
            if (event.getAction() == InputConstants.PRESS && !isPlayerInChat()) {
                if (ATTRIBUTE_MAPPING.getKey().getValue() == event.getKey()) {
                    NetworkHandler.INSTANCE.sendToServer(new CPacketOpenAttrubuteGUI());
                }
            }
        }

        // 检查玩家是否在聊天框中
        private static boolean isPlayerInChat() {
            return Minecraft.getInstance().screen instanceof ChatScreen;
        }
    }


    @Mod.EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (Minecraft.getInstance().screen == null) {
                ATTRIBUTE_MAPPING.consumeClick();
            }
            if (KAIGUAN_MAPPING.consumeClick()) {
                NetworkHandler.INSTANCE.sendToServer(new CPacketQiehuanWuhun());
            }
        }
    }


}
