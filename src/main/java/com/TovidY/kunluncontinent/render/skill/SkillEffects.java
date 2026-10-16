package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.render.skill.effect.Bahuang8ThunderEffect;
import com.TovidY.kunluncontinent.render.skill.effect.Bahuang9FieldEffect;
import com.TovidY.kunluncontinent.render.skill.effect.BeamEffect;
import com.TovidY.kunluncontinent.render.skill.effect.BeamPreset;
import com.TovidY.kunluncontinent.render.skill.effect.BurstEffect;
import com.TovidY.kunluncontinent.render.skill.effect.SigilEffect;
import com.TovidY.kunluncontinent.render.skill.effect.SigilPreset;
import com.TovidY.kunluncontinent.render.skill.effect.ShockwaveEffect;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 技能场域特效的<b>样式注册表</b> —— 服务端只发一个数字 id，客户端在这里换成具体特效。
 *
 * <h3>怎么接一个新技能</h3>
 * <ol>
 *   <li>在 {@code render/skill/effect/} 下写一个 {@link SkillEffect} 子类；</li>
 *   <li>在下面加一个 id 常量，并在 {@link #bootstrap()} 里注册它的工厂；</li>
 *   <li>服务端技能里的 {@code executeEffect} 发 {@code S2CSkillFieldPacket} 时带上这个 id。</li>
 * </ol>
 * <p>发数字而不是字符串：包更小，也不会因为拼错字符串变成"静默不显示"。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class SkillEffects {

    /** 由「发包方」调用的构造器。 */
    @FunctionalInterface
    public interface Factory {
        SkillEffect create(int ownerEntityId, Vec3 origin, float radius, long startTick, int durationTicks);
    }

    private static final Map<Integer, Factory> FACTORIES = new LinkedHashMap<>();
    private static boolean bootstrapped = false;

    /**
     * 已经打过「特效已创建」日志的样式 id。
     *
     * <p>每个样式每会话只报一次 —— 目的不是调试输出，而是把
     * <b>「服务端到底有没有发这个特效包」</b>变成一条可查的事实。</p>
     *
     * <p>为什么需要它：着色器「加载成功」的日志只在<b>首次</b>编译时出现，
     * 所以当某个着色器已经被别的技能加载过之后再测新技能，日志里<b>不会有任何新行</b>，
     * 于是「技能没发包」和「包发了但没画出来」这两种完全不同的故障在日志上长得一模一样。</p>
     */
    private static final Set<Integer> LOGGED_STYLES = new HashSet<>();

    private SkillEffects() {
    }

    public static void register(int id, Factory factory) {
        FACTORIES.put(id, factory);
    }

    /**
     * 建一个特效。
     *
     * @return 不认识这个 id 时返回 {@code null}（客户端比服务端旧的情况，静默忽略即可）
     */
    @Nullable
    public static SkillEffect create(int id, int ownerEntityId, Vec3 origin, float radius,
                                     long startTick, int durationTicks) {
        ensureBootstrap();
        Factory factory = FACTORIES.get(id);
        if (factory == null) {
            // 未注册的样式 id 原来会被彻底静默吃掉 —— 表现和"着色器加载失败"一模一样。
            // 这条每次都会打（真出问题时不刷屏才怪），方便第一时间定位。
            KlMain.LOGGER.error("[昆仑] 技能特效样式 id 未注册，已忽略：style={}（客户端 ID 表比服务端旧？）", id);
            return null;
        }
        SkillEffect effect = factory.create(ownerEntityId, origin, radius, startTick, durationTicks);
        if (LOGGED_STYLES.add(id)) {
            // 只报一次：够证明"这个样式的包确实到了客户端"，又不会刷屏
            KlMain.LOGGER.info("[昆仑] 技能特效已创建：style={} 实现={} duration={}tick radius={} owner={}",
                    id, effect.getClass().getSimpleName(), durationTicks, radius, ownerEntityId);
        }
        return effect;
    }

    /**
     * 重新武装「特效已创建」日志的节流集合。
     *
     * <p>资源重载（F3+T）时调用 —— 这样在不重启游戏的情况下就能重新记录一遍
     * "哪些样式真的被创建了"，方便连续排查。</p>
     */
    public static void resetLogThrottle() {
        LOGGED_STYLES.clear();
    }

    private static void ensureBootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        register(SkillEffectStyles.BAHUANG9_FIELD, Bahuang9FieldEffect::new);
        register(SkillEffectStyles.BAHUANG8_BOLT, Bahuang8ThunderEffect::new);

        // ---- 地面符文阵：同一个 SigilEffect，靠预设区分外观 ----
        register(SkillEffectStyles.SIGIL_HANYUE, sigil(SigilPreset.HANYUE));
        register(SkillEffectStyles.SIGIL_BUZHOU, sigil(SigilPreset.BUZHOU));
        register(SkillEffectStyles.SIGIL_LIETIAN, sigil(SigilPreset.LIETIAN));
        register(SkillEffectStyles.SIGIL_POJIE, sigil(SigilPreset.POJIE));

        // ---- 带状能量体：同一个 BeamEffect，靠预设区分外观 ----
        register(SkillEffectStyles.BEAM_STONE_PILLAR, beam(BeamPreset.STONE_PILLAR));
        register(SkillEffectStyles.BEAM_BUZHOU_PILLAR, beam(BeamPreset.BUZHOU_PILLAR));
        register(SkillEffectStyles.BEAM_POJIE_PILLAR, beam(BeamPreset.POJIE_PILLAR));

        // ---- 破魂枪：同样用 BeamEffect，但换专用 fragment shader ----
        register(SkillEffectStyles.POHUN8_RIFT,
                beam(BeamPreset.VOID_RIFT, BeamEffect.SHADER_RIFT));
        register(SkillEffectStyles.POHUN9_BLADE,
                beam(BeamPreset.DEATH_RAY, BeamEffect.SHADER_BLADE));

        // ---- 扩散光波 ----
        register(SkillEffectStyles.SHOCK_BUZHOU, ShockwaveEffect::new);

        // ---- 贴在生物身上的屏幕对齐特效：同一个 BurstEffect，靠 Style 区分外观 ----
        register(SkillEffectStyles.BURST_LIETIAN, burst(BurstEffect.LIETIAN_BURST));
        register(SkillEffectStyles.VORTEX_POJIE, burst(BurstEffect.POJIE_VORTEX));
        register(SkillEffectStyles.VORTEX_POJIE_LIGHT, burst(BurstEffect.POJIE_LIGHT));
    }

    /**
     * 用闭包把「预设」绑进工厂。
     *
     * <p>{@link Factory#create} 的签名里没有额外参数 —— 与其为了传风格去改网络协议或
     * 改工厂签名，不如注册多个 id、各自闭包捕获自己的预设。
     * 代价只是多几个 id 常量，换来的是一处都不用动。</p>
     */
    private static Factory sigil(SigilPreset preset) {
        return (ownerEntityId, origin, radius, startTick, durationTicks) ->
                new SigilEffect(ownerEntityId, origin, radius, startTick, durationTicks, preset);
    }

    private static Factory burst(BurstEffect.Style style) {
        return (ownerEntityId, origin, radius, startTick, durationTicks) ->
                new BurstEffect(ownerEntityId, origin, radius, startTick, durationTicks, style);
    }

    private static Factory beam(BeamPreset preset) {
        return (ownerEntityId, origin, radius, startTick, durationTicks) ->
                new BeamEffect(ownerEntityId, origin, radius, startTick, durationTicks, preset);
    }

    private static Factory beam(BeamPreset preset, String shaderName) {
        return (ownerEntityId, origin, radius, startTick, durationTicks) ->
                new BeamEffect(ownerEntityId, origin, radius, startTick, durationTicks, preset, shaderName);
    }
}
