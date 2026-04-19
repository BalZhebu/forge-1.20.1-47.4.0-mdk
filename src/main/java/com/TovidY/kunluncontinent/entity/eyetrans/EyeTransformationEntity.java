package com.TovidY.kunluncontinent.entity.eyetrans;

import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeTransformationEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeTransformationEntity.class, EntityDataSerializers.ITEM_STACK);

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

    public void signalTo(BlockPos pos, ItemStack stack) {
        this.targetX = pos.getX();
        this.targetZ = pos.getZ();
        this.setItem(stack.copy());
        this.life = 0;
    }

    public void setItem(ItemStack stack) {
        this.getEntityData().set(DATA_ITEM_STACK, stack);
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM_STACK, new ItemStack(ModItems.EYE_TRANSFORMATION.get()));
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

            if (this.life < 80) {
                double speed = horizontalDist < 5.0D ? 0.1D : 0.5D;
                double ySpeed = (this.life < 20) ? 0.2D : 0.0D;
                if (horizontalDist > 0.1D) {
                    this.setDeltaMovement(dx / horizontalDist * speed, ySpeed, dz / horizontalDist * speed);
                }
            } else {
                this.setDeltaMovement(0, -0.05, 0);
                if (this.life > 120) this.discard();
            }
        }
        if (this.level().isClientSide) {
            if (getItem().is(ModItems.EYE_DEEP_SEA.get())) {
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX(), this.getY() + 0.3D, this.getZ(), 0, 0.1, 0);
            } else {
                this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.3D, this.getZ(), 0, -0.1, 0);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.targetX = nbt.getDouble("TargetX");
        this.targetZ = nbt.getDouble("TargetZ");
        this.life = nbt.getInt("Life");
        if (nbt.contains("Item", 10)) {
            this.setItem(ItemStack.of(nbt.getCompound("Item")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putDouble("TargetX", this.targetX);
        nbt.putDouble("TargetZ", this.targetZ);
        nbt.putInt("Life", this.life);
        nbt.put("Item", this.getItem().save(new CompoundTag()));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) { return true; }
}