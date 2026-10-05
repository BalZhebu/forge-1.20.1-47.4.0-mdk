package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.datagen.worldgenprovider.ModCelestialTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * 神界树苗。
 *
 * <p>继承原版 {@link SaplingBlock}（它本身继承 {@code BushBlock}），
 * 所以自动获得：</p>
 * <ul>
 *   <li>{@code STAGE} 属性（0=幼苗, 1=长成一半，骨粉第一下只推进阶段）</li>
 *   <li>随机刻自然生长（亮度 ≥ 9，每 1/7 概率长一阶段）</li>
 *   <li>骨粉催熟（{@code performBonemeal}）</li>
 *   <li>细十字碰撞形状（不会挡路）</li>
 * </ul>
 *
 * <p>长出来的树就是 {@link ModCelestialTreeFeature#DIVINE_REALM_TREE} 那个地物
 * —— 即神界群系里自然生成的那棵大树，<b>形态完全一致</b>，
 * 只是树苗催熟时会跳过 {@code RarityFilter} 之类只影响"自然分布"的修饰器，
 * 直接以 100% 成功率放置。</p>
 */
public class DivineRealmSaplingBlock extends SaplingBlock {

    public DivineRealmSaplingBlock(BlockBehaviour.Properties properties) {
        // 传一个只认准神界树地物的 grower，替代原版的"树品种类枚举"
        super(new DivineRealmTreeGrower(), properties);
    }

    /**
     * 只会返回神界树这一个地物 key 的 {@link AbstractTreeGrower}。
     *
     * <p>原版每种树各有一个 Grower 子类（OakTreeGrower / CherryTreeGrower…），
     * 内部靠 {@code getConfiguredFeature} 返回自己的 key。我们只需要一个，
     * 所以匿名实现即可 —— 顺便省掉了"树苗可能长错树"的可能。</p>
     */
    private static class DivineRealmTreeGrower extends AbstractTreeGrower {

        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random,
                                                                          boolean hasFlowers) {
            // 固定返回神界树，不做随机分支
            return ModCelestialTreeFeature.DIVINE_REALM_TREE;
        }

        @Override
        public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos,
                                 BlockState state, RandomSource random) {

            ResourceKey<ConfiguredFeature<?, ?>> key = getConfiguredFeature(random, true);
            if (key == null) return false;

            var holderOpt = level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE)
                    .getHolder(key);
            if (holderOpt.isEmpty()) return false;
            var holder = holderOpt.get();

            // 让 Forge 的"树叶生长/骨粉"事件有机会改写特征（比如别的模组拦截）
            var event = net.minecraftforge.event.ForgeEventFactory.blockGrowFeature(
                    level, random, pos, holder);
            if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY) {
                return false;
            }
            var finalHolder = event.getFeature();
            if (finalHolder == null) return false;

            ConfiguredFeature<?, ?> feature = finalHolder.value();

            BlockState cleared = level.getFluidState(pos).createLegacyBlock();
            level.setBlock(pos, cleared, 4);

            if (feature.place(level, generator, random, pos)) {
                if (level.getBlockState(pos) == cleared) {
                    level.sendBlockUpdated(pos, state, cleared, 2);
                }
                return true;
            }
            // 放置失败要把树苗放回去，不然就凭空消失了
            level.setBlock(pos, state, 4);
            return false;
        }
    }
}
