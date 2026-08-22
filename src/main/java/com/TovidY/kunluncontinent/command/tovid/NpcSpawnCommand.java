package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.playernpc.NpcSkinRegistry;
import com.TovidY.kunluncontinent.entity.playernpc.NpcSoulGenerator;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class NpcSpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kunluncontinent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("npc")
                                // 先输入等级（整数），再输入 NPC 名字（贪婪字符串，全面支持中文）
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 999))
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    List<String> suggestions = new ArrayList<>();
                                                    for (int i = 0; i < NpcSkinRegistry.getSkinCount(); i++) {
                                                        suggestions.add(NpcSkinRegistry.getName(i).getString());
                                                    }
                                                    return SharedSuggestionProvider.suggest(suggestions, builder);
                                                })
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                    String npcName = StringArgumentType.getString(context, "name").trim();

                                                    ServerPlayer player = source.getPlayerOrException();
                                                    ServerLevel serverLevel = player.serverLevel();
                                                    Vec3 pos = player.position();

                                                    // 1. 根据传入名字匹配 SkinIndex
                                                    int foundSkinIndex = -1;
                                                    for (int i = 0; i < NpcSkinRegistry.getSkinCount(); i++) {
                                                        if (NpcSkinRegistry.getName(i).getString().equalsIgnoreCase(npcName)) {
                                                            foundSkinIndex = i;
                                                            break;
                                                        }
                                                    }

                                                    // 2. Fallback: 如果名字未找到，尝试作为皮肤 ID 数字解析
                                                    if (foundSkinIndex == -1) {
                                                        try {
                                                            int parsedId = Integer.parseInt(npcName);
                                                            if (parsedId >= 0 && parsedId < NpcSkinRegistry.getSkinCount()) {
                                                                foundSkinIndex = parsedId;
                                                            }
                                                        } catch (NumberFormatException ignored) {}
                                                    }

                                                    if (foundSkinIndex == -1) {
                                                        source.sendFailure(Component.literal("§c[昆仑大陆] 未找到名为 [" + npcName + "] 的NPC皮肤配置！"));
                                                        return 0;
                                                    }

                                                    // 3. 实例化与属性初始化
                                                    PlayerNpcEntity npc = new PlayerNpcEntity(EntityInit.PLAYER_NPC.get(), serverLevel);
                                                    npc.moveTo(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
                                                    npc.setSkinIndex(foundSkinIndex);

                                                    NpcSoulGenerator.initNpcSoulData(npc.getSoulCapability(), level);
                                                    npc.recalculateNpcStats();

                                                    serverLevel.addFreshEntity(npc);

                                                    source.sendSuccess(() -> Component.literal("§a[昆仑大陆] 成功召唤 NPC: §e"
                                                            + NpcSkinRegistry.getName(npc.getSkinIndex()).getString()
                                                            + " §a(等级: §6" + level + "§a)"), true);

                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}