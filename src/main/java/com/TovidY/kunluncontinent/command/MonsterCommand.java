package com.TovidY.kunluncontinent.command;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MonsterCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kunluncontinent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("monster")
                        // 使用 ResourceLocationArgument 代替 EntitySummonArgument
                        .then(Commands.argument("entity_id", ResourceLocationArgument.id())
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                .then(Commands.argument("nianxian", LongArgumentType.longArg(0, 999999999))
                                        .then(Commands.argument("applyAttribute", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    return spawnMonster(context.getSource(),
                                                            ResourceLocationArgument.getId(context, "entity_id"),
                                                            LongArgumentType.getLong(context, "nianxian"),
                                                            BoolArgumentType.getBool(context, "applyAttribute"));
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static int spawnMonster(CommandSourceStack source, ResourceLocation entityId, long nianxian, boolean applyAttribute) {
        ServerLevel level = source.getLevel();

        return EntityType.byString(entityId.toString()).map(entityType -> {
            Entity entity = entityType.create(level);
            if (entity instanceof Mob mob) {
                mob.moveTo(source.getPosition().x, source.getPosition().y, source.getPosition().z, 0, 0);
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    if (applyAttribute) {
                        cap.initNianxian(nianxian);
                        float maxHealth = cap.getMaxshengming();
                        var attr = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                        if (attr != null) {
                            attr.setBaseValue(maxHealth);
                            mob.setHealth(maxHealth); // 瞬间回满血
                        }
                    } else {
                        cap.setNianxian(nianxian);
                    }
                });

                level.addFreshEntity(mob);
                SynsAPI.synsEntityAttribute(mob);
                source.sendSuccess(() -> Component.literal("§a成功生成高阶生物！年限：" + nianxian), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§c该实体不支持魂兽属性系统"));
                return 0;
            }
        }).orElseGet(() -> {
            source.sendFailure(Component.literal("§c找不到实体 ID: " + entityId));
            return 0;
        });
    }
}