package com.TovidY.kunluncontinent.capability.playerattributes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

/**
 * 飞行撞墙的<b>专属伤害源</b>。
 *
 * <p>为什么不用 {@code playerAttack}：那个伤害源会套用原版/本项目的击杀归属逻辑，
 * 死亡消息由 {@code CombatTracker} 拼装，玩家看到的是"某某被某某杀死了"，
 * 没法给出"撞墙"这种自嘲文案。</p>
 *
 * <p>这里的做法是注册 <b>4 个自定义伤害类型</b>（数据驱动 json），
 * 每个 {@code message_id} 对应一句死亡消息，命中时随机挑一个 ——
 * 于是<b>原版死亡界面</b>和击杀播报都会用我们定的文案。</p>
 *
 * <p>伤害类型 json 位置：{@code data/kunluncontinent/damage_type/fly_crash_1..4.json}；
 * 语言键：{@code death.attack.kunluncontinent.fly_crash_N}。</p>
 */
public final class FlyCrashDamageSource {

    private FlyCrashDamageSource() {
    }

    private static final Random RANDOM = new Random();

    /** 4 个撞墙伤害类型，随机取用。 */
    private static final String[] IDS = {
            "fly_crash_1",
            "fly_crash_2",
            "fly_crash_3",
            "fly_crash_4"
    };

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation("kunluncontinent", id));
    }

    /**
     * 构造一次撞墙伤害源（随机 4 选 1）。
     *
     * <p><b>不传 causingEntity / directEntity</b> —— 这样
     * {@code DamageSource#getLocalizedDeathMessage} 会走"无归属"分支，
     * 用 {@code death.attack.<msgId>} 单参数格式，文案完全由我们控制，
     * 也不会被算成"自己杀自己"。</p>
     */
    public static DamageSource create(ServerLevel level) {
        String id = IDS[RANDOM.nextInt(IDS.length)];
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key(id)));
    }

    /** 便捷方法：直接对玩家造成一次撞墙反伤。 */
    public static void hurtPlayer(ServerLevel level, Player player, float damage) {
        player.hurt(create(level), damage);
    }
}
