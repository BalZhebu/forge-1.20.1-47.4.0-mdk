package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import com.TovidY.kunluncontinent.potion.ModEffects;

import java.util.List;
import java.util.Map;

/**
 * <b>天使武魂</b>的全部专属规则（唯一的权威落点）。
 *
 * <p>武魂本身只是一条名字（{@link Wuhunname#tianshi}），属性依旧由魂环决定；
 * 特殊的是围绕它的一整套「天命」设定，全部集中在本类，别散到各处去：</p>
 *
 * <ol>
 *   <li><b>独立 10% 觉醒</b> —— {@link #tryIndependentAwaken}。天使不在
 *       {@code wuhunListsnameall} 的平均池子里，每次觉醒先单独掷 {@link #AWAKEN_CHANCE_PERCENT}%；</li>
 *   <li><b>必定天才资质</b> —— 觉醒时 {@code PlayerUpgradeSystem#processAwakening} 把
 *       天赋随机值强制成 {@code 0}（天才区间），于是起始等级 = 1 + 9 = <b>10 级</b>；</li>
 *   <li><b>吸收第一枚魂环再补 10 级（开局限定）</b> —— {@link #onRingAbsorbed}。
 *       这 10 级<b>只给在 0 级觉醒仪式里抽到天使的人</b>（{@code isTianshiStartAwaken}）：
 *       老玩家吃「天使武魂精华」/ 命令拿到天使只多一个武魂，一级都不补；</li>
 *   <li><b>只认天使神</b> —— {@link #isAllowedGod}：天使玩家<b>禁止</b>获取其它任何神位
 *       （随机的「神明的注视」直接跳过）；</li>
 *   <li><b>70 级自动获得天使神神考</b> —— {@link #maybeGrantTrial}，
 *       不需要击杀亡灵去赌那 0.5% 的概率。</li>
 * </ol>
 *
 * <p>本类只处理规则，魂技在 {@code item/baseskillist/tianshi/}（1~9 已齐）。</p>
 */
public final class TianshiWuhun {

    /** 天使神的神位 id（见 {@link GodRegistry}）。 */
    public static final String ANGEL_GOD_ID = "angel_god";

    /**
     * 觉醒天使武魂的独立概率（百分比）。
     *
     * <p>天使<b>不参与</b> {@code PlayerAttributeCapability.wuhunListsnameall} 的平均分配，
     * 而是在每次觉醒时单独掷一次这个概率 —— 掷中就是天使，没掷中才去池子里等概率抽其它武魂。
     * 所以两者互斥：一次觉醒不会既出天使又出别的（双生武魂另算）。</p>
     */
    public static final int AWAKEN_CHANCE_PERCENT = 10;

    /** 吸收第一枚魂环后补发的等级数。 */
    public static final int FIRST_RING_BONUS_LEVELS = 10;

    /** 自动开启天使神神考的等级门槛（含）。 */
    public static final int AUTO_TRIAL_LEVEL = 70;

    // ==================== 对亡灵增伤（武魂开启时） ====================

    /** 天使武魂对亡灵的最低增伤：+30%（0 级）。 */
    public static final float UNDEAD_BONUS_MIN = 0.30F;

    /** 天使武魂对亡灵的满级增伤：+200%（即最终 ×3）。 */
    public static final float UNDEAD_BONUS_MAX = 2.00F;

    /**
     * 增伤达到封顶的等级。
     *
     * <p>取 100（斗罗里 100 级 = 成神）而不是 99：99 级极限斗罗拿 197%，
     * 100 级及以后统一 200% 封顶，避免神位玩家继续往上叠。</p>
     */
    public static final int UNDEAD_BONUS_MAX_LEVEL = 100;


    private TianshiWuhun() {
    }

    // ==================================================================================
    //  判定
    // ==================================================================================

    /** 这个玩家是否拥有天使武魂。 */
    public static boolean has(PlayerAttributeCapability cap) {
        if (cap == null) {
            return false;
        }
        List<String> names = cap.getWuhunListsname();
        return names != null && names.contains(Wuhunname.tianshi);
    }

