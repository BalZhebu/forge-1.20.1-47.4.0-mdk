package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家死亡复活的属性处理。
 *
 * <h3>为什么血量/精神力写在 {@code PlayerRespawnEvent} 而不是 {@code Clone} 里</h3>
 * <p>{@code PlayerEvent.Clone} 是在 {@code ServerPlayer#restoreFrom} 内部、由
 * {@code ForgeEventFactory.onPlayerClone} 触发的，而这个项目里<b>有两个</b> Clone 监听器
 * （本类 + {@code PlayerUpgradeEventListener}）都在 {@code deserializeNBT} 搬运数据，
 * 谁先谁后没有保证。后跑的那个会把先跑的那个刚设好的数值再覆盖一遍。</p>
 *
 * <p>{@code PlayerRespawnEvent} 在 {@code PlayerList#respawn} 的<b>最后一步</b>触发
 * （在 {@code serverplayer.setHealth(getHealth())} 之后），此时：
 * <ul>
 *   <li>两个 Clone 监听器都已经跑完，capability 是最终数据；</li>
 *   <li>之后原版不会再动血量，设多少就是多少。</li>
 * </ul>
 * 所以「重生送血」放在这里才稳定。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneEventListener {

    /** 重生后恢复的生命百分比（按<b>实际最大生命</b>算，含加点 / 武魂 / 魂骨 / 永久加成）。 */
    public static final float RESPAWN_HEALTH_RATIO = 0.7f;

    /**
     * 「这次重生是死亡导致的」标记。
     *
     * <p>Clone 里打上，Respawn 里消费掉。用来把死亡重生和
     * 「末地返回 / 跨维度重生」（同样是 Clone + Respawn，但不是死亡）区分开 ——
     * 后者不该被重置血量。</p>
     */
    private static final Set<UUID> PENDING_DEATH_RESPAWN = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        var oldPlayer = event.getOriginal();
        var newPlayer = event.getEntity();
        oldPlayer.reviveCaps();
        oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oldAttrs -> {
            newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
                newAttrs.deserializeNBT(oldAttrs.serializeNBT());
                if (event.isWasDeath() && newPlayer instanceof ServerPlayer serverPlayer) {
                    // 标记「待会儿 Respawn 时要送血」
                    PENDING_DEATH_RESPAWN.add(serverPlayer.getUUID());

                    // 只先把「血量上限」搬过去，避免重生瞬间 HUD 还是原版 20 点上限。
                    // 真正的血量数值留给 onPlayerRespawn 统一设置。
                    float maxHp = ModAttributeAPI.getMaxshengming(serverPlayer);
                    if (maxHp > 0) {
                        PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, maxHp);
                    }

                    CompoundTag nbtData = newAttrs.serializeNBT();
                    var packet = new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(
                            serverPlayer.getId(), nbtData);
                    com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            packet
                    );
                    // 重生后把神考数据也补发一份。
                    // 之前只发属性包，导致客户端 GodClientData 停在"死亡前"的旧值 ——
                    // 而它是静态字段，收过错的就不会自己纠正。
                    com.TovidY.kunluncontinent.network.NetworkHandler.sendToClient(
                            new com.TovidY.kunluncontinent.network.client.PacketSyncGodData(newAttrs),
                            serverPlayer);
                }
            });
        });
        oldPlayer.invalidateCaps();
    }

    /**
     * 死亡重生落地后：生命回 70%、精神力回满。
     *
     * <p>非死亡触发的重生（末地返回、跨维度）不在 {@link #PENDING_DEATH_RESPAWN} 里，直接跳过。</p>
     */    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!PENDING_DEATH_RESPAWN.remove(player.getUUID())) {
            return;
        }

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // ---- ① 生命：最大生命的 70%（上限取「含加点/武魂/魂骨」的真实值）----
            float maxHp = ModAttributeAPI.getMaxshengming(player);
            if (maxHp <= 0) {
                maxHp = player.getMaxHealth();
            }
            float hp = Math.max(1.0f, maxHp * RESPAWN_HEALTH_RATIO);

            // 先把 MAX_HEALTH 拉到真实上限，否则 setHealth 会被旧上限（20）夹住
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(maxHp);
            }
            cap.setShengming(hp);
            player.setHealth(hp);

            // ---- ② 精神力：直接回满 ----
            float maxMp = ModAttributeAPI.getMaxjingshenli(player);
            cap.setJingshenli(maxMp);

            // ---- ③ 同步给客户端（HUD / 面板 / 他人视角）----
            SynsAPI.synsPlayerAttribute(player);
        });
    }

    /**
     * 登录时清掉遗留标记。
     *
     * <p>玩家死了但没重生就退服（直接关客户端），标记会留在内存里；
     * 若不清，下次他在末地返回这种「非死亡重生」时会被误判成死亡重生而被重置血量。</p>
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PENDING_DEATH_RESPAWN.remove(serverPlayer.getUUID());
        }
    }
}
