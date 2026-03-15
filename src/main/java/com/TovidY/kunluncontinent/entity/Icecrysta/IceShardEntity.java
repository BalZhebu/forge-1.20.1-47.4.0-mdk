package com.TovidY.kunluncontinent.entity.Icecrysta;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.entity.EntityInit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class IceShardEntity extends ThrowableItemProjectile {
    public IceShardEntity(EntityType<? extends IceShardEntity> type, Level level) {
        super(type, level);
    }

    public IceShardEntity(Level level, LivingEntity owner) {
        super(EntityInit.ICE_SHARD.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ICE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity attacker) {
                float damage = ModAttributeAPI.getGongji(attacker);
                if (target.hurt(this.damageSources().thrown(this, attacker), damage)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) this.discard();
    }
}