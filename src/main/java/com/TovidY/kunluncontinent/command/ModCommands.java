package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModCommands {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AttributeCommand.register(event.getDispatcher());
        UpgradeTestCommand.register(event.getDispatcher());
        TianfuCommand.register(event.getDispatcher());
    }
}
