package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UnderwaterAltarTile extends BlockEntity {
    private int summonTick = 0;
    private boolean isSummoning = false;

    public UnderwaterAltarTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UNDERWATER_ALTAR_TILE.get(), pos, state);
    }

    public void startSummon() {
        this.isSummoning = true;
        this.summonTick = 200; // 10秒 = 200 ticks
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, UnderwaterAltarTile tile) {
        if (!tile.isSummoning) return;
        tile.summonTick--;

        if (!level.isClientSide) {
            if (tile.summonTick > 50 && tile.summonTick % 25 == 0) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(pos));
                    level.addFreshEntity(bolt);
                }
            }

            if (tile.summonTick <= 0) {
                tile.isSummoning = false;
                DemonWhaleEntity whale = EntityInit.DEMON_WHALE.get().create(level);
                if (whale != null) {
                    whale.moveTo(pos.getX() + 0.5, pos.getY() + 3.0, pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(whale);
                    ((ServerLevel)level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, 20, 0.5, 0.5, 0.5, 0.1);
                }
                tile.setChanged();
            }
        } else {
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SummonTick", this.summonTick);
        tag.putBoolean("IsSummoning", this.isSummoning);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.summonTick = tag.getInt("SummonTick");
        this.isSummoning = tag.getBoolean("IsSummoning");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getSummonTick() { return summonTick; }
    public boolean isSummoning() { return isSummoning; }
}