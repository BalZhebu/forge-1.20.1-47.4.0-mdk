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
    }

    public EyeTransformationEntity(Level level, double x, double y, double z) {
        this(EntityInit.EYE_TRANSFORMATION_ENTITY.get(), level);
        this.setPos(x, y, z);
    }

    public void signalTo(BlockPos pos) {
        this.targetX = (double)pos.getX();
        this.targetZ = (double)pos.getZ();
        this.life = 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        if (!this.level().isClientSide) {
            double dx = this.targetX - this.getX();
            double dz = this.targetZ - this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            this.life++;
            if (this.life < 60) {
                double speed = horizontalDist < 5.0D ? 0.1D : 0.5D;
                double ySpeed = (this.life < 20) ? 0.2D : 0.0D;
                if (horizontalDist > 0.1D) {
                    this.setDeltaMovement(dx / horizontalDist * speed, ySpeed, dz / horizontalDist * speed);
                } else {
                    this.setDeltaMovement(0, ySpeed, 0);
                }
            }
            else {
                this.setDeltaMovement(0, 0, 0);
                if (this.life > 100) {
                    this.discard();
                }
            }
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.3D, this.getZ(), 0, -0.1, 0);
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