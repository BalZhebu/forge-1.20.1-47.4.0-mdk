package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.command.tovid.AttributeCommand;
import com.TovidY.kunluncontinent.command.tovid.CoinDropCommand;
import com.TovidY.kunluncontinent.command.tovid.DropTableCommand;
import com.TovidY.kunluncontinent.command.tovid.HunguCommand;
import com.TovidY.kunluncontinent.command.tovid.MonsterCommand;
import com.TovidY.kunluncontinent.command.tovid.NpcDebugCommand;
import com.TovidY.kunluncontinent.command.tovid.NpcSpawnCommand;
import com.TovidY.kunluncontinent.command.tovid.NeidanCommand;
import com.TovidY.kunluncontinent.command.tovid.TianfuCommand;
import com.TovidY.kunluncontinent.command.tovid.TowerCommand;
import com.TovidY.kunluncontinent.command.tovid.UpgradeTestCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 指令注册入口。
 *
 * <p><b>改造说明</b>：原先 12 个指令类各自调用
 * {@code dispatcher.register(Commands.literal("kunluncontinent")...)}，
 * 同一个根节点被重复注册 12 次（Brigadier 会静默合并子节点，所以能跑，
 * 但权限等级、命名、出错信息各写一遍，极易不一致，且看不出真实指令全貌）。</p>
 *
 * <p>现在改为：各指令类只提供 {@code node()}（自己的子命令树），
 * 由 {@link KLCommands#register} 统一挂到唯一的 {@code kunluncontinent} 根节点下，
 * 权限等级也只在 {@link KLCommands#ADMIN_PERMISSION} 定义一次。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        // ---- 属性与养成 ----
        KLCommands.register(d, AttributeCommand.node());
        KLCommands.register(d, TianfuCommand.node());

        // ---- 内丹与掉落 ----
        KLCommands.register(d, NeidanCommand.node());
        KLCommands.register(d, MonsterCommand.node());
        KLCommands.register(d, HunguCommand.node());
        KLCommands.register(d, CoinDropCommand.node());
        KLCommands.register(d, DropTableCommand.node());

        // ---- NPC ----
        KLCommands.register(d, NpcSpawnCommand.node());
        KLCommands.register(d, NpcDebugCommand.node());

        // ---- 魂塔与神考 ----
        KLCommands.register(d, TowerCommand.node());
        KLCommands.register(d, com.TovidY.kunluncontinent.command.tovid.ShenweiCommand.node());
        KLCommands.register(d, com.TovidY.kunluncontinent.command.shenkao.ShenweiCommand.node());

        // ⚠️ 这两个挂在「目标玩家」之外（不是 shenwei 的子命令），所以要单独注册
        KLCommands.register(d, com.TovidY.kunluncontinent.command.tovid.ShenweiCommand.completeNode());
        KLCommands.register(d, com.TovidY.kunluncontinent.command.tovid.ShenweiCommand.grantNode());

        // ⚠️ /testupgrade 历史上是独立根节点，不在 /kunluncontinent 下，保持原样
        UpgradeTestCommand.register(d);
    }
}