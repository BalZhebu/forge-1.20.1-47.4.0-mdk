package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//指令格式/kunluncontinent attribute <属性名称> <增加/减少> <值> <玩家>

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class AttributeCommand {

    private record AttributeHandler(
            java.util.function.Function<PlayerAttributeCapability, Float> getter,
            java.util.function.BiConsumer<PlayerAttributeCapability, Float> setter,
            float minValue,
            java.util.function.BiConsumer<ServerPlayer, Float> extraAction
    ) {}

    private static final Map<String, AttributeHandler> ATTRIBUTES = new java.util.HashMap<>();

    static {
        registerAttr("shengming", PlayerAttributeCapability::getShengming, PlayerAttributeCapability::setShengming, Float.NEGATIVE_INFINITY, (player, val) -> player.setHealth(Math.min(val, player.getMaxHealth())));
        registerAttr("maxshengming", PlayerAttributeCapability::getMaxshengming, PlayerAttributeCapability::setMaxshengming, 1.0f, PlayerAttributeInit::syncMaxHealthToPlayer);
        registerAttr("jingshenli", PlayerAttributeCapability::getJingshenli, PlayerAttributeCapability::setJingshenli, Float.NEGATIVE_INFINITY, null);
        registerAttr("maxjingshenli", PlayerAttributeCapability::getMaxjingshenli, PlayerAttributeCapability::setMaxjingshenli, 1.0f, null);
        registerAttr("mingzhong", PlayerAttributeCapability::getMingzhong, PlayerAttributeCapability::setMingzhong, Float.NEGATIVE_INFINITY, null);
        registerAttr("fangyu", PlayerAttributeCapability::getFangyu, PlayerAttributeCapability::setFangyu, 0.0f, null);
        registerAttr("gongji", PlayerAttributeCapability::getGongji, PlayerAttributeCapability::setGongji, 0.0f, null);
        registerAttr("baojilv", PlayerAttributeCapability::getBaojilv, PlayerAttributeCapability::setBaojilv, Float.NEGATIVE_INFINITY, null);
        registerAttr("baojishanghai", PlayerAttributeCapability::getBaojishanghai, PlayerAttributeCapability::setBaojishanghai, 0.0f, null);
        registerAttr("xixue", PlayerAttributeCapability::getXixue, PlayerAttributeCapability::setXixue, Float.NEGATIVE_INFINITY, null);
        registerAttr("shanbi", PlayerAttributeCapability::getShanbi, PlayerAttributeCapability::setShanbi, Float.NEGATIVE_INFINITY, null);
        registerAttr("kangbao", PlayerAttributeCapability::getKangbao, PlayerAttributeCapability::setKangbao, Float.NEGATIVE_INFINITY, null);
        registerAttr("jingyan", PlayerAttributeCapability::getJingyan, PlayerAttributeCapability::setJingyan, 0.0f, null);
        registerAttr("dengji", attr -> (float) attr.getDengji(), (attr, val) -> attr.setDengji(val.intValue()), 0.0f, null);
        registerAttr("maxjingyan", PlayerAttributeCapability::getMaxjingyan, PlayerAttributeCapability::setMaxjingyan, 1.0f, null);
        registerAttr("wuchuan", PlayerAttributeCapability::getWuchuan, PlayerAttributeCapability::setWuchuan, Float.NEGATIVE_INFINITY, null);
        registerAttr("shengminghuifu", PlayerAttributeCapability::getShengmingHuifu, PlayerAttributeCapability::setShengmingHuifu, Float.NEGATIVE_INFINITY, null);
    }

    private static void registerAttr(String name, java.util.function.Function<PlayerAttributeCapability, Float> getter, java.util.function.BiConsumer<PlayerAttributeCapability, Float> setter, float min, java.util.function.BiConsumer<ServerPlayer, Float> action) {
        ATTRIBUTES.put(name, new AttributeHandler(getter, setter, min, action));
    }

    private static final SuggestionProvider<CommandSourceStack> ATTRIBUTE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(ATTRIBUTES.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kunluncontinent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("attribute")
                                // 💡 极致优化 3：换用 word() 类型，不仅运行速度更快，玩家输入命令时再也不需要加无脑的双引号了！
                                .then(Commands.argument("attributeName", StringArgumentType.word())
                                        .suggests(ATTRIBUTE_SUGGESTIONS)
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                                        .executes(context -> modifyAttribute(context, null))
                                                        .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(context -> modifyAttribute(context, EntityArgument.getPlayer(context, "player")))
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int modifyAttribute(CommandContext<CommandSourceStack> context, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = (target == null) ? context.getSource().getPlayerOrException() : target;
        String attributeName = StringArgumentType.getString(context, "attributeName").toLowerCase();
        float value = FloatArgumentType.getFloat(context, "value");
        CommandSourceStack source = context.getSource();

        AttributeHandler handler = ATTRIBUTES.get(attributeName);
        if (handler == null) {
            source.sendFailure(Component.literal("§c未知的属性名称: " + attributeName + "，请联系作者TovidY"));
            source.sendFailure(Component.literal("§e可用属性: " + String.join(", ", ATTRIBUTES.keySet())));
            return 0;
        }

        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(attr -> {
            float oldValue = handler.getter.apply(attr);
            float newValue = handler.minValue == Float.NEGATIVE_INFINITY ? oldValue + value : Math.max(handler.minValue, oldValue + value);

            handler.setter.accept(attr, newValue);

            if (handler.extraAction != null) {
                handler.extraAction.accept(player, newValue);
            }

            String operation = value >= 0 ? "增加" : "减少";
            source.sendSuccess(() -> Component.literal(
                    "§a成功" + operation + "玩家 §e" + player.getName().getString() + " §a的 §6" + attributeName + " §a属性"
            ), true);
            source.sendSuccess(() -> Component.literal(
                    "§7" + oldValue + " §f-> §b" + String.format("%.2f", newValue) + " §7(变化: " + (value >= 0 ? "§a+" : "§c") + String.format("%.2f", value) + "§7)"
            ), false);

            syncToClient(player, attr);

            if ("jingyan".equals(attributeName) && value > 0) {
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, attr);
            }
            return 1;
        }).orElse(0);
    }

    private static void syncToClient(ServerPlayer player, PlayerAttributeCapability attr) {
        CompoundTag nbtData = attr.serializeNBT();
        var packet = new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(player.getId(), nbtData);
        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}