    /** 玩家所有武魂加起来一共有多少枚魂环。 */
    public static int totalRings(PlayerAttributeCapability cap) {
        if (cap == null) {
            return 0;
        }
        Map<String, List<com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability>> map =
                cap.getMonsterCapabilityLists();
        if (map == null || map.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (List<?> list : map.values()) {
            if (list != null) {
                total += list.size();
            }
        }
        return total;
    }

    // ==================================================================================
    //  战斗：天使武魂开启时，对亡灵生物增伤
    // ==================================================================================

    /**
     * 当前「开启中」的武魂是不是天使。
     *
     * <p>{@link PlayerAttributeCapability#getWuhunName()} 在 {@code hunhuankuaiguan < 0}
     * （玩家把武魂关了）时返回 {@code null}，所以这一个判断同时覆盖
     * 「有没有天使」和「天使是不是正开着」。</p>
     */
    public static boolean isActiveWuhun(PlayerAttributeCapability cap) {
        return cap != null && Wuhunname.tianshi.equals(cap.getWuhunName());
    }

    /**
     * 天使武魂的<b>亡灵增伤倍率</b>（乘在伤害基底上）。
     *
     * <p>0 级 +30%（×1.30）→ {@value #UNDEAD_BONUS_MAX_LEVEL} 级 +200%（×3.00），
     * 中间按等级<b>线性</b>插值，超等级封顶。改数值只动
     * {@link #UNDEAD_BONUS_MIN} / {@link #UNDEAD_BONUS_MAX} / {@link #UNDEAD_BONUS_MAX_LEVEL}。</p>
     *
     * <p>与天使神神位被动（{@code AngelGodBuff} 对亡灵 ×11）不冲突也不重复判定：
     * 那个是「封神」奖励，挂在 {@code LivingHurtEvent} 的 LOWEST 上乘最终值；
     * 这里乘的是 {@code CombatEventHandler} 里的伤害基底，两者是乘算叠加。</p>
     *
     * @return 伤害倍率；武魂没开 / 不是天使 → {@code 1.0}
     */
    public static float undeadDamageMultiplier(PlayerAttributeCapability cap) {
        if (!isActiveWuhun(cap)) {
            return 1.0F;
        }
        int level = Math.max(0, cap.getDengji());
        float t = Math.min(1.0F, level / (float) UNDEAD_BONUS_MAX_LEVEL);
        return 1.0F + UNDEAD_BONUS_MIN + (UNDEAD_BONUS_MAX - UNDEAD_BONUS_MIN) * t;
    }

    // ==================================================================================
    //  觉醒：独立的 10% 抽签
    // ==================================================================================

    /**
     * 觉醒时的「天使抽签」—— 由 {@code PlayerHunhuanAPI#rollOneWuhun} 在抽普通池子<b>之前</b>调用。
     *
     * <p>掷中就直接把天使加给玩家并返回 {@code true}，调用方要立刻 return 天使命名
     * （天赋分支靠这个返回值把自己钉成天才）。</p>
     *
     * <p>已拥有天使的玩家不再参与抽签：否则每次觉醒都会先白掷一次 10%，
     * 相当于把「抽歪」的概率白送给玩家。</p>
     */
    /**
     * 觉醒时的「天使抽签」—— 由 {@code PlayerHunhuanAPI#rollOneWuhun} 在抽普通池子<b>之前</b>调用。
     *
     * <p><b>只判定、不施加</b>：抽中后由调用方统一 {@code addWuHun}，
     * 这样双生抽第二个武魂时也能复用同一套判定（第二个同样可以抽到天使）。</p>
     *
     * @param exclude 本次不允许抽到的名字（双生抽第二个时是「已拥有」清单）
     */
    public static boolean rollIndependentAngel(ServerPlayer player, PlayerAttributeCapability cap,
                                              java.util.Collection<String> exclude) {
        if (has(cap)) {
            return false;   // 已有天使，不再抽（否则每次觉醒都白掷一次 10%）
        }
        if (exclude != null && exclude.contains(Wuhunname.tianshi)) {
            return false;
        }
        return player.getRandom().nextInt(100) < AWAKEN_CHANCE_PERCENT;
    }

    // ==================================================================================
    //  神位：只认天使神
    // ==================================================================================

    /**
     * 这个玩家能不能获得指定的神位。
     *
     * <p><b>天使武魂只认天使神</b>：其它神位一律不给（{@link PlayerAttributeCapability#initializeGodExam}
     * 会直接拒掉，随机触发也会在 {@code KLivingDeathEvent#handleGodGlimpse} 里提前 return）。
     * 天使神自己则是 70 级自动发放，见 {@link #maybeGrantTrial}。</p>
     */
    public static boolean isAllowedGod(PlayerAttributeCapability cap, String godId) {
        return !has(cap) || ANGEL_GOD_ID.equals(godId);
    }

    // ==================================================================================
    //  吸环后补 10 级
    // ==================================================================================

    /**
     * 吸收一枚魂环之后调用（{@code PlayerHunhuanAPI#addHunhuan}）。
     *
     * <h3>只有「开局限定」才补这 10 级</h3>
     * <p>两个条件缺一不可：</p>
     * <ol>
     *   <li>{@code isTianshiStartAwaken()} —— 天使必须是在 <b>0 级觉醒仪式</b>里抽到的。
     *       已经成型的老玩家吃“天使武魂精华”拿到天使，只多一个武魂，<b>一级都不补</b>；</li>
     *   <li>{@code isTianshiRingBonusGiven()} 为 false —— 只发一次。</li>
     * </ol>
     * <p>⚠️ 旧实现只看 {@code totalRings >= 1}：已觉醒的玩家身上本来就有环，
     * 于是「吃精华拿到天使 → 下次随便吸一枚环」就白跳 10 级。这条判断已废弃。</p>
     */
    public static void onRingAbsorbed(ServerPlayer player, PlayerAttributeCapability cap) {
        if (!has(cap) || cap.isTianshiRingBonusGiven()) {
            return;
        }
        // 非 0 级觉醒获得的天使：直接锁死，连老存档都不会再误发
        if (!cap.isTianshiStartAwaken()) {
            cap.setTianshiRingBonusGiven(true);
            return;
        }
        if (totalRings(cap) < 1) {
            return;
        }
        cap.setTianshiRingBonusGiven(true);

        PlayerUpgradeSystem.grantLevelsDirectly(player, cap, FIRST_RING_BONUS_LEVELS);
        player.displayClientMessage(Component.literal("§e天使血脉苏醒 §7+" + FIRST_RING_BONUS_LEVELS + " 级"), true);

        SynsAPI.synsPlayerAttribute(player);
        maybeGrantTrial(player, cap);
    }

    // ==================================================================================
    //  70 级自动开天使神神考
    // ==================================================================================

    /**
     * 拥有天使武魂、等级 ≥{@link #AUTO_TRIAL_LEVEL}、且还没有任何神位/神考 → 直接开启天使神神考。
     *
     * <p>与 {@code KLivingDeathEvent#triggerGodExam} 的区别：<b>不播幻境仪式、不把玩家传回出生点</b>。
     * 那套演出是给「击杀生物随机触发」用的（15 秒后强制传送），自动发放时突然被传走会莫名其妙，
     * 所以这里只做必要的事：写神考数据 + 全服通告 + 同步面板 + 给一个「神明的注视」效果提示。</p>
     *
     * <p>调用点：{@code PlayerUpgradeSystem#processSuccessfulUpgrade}（升级时）、
     * {@code PlayerHunhuanAPI#addWuHun}（高等级玩家后来才觉醒天使）。两处都可能重复触发，所以先查
     * {@code godName} 是否为空。</p>
     */
    public static void maybeGrantTrial(ServerPlayer player, PlayerAttributeCapability cap) {
        if (!has(cap) || cap.getDengji() < AUTO_TRIAL_LEVEL) {
            return;
        }
        if (cap.getGodName() != null && !cap.getGodName().isEmpty()) {
            return;   // 已有神位或已在神考中
        }

        cap.initializeGodExam(player, ANGEL_GOD_ID);
        AchievementAPI.onGodExamStart(player);

        GodInfo info = GodRegistry.GODS.get(ANGEL_GOD_ID);
        String godName = "§e" + (info == null ? "天使神" : info.getName());

        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(
                    "§l§f【神之遗迹】§e" + player.getName().getString()
                            + " §f觉醒了天使武魂并修至 §e" + cap.getDengji()
                            + " §f级，直接获得 " + godName + " §f的传承！"), false);
        }

        player.displayClientMessage(Component.literal("§6天使神·传承 §7九道神考已开启"), true);

        player.addEffect(new MobEffectInstance(ModEffects.THE_GAZE_OF_GOD.get(), 310, 0, false, false));

        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
        SynsAPI.synsPlayerAttribute(player);
    }
}
