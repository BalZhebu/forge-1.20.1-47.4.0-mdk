package com.TovidY.kunluncontinent.godclass.buff;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 修罗神神位被动 ——「修罗威慑」。
 *
 * <p>所有生物<b>不敢靠近你，但仍对你有敌意</b> —— 行为等同"怕猫的生物 / 苦力怕"：
 * 玩家在附近时生物会主动<b>逃离</b>，但玩家主动攻击它，它照样还手。</p>
 *
 * <p><b>前提：年限 ≥ 500000 年的生物不受影响。</b></p>
 *
 * <p>实现拆两半，缺一不可：</p>
 * <ol>
 *   <li><b>「不敢靠近」</b>：给生物挂一个临时 {@link AvoidEntityGoal} 指向该玩家。
 *       这就是原版"怕猫的生物 / 苦力怕"用的同一套机制 —— 目标进范围就产生远离的移动力。</li>
 *   <li><b>「仍有敌意」</b>：<b>不</b>干预它的索敌。AI 采样到玩家时照常锁定，
 *       所以玩家打它就会被反击（AvoidEntityGoal 只影响移动方向，不影响索敌）。</li>
 * </ol>
 *
 * <p>⛔ <b>绝不能在这里清空攻击目标</b>（不要用 {@code LivingChangeTargetEvent}）——
 * 那会把"敌意"也一起消掉，就变成"打不动"了，与需求完全相反。</p>
 */
public class AsuraGodBuff extends GodBuff {

    public static final AsuraGodBuff INSTANCE = new AsuraGodBuff();

    /** ⭐ 年限门槛：≥ 50 万年的生物免疫。 */
    public static final long IMMUNE_NIANXIAN = 500_000L;

    /** 威慑半径（格）。生物在这个半径外就开始远离。 */
    public static final int FEAR_RADIUS = 16;

    /** 规避速度系数，越大跑得越快。 */
    private static final double FEAR_SPEED = 1.2D;

    /** 已挂 Goal 的 "生物UUID|玩家UUID"，防止重复挂。 */
    private static final Set<String> TAGGED = new HashSet<>();

    public AsuraGodBuff() {
        super("asura_god", ChatFormatting.DARK_RED, "修罗威慑",
                "众生不敢近身（年限≥ " + IMMUNE_NIANXIAN + " 年者免疫），但仍怀敌意");
    }

    /** 年限免疫判定。 */
    public static boolean isImmuneByNianxian(LivingEntity entity) {
        return GodBuffs.nianxianOf(entity) >= IMMUNE_NIANXIAN;
    }

    @Override
    public void onMobTick(LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel slevel)) return;

        // ⭐ 只在本维度存在修罗神玩家时才做这件事，空维度零开销
        ServerPlayer asura = null;
        for (Player p : slevel.players()) {
            if (p instanceof ServerPlayer sp && GodBuffs.hasGod(sp, godId())) {
                asura = sp;
                break;
            }
        }
        if (asura == null) return;

        // 只扫玩家周围 FEAR_RADIUS + 一点余量，不做全维度遍历。
        // ⚠️ 用 LivingEntity.class 而不是 Mob.class —— 后者会把 T 推断成 Mob，
        //    于是 `mob instanceof Player` 在编译期就被判定为"不可能"（Mob 和 Player 是兄弟类）。
        var box = asura.getBoundingBox().inflate(FEAR_RADIUS + 8.0D);
        for (LivingEntity entity : slevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity instanceof Player) continue;         // 不影响玩家
            // AvoidEntityGoal 只接受 PathfinderMob（要能寻路才能"绕开"）
            if (!(entity instanceof net.minecraft.world.entity.PathfinderMob pathMob)) continue;
            if (isImmuneByNianxian(entity)) continue;       // 50 万年以上的免疫
            applyFearGoal(pathMob, asura);
        }
    }

    /** 挂上远离 Goal（同一对只挂一次）。 */
    private static void applyFearGoal(net.minecraft.world.entity.PathfinderMob mob, Player god) {
        String key = mob.getUUID() + "|" + god.getUUID();
        if (!TAGGED.add(key)) return;   // 之前已挂

        // ⚠️ AvoidEntityGoal 的第 2 参是「要躲的实体**类**」不是实体实例 ——
        //    它内部每 tick 用 "范围内有没有这个类" 判断要不要逃。
        //    传 Player.class 意味着「远离所有玩家」；若要只躲特定玩家
        //    得换成 (e) -> e.getUUID().equals(god.getUUID()) 的重载。
        mob.goalSelector.addGoal(0,
                new AvoidEntityGoal(mob, Player.class, FEAR_RADIUS, 1.0D, FEAR_SPEED));
    }
}
