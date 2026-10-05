package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.entity.playernpc.NpcBoneGenerator;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * NPC 魂骨的<b>辅助</b>调试指令：{@code /kunluncontinent npcdebug ...}
 *
 * <p><b>生成带魂骨的 NPC 请用 {@link NpcSpawnCommand}</b>：
 * {@code /kunluncontinent npc 99 暮雨 1000000}（名字后加年限即必定带骨）。
 * 之前单独的 {@code spawnwithbone} 已合并进那一条指令，这里不再重复一份。</p>
 *
 * <pre>
 * /kunluncontinent npcdebug bone &lt;nianxian&gt;   给自己 1 枚指定年限的魂骨
 * /kunluncontinent npcdebug boneinfo              16 格内 NPC 魂骨一览
 * /kunluncontinent npcdebug odds &lt;level&gt;        概率速查（调平衡用）
 * </pre>
 */
public class NpcDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kunluncontinent")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("npcdebug")
                                .then(Commands.literal("bone")
                                        .then(Commands.argument("nianxian", IntegerArgumentType.integer(1, 10000000))
                                                .executes(ctx -> giveBone(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "nianxian")))))

                                .then(Commands.literal("boneinfo")
                                        .executes(ctx -> showBoneInfo(ctx.getSource())))

                                .then(Commands.literal("odds")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 999))
                                                .executes(ctx -> showOdds(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "level")))))
                        )
        );
    }

    private static int giveBone(CommandSourceStack source, int nianxian) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§c[调试] 该指令必须由玩家执行"));
            return 0;
        }
        ItemStack stack = NpcBoneGenerator.debugCreateBone(nianxian);
        player.getInventory().add(stack);
        source.sendSuccess(() -> Component.literal(
                "§a[调试] 已给予 1 枚 §b" + nianxian + " 年§a魂骨"), true);
        return 1;
    }

    private static int showBoneInfo(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§c[调试] 该指令必须由玩家执行"));
            return 0;
        }
        ServerLevel level = player.serverLevel();

        List<? extends PlayerNpcEntity> found = level.getEntitiesOfClass(
                PlayerNpcEntity.class, player.getBoundingBox().inflate(16));

        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7[调试] 16 格内没有 NPC"), false);
            return 0;
        }

        int withBone = 0;
        var sb = new StringBuilder();
        for (PlayerNpcEntity npc : found) {
            boolean has = NpcBoneGenerator.hasBone(npc);
            if (has) withBone++;
            sb.append(has ? "§a✔" : "§8✘").append(" §f")
                    .append(npc.getName().getString()).append(" §8| §7Lv.")
                    .append(npc.getSoulCapability().getDengji());
            if (has) {
                sb.append(" §8| §b").append(countBones(npc)).append(" 枚")
                        .append(" §8| §7词条种类 ").append(npc.getSoulCapability().getBoneOnlyStats().size())
                        .append(" §8| §a攻+").append(fmt(npc.getSoulCapability().getBoneOnlyStats()
                                .getOrDefault("gongji", 0f)));
            }
            sb.append("\n");
        }

        final String text = sb.toString();
        final int count = found.size();
        final int bones = withBone;
        source.sendSuccess(() -> Component.literal(
                "§6[调试] §f16 格内 §e" + count + " §f只 NPC，其中 §b" + bones + " §f只带魂骨\n" + text), false);
        return count;
    }

    private static int showOdds(CommandSourceStack source, int level) {
        double has = NpcBoneGenerator.npcHasBoneChance(level);
        double drop = NpcBoneGenerator.dropChance(level);
        double both = has * drop;
        double seven = NpcBoneGenerator.sevenBoneChance(level);
        long sample = NpcBoneGenerator.rollNianxian(level);

        source.sendSuccess(() -> Component.literal(
                "§6[概率] §f等级 §e" + level + "\n"
                        + "§7  年限范围样例：§b" + sample + " 年\n"
                        + "§7  第 1 枚自带概率：§b" + String.format("%.4f%%", has * 100)
                        + " §8(1/" + String.format("%.0f", 1.0 / has) + ")\n"
                        + "§7  击杀掉率：§b" + String.format("%.0f%%", drop * 100) + " §8(带骨必掉)\n"
                        + "§7  综合拿到 1 枚：§b" + String.format("%.6f%%", both * 100)
                        + " §8(约 1/" + String.format("%.0f", 1.0 / both) + ")\n"
                        + "§7  §d出现满 7 枚§7：§b" + String.format("%.3e%%", seven * 100)
                        + "\n§8    ≈ 1/" + String.format("%,.0f", 1.0 / seven) + " （理论存在，实测见不到）"),
                false);
        return 1;
    }

    /** 数一下 NPC 魂骨槽里实际有几枚（比 NBT 可靠，槽可能被其它代码动过）。 */
    private static int countBones(PlayerNpcEntity npc) {
        int n = 0;
        for (int i = 0; i < 7; i++) {
            if (!npc.getSoulCapability().getHunguInventory().getStackInSlot(i).isEmpty()) n++;
        }
        return n;
    }

    private static String fmt(float v) {
        if (v == (long) v) return String.format("%,d", (long) v);
        return String.format("%,.1f", v);
    }
}