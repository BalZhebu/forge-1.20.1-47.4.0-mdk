package com.TovidY.kunluncontinent.item.baseskillist.tianshi;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * 天使系魂技的「实力」判定 —— <b>临时实现，等着被替换</b>。
 *
 * <h3>为什么单独拎一个类出来</h3>
 * <p>第六魂技（禁锢时长）与第九魂技（斩杀判定）都需要「谁比谁强」这个结论。
 * 用户之后会写一个专门判断实力高低的工具类，届时<b>只需要改本类</b>，
 * 六个技能类一行都不用动 —— 所以这里除了战力公式，不写任何别的逻辑。</p>
 *
 * <h3>现在的临时算法</h3>
 * <pre>
 *   战力 = 最大生命 + 攻击力 × 10
 * </pre>
 * <p>全部走 {@link ModAttributeAPI}：玩家的加点/魂环/魂骨/护甲、怪物的
 * {@code MobAttributeCapability}（含挑战塔层数倍率）都会算进去，
 * 比读原版 {@code getMaxHealth()} 准得多。取不到模组属性时回退到原版属性。</p>
 */
public final class TianshiPower {

    /** 战力公式里攻击力的权重。 */
    private static final float ATTACK_WEIGHT = 10.0f;

    /** 禁锢时长下限（秒）。 */
    public static final int JINGU_MIN_SECONDS = 3;
    /** 禁锢时长上限（秒）。 */
    public static final int JINGU_MAX_SECONDS = 120;

    /**
     * 战力比值的两个端点：
     * <ul>
     *   <li>{@link #RATIO_WEAK}（对手只有自己 1/8）及以下 → 满时长；</li>
     *   <li>{@link #RATIO_STRONG}（对手是自己 2 倍）及以上 → 最短时长。</li>
     * </ul>
     * 中间取对数插值 —— 实力差距用手感衡量更像是对数关系，
     * 线性插值会让「稍微弱一点」和「弱很多」几乎没区别。
     */
    private static final float RATIO_WEAK = 0.125f;
    private static final float RATIO_STRONG = 2.0f;

    private TianshiPower() {
    }

    /** 任意实体的战力。 */
    public static float of(Entity entity) {
        if (entity == null) {
            return 0.0f;
        }
        float hp = ModAttributeAPI.getMaxshengming(entity);
        float atk = ModAttributeAPI.getGongji(entity);
        if (entity instanceof LivingEntity living) {
            if (hp <= 0.0f) {
                hp = living.getMaxHealth();
            }
            if (atk <= 0.0f) {
                atk = (float) living.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
        }
        return Math.max(0.0f, hp) + Math.max(0.0f, atk) * ATTACK_WEIGHT;
    }

    /**
     * 「目标比自己弱多少」→ 0~1（1 = 弱得离谱，0 = 比自己强得多）。
     *
     * <p>{@code log} 插值：比值 0.125 给 1，比值 2.0 给 0，端点外钳制。</p>
     */
    public static float weakness(Player caster, Entity target) {
        float mine = Math.max(1.0f, of(caster));
        float theirs = Math.max(0.0f, of(target));
        // 目标战力为 0（纯建筑类实体）当作极弱
        float ratio = theirs <= 0.0f ? 0.0f : theirs / mine;
        if (ratio <= RATIO_WEAK) {
            return 1.0f;
        }
        if (ratio >= RATIO_STRONG) {
            return 0.0f;
        }
        double hi = Math.log(RATIO_STRONG);
        double lo = Math.log(RATIO_WEAK);
        double t = (hi - Math.log(ratio)) / (hi - lo);
        return (float) Math.max(0.0, Math.min(1.0, t));
    }

    /**
     * 禁锢时长（tick）：3 秒 ~ 120 秒，越弱锁得越久。
     *
     * <p>⚠️ 这是「禁锢多久」的唯一落点。换实力工具类时改这里，
     * 第六魂技与第八魂技会同时跟着变。</p>
     */
    public static int jinguTicks(Player caster, Entity target) {
        float w = weakness(caster, target);
        int seconds = Math.round(JINGU_MIN_SECONDS + (JINGU_MAX_SECONDS - JINGU_MIN_SECONDS) * w);
        return seconds * 20;
    }
}
