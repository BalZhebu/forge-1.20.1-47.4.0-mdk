package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.event.client.PlayerAttributeInit;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;

public class AttributeCommand {
    
    // 所有可用的属性名称
    private static final List<String> ATTRIBUTE_NAMES = Arrays.asList(
        "shengming", "maxshengming", "jingshenli", "maxjingshenli",
        "mingzhong", "fangyu", "gongji", "baojilv", "baojishanghai",
        "xixue", "shanbi", "kangbao", "jingyan", "dengji", "maxjingyan"
    );
    
    // 属性名称建议提供器
    private static final SuggestionProvider<CommandSourceStack> ATTRIBUTE_SUGGESTIONS = 
        (context, builder) -> SharedSuggestionProvider.suggest(ATTRIBUTE_NAMES, builder);
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2)) // 需要OP权限
                .then(Commands.literal("attribute")
                    .then(Commands.argument("attributeName", StringArgumentType.string())
                        .suggests(ATTRIBUTE_SUGGESTIONS)
                        .then(Commands.literal("add")
                            .then(Commands.argument("value", FloatArgumentType.floatArg())
                                .executes(context -> modifySelfAttribute(context))
                                .then(Commands.argument("player", EntityArgument.player())
                                    .executes(context -> modifyPlayerAttribute(context))
                                )
                            )
                        )
                    )
                )
        );
    }
    
    /**
     * 修改执行者自己的属性
     */
    private static int modifySelfAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String attributeName = StringArgumentType.getString(context, "attributeName");
        float value = FloatArgumentType.getFloat(context, "value");
        
        return modifyAttribute(player, attributeName, value, context.getSource());
    }
    
    /**
     * 修改指定玩家的属性
     */
    private static int modifyPlayerAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        String attributeName = StringArgumentType.getString(context, "attributeName");
        float value = FloatArgumentType.getFloat(context, "value");
        
        return modifyAttribute(targetPlayer, attributeName, value, context.getSource());
    }
    
    /**
     * 核心修改逻辑
     */
    private static int modifyAttribute(ServerPlayer player, String attributeName, float value, CommandSourceStack source) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(attr -> {
            float oldValue;
            float newValue;
            
            switch (attributeName.toLowerCase()) {
                case "shengming":
                    oldValue = attr.getShengming();
                    newValue = oldValue + value;
                    attr.setShengming(newValue);
                    // 同步当前生命值到游戏
                    player.setHealth(Math.min(newValue, player.getMaxHealth()));
                    break;
                    
                case "maxshengming":
                    oldValue = attr.getMaxshengming();
                    newValue = Math.max(1.0f, oldValue + value); // 最大生命值不能低于1
                    attr.setMaxshengming(newValue);
                    // 同步到原生属性系统（支持超过1024）
                    PlayerAttributeInit.syncMaxHealthToPlayer(player, newValue);
                    break;
                    
                case "jingshenli":
                    oldValue = attr.getJingshenli();
                    newValue = oldValue + value;
                    attr.setJingshenli(newValue);
                    break;
                    
                case "maxjingshenli":
                    oldValue = attr.getMaxjingshenli();
                    newValue = Math.max(1.0f, oldValue + value);
                    attr.setMaxjingshenli(newValue);
                    break;
                    
                case "mingzhong":
                    oldValue = attr.getMingzhong();
                    newValue = oldValue + value;
                    attr.setMingzhong(newValue);
                    break;
                    
                case "fangyu":
                    oldValue = attr.getFangyu();
                    newValue = Math.max(0.0f, oldValue + value); // 防御不能为负
                    attr.setFangyu(newValue);
                    break;
                    
                case "gongji":
                    oldValue = attr.getGongji();
                    newValue = Math.max(0.0f, oldValue + value); // 攻击力不能为负
                    attr.setGongji(newValue);
                    break;
                    
                case "baojilv":
                    oldValue = attr.getBaojilv();
                    newValue = oldValue + value;
                    attr.setBaojilv(newValue);
                    break;
                    
                case "baojishanghai":
                    oldValue = attr.getBaojishanghai();
                    newValue = Math.max(0.0f, oldValue + value);
                    attr.setBaojishanghai(newValue);
                    break;
                    
                case "xixue":
                    oldValue = attr.getXixue();
                    newValue = oldValue + value;
                    attr.setXixue(newValue);
                    break;
                    
                case "shanbi":
                    oldValue = attr.getShanbi();
                    newValue = oldValue + value;
                    attr.setShanbi(newValue);
                    break;
                    
                case "kangbao":
                    oldValue = attr.getKangbao();
                    newValue = oldValue + value;
                    attr.setKangbao(newValue);
                    break;
                    
                case "jingyan":
                    oldValue = attr.getJingyan();
                    newValue = Math.max(0.0f, oldValue + value); // 经验不能为负
                    attr.setJingyan(newValue);
                    break;
                    
                case "dengji":
                    oldValue = attr.getDengji();
                    newValue = Math.max(0.0f, oldValue + value); // 等级不能为负
                    attr.setDengji((int)newValue);
                    break;
                    
                case "maxjingyan":
                    oldValue = attr.getMaxjingyan();
                    newValue = Math.max(1.0f, oldValue + value); // 最大经验不能低于1
                    attr.setMaxjingyan(newValue);
                    break;
                    
                default:
                    source.sendFailure(Component.literal("§c未知的属性名称: " + attributeName));
                    source.sendFailure(Component.literal("§e可用属性: " + String.join(", ", ATTRIBUTE_NAMES)));
                    return 0;
            }
            
            // 发送成功消息
            String operation = value >= 0 ? "增加" : "减少";
            source.sendSuccess(() -> Component.literal(
                "§a成功" + operation + "玩家 §e" + player.getName().getString() + 
                " §a的 §6" + attributeName + " §a属性"
            ), true);
            source.sendSuccess(() -> Component.literal(
                "§7" + oldValue + " §f-> §b" + String.format("%.2f", newValue) + 
                " §7(变化: " + (value >= 0 ? "§a+" : "§c") + String.format("%.2f", value) + "§7)"
            ), false);
            
            // 同步数据到客户端
            syncToClient(player, attr);
            
            // 如果是经验值属性，检查是否需要升级
            if (attributeName.equalsIgnoreCase("jingyan")) {
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, attr);
            }
            
            return 1;
        }).orElse(0);
    }
    
    /**
     * 同步属性到客户端
     */
    private static void syncToClient(ServerPlayer player, com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability attr) {
        SPacketSyncPlayerAttribute packet = new SPacketSyncPlayerAttribute(
            attr.getShengming(), attr.getMaxshengming(), attr.getJingshenli(), attr.getMaxjingshenli(),
            attr.getMingzhong(), attr.getFangyu(), attr.getGongji(), attr.getBaojilv(), attr.getBaojishanghai(),
            attr.getXixue(), attr.getShanbi(), attr.getKangbao(), attr.getJingyan(), attr.getDengji(), attr.getMaxjingyan()
        );
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
