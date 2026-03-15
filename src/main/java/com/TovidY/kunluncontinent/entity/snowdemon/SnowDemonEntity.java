package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.concurrent.atomic.AtomicReference;

public class SnowDemonEntity extends Monster {

    public SnowDemonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 9.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static boolean checkSnowDemonSpawnRules(EntityType<SnowDemonEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.getBiome(pos).is(ModDimensions.POLAR_ICE_BIOME)) {
            return false;
        }
        int surfaceHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() < surfaceHeight - 2) {
            return false;
        }
        return level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), entityType)
                && level.getRawBrightness(pos, 0) >= 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && this.isAlive()) {
            if (this.getTarget() != null && ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                this.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    if (attr.getNianxian() > 5000) {
                        breakEnvironment();
                    }
                });
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);
        if (this.random.nextFloat() < 0.1F) {
            this.spawnAtLocation(ModItems.RINSEI_INGOT.get());
        }
    }

    private void breakEnvironment() {
        AABB aabb = this.getBoundingBox().inflate(0.1D);
        int yMin = Mth.floor(this.getY() + 0.5D);
        int yMax = Mth.floor(aabb.maxY);
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(aabb.minX), yMin, Mth.floor(aabb.minZ),
                Mth.floor(aabb.maxX), yMax, Mth.floor(aabb.maxZ))) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(this.level(), pos) <= 2.0F) {
                this.level().destroyBlock(pos, true, this);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getVisualScale());
    }

    public float getVisualScale() {
        AtomicReference<Float> s = new AtomicReference<>(1.0F);
        this.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            s.set(1.0F + (float)attr.getNianxian() / 50000.0F);
        });
        return Math.min(s.get(), 4.0F); // 最高 4 倍体型
    }
}
