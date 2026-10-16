package com.TovidY.kunluncontinent.special;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * 「特殊生物」的魂环行为描述 —— 娱乐魂环 / 剧情魂环的接入点。
 *
 * <h3>为什么需要它</h3>
 * <p>普通魂兽的魂环完全由年限驱动：掉不掉看 {@code KLConfig} 的档位概率，
 * 给什么魂技看各武魂的 {@code SkillLibrary} 技能池。但有些生物是
 * <b>娱乐性 / 剧情性</b>的（比如孤竹），它们的魂环不该走那套概率与技能池，
 * 而要有一套自己的规则。</p>
 *
 * <h3>怎么接一个新生物</h3>
 * <ol>
 *   <li>建一个 {@code special/<生物名>/} 子包；</li>
 *   <li>在里面实现本接口（+ 需要的特殊魂技类）；</li>
 *   <li>在 {@link SpecialSoulRingRegistry} 的 {@code ensureInit()} 里加一行 {@code register(...)}。</li>
 * </ol>
 * <p>掉落判定（{@code KLivingDeathEvent}）和技能分配（{@code HunhuanEntity#handleAbsorbed}）
 * 都会自动查表，<b>不需要再改任何流程代码</b>。</p>
 */
public interface SpecialSoulRing {

    /** 掉落这个魂环的生物类型。 */
    EntityType<?> entityType();

    /**
     * 魂环是否<b>必定</b>掉落（跳过年限概率表）。
     * <p>默认 {@code true} —— 娱乐魂环本来就不该被概率卡住。</p>
     */
    default boolean alwaysDropSoulRing() {
        return true;
    }

    /**
     * 魂骨是否<b>必定不掉</b>（彻底不掷骰）。
     * <p>默认 {@code true} —— 娱乐生物不该顺带产出魂骨。</p>
     */
    default boolean neverDropSoulBone() {
        return true;
    }

    /**
     * 这个魂环被吸收时，<b>它所占据的那个槽位</b>应该赋予的魂技。
     *
     * <p>返回 {@code null} 表示「回落到该武魂的正常技能池」。</p>
     *
     * @param wuhun     吸收时玩家当前开启的武魂名
     * @param ringIndex 该魂环在魂环列表中的序号（1 起，同时也等于魂技槽位 +1）
     * @param random    世界随机源（想做随机变体时用）
     */
    @Nullable
    BaseSkillItem createSkill(String wuhun, int ringIndex, RandomSource random);
}
