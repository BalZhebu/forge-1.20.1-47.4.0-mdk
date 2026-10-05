package com.TovidY.kunluncontinent.item.baseskillist;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 声明式魂技的特效逻辑。
 *
 * <p>只在服务端执行 —— {@link LambdaSkillItem} 已经把 level / player 转成了服务端类型，
 * lambda 里不用再做类型判断。</p>
 */
@FunctionalInterface
public interface SkillEffect {

    /**
     * @param level            服务端世界
     * @param player           释放者
     * @param powerMultiplier  魂环年限倍率（{@link BaseSkillItem#getPowerMultiplier} 的分档结果）
     * @param finalDamage      基类算好的伤害 = 攻击力 × 技能倍率 × 年限倍率
     */
    void execute(ServerLevel level, ServerPlayer player, float powerMultiplier, float finalDamage);
}
