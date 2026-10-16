package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.world.entity.player.Player;

/**
 * 技能特效的<b>全局开关判据</b> —— 决定某个玩家看到的技能场域特效
 * 走「粒子」还是走「着色器」。
 *
 * <p>面板上只有一条配置项「技能特效优化」，<b>所有</b>接了着色器的技能共用它，
 * 不要再为单个技能加开关（加一条就多一处要同步的地方）。</p>
 *
 * <h3>⚠️ 这一位的语义是反向的</h3>
 * <p><b>开启（默认）= 用粒子特效</b>，<b>关闭 = 用着色器渲染</b>。
 * 面板上叫「优化」，所以"开启"代表沿用既有那套粒子方案，
 * 玩家主动关掉它才切到新的着色器表现。</p>
 *
 * <h3>⚠️ 判断必须放在服务端</h3>
 * <p>粒子是服务端发 {@code ClientboundLevelParticlesPacket} 下发的，客户端收到就得画，
 * <b>没有本地过滤能力</b>。想让粒子真的不出现，只能在服务端决定发不发；
 * 场域特效包同理，两边在同一个地方二选一。</p>
 *
 * <p>这个类刻意<b>不加</b> {@code @OnlyIn(Dist.CLIENT)}：服务端发包前要调用它，
 * 一旦标了客户端专属，专用服务器上就会 {@code NoClassDefFoundError}。
 * （同包的 {@link SkillEffectStyles} 出于同样理由也是普通类。）</p>
 */
public final class SkillFxToggle {

    /**
     * 「技能特效优化」在玩家配置位掩码里的下标。
     *
     * <p>对应 {@code ConfigScreen} 里 {@code rawItems} 的 {@code ConfigItemData("技能特效优化", 13)}，
     * 默认值见 {@code PlayerAttributeCapability#DEFAULT_CONFIG_FLAGS}（默认开启）。</p>
     */
    public static final int CONFIG_SKILL_FX_SHADER = 13;

    /**
     * 该玩家是否使用<b>着色器版</b>技能特效。
     *
     * <p>注意取的是开关的<b>反</b>值：关闭「技能特效优化」才走着色器。
     * 取不到 capability 时返回 {@code false}（= 用粒子），保证异常情况下
     * 退回既有表现，而不是突然变成着色器。</p>
     */
    public static boolean useShader(Player player) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> !cap.isConfigOpen(CONFIG_SKILL_FX_SHADER))
                .orElse(false);
    }

    private SkillFxToggle() {
    }
}
