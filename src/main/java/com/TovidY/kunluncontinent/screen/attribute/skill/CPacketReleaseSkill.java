package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.S2CCastingSyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability.CASTING_SLOWDOWN_MODIFIER;

/**
 * 释放魂技（当前选中槽位）。
 *
 * <p>本类同时是<b>「一次释放」全部规则的唯一落点</b>：{@link #tryRelease} 里那串判定
 * （开关型先试关闭 → 吟唱互斥 → 精神力 → 冷却 → 吟唱 / 瞬发）由单发路径和
 * 「一键释放」（{@link SkillBatchRelease}）共用，所以两条路径的规则永远不会走偏。</p>
 */
public class CPacketReleaseSkill {

    /**
     * 「一键释放」里那<b>唯一一次</b>吟唱的时长（tick）。{@code 70} = 3.5 秒。
     *
     * <p>一批里最多只会发起一次吟唱（吟唱位本来也只有一个），
     * 所以这里锁死一个定值，队列据此知道"要等多久"。</p>
     *
     * <p>只影响批量释放；单独释放仍然用技能自己的 {@link BaseSkillItem#getCastTime()}。</p>
     */
    public static final int BATCH_CAST_TICKS = 70;

    public CPacketReleaseSkill() {
    }

    public static void encode(CPacketReleaseSkill msg, FriendlyByteBuf buffer) {
    }

    public static CPacketReleaseSkill decode(FriendlyByteBuf buffer) {
        return new CPacketReleaseSkill();
    }

    public static void handle(CPacketReleaseSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap ->
                    executeRelease(player, cap));
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * 释放模式 —— 决定哪些规则要放宽、哪些要收紧。
     *
     * <p>用枚举而不是两个 boolean，是因为「一键释放里第一次吟唱」和
     * 「本批之后的所有槽位」这两件事的差异必须一眼看出来，否则很容易改错。</p>
     */
    public enum ReleaseMode {
        /** 单发（轮盘释放键 / V 键 / 单击快捷键）：完全按技能自身规则走。 */
        SINGLE,
        /** 一键释放：本批<b>还没有</b>吟唱过 —— 允许这个技能发起那唯一一次吟唱。 */
        BATCH_ALLOW_CAST,
        /** 一键释放：本批<b>已经</b>吟唱过 —— 吟唱技能一律跳过（一批只唱一次）。 */
        BATCH_NO_CAST;

        boolean isBatch() {
            return this != SINGLE;
        }

        /** 批量模式下不打印失败类提示（放完由 SkillBatchRelease 汇总）。 */
        boolean quiet() {
            return isBatch();
        }

        boolean allowsCast() {
            return this != BATCH_NO_CAST;
        }
    }

    /**
     * 一次释放尝试的结果。
     *
     * <p>单发路径用它决定要不要回同步；批量路径用它汇总成一条提示。</p>
     */
    public enum Result {
        /** 瞬发技能已经结算完成。 */
        SUCCESS,
        /** 只是关闭了一个开关型技能（武魂真身）。 */
        TOGGLE_OFF,
        /** 开始吟唱，结果要等吟唱结束才出。 */
        STARTED_CASTING,
        /** 规则都通过了，但技能自己没做事（{@code handleRelease} 返回 false）。 */
        FAILED,
        /** 没有开启武魂。 */
        NO_WUHUN,
        /** 槽位是空的。 */
        EMPTY_SLOT,
        /** 正在吟唱中，拒绝新的释放。 */
        CASTING,
        /** 精神力不够。 */
        NOT_ENOUGH_MP,
        /** 还在冷却。 */
        ON_COOLDOWN,
        /** 一键释放专用：开关型技能不参与（避免「一键把自己的真身关了」）。 */
        SKIPPED_TOGGLE,
        /** 一键释放专用：本批已经吟唱过了，这个吟唱技能跳过（一批只唱一次）。 */
        SKIPPED_CAST,
        /** 一键释放专用：这次吟唱被中途打断了（伤害 / 换武魂 / 关武魂），没能放出来。 */
        CAST_INTERRUPTED
    }

