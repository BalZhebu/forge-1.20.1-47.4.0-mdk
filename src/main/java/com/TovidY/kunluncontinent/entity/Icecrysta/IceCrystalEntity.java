package com.TovidY.kunluncontinent.entity.Icecrysta;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class IceCrystalEntity extends Monster implements RangedAttackMob {
    public IceCrystalEntity(EntityType<? extends IceCrystalEntity> type, Level level) {
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
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 20.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && IceCrystalEntity.this.distanceTo(IceCrystalEntity.this.getTarget()) > 4.0D;
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && IceCrystalEntity.this.distanceTo(IceCrystalEntity.this.getTarget()) <= 4.0D;
            }
        });
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = ModAttributeAPI.getGongji(this);
        boolean flag = target.hurt(this.damageSources().mobAttack(this), damage);
        if (flag && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        return flag;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float scale = 1.0F;
        return super.getDimensions(pose).scale(scale);
    }

    public static boolean checkIceCrystalSpawnRules(EntityType<IceCrystalEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getBiome(pos).is(ModDimensions.POLAR_ICE_BIOME)) {
            if (random.nextFloat() > 0.05F) {
                return false;
            }
        }
        return level.canSeeSky(pos);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);
        if (this.random.nextFloat() < 0.1F) {
            this.spawnAtLocation(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT.get());
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        IceShardEntity shard = new IceShardEntity(this.level(), this);
        double d0 = target.getEyeY() - 1.1;
        double d1 = target.getX() - this.getX();
        double d2 = d0 - shard.getY();
        double d3 = target.getZ() - this.getZ();
        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.2;
        shard.shoot(d1, d2 + d4, d3, 1.6F, 1.0F);

        this.playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(shard);
    }
}