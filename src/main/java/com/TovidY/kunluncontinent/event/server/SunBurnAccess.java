package com.TovidY.kunluncontinent.event.server;

/**
 * 让外部能问到「这只 Mob 此刻是不是正被阳光灼烧」。
 *
 * <p>原版把这个判断写成 {@code Mob#isSunBurnTick()}（protected），而且<b>调用点散落在各个亡灵子类</b>
 * 的 {@code aiStep} 里（Zombie / AbstractSkeleton / Phantom …），没法在事件里统一复刻 ——
 * 自己写判定会把「亡灵但不怕太阳」的生物（比如凋灵）也一起算进去。</p>
 *
 * <p>所以由 {@code MixinMobSunBurn} 在方法返回处记一笔，这里只负责读。</p>
 *
 * <p>⚠️ <b>这个接口绝对不能放进 {@code com.TovidY.kunluncontinent.mixin} 包</b>：
 * mixin config 把那个包整个划成了 mixin 私有区，普通类引用它会直接炸
 * {@code IllegalClassLoadError: ... is in a defined mixin package ... cannot be referenced directly}，
 * 而且是发生在 {@code Blocks.<clinit>} 那种极早期阶段，整个游戏起不来。</p>
 */
public interface SunBurnAccess {

    /** 最近是否处于阳光灼烧（带 {@code 3} 秒宽限，见 {@code MixinMobSunBurn} 里的说明）。 */
    boolean kunlun$isSunBurning();

    /** 每 tick 调一次，衰减宽限期。 */
    void kunlun$tickSunBurn();
}
