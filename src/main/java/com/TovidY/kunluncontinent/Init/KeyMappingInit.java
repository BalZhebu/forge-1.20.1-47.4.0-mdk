package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.CPacketOpenAttrubuteGUI;
import com.TovidY.kunluncontinent.network.client.CPacketQiehuanWuhun;
import com.TovidY.kunluncontinent.screen.attribute.skill.CPacketCycleSkill;
import com.TovidY.kunluncontinent.screen.attribute.skill.CPacketReleaseSkill;
import com.TovidY.kunluncontinent.screen.attribute.skill.SkillWheelScreen;
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

//按键注册类
public class KeyMappingInit {
    private static int rKeyDownTicks = 0;

    public static final KeyMapping ATTRIBUTE_MAPPING = new KeyMapping("attribute_mapping", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KlMain.MOD_ID);
    public static final KeyMapping KAIGUAN_MAPPING = new KeyMapping("kaiguan_mapping", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KlMain.MOD_ID);
    public static final KeyMapping SKILL_WHEEL = new KeyMapping("key.kunlun.skill_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KlMain.MOD_ID);
    public static final KeyMapping RELEASE_SKILL = new KeyMapping("key.kunlun.release_skill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KlMain.MOD_ID);

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RegisterBindings {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(ATTRIBUTE_MAPPING);
            event.register(KAIGUAN_MAPPING);
            event.register(SKILL_WHEEL);
            event.register(RELEASE_SKILL);
        }
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            if (SKILL_WHEEL.isDown()) {
                rKeyDownTicks++;
                if (rKeyDownTicks == 6 && mc.screen == null) {
                    mc.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        mc.setScreen(new SkillWheelScreen(cap));
                    });
                }
            } else {
                if (rKeyDownTicks > 0 && rKeyDownTicks <= 5) {
                    NetworkHandler.INSTANCE.sendToServer(new CPacketCycleSkill());
                }
                rKeyDownTicks = 0;
            }

            while (RELEASE_SKILL.consumeClick()) {
                NetworkHandler.INSTANCE.sendToServer(new CPacketReleaseSkill());
            }

            // 2. 只有当玩家未打开任何界面 (mc.screen == null) 时，才响应 O 键与 K 键
            if (mc.screen == null) {
                while (ATTRIBUTE_MAPPING.consumeClick()) {
                    NetworkHandler.INSTANCE.sendToServer(new CPacketOpenAttrubuteGUI());
                }
                while (KAIGUAN_MAPPING.consumeClick()) {
                    NetworkHandler.INSTANCE.sendToServer(new CPacketQiehuanWuhun());
                }
            }
        }
    }
}