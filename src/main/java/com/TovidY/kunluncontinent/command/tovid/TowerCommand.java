package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.command.KLCommands;
import com.TovidY.kunluncontinent.tower.TowerSpawnerEngine;
import com.TovidY.kunluncontinent.tower.floor.TowerFloorRegistry;
import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 挑战塔管理指令：{@code /kunluncontinent tower ...}
 *
 * <h3>原有（操作玩家进度）</h3>
 * <pre>
 *   /kunluncontinent tower &lt;target&gt; debugForceGod &lt;true|false&gt;
 *   /kunluncontinent tower &lt;target&gt; reset
 *   /kunluncontinent tower &lt;target&gt; add &lt;层数&gt;
 * </pre>
 *
 * <h3>新增（测试刷怪，不动挑战状态）</h3>
 * <pre>
 *   /kunluncontinent tower spawn &lt;层&gt;                        # 以自己为基准，在面前刷整层怪
 *   /kunluncontinent tower spawn &lt;层&gt; &lt;target&gt;               # 给指定玩家刷
 *   /kunluncontinent tower spawn &lt;层&gt; level &lt;词条等级&gt;       # 覆盖本层词条等级（1~10）
 *   /kunluncontinent tower spawnWith &lt;层&gt; &lt;词条&gt; [target]    # 只刷 1 只，且只带这一条词条
 *   /kunluncontinent tower clear [target]                    # 清掉该玩家名下的塔怪
 *   /kunluncontinent tower skills                            # 列出全部词条与登场层
 * </pre>
 *
 * <p><b>层号是"玩家看到的层号"（1 基）</b>，内部再转成 0 基索引 ——
 * 指令里写 {@code spawn 37} 就是第 37 层，不用心算 ±1。</p>
 *
 * <p><b>为什么 {@code spawnWith} 的词条用「字面量节点」而不是字符串参数：</b>
 * Brigadier 的 {@code StringArgumentType.word()} 不允许非 ASCII 字符，
 * {@code string()} 又要求加引号（{@code "血祭"}），两样都很难用。
 * 直接给每个词条挂一个字面量节点，既支持中文又能拿到原生的 Tab 补全。</p>
 */
public class TowerCommand {

