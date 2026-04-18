package com.TovidY.kunluncontinent.entity.demon;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DemonWhaleEntity extends Monster {

    private int skillCooldown = 300;

    private static final EntityDataAccessor<Boolean> IS_CASTING = SynchedEntityData.defineId(DemonWhaleEntity.class, EntityDataSerializers.BOOLEAN);

    private int castTimer = 0; // 蓄力计时器（60 ticks = 3秒）
    private int pendingSkill = -1; // 准备释放的技能编号

    private int waveTicks = 0;

    protected PathNavigation waterNav;
    protected PathNavigation groundNav;

    protected final MoveControl landMoveControl;
    protected final SmoothSwimmingMoveControl aquaticMoveControl;

    public final AnimationState walkAnimationState = new AnimationState();

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.PROGRESS
    )).setDarkenScreen(true);

    private float lastScale = -1.0F;

    public DemonWhaleEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.landMoveControl = new MoveControl(this);
        this.aquaticMoveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CASTING, false);
    }

    public boolean isCasting() {
        return this.entityData.get(IS_CASTING);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.distanceToSqr(target) > this.getMeleeAttackRangeSqr((LivingEntity) target) * 1.5D) {
            return false;
        }

        Vec3 originalMovement = target.getDeltaMovement();
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            target.setDeltaMovement(originalMovement);
            target.hurtMarked = true;
        }
        return hurt;
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity target) {
        float f = this.getBbWidth() * 0.8F;
        return (double)(f * f + target.getBbWidth());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.isAlive() && this.getTarget() != null) {
                if (skillCooldown > 0 && !isCasting()) {
                    skillCooldown--;
                }
                else if (skillCooldown <= 0 && !isCasting()) {
                    startCasting();
                }
            }
            if (isCasting()) {
                castTimer--;
                if (castTimer <= 0) {
                    this.entityData.set(IS_CASTING, false);
                    if (pendingSkill != -1) {
                        triggerSkill(pendingSkill);
                        pendingSkill = -1;
                    }
                    skillCooldown = 300 + this.random.nextInt(301);
                }
            }
            if (waveTicks > 0) {
                handleWaterWave();
                waveTicks--;
            }
        } else {
            if (this.isCasting()) {
                renderCastingParticles();
            }
        }
    }

    private void renderCastingParticles() {
        for (int i = 0; i < 5; i++) {
            double dx = this.getX() + (this.random.nextDouble() - 0.5D) * 10.0D;
            double dy = this.getY() + this.random.nextDouble() * 5.0D;
            double dz = this.getZ() + (this.random.nextDouble() - 0.5D) * 10.0D;
            double vx = (this.getX() - dx) * 0.1D;
            double vy = (this.getY() + 2.0D - dy) * 0.1D;
            double vz = (this.getZ() - dz) * 0.1D;
            this.level().addParticle(ParticleTypes.SOUL, dx, dy, dz, vx, vy, vz);
            this.level().addParticle(ParticleTypes.GLOW, dx, dy, dz, 0, 0, 0);
        }
    }

    private void startCasting() {
        this.castTimer = 60; // 3秒蓄力
        this.pendingSkill = this.random.nextInt(3); // 预选技能
        this.entityData.set(IS_CASTING, true);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 1.0F, 0.5F);
    }

    private void triggerSkill(int type) {
        switch (type) {
            case 0 -> executeThunderstorm();
            case 1 -> { waveTicks = 100; }
            case 2 -> executeLeap();
        }
    }

    // --- 技能 1：雷暴 (Thunderstorm) ---
    private void executeThunderstorm() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(50.0D));
        for (Player player : players) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
            if (lightning != null) {
                lightning.moveTo(player.position());
                this.level().addFreshEntity(lightning);
                player.hurt(this.damageSources().mobAttack(this), 12.0F);
            }
        }
        // 特效：全场蓝色粒子
        ((ServerLevel)this.level()).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 100, 5, 5, 5, 0.5D);
    }

    private void handleWaterWave() {
        if (waveTicks % 10 == 0) { // 每0.5秒扩散一圈
            float radius = (100 - waveTicks) * 0.2F + 2.0F; // 圈越大半径越大
            for (int i = 0; i < 360; i += 5) { // 渲染圆形粒子圈
                double rad = Math.toRadians(i);
                double px = this.getX() + Math.cos(rad) * radius;
                double pz = this.getZ() + Math.sin(rad) * radius;
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.SPLASH, px, this.getY() + 0.5D, pz, 2, 0.1, 0, 0.1, 0.01D);
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.BUBBLE, px, this.getY() + 0.5D, pz, 1, 0.1, 0.1, 0.1, 0.01D);
            }
            List<Player> targets = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(radius));
            for (Player p : targets) {
                p.hurt(this.damageSources().mobAttack(this), 4.0F);
            }
        }
    }

    // --- 技能 3：跃起 (Leap Attack) ---
    private void executeLeap() {

        this.setDeltaMovement(this.getDeltaMovement().add(0, 2.5D, 0));

        this.level().broadcastEntityEvent(this, (byte)60);

        this.level().getServer().tell(new TickTask(this.level().getServer().getTickCount() + 20, () -> {
            if (this.isAlive()) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 10, 3, 0.5, 3, 0.1D);
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(), this.getZ(), 5, 1, 1, 1, 0.1D);
                List<Player> targets = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(20.0D));
                for (Player p : targets) {
                    p.hurt(this.damageSources().mobAttack(this), 15.0F);
                    double dx = p.getX() - this.getX();
                    double dz = p.getZ() - this.getZ();
                    p.knockback(2.0D, -dx, -dz);
                }
            }
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        if (!this.level().isClientSide) {
            float currentScale = getVisualScale();
            if (currentScale != lastScale) {
                this.refreshDimensions();
                this.lastScale = currentScale;
            }
            if (this.isInWater()) {
                if (this.navigation != this.waterNav) this.navigation = this.waterNav;
                if (this.moveControl != this.aquaticMoveControl) this.moveControl = this.aquaticMoveControl;
            } else {
                if (this.navigation != this.groundNav) this.navigation = this.groundNav;
                if (this.moveControl != this.landMoveControl) this.moveControl = this.landMoveControl;
            }
        }
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        float scale = getVisualScale();
        return super.getDimensions(pPose).scale(scale);
    }

    private void setupAnimationStates() {
        if (this.level().isClientSide()) {
            this.walkAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0D, 40));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8D, 60));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isSensitiveToWater() {
        return false; // 对水不敏感
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        this.waterNav = new WaterBoundPathNavigation(this, level);
        this.groundNav = new GroundPathNavigation(this, level);
        return this.groundNav;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true; // 水下呼吸
    }

    @Override
    public void baseTick() {
        int i = this.getAirSupply();
        super.baseTick();
        if (this.isAlive() && !this.isInWater()) {
            this.setAirSupply(300);
        }
        if (!this.level().isClientSide) {
            // 只有当状态真正改变且移动停止时才强制重置导航，减少抽搐
            boolean inWater = this.isInWater();
            if (inWater && this.navigation != this.waterNav) {
                this.navigation.stop(); // 切换前先停下当前路径
                this.navigation = this.waterNav;
                this.moveControl = this.aquaticMoveControl;
            } else if (!inWater && this.navigation != this.groundNav) {
                this.navigation.stop();
                this.navigation = this.groundNav;
                this.moveControl = this.landMoveControl;
            }
        }
    }

    public float getVisualScale() {
        AtomicReference<Float> s = new AtomicReference<>(1.0F);
        this.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            s.set(1.0F + (float)attr.getNianxian() / 50000.0F);
        });
        return Math.min(s.get(), 2.5F);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D).add(0, -0.005D, 0));
        } else {
            super.travel(travelVector);
        }
    }
}
