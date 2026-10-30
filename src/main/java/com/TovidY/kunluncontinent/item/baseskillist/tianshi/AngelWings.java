package com.TovidY.kunluncontinent.item.baseskillist.tianshi;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 「天使之翼」的开关状态 —— 唯一真相源（第二魂技）。
 *
 * <h3>和武魂真身的区别</h3>
 * <ul>
 *   <li>真身状态存 {@code persistentData}（纯服务端），因为客户端不需要知道；</li>
 *   <li>天使之翼会影响<b>战斗公式</b>（飞行时不再削减伤害），而 {@code CombatEventHandler}
 *       在客户端也跑一遍，所以状态必须<b>双端都能读</b> → 存在 capability 的
 *       {@code angelWingsActive} 字段里，随 {@code SPacketPlayerAttribute} 同步。</li>
 * </ul>
 *
 * <h3>它做什么</h3>
 * <ol>
 *   <li>开启：{@code mayfly = true}（不看精神力上限是否到 5000），可以自由起飞；</li>
 *   <li>飞行时照旧每 10 tick 扣精神力 —— 那段逻辑在 {@code PWPlayerTickEvent#updatePlayerFly}，
 *       本类<b>不</b>重复实现；</li>
 *   <li>飞行伤害削减取消（{@code CombatEventHandler#getFlyingDamageMultiplier} 返回 1.0）；</li>
 *   <li>精神力跌破上限的 {@link #AUTO_CLOSE_RATIO} → 自动关闭。</li>
 * </ol>
 */
public final class AngelWings {

    /** 精神力低于「上限 × 该比例」时自动关闭。 */
    public static final float AUTO_CLOSE_RATIO = 0.10f;

    private AngelWings() {
    }

    /** 玩家现在是否张开了天使之翼（双端可读）。 */
    public static boolean isActive(Player player) {
        if (player == null) {
            return false;
        }
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::isAngelWingsActive)
                .orElse(false);
    }

    /** 服务端：开启。调用方负责校验精神力门槛。 */
    public static void activate(ServerPlayer player, PlayerAttributeCapability cap) {
        cap.setAngelWingsActive(true);
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        playActivateFx(player);
        player.displayClientMessage(Component.literal("§e§l【天使之翼】§f圣翼展开，你已获得飞行之能"), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.8f, 1.4f);
        SynsAPI.synsPlayerAttribute(player);
    }

    /** 服务端：关闭。 */
    public static void deactivate(ServerPlayer player, PlayerAttributeCapability cap) {
        cap.setAngelWingsActive(false);
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
        playDeactivateFx(player);
        SynsAPI.synsPlayerAttribute(player);
    }

    /**
     * 服务端每 tick 调一次（由 {@code updatePlayerFly} 调用，只在已开启时进）。
     *
     * <p>自动关闭：精神力跌破 {@link #AUTO_CLOSE_RATIO} 就收翼 —— 否则「开着翅膀挂机」
     * 会变成免费无限飞行。</p>
     */
    public static void tickServer(ServerPlayer player, PlayerAttributeCapability cap) {
        if (!cap.isAngelWingsActive()) {
            return;
        }
        float max = ModAttributeAPI.getMaxjingshenli(player);
        if (max > 0.0f && cap.getJingshenli() < max * AUTO_CLOSE_RATIO) {
            deactivate(player, cap);
            player.displayClientMessage(Component.literal("§c精神力不足，天使之翼自行消散"), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.7f, 1.5f);
        }
    }

    // ==================================================================================
    //  视觉
    // ==================================================================================

    /** 展开：两片向上外扩的光翼 + 一圈金色环。 */
    public static void playActivateFx(ServerPlayer player) {
        // ---- 着色器版：什么都不发 ----
        // 翅膀虚影是**持续渲染**的（AngelWingRenderer 每帧读 angelWingsActive），
        // 不是一次性特效包 —— 这里再发一个包反而会在翅膀外面多闪一下。
        if (SkillFxToggle.useShader(player)) {
            return;
        }

        ParticleFx fx = ParticleFx.of(player.level(), player);
        if (fx == null) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        double rot = level.getGameTime() * 0.4;
        Vec3 base = player.position();
        fx.budget(1200);

        // 左 / 右两片光翼：由内向外逐层张开的弧线
        for (int side = -1; side <= 1; side += 2) {
            for (int layer = 0; layer < 4; layer++) {
                double height = 1.0 + layer * 0.55;
                double span = 1.1 + layer * 0.55;
                Vec3 from = base.add(side * 0.25, height, 0);
                Vec3 to = base.add(side * span, height + 0.5, 0);
                fx.line(layer % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.FIREWORK,
                        from, to, 0.22, 0.02, 0.02, Vec3.ZERO);
            }
        }
        fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.FIREWORK, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.8, rot);
        fx.burst(ParticleTypes.END_ROD, base.add(0, 1.2, 0), 30, 0.7, true);
        fx.dome(ParticleTypes.FIREWORK, base, 2.2, 3, 14);
    }

    /** 收起：光点向内塌陷。 */
    public static void playDeactivateFx(ServerPlayer player) {
        // 着色器版同样不发包：angelWingsActive 已经变 false，AngelWingRenderer 下一帧就不画了
        if (SkillFxToggle.useShader(player)) {
            return;
        }

        ParticleFx fx = ParticleFx.of(player.level(), player);
        if (fx == null) {
            return;
        }
        Vec3 base = player.position().add(0, 1.1, 0);
        fx.budget(500);
        fx.burst(ParticleTypes.END_ROD, base, 24, 0.35, false);
        fx.circle(ParticleTypes.FIREWORK, base, ParticleFx.Axis.Y, 1.6, 18, 0, 0.05);
    }
}
