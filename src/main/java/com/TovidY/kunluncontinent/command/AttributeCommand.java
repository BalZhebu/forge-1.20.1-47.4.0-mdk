package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.event.client.PlayerAttributeInit;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;

//指令格式/kunluncontinent attribute <属性名称> <增加/减少> <值> <玩家>
// 属性修改指令

public class AttributeCommand {

    private static final List<String> ATTRIBUTE_NAMES = Arrays.asList(
        "shengming", "maxshengming", "jingshenli", "maxjingshenli",
        "mingzhong", "fangyu", "gongji", "baojilv", "baojishanghai",
        "xixue", "shanbi", "kangbao", "jingyan", "dengji", "maxjingyan",
        "wuchuan", "shengminghuifu"
    );

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

    private static int modifySelfAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String attributeName = StringArgumentType.getString(context, "attributeName");
        float value = FloatArgumentType.getFloat(context, "value");
        
        return modifyAttribute(player, attributeName, value, context.getSource());
    }

    private static int modifyPlayerAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        String attributeName = StringArgumentType.getString(context, "attributeName");
        float value = FloatArgumentType.getFloat(context, "value");
        
        return modifyAttribute(targetPlayer, attributeName, value, context.getSource());
    }

    private static int modifyAttribute(ServerPlayer player, String attributeName, float value, CommandSourceStack source) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(attr -> {
            float oldValue;
            float newValue;
            
            switch (attributeName.toLowerCase()) {
                case "shengming":
                    oldValue = attr.getShengming();
                    newValue = oldValue + value;
                    attr.setShengming(newValue);
                    player.setHealth(Math.min(newValue, player.getMaxHealth()));
                    break;
                    
                case "maxshengming":
                    oldValue = attr.getMaxshengming();
                    newValue = Math.max(1.0f, oldValue + value);
                    attr.setMaxshengming(newValue);
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
                    newValue = Math.max(0.0f, oldValue + value);
                    attr.setFangyu(newValue);
                    break;
                    
                case "gongji":
                    oldValue = attr.getGongji();
                    newValue = Math.max(0.0f, oldValue + value);
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
                    newValue = Math.max(0.0f, oldValue + value);
                    attr.setJingyan(newValue);
                    break;
                    
                case "dengji":
                    oldValue = attr.getDengji();
                    newValue = Math.max(0.0f, oldValue + value);
                    attr.setDengji((int)newValue);
                    break;
                    
                case "maxjingyan":
                    oldValue = attr.getMaxjingyan();
                    newValue = Math.max(1.0f, oldValue + value);
                    attr.setMaxjingyan(newValue);
                    break;
                case "wuchuan":
                    oldValue = attr.getWuchuan();
                    newValue = oldValue + value;
                    attr.setWuchuan(newValue);
                    break;
                case "shengminghuifu":
                    oldValue = attr.getShengmingHuifu();
                    newValue = oldValue + value;
                    attr.setShengmingHuifu(newValue);
                    break;
                default:
                    source.sendFailure(Component.literal("§c未知的属性名称: " + attributeName + "请联系作者TovidY"));
                    source.sendFailure(Component.literal("§e可用属性: " + String.join(", ", ATTRIBUTE_NAMES)));
                    return 0;
            }

            String operation = value >= 0 ? "增加" : "减少";
            source.sendSuccess(() -> Component.literal(
                "§a成功" + operation + "玩家 §e" + player.getName().getString() + 
                " §a的 §6" + attributeName + " §a属性"
            ), true);
            source.sendSuccess(() -> Component.literal(
                "§7" + oldValue + " §f-> §b" + String.format("%.2f", newValue) + 
                " §7(变化: " + (value >= 0 ? "§a+" : "§c") + String.format("%.2f", value) + "§7)"
            ), false);
            syncToClient(player, attr);
            if (attributeName.equalsIgnoreCase("jingyan")) {
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, attr);
            }
            return 1;
        }).orElse(0);
    }

    // 同步属性到客户端
    private static void syncToClient(ServerPlayer player, com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability attr) {
        CompoundTag nbtData = attr.serializeNBT();
        com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute packet =
                new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(player.getId(), nbtData);
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
        );
    }

}
