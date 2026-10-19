package com.TovidY.kunluncontinent.mixin;

import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让「极冰领域」永久处于降雪状态。
 *
 * <h3>为什么不能用 {@code setWeatherParameters}</h3>
 * <p>除了主世界，每个维度的 {@code ServerLevelData} 都是
 * {@code DerivedLevelData}，它的天气 setter <b>全是空实现</b>：</p>
 * <pre>
 *   public void setRaining(boolean raining) { }        // 什么都不做
 *   public void setRainTime(int time) { }
 *   public boolean isRaining() { return this.wrapped.isRaining(); }   // 读主世界
 * </pre>
 * <p>所以：</p>
 * <ul>
 *   <li>在极冰维度执行 {@code /weather rain} <b>完全没有效果</b>（写入被丢弃）；</li>
 *   <li>{@code ServerLevel#setWeatherParameters} 同样无效；</li>
 *   <li>极冰维度下不下雪，实际取决于<b>主世界</b>的天气 ——
 *       这就是"挂机很久也不下雪、偶尔看到雪"的原因。</li>
 * </ul>
 *
 * <h3>做法</h3>
 * <p>只截获一个调用点：{@code advanceWeatherCycle} 里的 {@code levelData.isRaining()}。
 * 让它在极冰维度恒返回 {@code true}，于是原版那段</p>
 * <pre>
 *   if (levelData.isRaining()) rainLevel += 0.01F; else rainLevel -= 0.01F;
 * </pre>
 * <p>会一路把 {@code rainLevel} 推到 1.0 并<b>稳定下来</b>。之后：</p>
 * <ul>
 *   <li>{@code Level#isRaining()}（= {@code getRainLevel(1.0F) > 0.2}）自动为 true，
 *       所以不必再单独注入它；极寒 handler 的"暴风雪"分支也会正常触发；</li>
 *   <li>{@code oRainLevel != rainLevel} 只在上涨那 100 tick 内成立，期间把
 *       {@code RAIN_LEVEL_CHANGE} 发给该维度玩家，客户端 {@code rainLevel} 被推到 1；</li>
 *   <li>稳定后不再有任何天气包 —— <b>常态零发包、零字段写入</b>。</li>
 * </ul>
 *
 * <p>⚠️ {@code isRaining()} / {@code getRainLevel()} 都声明在 {@link net.minecraft.world.level.Level}，
 * {@code ServerLevel} 并没有覆写它们，所以不能在本类里 {@code @Inject} 这两个方法
 * （运行期会报 InjectionError）。</p>
 *
 * <p><b>为什么是"下雪"而不是"下雨"</b>：降水类型只看群系温度
 * （{@code Biome#coldEnoughToSnow} = {@code temperature < 0.15}），
 * 极冰群系是 {@code temperature(-0.8F)}，所以同样一场雨在该维度必然渲染为雪。</p>
 *
 * <p><b>副作用</b>：该维度会一直有原版的降雨音效（雪在代码里仍属 precipitation），
 * 且主世界的天气不会因此改变 —— 每个维度各算各的。</p>
 */
@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    private boolean kunlun$isPolarIceRealm() {
        return ((ServerLevel) (Object) this).dimension().equals(ModDimensions.POLAR_ICE_REALM_LEVEL_KEY);
    }

    /**
     * 让 {@code advanceWeatherCycle} 里的降雨强度计算读到 {@code true}。
     *
     * <p>原逻辑是 {@code levelData.isRaining() ? rainLevel += 0.01F : rainLevel -= 0.01F}，
     * 而该维度的 {@code levelData}（{@code DerivedLevelData}）读的是主世界状态。
     * 截获这一个调用点后，极冰维度的 {@code rainLevel} 会一路涨到 1.0 并稳定，
     * {@code oRainLevel != rainLevel} 不再成立 —— <b>稳定后不再发任何天气包</b>。</p>
     *
     * <p>⚠️ 目标类型必须是 {@link WritableLevelData} 而不是 {@code LevelData}：
     * {@code Level#levelData} 字段声明的类型就是 {@code WritableLevelData}，
     * 字节码里是 {@code invokeinterface WritableLevelData.isRaining}。
     * 写成 {@code LevelData} 会导致 {@code (0/1) succeeded} 的注入失败并崩服（已踩）。</p>
     */
    @Redirect(
            method = "advanceWeatherCycle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/WritableLevelData;isRaining()Z")
    )
    private boolean kunlun$forceRainingForCycle(WritableLevelData levelData) {
        if (kunlun$isPolarIceRealm()) {
            return true;
        }
        return levelData.isRaining();
    }
}
