package com.TovidY.kunluncontinent.entity.lonebamboo;

import com.TovidY.kunluncontinent.worldgen.ModBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * 孤竹 —— 主世界地表的一株静止景观竹子。
 *
 * <h3>行为</h3>
 * <ul>
 *   <li><b>不移动</b>：{@link #registerGoals()} 留空（没有任何 AI 目标），
 *       且 {@link #isPushable()} 返回 false，实体推不动它。</li>
 *   <li><b>和平</b>：继承 {@link PathfinderMob}（不是 Monster），
 *       {@link #canAttack} 直接返回 false，永远不会主动攻击。</li>
 *   <li><b>不消失</b>：{@link #removeWhenFarAway} 返回 false —— 它是景观，
 *       玩家走远了再回来应该还在。</li>
 * </ul>
 *
 * <h3>生成</h3>
 * <p>刷怪表由 {@code ModBiomeModifiers.ADD_LONE_BAMBOO} 挂在
 * {@code #minecraft:is_overworld} 上，具体位置校验见
 * {@link #checkLoneBambooSpawnRules}：可生成动物的地面 + 两格净空 +
 * 排除 {@link ModBiomeTags#LONE_BAMBOO_BLOCKED}（沙漠/海洋/沙滩/河流）。
 * <b>刻意不做亮度判定，所以昼夜都会生成。</b></p>
 */
public class LoneBambooEntity extends PathfinderMob {

    // ==================================================================================
    //  随机体型
    // ==================================================================================

    /** 最小体型倍率（= 模型原始大小）。 */
    public static final float MIN_SCALE = 1.0F;

    /** 最大体型倍率。 */
    public static final float MAX_SCALE = 5.0F;

    private static final String NBT_SCALE = "BambooScale";

    /**
     * 体型倍率（{@link #MIN_SCALE} ~ {@link #MAX_SCALE}），每株生成时随机一次。
     *
     * <p><b>为什么用同步数据而不是普通字段：</b>命中箱的宽度在客户端也要一致，
     * 否则客户端射线检测用的还是 1 倍箱，玩家点不中高处那一截（表现为
     * 「大竹子只能点根部才有效」）。</p>
     */
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(LoneBambooEntity.class, EntityDataSerializers.FLOAT);

    /**
     * 常态摇曳动画的状态。
     *
     * <p>{@code AnimationState} 在 {@code net.minecraft.world.entity} 包，是 common 类，
     * 双端持有都安全（它本身不做任何客户端专属的事）。真正的关键点是：
     * <b>它必须放在实体上</b>，因为模型实例是所有同种实体共用的，
     * 而 {@code updateTime} 算的是帧间增量，共用会互相打断。</p>
     */
    public final AnimationState idleAnimationState = new AnimationState();

    public LoneBambooEntity(EntityType<? extends LoneBambooEntity> type, Level level) {
        super(type, level);
        // 生成时随机体型。客户端也会先随机一份，随后被同步数据覆盖 —— 无副作用。
        this.entityData.set(DATA_SCALE, rollScale(level.getRandom()));
        // Entity 构造时用的是「类型注册的原始尺寸」，不走 getDimensions(Pose)，
        // 所以改完体型必须手动刷一次命中箱。
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SCALE, MIN_SCALE);
    }

    /** 随机体型：{@link #MIN_SCALE} ~ {@link #MAX_SCALE}。 */
    public static float rollScale(RandomSource random) {
        return MIN_SCALE + random.nextFloat() * (MAX_SCALE - MIN_SCALE);
    }

    /** 当前体型倍率。 */
    public float getBambooScale() {
        return this.entityData.get(DATA_SCALE);
    }

    /** 设置体型倍率（自动钳制到合法区间），并立刻刷新命中箱。 */
    public void setBambooScale(float scale) {
        float clamped = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (this.entityData.get(DATA_SCALE) == clamped) {
            return;
        }
        this.entityData.set(DATA_SCALE, clamped);
        this.refreshDimensions();
    }

    /**
     * 让命中箱跟着体型一起缩放。
     *
     * <p>1.20.1 没有「体型属性」，{@code LivingEntity#getDimensions(Pose)} 内部就是
     * {@code super.getDimensions(pose).scale(this.getScale())} —— 所以覆盖
     * {@code getScale()} 是官方认可的缩放挂点（原版乌龟就是这么做的）。
     * 模型本身的缩放见 {@code LoneBambooRenderer#scale}。</p>
     */
    @Override
    public float getScale() {
        return this.getBambooScale();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SCALE.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(NBT_SCALE, this.getBambooScale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(NBT_SCALE)) {
            this.setBambooScale(tag.getFloat(NBT_SCALE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // 常态动画：没有触发条件，启动一次之后一直循环。
            // startIfStopped 是幂等的，重复调用只是一次 boolean 检查。
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)   // 不移动，速度直接给 0
                .add(Attributes.FOLLOW_RANGE, 8.0D);
    }

    /** 不注册任何 AI：没有目标 = 不会自己走动、不会看向玩家。 */
    @Override
    protected void registerGoals() {
    }

    /** 和平生物：即使被攻击也不还手（没有攻击目标，也没法选中目标）。 */
    @Override
    public boolean canAttack(LivingEntity target) {
        return false;
    }

    /** 竹子插在地里，不能被别的实体推走。 */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** 景观实体：不随玩家远离而消失。 */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** 「孤」竹：同一簇最多 1 株。 */
    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    /**
     * 生成期的「占地」判定一律用 {@link EntityType} 的原始尺寸，不用缩放后的命中箱。
     *
     * <h3>为什么必须这么写（崩服级别的坑）</h3>
     * 原版 {@code NaturalSpawner#spawnMobsForChunkGeneration} 把候选坐标钳在
     * {@code [chunkMin + typeWidth, chunkMax - typeWidth]}，这个钳制用的是
     * {@link EntityType#getWidth()}（注册尺寸），<b>不是</b>实体实际缩放后的宽度；
     * 但紧接着的 {@code Mob#checkSpawnObstruction} 却拿 {@code getBoundingBox()}
     * （= 缩放后的命中箱）去 {@code containsAnyLiquid}。正常生物两者一致，永远贴着区块内 ——
     * 孤竹最大 5 倍体型，半宽 1.5 格 > 注册半宽 0.3 格，
     * 于是会在区块边界外读方块。
     *
     * <p>而 {@code ChunkStatus.SPAWN} 那一步的 {@code WorldGenRegion} 半径是 0
     * （{@code ChunkStatus#SPAWN} 传的 {@code -1}）——<b>只有中心那一个区块</b>，
     * 一旦读到隔壁区块就直接</p>
     * <pre>RuntimeException: We are asking a region for a chunk out of bound | 15 -61</pre>
     * <p>崩服。这也解释了「指令生成没事、自然生成才炸」：指令走的是已加载区块，
     * 只有区块生成阶段才有这个 1 区块的窄区域。</p>
     *
     * <p>用原始尺寸做判定后，数学上与原版完全同构：钳制下限 {@code minX + 0.6}、
     * 半宽 0.3 → 包围盒始终落在本区块内。判「能不能站」只关心根部占地，
     * 视觉放大多少不影响这一点；运行时命中箱仍是 5 倍的（见 {@link #getScale()}）。</p>
     */
    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        AABB footprint = this.getType().getDimensions().makeBoundingBox(this.position());
        return !level.containsAnyLiquid(footprint)
                && level.isUnobstructed(this, Shapes.create(footprint));
    }

    /**
     * 生成位置校验。
     *
     * <p>注意 {@code level} 传进来的 pos 是高度图
     * （{@code MOTION_BLOCKING_NO_LEAVES}）给出的地表坐标，所以这里不需要
     * 自己找地面；但还是要确认脚下是能长植物的方块、头顶有两格空间。</p>
     */
    public static boolean checkLoneBambooSpawnRules(EntityType<LoneBambooEntity> type,
                                                    ServerLevelAccessor level,
                                                    MobSpawnType spawnType,
                                                    BlockPos pos,
                                                    RandomSource random) {
        // 脚下必须是「动物可生成」的方块（草方块/泥土等；沙子不在其中，沙漠天然被排除）
        if (!level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
            return false;
        }
        // 两格净空，别把竹竿塞进方块里
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
            return false;
        }
        // 沙漠 / 海洋 / 沙滩 / 河流：标签内容见 ModBiomeTagsProvider。
        // 只在「自然生成 / 区块生成」时过滤 —— 刷怪蛋、指令、刷怪笼是玩家主动行为，
        // 强行拦掉只会让人以为实体没注册成功。
        if (!isManualSpawn(spawnType) && level.getBiome(pos).is(ModBiomeTags.LONE_BAMBOO_BLOCKED)) {
            return false;
        }
        // 不做亮度判定 → 昼夜都能生成
        return true;
    }

    /** 玩家主动生成的几种方式：不受群系黑名单限制。 */
    private static boolean isManualSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.SPAWNER;
    }
}
