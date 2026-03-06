package com.TovidY.kunluncontinent.entity.eyetrans;

import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeTransformationEntity extends Entity implements ItemSupplier {
    private double targetX;
    private double targetZ;
    private int life;

    public EyeTransformationEntity(EntityType<? extends EyeTransformationEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
    }

    public EyeTransformationEntity(Level level, double x, double y, double z) {
        this(EntityInit.EYE_TRANSFORMATION_ENTITY.get(), level);
        this.setPos(x, y, z);
    }

    public void signalTo(BlockPos pos) {
        int range = 500;
        this.targetX = pos.getX() + (this.random.nextInt(range * 2 + 1) - range);
        this.targetZ = pos.getZ() + (this.random.nextInt(range * 2 + 1) - range);
        this.life = 0;

        double dx = this.targetX - this.getX();
        double dz = this.targetZ - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        this.setDeltaMovement(dx / dist * 0.4D, 0.2D, dz / dist * 0.4D);
        this.hasImpulse = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            double dx = this.targetX - this.getX();
            double dz = this.targetZ - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist > 1.0D) {
                Vec3 mov = this.getDeltaMovement();
                this.setDeltaMovement(dx / dist * 0.4D, mov.y * 0.9D + 0.05D, dz / dist * 0.4D);
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            this.life++;
            if (this.life > 100) {
                this.discard();
            }
        } else {
            this.setPos(this.getX() + this.getDeltaMovement().x,
                    this.getY() + this.getDeltaMovement().y,
                    this.getZ() + this.getDeltaMovement().z);

            if (this.random.nextFloat() > 0.3f) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.5D, this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override public ItemStack getItem() { return new ItemStack(ModItems.EYE_TRANSFORMATION.get()); }
    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag nbt) {}
    @Override protected void addAdditionalSaveData(CompoundTag nbt) {}
}