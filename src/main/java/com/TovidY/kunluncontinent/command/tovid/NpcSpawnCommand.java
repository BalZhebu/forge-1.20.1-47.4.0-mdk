package com.TovidY.kunluncontinent.command.tovid;

import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.playernpc.NpcBoneGenerator;
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

/**
     * NPC 生成指令：{@code /kunluncontinent npc <level> <name...> [nianxian] [count]}
 *
 * <p><b>为什么年限/数量要"从贪心串里拆"</b>：{@code name} 用的是 {@code greedyString}（贪婪字符串），
 * 它会把后面所有内容一起吞掉—— 所以
 * {@code /kunluncontinent npc 99暮雨 1000000} 里 brigadier 只认为 name = "暮雨 1000000"，
 * 找不到皮肤"暮雨 1000000" → 报"未找到 NPC 皮肤"。</p>
 *
 * <p>解决办法：{@link #resolveSkin} 拿整串去匹配皮肤，匹配不上就<b>从末尾逐个剥掉纯数字</b>，
 * 第一个剥下来的当年限、第二个当数量，剩下的再匹配皮肤。</p>
 *
 * <p><b>⚠️ 消歧规则（重要）</b>：单数字时<b>一律当年限</b>，不当年数——
 * 因为魂骨年限和魂技等级都可能写成小数字，年限是更常用的那个。
 * 想要"多枚+ 随机年限"必须写两个数（{@code 暮雨 0 7}，其中 0 = 年限随机）。</p>
 *
 * <pre>
 *   /kunluncontinent npc 99 暮雨# 普通生成（自然概率，大概率没骨）
 *   /kunluncontinent npc 99 暮雨 1000000            # 必定带 100 万年魂骨（1 枚）
 *   /kunluncontinent npc 99 暮雨 1000000 7          # 必定带 7 枚，全是100 万年（调试用）
 *   /kunluncontinent npc 99 暮雨 0 7# 7 枚，年限随机
 *   /kunluncontinent npc 99 4th 1300000             # 名字含数字也没问题
 *   /kunluncontinent npc 99 0# 纯数字当皮肤 ID
 * </pre>
 */
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
                                                .executes(NpcSpawnCommand::spawn))
                                )
                        )
        );
    }

    /** 指令主执行体。 */
    private static int spawn(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int level = IntegerArgumentType.getInteger(context, "level");
        String raw = StringArgumentType.getString(context, "name").trim();

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§c[昆仑大陆] 该指令必须由玩家执行"));
            return 0;
        }
        ServerLevel serverLevel = player.serverLevel();
        Vec3 pos = player.position();

        // 1. 拆出年限（末尾纯数字）+ 皮肤名
        Resolved resolved = resolveSkin(raw);
        if (resolved == null) {
            source.sendFailure(Component.literal("§c[昆仑大陆] 未找到 NPC 皮肤 §e[" + raw + "]§c！"
                    + "\n§7  可用名字：§f" + String.join(" §7/ §f", skinNames())
                    + "\n§7  也可以在名字后加年限（必带骨）：§f/kunluncontinent npc 99 暮雨 1000000"
                    + "\n§7  再加枚数（1~7，调试用）：§f/kunluncontinent npc 99 暮雨 1000000 7"));
            return 0;
        }
        int skinIndex = resolved.skinIndex;
        long nianxian = resolved.nianxian;
        int exactCount = resolved.exactCount;

        // 2. 实例化与属性初始化
        PlayerNpcEntity npc = new PlayerNpcEntity(EntityInit.PLAYER_NPC.get(), serverLevel);
        npc.moveTo(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        npc.setSkinIndex(skinIndex);

        NpcSoulGenerator.initNpcSoulData(npc.getSoulCapability(), level);

        // 3. 指定了枚数就强制装满；给了年限就强制装（每枚同年限）；否则走自然概率
        int boneCount = NpcBoneGenerator.equipBones(npc, level, nianxian, exactCount);
        npc.recalculateNpcStats();

        serverLevel.addFreshEntity(npc);

        final int finalBoneCount = boneCount;
        final String boneInfo;
        if (boneCount > 0) {
            var stats = npc.getSoulCapability().getBoneOnlyStats();
            boneInfo = "\n§b  魂骨 §f" + boneCount + " 枚 §8| §7词条种类 " + stats.size()
                    + "\n§a  攻击 §f+" + fmt(stats.getOrDefault("gongji", 0f))
                    + " §8| §a生命 §f+" + fmt(stats.getOrDefault("maxshengming", 0f))
                    + " §8| §a防御 §f+" + fmt(stats.getOrDefault("fangyu", 0f))
                    + (exactCount > 0 ? " §8| §b§l强制 " + exactCount + " 枚" : "");
        } else {
            boneInfo = "\n§8  （本次未带魂骨，概率极低；想必定带请在名字后加年限，如 §f暮雨 1000000§8）";
        }

        source.sendSuccess(() -> Component.literal(
                "§a[昆仑大陆] 成功召唤 NPC: §e" + NpcSkinRegistry.getName(npc.getSkinIndex()).getString()
                        + " §a(等级: §6" + level + "§a)"
                        + boneInfo
                        + (finalBoneCount > 0 ? "\n§7  杀掉它会掉出身上全部 §b" + finalBoneCount + " §7枚魂骨" : "")),
                true);

        return 1;
    }

    /** 皮肤解析结果：皮肤下标 + 年限（-1 = 随机） + 强制枚数（0 = 走自然概率）。 */
    private static class Resolved {
        final int skinIndex;
        final long nianxian;
        final int exactCount;

        Resolved(int skinIndex, long nianxian, int exactCount) {
            this.skinIndex = skinIndex;
            this.nianxian = nianxian;
            this.exactCount = exactCount;
        }
    }

    /**
     * 从贪心串里解析出「皮肤 + 可选年限 + 可选枚数」。
     *
     * <p>算法：整串先按名字匹配、再按数字 ID 解析；都不成就把<b>末尾纯数字</b>依次剥掉
     * （最多剥两个：先剥的当年限、后剥的当枚数），剩下的再匹配皮肤。</p>
     *
     * <p>枚数限制 1~7；剥到0 或超出范围的数字一律当"无效"，退回只当年限处理。</p>
     *
     * @return 解析结果；无法解析返回 {@code null}
     */
    private static Resolved resolveSkin(String raw) {
        if (raw.isEmpty()) return null;

        // ① 整串当名字
        int byName = matchSkinByName(raw);
        if (byName >= 0) return new Resolved(byName, -1, 0);

        // ② 整串当数字 ID
        int byId = parseSkinId(raw);
        if (byId >= 0) return new Resolved(byId, -1, 0);

        // ③ 从末尾依次剥纯数字（最多两个：先剥的当年限、后剥的当枚数）
        String rest = raw;
        long nianxian = -1;
        int exactCount = 0;

        for (int step = 0; step < 2; step++) {
            int sp = lastSpace(rest);
            if (sp <= 0 || sp >= rest.length() - 1) break;
            String numPart = rest.substring(sp + 1).trim();
            if (!isAllDigits(numPart)) break;
            long value;
            try {
                value = Long.parseLong(numPart);
            } catch (NumberFormatException e) {
                break;
            }
            rest = rest.substring(0, sp).trim();

            if (step == 0) {
                // 第一个数字：0 或超出年限上限 → 当"随机年限"，其余当年限
                nianxian = (value > 0 && value <= 99_999_999L) ? value : -1;
            } else {
                // 第二个数字：1~7 才当枚数，其它忽略
                if (value >= 1 && value <= 7) exactCount = (int) value;
            }
        }

        // ④ 剩下的部分按名字 / 数字 ID 匹配
        int idx = matchSkinByName(rest);
        if (idx < 0) idx = parseSkinId(rest);
        if (idx < 0) return null;

        // 剥出来的数字一个都用不上 → 说明它本来就只是皮肤名的一部分
        boolean usedNumber = nianxian > 0 || exactCount > 0;
        return new Resolved(idx, usedNumber ? nianxian : -1, exactCount);
    }

    private static List<String> skinNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < NpcSkinRegistry.getSkinCount(); i++) {
            names.add(NpcSkinRegistry.getName(i).getString());
        }
        return names;
    }

    private static int matchSkinByName(String name) {
        for (int i = 0; i < NpcSkinRegistry.getSkinCount(); i++) {
            if (NpcSkinRegistry.getName(i).getString().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private static int parseSkinId(String s) {
        try {
            int id = Integer.parseInt(s);
            return (id >= 0 && id < NpcSkinRegistry.getSkinCount()) ? id : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** 最后一个空格的下标；没有空格返回 -1。 */
    private static int lastSpace(String s) {
        return s.lastIndexOf(' ');
    }

    private static boolean isAllDigits(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    private static String fmt(float v) {
        if (v == (long) v) return String.format("%,d", (long) v);
        return String.format("%,.1f", v);
    }
}