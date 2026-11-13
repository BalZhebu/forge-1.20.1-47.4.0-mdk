package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleEntity;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldSender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 海底祭坛 —— 深渊领主（魔鲸）的召唤台。
 *
 * <h3>召唤动画（10 秒 / 200 tick）：一座「法阵塔」</h3>
 * <p>祭坛上方自下而上垒起五层平铺法阵，越往上越大、越亮，最后在塔顶再补几枚小阵：</p>
 * <pre>
 *   y+8.8  r=2.0   八边 + 六芒（收口）
 *   y+7.2  r=3.4   十二边 + 三芒 + 爪痕（封印）
 *   y+5.2  r=6.8   24 边 + 12 芒（最大的一层）
 *   y+3.2  r=4.6   正六边形 + 六芒星
 *   y+1.5  r=2.8   正五边形 + 五芒星
 *   ───── 祭坛 ─────
 * </pre>
 * <p>每层错开 15 tick 亮起（0/15/30/45/60），时长按「剩余时间」给，所以整座塔会同时淡出；
 * 最后 80 tick 在塔顶陆续补三枚小阵（120/148/172 tick）。</p>
 *
 * <h3>为什么不再用冲击环 / 爆光 / 光柱</h3>
 * <ul>
 *   <li>冲击环原来每 6~20 tick 就发一个、且时长比扩散时间短 —— 环永远推到一半就过期，
 *       一堆半成品叠在中心，看着就是「卡在原地、越来越亮」。现在只在收尾放三圈
 *       （时长 72~80 tick，足够扩散到头），并且 {@code ShockwaveEffect} 也修了按时长自适应扩散；</li>
 *   <li>爆光（billboard）与漩涡叠在同一位置会互相遮挡、越叠越白，已全部撤掉；</li>
 *   <li>竖直光柱会挡住法阵塔的层次，也撤掉 —— 竖向层次改由「塔」本身表达。</li>
 * </ul>
 * <p>粒子只做点缀：螺旋上涌的气泡、每层法阵上的符文光点、中心幽火、临界火花。</p>
 */
public class UnderwaterAltarTile extends BlockEntity {

    /** 召唤总时长（tick）。10 秒。 */
    public static final int SUMMON_TICKS = 200;

    /** 法阵塔各层：{高度偏移, 半径, 出现时刻}，样式见 {@link #TOWER_STYLES}。 */
    private static final float[][] TOWER = {
            {1.5F, 2.8F, 0.0F},
            {3.2F, 4.6F, 15.0F},
            {5.2F, 6.8F, 30.0F},
            {7.2F, 3.4F, 45.0F},
            {8.8F, 2.0F, 60.0F},
    };

    /** 与 {@link #TOWER} 一一对应：五边 → 六边 → 24 边 → 封印 → 八边。 */
    private static final int[] TOWER_STYLES = {
            SkillEffectStyles.SIGIL_DEEPSEA_PENTA,
            SkillEffectStyles.SIGIL_DEEPSEA_HEXA,
            SkillEffectStyles.SIGIL_DEEPSEA_ALTAR,
            SkillEffectStyles.SIGIL_DEEPSEA_SEAL,
            SkillEffectStyles.SIGIL_DEEPSEA_INNER,
    };

    private int summonTick = 0;
    private boolean isSummoning = false;

