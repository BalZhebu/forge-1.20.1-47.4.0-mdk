package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 「增益开关」状态机 —— 与武魂真身（{@link ZhenshenState}）同一套思路，但<b>支持多个并存</b>。
 *
 * <h3>和真身的区别</h3>
 * <ul>
 *   <li>真身：一个玩家同时只能开一个（NBT 单键）；</li>
 *   <li>增益开关：一个玩家可以同时挂好几个（石肤 + 搬山 + 石铠…），各自记录
 *       自己的增益效果 / 等级 / 所属武魂 / 维持费增量。</li>
 * </ul>
 *
 * <h3>自动关闭条件（每 20 tick 检查一次，由 {@code PWPlayerTickEvent} 调用）</h3>
 * <ul>
 *   <li>武魂被关闭 → 该条目关闭；</li>
 *   <li><b>切换武魂</b> → 条目所属武魂 ≠ 当前武魂，全部自动关闭；</li>
 *   <li>精神力跌破最大值 × {@link #AUTO_CLOSE_RATIO} → 全部关闭。</li>
 * </ul>
 *
 * <h3>维持费</h3>
 * <p>每个条目有自己的增量（石肤 8 倍、啸月 12 倍…），总增量 =
 * {@code 1 + Σ(各条目增量 - 1)}，与真身的 20 倍维持费<b>相乘</b>叠加。</p>
 *
 * <p>状态存在玩家 persistentData 的 ListTag 里，随存档与重生沿用；纯服务端机制。</p>
 */
public final class BuffToggleState {

    public static final float AUTO_CLOSE_RATIO = 0.10f;
    /** 每次刷新给的增益时长（tick），比 20 tick 的结算周期长，保证不断档。 */
    private static final int EFFECT_DURATION = 100;

    private static final String NBT_LIST = "KL_BuffToggles";

    /**
     * 动态效果解析器：有的增益随环境变化（啸月：白天大日 / 夜晚邀月），
     * 每次刷新时回调它拿"当前该给什么效果"。
     */
    private static final Map<String, Function<ServerPlayer, MobEffect>> RESOLVERS = new HashMap<>();

    private BuffToggleState() {
    }

    /** 注册某个开关 id 的动态效果解析器（啸月用；静态增益不用注册）。 */
    public static void registerResolver(String toggleId, Function<ServerPlayer, MobEffect> resolver) {
        RESOLVERS.put(toggleId, resolver);
    }

    // ==================================================================================
    //  查询
    // ==================================================================================

    public static boolean isActive(Player player, String toggleId) {
        for (CompoundTag tag : entries(player)) {
            if (tag.getString("Id").equals(toggleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 开启中的增益带来的<b>维持费总倍率</b>（与真身的维持倍率相乘叠加）。
     * <p>每条目贡献 {@code (mult - 1)}，无条目时 = 1。</p>
     */
    public static float upkeepMultiplier(Player player) {
        float total = 1.0F;
        for (CompoundTag tag : entries(player)) {
            total += Math.max(0.0F, tag.getFloat("Mult") - 1.0F);
        }
        return total;
    }

    public static boolean canActivate(Player player, PlayerAttributeCapability cap) {
        float max = ModAttributeAPI.getMaxjingshenli(player);
        return max <= 0.0F || cap.getJingshenli() > max * AUTO_CLOSE_RATIO;
    }

    // ==================================================================================
    //  开关
    // ==================================================================================

    /** 开启（调用方负责 {@link #canActivate} 校验与提示）。 */
    public static void activate(ServerPlayer player, String toggleId, MobEffect effect,
                                int amplifier, float upkeepMultiplier, String wuhun) {
        // 同一个开关不允许重复挂
        deactivate(player, toggleId);

        CompoundTag data = player.getPersistentData();
        ListTag list = data.getList(NBT_LIST, Tag.TAG_COMPOUND);
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", toggleId);
        tag.putString("Effect", String.valueOf(ForgeRegistries.MOB_EFFECTS.getKey(effect)));
        tag.putInt("Amp", amplifier);
        tag.putFloat("Mult", Math.max(1.0F, upkeepMultiplier));
        tag.putString("Wuhun", wuhun);
        list.add(tag);
        data.put(NBT_LIST, list);

        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amplifier, false, false, true));
        SynsAPI.synsPlayerAttribute(player);
    }

    /** 关闭某一条（移除对应增益并清掉条目）。 */
    public static void deactivate(ServerPlayer player, String toggleId) {
        CompoundTag data = player.getPersistentData();
        ListTag list = data.getList(NBT_LIST, Tag.TAG_COMPOUND);
        ListTag kept = new ListTag();
        for (Tag t : list) {
            CompoundTag tag = (CompoundTag) t;
            if (tag.getString("Id").equals(toggleId)) {
                removeEffect(player, tag.getString("Effect"));
            } else {
                kept.add(tag);
            }
        }
        data.put(NBT_LIST, kept);
        SynsAPI.synsPlayerAttribute(player);
    }

    /** 关闭全部（切武魂 / 精神力见底时）。 */
    public static void deactivateAll(ServerPlayer player, @Nullable String message) {
        ListTag list = data(player).getList(NBT_LIST, Tag.TAG_COMPOUND);
        for (Tag t : list) {
            removeEffect(player, ((CompoundTag) t).getString("Effect"));
        }
        data(player).remove(NBT_LIST);
        if (message != null && !message.isEmpty()) {
            player.displayClientMessage(Component.literal(message), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
        SynsAPI.synsPlayerAttribute(player);
    }

    // ==================================================================================
    //  每 20 tick 的维持结算（由 PWPlayerTickEvent 调用）
    // ==================================================================================

    public static void tickServer(ServerPlayer player, PlayerAttributeCapability cap) {
        List<CompoundTag> entries = entries(player);
        if (entries.isEmpty()) {
            return;
        }
        String current = cap.getWuhunName();
        List<CompoundTag> keep = new ArrayList<>();
        boolean changed = false;

        for (CompoundTag tag : entries) {
            // 1. 武魂被关闭 / 中途换了武魂 → 全部自动关闭
            if (current == null || !current.equals(tag.getString("Wuhun"))) {
                removeEffect(player, tag.getString("Effect"));
                changed = true;
                continue;
            }

            MobEffect effect = resolve(tag, player);
            if (effect == null) {
                changed = true;   // 效果 ID 解析不出来（注册表变动/存档残留），静默清理
                continue;
            }
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, tag.getInt("Amp"), false, false, true));
            keep.add(tag);
        }

        // 2. 精神力跌破下限 → 剩下的也全部关闭
        float max = ModAttributeAPI.getMaxjingshenli(player);
        if (!keep.isEmpty() && max > 0.0F && cap.getJingshenli() < max * AUTO_CLOSE_RATIO) {
            for (CompoundTag tag : keep) {
                removeEffect(player, tag.getString("Effect"));
            }
            keep.clear();
            player.displayClientMessage(Component.literal("§c精神力不足，所有增益开关自动解除"), true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.7f, 1.4f);
            changed = true;
        }

        CompoundTag data = data(player);
        if (changed) {
            ListTag list = new ListTag();
            for (CompoundTag tag : keep) {
                list.add(tag);
            }
            data.put(NBT_LIST, list);
        } else if (entries.size() != keep.size()) {
            // resolver 每次刷新可能换效果（啸月昼夜切换），把最新效果 id 写回去
            ListTag list = new ListTag();
            for (CompoundTag tag : keep) {
                MobEffect effect = resolve(tag, player);
                if (effect != null) {
                    tag.putString("Effect", String.valueOf(ForgeRegistries.MOB_EFFECTS.getKey(effect)));
                }
                list.add(tag);
            }
            data.put(NBT_LIST, list);
        }
        SynsAPI.synsPlayerAttribute(player);
    }

    // ==================================================================================
    //  内部
    // ==================================================================================

    private static CompoundTag data(Player player) {
        return player.getPersistentData();
    }

    private static List<CompoundTag> entries(Player player) {
        List<CompoundTag> out = new ArrayList<>();
        for (Tag t : data(player).getList(NBT_LIST, Tag.TAG_COMPOUND)) {
            out.add((CompoundTag) t);
        }
        return out;
    }

    private static void removeEffect(Player player, String effectId) {
        if (effectId == null || effectId.isEmpty()) {
            return;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
        if (effect != null) {
            player.removeEffect(effect);
        }
    }

    @Nullable
    private static MobEffect resolve(CompoundTag tag, ServerPlayer player) {
        Function<ServerPlayer, MobEffect> resolver = RESOLVERS.get(tag.getString("Id"));
        if (resolver != null) {
            return resolver.apply(player);
        }
        String id = tag.getString("Effect");
        if (id.isEmpty()) {
            return null;
        }
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));
    }
}
