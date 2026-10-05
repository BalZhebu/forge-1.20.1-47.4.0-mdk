package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.entity.demon.DemonWhaleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;

/**
 * 海神神位被动 ——「海神之威」。
 *
 * <p>所有海洋生物<b>永不对海神为敌</b>：不主动攻击、也不反击、不被仇恨锁定。</p>
 *
 * <p><b>魔鲸除外</b>（{@link DemonWhaleEntity} 不受影响，照常攻击）。</p>
 *
 * <p>⭐ <b>实现方式：每 tick 主动清目标，而不是靠事件</b>。
 * 之前用 {@code LivingChangeTargetEvent} 实测<b>无效</b>（溺尸照样攻击），原因是：</p>
 * <ul>
 *   <li>该事件只在 AI <b>决定换目标</b>的那一刻触发；生物一旦锁定玩家就不再"换"，
 *       事件根本不会再次触发；</li>
 *   <li>它也只能否决"这一次赋值"，无法清掉<b>已存在</b>的目标。</li>
 * </ul>
 * 所以改为每 tick（END 阶段）检查「目标 / 仇恨是不是海神」，是就清掉。
 * 效果：生物每个 tick 都会被洗掉对海神的敌意 → <b>永远打不到他</b>。
 * 开销：只在<b>该维度存在海神玩家</b>时才扫描玩家周围，不是全维度遍历。</p>
 */
public class SeaGodBuff extends GodBuff {

    public static final SeaGodBuff INSTANCE = new SeaGodBuff();

    /** 检查半径（格）。32 足够覆盖玩家的常规敌对索敌范围。 */
    private static final int CHECK_RADIUS = 32;

    public SeaGodBuff() {
        super("sea_god", ChatFormatting.AQUA, "海神之威",
                "一切海洋生物永不与之为敌（魔鲸除外）");
    }

    /** 魔鲸豁免—— Boss 级生物不受海神威压。 */
    @Override
    public boolean isImmune(LivingEntity target) {
        return target instanceof DemonWhaleEntity;
    }

    @Override
    public void onMobTick(LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel slevel)) return;

        // ⭐ 本维度没有海神玩家 → 直接返回，零开销
        ServerPlayer seaGod = null;
        for (Player p : slevel.players()) {
            if (p instanceof ServerPlayer sp && GodBuffs.hasGod(sp, godId())) {
                seaGod = sp;
                break;
            }
        }
        if (seaGod == null) return;

        var box = seaGod.getBoundingBox().inflate(CHECK_RADIUS);
        // ⚠️ 用 LivingEntity.class 而非 Mob.class —— 后者会把T 推断成 Mob，
        //    于是 `instanceof Player` 在编译期就报错（Mob 与 Player 是兄弟类）。
        for (LivingEntity entity : slevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity instanceof Player) continue;   // 不影响玩家
            if (!(entity instanceof Mob mob)) continue;
            if (isImmune(mob)) continue;              // 魔鲸豁免
            if (!shouldAffect(mob)) continue;         // 只管海洋生物

            // ① 攻击目标是海神 → 清掉（AI 下一 tick 会重新采样，找不到就当没这人）
            if (mob.getTarget() == seaGod) {
                mob.setTarget(null);
            }
            // ② 最近被攻击者是海神 → 清掉，否則它会凭 3 秒仇恨记忆立刻重新锁定
            if (mob.getLastHurtByMob() == seaGod) {
                mob.setLastHurtByMob(null);
            }
        }
    }

    /**
     * 这只生物是否受海神威压。
     *
     * <p>判定：{@code MobType.WATER} 或名字含海洋关键词。
     * 溺尸（{@code DROWNED}）的 {@code MobType} 就是 {@code WATER} → 会被正确覆盖。</p>
     */
    private static boolean shouldAffect(Mob mob) {
        return mob.getMobType() == net.minecraft.world.entity.MobType.WATER
                || isSeaNamed(mob);
    }

    private static boolean isSeaNamed(Mob mob) {
        String name = mob.getName().getString().toLowerCase(java.util.Locale.ROOT);
        return name.contains("fish") || name.contains("ocean") || name.contains("sea")
                || name.contains("whale") || name.contains("shark") || name.contains("dolphin")
                || name.contains("squid") || name.contains("cod") || name.contains("salmon")
                || name.contains("drowned") || name.contains("guardian")
                || name.contains("kraken") || name.contains("leviathan");
    }
}
