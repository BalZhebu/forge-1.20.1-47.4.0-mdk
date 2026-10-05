package com.TovidY.kunluncontinent.godclass.buff;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 天使神神位被动 ——「圣光裁决」。
 *
 * <p>对所有<b>亡灵性生物</b>造成的伤害 <b>+1000%</b>（即最终伤害 × 11）。</p>
 *
 * <p>判定用 {@code MobType.UNDEAD}（原版僵尸/骷髅/凋灵/幻翼/潜影贝都是这一类，
 * 与本项目 {@code KLivingDeathEvent} 判定亡灵保持同一口径）。</p>
 *
 * <p>加伤位置选在 {@link LivingHurtEvent}：此时伤害已算出但还没扣血，
 * 且 {@code CombatEventHandler} 也在改同一个事件 —— 两者都改同一个值时
 * <b>后者覆盖前者</b>，所以这里用 {@code event.getAmount()} 读当前值再乘，
 * 而不是自己重算，避免把玩家的属性加成算丢。</p>
 */
public class AngelGodBuff extends GodBuff {

    public static final AngelGodBuff INSTANCE = new AngelGodBuff();

    /** 伤害倍率：+1000% = ×11。 */
    public static final float DAMAGE_MULTIPLIER = 11.0F;

    public AngelGodBuff() {
        super("angel_god", ChatFormatting.YELLOW, "圣光裁决",
                "对亡灵生物造成的伤害 +1000%");
    }

    @Override
    public float onLivingHurt(LivingHurtEvent event) {
        // 1. 攻击者必须持有该神位
        if (!(event.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return Float.NaN;
        }
        if (!GodBuffs.hasGod(player, godId())) return Float.NaN;

        // 2. 受害方必须是亡灵
        if (!isUndead(event.getEntity())) return Float.NaN;

        // 3. 乘算：读当前值（可能已被 CombatEventHandler 改过）再乘
        return event.getAmount() * DAMAGE_MULTIPLIER;
    }

    /** 亡灵判定，与项目其他地方统一用 MobType。 */
    public static boolean isUndead(LivingEntity entity) {
        return entity.getMobType() == net.minecraft.world.entity.MobType.UNDEAD;
    }
}
