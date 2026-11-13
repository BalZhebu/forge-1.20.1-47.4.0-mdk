package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.EntityInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 雪魔的远程攻击弹射物 —— <b>冰球</b>。
 *
 * <h3>为什么不用 {@code ThrowableItemProjectile}</h3>
 * <p>那个基类要有一个 {@code Item} 才能渲染（{@code ThrownItemRenderer} 画的是物品图标）。
 * 冰球用的是 Blockbench 的 3D 模型（{@link IceBallModel}），所以直接继承
 * {@link ThrowableProjectile} + 自写 {@link IceBallRenderer}。</p>
 *
 * <h3>手感参数</h3>
 * <ul>
 *   <li>{@link #getGravity()} = 0：直线飞行，好瞄准；</li>
 *   <li>速度由发射端 {@code shoot(...)} 决定（约 1.9 格/tick，比玩家跑得快）；</li>
 *   <li>飞行时每 tick 撒雪花粒子，形成拖尾；</li>
 *   <li>命中造成雪魔攻击力的伤害 + 2 秒缓慢 I；</li>
 *   <li>{@value #MAX_LIFE} tick 后自动消失，避免飞出世界还在跑。</li>
 * </ul>
 */
public class IceBallEntity extends ThrowableProjectile {

    /** 存活上限（5 秒）。 */
    private static final int MAX_LIFE = 100;
    /** 命中附带的缓慢时长（tick）。 */
    private static final int SLOW_TICKS = 40;

    public IceBallEntity(EntityType<? extends IceBallEntity> type, Level level) {
        super(type, level);
    }

    public IceBallEntity(Level level, LivingEntity owner) {
        super(EntityInit.ICE_BALL.get(), owner, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        // 没有需要同步的额外字段（位置/速度由原版 Entity 自己同步）
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            // 雪花拖尾：每 tick 撒 3 个，稍微散开一点形成"尾迹"
            for (int i = 0; i < 3; i++) {
                double dx = (this.random.nextDouble() - 0.5D) * 0.45D;
                double dy = (this.random.nextDouble() - 0.5D) * 0.45D;
                double dz = (this.random.nextDouble() - 0.5D) * 0.45D;
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + dx, this.getY() + dy, this.getZ() + dz,
                        0.0D, 0.0D, 0.0D);
            }
            // 拖尾终点再补一点雪粉，高速时看起来更连贯
            this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        if (this.tickCount > MAX_LIFE) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            return;
        }
        Entity owner = this.getOwner();
        if (result.getEntity() instanceof LivingEntity target && owner instanceof LivingEntity attacker) {
            float damage = (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if (target.hurt(this.damageSources().thrown(this, attacker), damage)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 0));
            }
        }
        this.burst();
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.burst();
            this.discard();
        }
    }

    /** 命中时的碎冰粒子（服务端发，所有客户端都能看到）。 */
    private void burst() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    this.getX(), this.getY(), this.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