    /** 释放当前选中槽位（轮盘释放键 / V 键 / 左侧快捷绑定）。 */
    public static void executeRelease(ServerPlayer player, PlayerAttributeCapability cap) {
        Result result = tryRelease(player, cap, cap.getSelectedSkillSlot(), ReleaseMode.SINGLE);
        if (result == Result.TOGGLE_OFF || result == Result.SUCCESS || result == Result.FAILED) {
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    /**
     * 尝试释放某个槽位。
     *
     * @param mode 释放模式（单发 / 批量允许吟唱 / 批量禁止吟唱）。
     *             批量模式下：<ul>
     *                <li><b>不打印失败类提示</b> —— 否则 9 个技能会把 ActionBar 刷成一片，
     *                    玩家只看得到最后一条，根本不知道哪些成功哪些失败。
     *                    放完会由 {@code SkillBatchRelease} 给一条汇总；</li>
     *                <li><b>不做开关型技能的开 / 关</b> —— 一键释放不该顺手改变「真身开着」这种状态；</li>
     *                <li><b>一批最多只发起一次吟唱</b>：第一次遇到的吟唱技能锁死
     *                    {@link #BATCH_CAST_TICKS}（3.5 秒），之后的吟唱技能直接跳过。</li>
     *              </ul>
     * @return 本次尝试的结果，调用方据此决定提示、回同步与排队
     */
    public static Result tryRelease(ServerPlayer player, PlayerAttributeCapability cap, int slot, ReleaseMode mode) {
        String currentWuhun = cap.getWuhunName();
        if (currentWuhun == null) {
            if (!mode.quiet()) {
                player.displayClientMessage(Component.literal("§c请先开启武魂！"), true);
            }
            return Result.NO_WUHUN;
        }

        BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(currentWuhun);
        if (skills == null || slot < 0 || slot >= 9) {
            // 与原逻辑一致：这种情况静默返回，不提示
            return Result.EMPTY_SLOT;
        }
        BaseSkillItem skill = skills[slot];
        if (skill == null) {
            if (!mode.quiet()) {
                player.displayClientMessage(Component.literal("§c当前槽位未装备魂技！"), true);
            }
            return Result.EMPTY_SLOT;
        }

        // 该槽位对应魂环的年限（决定消耗系数与威力倍率）
        int nianxian = 10;
        List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
        if (rings != null && slot < rings.size()) {
            nianxian = (int) rings.get(slot).getNianxian();
        }

        // 开关型技能（武魂真身）在批量模式下直接跳过：
        // 一键释放不该顺手把真身关掉，也不该在没有真身时把它开起来
        if (mode.isBatch() && !skill.hasUpfrontCost()) {
            return Result.SKIPPED_TOGGLE;
        }

        // 开关型技能的「已开启时再按 = 关闭」必须放在最前面：
        // 开启后玩家立刻处于冷却中，走正常流程会被下面的冷却检查拦住，
        // 导致「开了就关不掉」。
        if (!mode.isBatch() && skill.tryToggleOff(player)) {
            return Result.TOGGLE_OFF;
        }

        if (cap.getCurrentCastingSkill() != null) {
            if (!mode.quiet()) {
                player.displayClientMessage(Component.literal("§7正在吟唱中..."), true);
            }
            return Result.CASTING;
        }

        // 一批只唱一次：本批已经吟唱过了，后面的吟唱技能直接跳过
        if (skill.getCastTime() > 0 && !mode.allowsCast()) {
            return Result.SKIPPED_CAST;
        }

        float costMultiplier = skill.getCostMultiplier(nianxian);
        float finalCost = skill.getFinalCost(costMultiplier);
        if (skill.hasUpfrontCost() && cap.getJingshenli() < finalCost) {
            if (!mode.quiet()) {
                player.displayClientMessage(
                        Component.literal("§c精神力不足！需要 §e" + String.format("%.1f", finalCost) + " §c当前 §e" + String.format("%.1f", cap.getJingshenli())),
                        true
                );
            }
            return Result.NOT_ENOUGH_MP;
        }

        long lastUsed = cap.getSkillLastUsedTime(currentWuhun, slot);
        long currentTime = player.level().getGameTime();
        int cooldownTicks = skill.getCooldownTicks();
        if (currentTime - lastUsed < cooldownTicks) {
            if (!mode.quiet()) {
                float remainingSeconds = (cooldownTicks - (currentTime - lastUsed)) / 20.0f;
                player.displayClientMessage(
                        Component.literal("§c魂技冷却中... 剩余 §e" + String.format("%.1f", remainingSeconds) + "§cs"),
                        true
                );
            }
            return Result.ON_COOLDOWN;
        }

        int castTime = skill.getCastTime();
        if (castTime > 0) {
            // 批量释放：那唯一一次吟唱的时长锁死，队列据此安排"等多久再放下一个"
            int effectiveCastTime = mode.isBatch() ? BATCH_CAST_TICKS : castTime;

            boolean protectedCast =
                    player.getRandom().nextFloat() < ModAttributeAPI.getKangdapan(player);

            cap.startCasting(skill, slot, effectiveCastTime, protectedCast);
            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && !speedAttr.hasModifier(CASTING_SLOWDOWN_MODIFIER)) {
                speedAttr.addTransientModifier(CASTING_SLOWDOWN_MODIFIER);
            }
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CCastingSyncPacket(effectiveCastTime));

            if (mode.isBatch()) {
                // 批量里的吟唱要占好几秒，必须让玩家知道"正在唱哪个、还要多久"，
                // 否则这期间一点反馈都没有。用 Component 链而不是 getString() ——
                // 服务端取不到翻译文本，必须交给客户端解析。
                player.displayClientMessage(Component.literal("§7一键释放 §8· §e吟唱中 §7")
                        .append(Component.translatable(skill.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" §8(锁定 " + (BATCH_CAST_TICKS / 20.0f) + " 秒)")), true);
            } else {
                player.displayClientMessage(protectedCast
                        ? Component.literal("")
                        : Component.translatable("gui.kunluncontinent.casting"), true);
            }
            return Result.STARTED_CASTING;
        }

        boolean cast = skill.handleRelease(player.level(), player, nianxian);
        if (cast) {
            cap.setSkillLastUsedTime(currentWuhun, slot, currentTime);
        }
        return cast ? Result.SUCCESS : Result.FAILED;
    }
}
