package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//指令注册
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AttributeCommand.register(event.getDispatcher());
        UpgradeTestCommand.register(event.getDispatcher());
        TianfuCommand.register(event.getDispatcher());
        NeidanCommand.register(event.getDispatcher());
        MonsterCommand.register(event.getDispatcher());
        HunguCommand.register(event.getDispatcher());
        ShenweiCommand.register(event.getDispatcher());
        com.TovidY.kunluncontinent.command.shenkao.ShenweiCommand.register(event.getDispatcher());
    }
}