    /** 层号参数的下限（第 1 层）。 */
    private static final int MIN_FLOOR = 1;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        KLCommands.register(dispatcher, node());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("tower")
                // ==================== 原有：改玩家爬塔进度 ====================
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("debugForceGod")
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            boolean enable = BoolArgumentType.getBool(context, "enable");
                                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                                attr.debugForceSuccess = enable;
                                                String statusText = enable ? "§c§l开启（下次通关必定获取神位）" : "§a关闭（恢复常规概率）";
                                                context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 已将修士 " + player.getName().getString() + " 的爬塔必定得神位状态设置为: " + statusText), true);
                                            });
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("reset")
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");

                                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                        attr.setCurrentTowerFloor(0);
                                        context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 成功将修士 " + player.getName().getString() + " 的幻境层数重置为第 1 层"), true);
                                    });
                                    return 1;
                                })
                        )
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                                int oldFloor = attr.getCurrentTowerFloor();
                                                attr.setCurrentTowerFloor(oldFloor + amount);
                                                context.getSource().sendSuccess(() -> Component.literal("§a[昆仑大陆] 成功将修士 " + player.getName().getString() + " 的幻境层数变动 " + amount + " 层（当前处于: " + (attr.getCurrentTowerFloor() + 1) + " 层）"), true);
                                            });
                                            return 1;
                                        })
                                )
                        )
                )
                // ==================== 新增：测试刷怪 ====================
                .then(spawnNode())
                .then(spawnWithNode())
                .then(clearNode())
                .then(Commands.literal("skills").executes(TowerCommand::listSkills));
    }

    // ==================================================================================
    //  /tower spawn <层> [level <词条等级>] [target]
    // ==================================================================================

    private static LiteralArgumentBuilder<CommandSourceStack> spawnNode() {
        return Commands.literal("spawn")
                .then(Commands.argument("floor", IntegerArgumentType.integer(MIN_FLOOR))
                        // 自己
                        .executes(ctx -> spawnFloor(ctx, self(ctx), floorIndex(ctx), 0))
                        // 指定玩家
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> spawnFloor(ctx, EntityArgument.getPlayer(ctx, "target"), floorIndex(ctx), 0)))
                        // 覆盖词条等级（用于在低层直接测满级词条）
                        .then(Commands.literal("level")
                                .then(Commands.argument("skillLevel", IntegerArgumentType.integer(1, TowerSkillPool.MAX_LEVEL))
                                        .executes(ctx -> spawnFloor(ctx, self(ctx), floorIndex(ctx),
                                                IntegerArgumentType.getInteger(ctx, "skillLevel")))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> spawnFloor(ctx, EntityArgument.getPlayer(ctx, "target"),
                                                        floorIndex(ctx), IntegerArgumentType.getInteger(ctx, "skillLevel"))))
                                )
                        )
                );
    }

    private static int spawnFloor(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                                  int floorIndex, int overrideSkillLevel) {
        if (!checkFloor(ctx, floorIndex)) return 0;

        BlockPos center = spawnCenter(player);
        int count = TowerSpawnerEngine.spawnTestFloor(player, floorIndex, center, overrideSkillLevel);
        if (count <= 0) {
            ctx.getSource().sendFailure(Component.literal("§c[昆仑大陆] 第 " + (floorIndex + 1) + " 层没有任何可生成的怪物，请检查配表"));
            return 0;
        }

        TowerFloorRegistry.FloorData data = TowerFloorRegistry.getFloorData(floorIndex);
        int level = overrideSkillLevel > 0 ? overrideSkillLevel : data.skillLevel;
        String pos = center.getX() + ", " + center.getY() + ", " + center.getZ();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a[昆仑大陆] 已在 §e" + pos + " §a生成第 §6" + (floorIndex + 1) + "§a 层的 §e" + count + "§a 只怪物"
                        + "（生命/防御/攻击×" + trimMultiplier(data.attributeMultiplier)
                        + "，词条等级 §6" + TowerSkillPool.toRoman(level) + "§a）"
                        + "§7  清理用 /kunluncontinent tower clear"), true);
        return count;
    }

    // ==================================================================================
    //  /tower spawnWith <层> <词条> [target]
    // ==================================================================================

    /**
     * 词条子节点：每个词条一个字面量。
     *
     * <p>指定词条<b>不受</b> {@code getMinFloor()} 限制 —— 就是用来在低层验证高层词条的。</p>
     */
    private static LiteralArgumentBuilder<CommandSourceStack> spawnWithNode() {
        var floorNode = Commands.argument("floor", IntegerArgumentType.integer(MIN_FLOOR));
        for (TowerSkillPool.ITowerModifier modifier : TowerSkillPool.allSkills()) {
            floorNode.then(Commands.literal(modifier.getName())
                    .executes(ctx -> spawnWithSkill(ctx, self(ctx), floorIndex(ctx), modifier.getName()))
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(ctx -> spawnWithSkill(ctx, EntityArgument.getPlayer(ctx, "target"),
                                    floorIndex(ctx), modifier.getName()))));
        }
        return Commands.literal("spawnWith").then(floorNode);
    }

    private static int spawnWithSkill(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                                      int floorIndex, String skillName) {
        if (!checkFloor(ctx, floorIndex)) return 0;

        BlockPos center = spawnCenter(player);
        var mob = TowerSpawnerEngine.spawnTestMonster(player, floorIndex, skillName, center);
        if (mob == null) {
            ctx.getSource().sendFailure(Component.literal("§c[昆仑大陆] 第 " + (floorIndex + 1) + " 层第一条刷怪配置无效，无法生成测试怪物"));
            return 0;
        }

        TowerFloorRegistry.FloorData data = TowerFloorRegistry.getFloorData(floorIndex);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a[昆仑大陆] 已生成 1 只 §e" + mob.getType().getDescription().getString()
                        + "§a（第 §6" + (floorIndex + 1) + "§a 层数值，只带 §6"
                        + TowerSkillPool.displayName(skillName, data.skillLevel) + "§a）"), true);
        return 1;
    }

    // ==================================================================================
    //  /tower clear [target]
    // ==================================================================================

    private static LiteralArgumentBuilder<CommandSourceStack> clearNode() {
        return Commands.literal("clear")
                .executes(ctx -> clear(ctx, self(ctx)))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> clear(ctx, EntityArgument.getPlayer(ctx, "target"))));
    }

    private static int clear(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a[昆仑大陆] 已清除修士 " + player.getName().getString() + " 名下的全部塔内怪物"), true);
        return 1;
    }

    // ==================================================================================
    //  /tower skills
    // ==================================================================================

    private static int listSkills(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        source.sendSuccess(() -> Component.literal(
                "§6§l[词条总览] §7共 " + TowerSkillPool.allSkills().size() + " 条"
                        + "（§e被动§7 / §6主动§7），层号为首次登场层"), false);

        for (TowerSkillPool.ITowerModifier modifier : TowerSkillPool.allSkills()) {
            String type = modifier.isActive() ? "§6主动" : "§e被动";
            String unlock = modifier.getMinFloor() <= 0 ? "§7全层" : "§7第 " + (modifier.getMinFloor() + 1) + " 层起";
            source.sendSuccess(() -> Component.literal(
                    "  " + type + "§r " + modifier.getName() + "  " + unlock), false);
        }
        return TowerSkillPool.allSkills().size();
    }

    // ==================================================================================
    //  工具
    // ==================================================================================

    /** 当前指令执行者（必须由玩家执行）。 */
    private static ServerPlayer self(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    /** 把玩家写的层号（1 基）转成内部 0 基层索引。 */
    private static int floorIndex(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "floor") - 1;
    }

    /** 层号越界检查（配表总层数由 {@link TowerFloorRegistry#getTotalFloors()} 决定）。 */
    private static boolean checkFloor(CommandContext<CommandSourceStack> ctx, int floorIndex) {
        int total = TowerFloorRegistry.getTotalFloors();
        if (floorIndex < 0 || floorIndex >= total) {
            ctx.getSource().sendFailure(Component.literal(
                    "§c[昆仑大陆] 层号超出范围，当前只配置了第 1 ~ " + total + " 层"));
            return false;
        }
        return true;
    }

    /** 刷怪中心：玩家面前 4 格（刷出来不会直接贴脸，也方便观察）。 */
    private static BlockPos spawnCenter(ServerPlayer player) {
        return player.blockPosition().relative(player.getDirection(), 4);
    }

    /** 倍率显示：整数不带小数点（3.0 → 3），非整数保留一位（2.5 → 2.5）。 */
    private static String trimMultiplier(float multiplier) {
        if (multiplier == (long) multiplier) {
            return String.valueOf((long) multiplier);
        }
        return String.format("%.1f", multiplier);
    }
}