    public UnderwaterAltarTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UNDERWATER_ALTAR_TILE.get(), pos, state);
    }

    public void startSummon() {
        this.isSummoning = true;
        this.summonTick = SUMMON_TICKS;
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, UnderwaterAltarTile tile) {
        if (!tile.isSummoning) {
            return;
        }
        // 表现层全部由服务端发包 / 发粒子，客户端不需要自己推进
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int elapsed = SUMMON_TICKS - tile.summonTick;   // 0 → 200
        tile.playSummonFx(serverLevel, pos, elapsed);
        tile.spawnSummonParticles(serverLevel, pos, elapsed);

        tile.summonTick--;

        if (tile.summonTick < 0) {
            tile.isSummoning = false;
            tile.summonTick = 0;
            tile.spawnBoss(serverLevel, pos);
            tile.setChanged();
        }
    }

    // ==================================================================================
    //  着色器：法阵塔
    // ==================================================================================

    private void playSummonFx(ServerLevel level, BlockPos pos, int elapsed) {
        double cx = pos.getX() + 0.5;
        double baseY = pos.getY() + 0.55;   // 祭坛顶面（碰撞箱 8/16 高）
        double cz = pos.getZ() + 0.5;

        // ① 法阵塔：自下而上逐层点亮
        for (int i = 0; i < TOWER.length; i++) {
            if (elapsed != (int) TOWER[i][2]) {
                continue;
            }
            int duration = Math.max(40, SUMMON_TICKS - elapsed);
            SkillFieldSender.broadcast(level, TOWER_STYLES[i],
                    cx, baseY + TOWER[i][0], cz, TOWER[i][1], duration);
        }

        // ② 塔顶补几枚小阵（最后 80 tick 陆续亮起，短时长，一闪一闪）
        if (elapsed == 120) {
            SkillFieldSender.broadcast(level, SkillEffectStyles.SIGIL_DEEPSEA_PENTA,
                    cx, baseY + 9.8, cz, 1.6F, 60);
        } else if (elapsed == 148) {
            SkillFieldSender.broadcast(level, SkillEffectStyles.SIGIL_DEEPSEA_SEAL,
                    cx, baseY + 10.4, cz, 1.2F, 52);
        } else if (elapsed == 172) {
            SkillFieldSender.broadcast(level, SkillEffectStyles.SIGIL_DEEPSEA_HEXA,
                    cx, baseY + 9.2, cz, 1.5F, 28);
        }

        // ③ 塔顶最后一枚小阵亮起后，一道光从天而降接在祭坛上（能量柱）
        if (elapsed == 172) {
            SkillFieldSender.broadcast(level, SkillEffectStyles.BEAM_DEEPSEA_HEAVEN,
                    cx, baseY, cz, 1.0F, 34);
        }

        // ④ 声音：开场一次、临界一次
        if (elapsed == 0) {
            level.playSound(null, cx, baseY, cz, SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.BLOCKS, 0.8F, 0.6F);
        } else if (elapsed == SUMMON_TICKS - 10) {
            level.playSound(null, cx, baseY, cz, SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    // ==================================================================================
    //  粒子点缀
    // ==================================================================================

    private void spawnSummonParticles(ServerLevel level, BlockPos pos, int elapsed) {
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double baseY = pos.getY() + 0.6;

        // ① 螺旋上涌的气泡：半径随塔身张开，高度一路升到塔顶
        double angle = elapsed * 0.35;
        double radius = 1.2 + 4.6 * (elapsed / (double) SUMMON_TICKS);
        for (int i = 0; i < 8; i++) {
            double a = angle + i * (Math.PI / 4.0);
            double x = cx + Math.cos(a) * radius;
            double z = cz + Math.sin(a) * radius;
            double y = baseY + 0.2 + (elapsed * 0.06 + i * 0.4) % 9.0;
            level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 1, 0.0, 0.05, 0.0, 0.01);
        }

        // ② 每层法阵上的符文光点（每 20 tick 一圈）
        if (elapsed % 20 == 0) {
            for (int i = 0; i < 3; i++) {
                glowRing(level, cx, baseY + TOWER[i][0], cz, TOWER[i][1], i);
            }
        }

        // ③ 中心幽火柱（只到塔身中段，不再拉满整根）
        if (elapsed % 10 == 0) {
            for (int i = 0; i < 5; i++) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        cx, baseY + i * 1.2, cz, 1, 0.25, 0.10, 0.25, 0.0);
            }
        }

        // ④ 临界火花
        if (elapsed > SUMMON_TICKS - 30 && elapsed % 3 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    cx, baseY + 2.0, cz, 6, 1.2, 1.6, 1.2, 0.15);
        }
    }

    /** 在某层法阵上撒一圈幽光点（相位按层号错开，看起来像各自在转）。 */
    private static void glowRing(ServerLevel level, double cx, double y, double cz,
                                 double radius, int layer) {
        int count = 12;
        double phase = layer * 0.7;
        for (int i = 0; i < count; i++) {
            double a = phase + i * (Math.PI * 2.0 / count);
            level.sendParticles(ParticleTypes.GLOW,
                    cx + Math.cos(a) * radius, y, cz + Math.sin(a) * radius,
                    1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    // ==================================================================================
    //  召唤产物
    // ==================================================================================

    private void spawnBoss(ServerLevel level, BlockPos pos) {
        DemonWhaleEntity whale = EntityInit.DEMON_WHALE.get().create(level);
        if (whale == null) {
            return;
        }
        whale.moveTo(pos.getX() + 0.5, pos.getY() + 3.0, pos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(whale);

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.6;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, pos.getY() + 1.5, cz,
                12, 1.0, 1.0, 1.0, 0.1);
        // 破水而出：只留气泡与幽火（冲击环这类"平面圆环"已全部撤掉）
        level.sendParticles(ParticleTypes.BUBBLE, cx, pos.getY() + 1.0, cz, 120, 3.5, 2.5, 3.5, 0.25);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, pos.getY() + 1.0, cz, 60, 2.5, 2.0, 2.5, 0.12);

        level.playSound(null, cx, cy, cz, SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.BLOCKS, 1.0F, 0.4F);
    }

    // ==================================================================================
    //  存档 / 同步
    // ==================================================================================

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
