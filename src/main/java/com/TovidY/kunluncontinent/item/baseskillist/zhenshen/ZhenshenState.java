package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * 武魂真身的开关状态机 —— 唯一真相源。
 *
 * <h3>模型</h3>
 * <p>武魂真身不是一个「给 800 tick 增益」的一次性技能，而是一个<b>开关</b>：</p>
 * <ul>
 *   <li>释放 → 开启，持续获得全属性增幅；</li>
 *   <li>开启期间再释放 → 关闭；</li>
 *   <li>精神力跌破最大值的 {@link #AUTO_CLOSE_RATIO} → 自动关闭；</li>
 *   <li>武魂被关闭 / 玩家换了武魂 → 自动关闭（否则会白扣费却拿不到对应武魂的增幅）。</li>
 * </ul>
 * <p>开启期间的维护费 = <b>{@link #UPKEEP_MULTIPLIER} 倍</b>普通武魂维持消耗，
 * 由 {@code PWPlayerTickEvent#updatePlayerFly} 每 20 tick 结算一次
 * （见那里的 {@code ZhenshenState.upkeepMultiplier}）。</p>
 *
 * <h3>为什么状态存在玩家 persistentData 里</h3>
 * <p>增益本身是 {@code MobEffect}，客户端能从效果图标看到状态，
 * 所以不需要额外同步；{@code Entity#getPersistentData()} 会随玩家存档、
 * 也会在重生时沿用，正好够用。真身是纯服务端机制，客户端不需要知道细节。</p>
 */
public final class ZhenshenState {

    /** 开启期间的维护费倍率（相对普通武魂维持消耗）。 */
    public static final float UPKEEP_MULTIPLIER = 20.0f;

    /** 精神力低于「最大值 × 该比例」时自动关闭。 */
    public static final float AUTO_CLOSE_RATIO = 0.10f;

    /** 每次刷新给的增益时长（tick）。比 {@code PWPlayerTickEvent} 的 20 tick 结算周期长，保证不断档。 */
    private static final int EFFECT_DURATION = 100;

    private static final String NBT_ACTIVE = "KL_ZhenshenActive";
    private static final String NBT_EFFECT = "KL_ZhenshenEffect";
    private static final String NBT_WUHUN = "KL_ZhenshenWuhun";

    private ZhenshenState() {
    }

    public static boolean isActive(Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    /**
     * 武魂维持费的倍率：开启真身时 {@link #UPKEEP_MULTIPLIER}，否则 1。
     * <p>这是「真身贵 10 倍」的唯一落地点，别在别处再乘一次。</p>
     */
    public static float upkeepMultiplier(Player player) {
        return isActive(player) ? UPKEEP_MULTIPLIER : 1.0f;
    }

    /** 开启门槛：精神力必须高于自动关闭线，否则会开完立刻自动关闭。 */
    public static boolean canActivate(Player player, PlayerAttributeCapability cap) {
        float max = ModAttributeAPI.getMaxjingshenli(player);
        return max <= 0.0f || cap.getJingshenli() > max * AUTO_CLOSE_RATIO;
    }

    /**
     * 开启。调用方负责校验 {@link #canActivate} 与武魂是否已开启。
     *
     * @param wuhun 开启时玩家身上的武魂名，用来在换武魂时自动关闭真身
     */
    public static void activate(ServerPlayer player, MobEffect effect, String wuhun) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(NBT_ACTIVE, true);
        data.putString(NBT_EFFECT, String.valueOf(ForgeRegistries.MOB_EFFECTS.getKey(effect)));
        data.putString(NBT_WUHUN, wuhun);

        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, 0, false, false, true));
        SynsAPI.synsPlayerAttribute(player);
    }

    /** 手动/被动关闭：移除增益并清状态。 */
    public static void deactivate(ServerPlayer player) {
        MobEffect effect = resolveEffect(player);
        if (effect != null) {
            player.removeEffect(effect);
        }
        clear(player);
        SynsAPI.synsPlayerAttribute(player);
    }

    /**
     * 服务端每 20 tick 调一次（与武魂维持扣费同频，判断的是扣费后的精神力）。
     * 负责：刷新增益时长 + 检查所有自动关闭条件。
     */
    public static void tickServer(ServerPlayer player, PlayerAttributeCapability cap) {
        if (!isActive(player)) return;

        MobEffect effect = resolveEffect(player);
        if (effect == null) {
            // 效果 ID 解析不出来（注册表变动/存档残留），静默清理
            clear(player);
            return;
        }

        // 1. 武魂被关掉
        String current = cap.getWuhunName();
        if (current == null) {
            closeWithMessage(player, effect, "§7武魂已关闭，武魂真身随之消散");
            return;
        }

        // 2. 中途换了武魂：真身属于原武魂，继续留着会白扣费
        String origin = player.getPersistentData().getString(NBT_WUHUN);
        if (!current.equals(origin)) {
            closeWithMessage(player, effect, "§7已更换武魂，武魂真身随之消散");
            return;
        }

        // 3. 精神力跌破下限
        float max = ModAttributeAPI.getMaxjingshenli(player);
        if (max > 0.0f && cap.getJingshenli() < max * AUTO_CLOSE_RATIO) {
            closeWithMessage(player, effect, "§c精神力不足，武魂真身自动解除");
            return;
        }

        // 4. 还活着 → 续上增益
        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, 0, false, false, true));
    }

    // ==================================================================================

    private static void closeWithMessage(ServerPlayer player, MobEffect effect, String message) {
        player.removeEffect(effect);
        clear(player);
        player.displayClientMessage(Component.literal(message), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.7f, 1.4f);
        SynsAPI.synsPlayerAttribute(player);
    }

    private static void clear(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(NBT_ACTIVE);
        data.remove(NBT_EFFECT);
        data.remove(NBT_WUHUN);
    }

    @Nullable
    private static MobEffect resolveEffect(Player player) {
        String id = player.getPersistentData().getString(NBT_EFFECT);
        if (id.isEmpty()) return null;
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));
    }
}
