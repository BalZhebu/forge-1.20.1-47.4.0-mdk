package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * 极寒冰域的噪声设置（地形骨架）。
 *
 * <h3>做了什么</h3>
 * <p>整套地形参数**原样照抄主世界**（{@code NoiseSettings.OVERWORLD_NOISE_SETTINGS} +
 * {@code NoiseRouterData.overworld}），只换两处方块：</p>
 * <ol>
 *   <li>{@code defaultBlock}：原版 {@code minecraft:stone} → <b>极寒石</b>
 *       （地形主体、洞穴壁、海底全部跟着变）；</li>
 *   <li>表面规则最前面插一条：<b>y=0~8 的垂直带直接铺极寒石</b>。</li>
 * </ol>
 *
 * <h3>为什么第 2 条必须加</h3>
 * <p>深板岩<b>不是</b>噪声设置里的方块，而是 {@code SurfaceRuleData.overworld()} 里的一条规则：</p>
 * <pre>
 *   ifTrue(verticalGradient("deepslate", absolute(0), absolute(8)), DEEPSLATE)
 * </pre>
 * <p>它会在 y=0~8 把任何方块刷成深板岩 —— 所以只改 {@code defaultBlock} 是挡不住的
 * （这也是"为什么我改了石头，地下还是有深板岩"的原因）。</p>
 *
 * <p>{@code SurfaceRules.sequence} 是「第一条命中的规则生效」，把我们那条极寒石规则
 * 放在原版规则<b>前面</b>，y=0~8 就会先被极寒石吃掉，原版那条永远轮不到。</p>
 *
 * <p>⚠️ 气泡/含水层/矿脉等其余参数与主世界完全一致，不要为了"更冷"去动它们，
 * 否则会连带改变地形高度和洞穴分布。</p>
 */
public final class ModPolarWorldGen {

    private ModPolarWorldGen() {
    }

    public static void bootstrapPolarNoise(BootstapContext<NoiseGeneratorSettings> context) {
        // 原版主世界那一份（public 工厂方法），拿它的字段当基底再改两处。
        // ⚠️ 不能直接调 NoiseSettings.OVERWORLD_NOISE_SETTINGS / NoiseRouterData.overworld ——
        //    那两个是 protected，跨包摸不到。
        NoiseGeneratorSettings base = NoiseGeneratorSettings.overworld(context, false, false);

        context.register(ModDimensions.POLAR_ICE_NOISE, new NoiseGeneratorSettings(
                base.noiseSettings(),
                ModBlocks.EXTREME_COLD_STONE.get().defaultBlockState(),   // ⭐ 石头 → 极寒石
                base.defaultFluid(),
                base.noiseRouter(),
                polarSurfaceRules(),
                base.spawnTarget(),
                base.seaLevel(),
                base.disableMobGeneration(),
                base.aquifersEnabled(),
                base.oreVeinsEnabled(),
                base.useLegacyRandomSource()
        ));
    }

    /**
     * 表面规则 = 「y0~8 铺极寒石」+ 原版主世界规则。
     *
     * <p>前一条只影响原版深板岩带所在的那 8 格，其余地表（草/雪/沙/海底）完全走原版。</p>
     */
    private static SurfaceRules.RuleSource polarSurfaceRules() {
        return SurfaceRules.sequence(
                // 顺序很重要：这条必须先命中，才能盖掉原版的 deepslate 规则
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("polar_cold_stone_band",
                                VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)),
                        SurfaceRules.state(ModBlocks.EXTREME_COLD_STONE.get().defaultBlockState())),
                SurfaceRuleData.overworld()
        );
    }
}
