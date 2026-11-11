package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.Wuhunname;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import com.TovidY.kunluncontinent.network.server.S2COpenLoneBambooChoicePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <b>孤竹神 · 彩蛋养成路</b>的唯一权威落点。
 *
 * <h3>触发条件（全部满足才弹选择框）</h3>
 * <ul>
 *   <li>玩家<b>只有一个</b>武魂（双生 / 三生一律不给，避免和天赋锁天才的规则打架）；</li>
 *   <li>这个武魂不在 {@link #EXCLUDED_WUHUN} 里（目前只有天使，以后新增直接往里加）；</li>
 *   <li>这个武魂的<b>第一枚、也是唯一一枚</b>魂环来自孤竹（{@code sourceType == lone_bamboo}）；</li>
 *   <li>玩家当前没有任何神考、也没封神。</li>
 * </ul>
 *
 * <h3>两条路</h3>
 * <ul>
 *   <li><b>正常魂师之路</b>：什么都不做，玩家继续正常玩；</li>
 *   <li><b>孤竹神位之路</b>：直接给孤竹神考（{@link #startGod}，绕过等级/环数等门槛），
 *       代价是<b>从此只能吸收孤竹魂环</b>（见 {@link #allowRing}），也拿不到别神的神位。</li>
 * </ul>
 *
 * <h3>九考</h3>
 * <p>第 1~8 考 = 吸收第 2~9 枚孤竹魂环，每考奖励一根原版竹子；
 * 第 9 考是事件考（{@link LoneBambooTrial}），不由本类判定。</p>
 *
 * <h3>兜底：不选 = 选左边</h3>
 * <p>弹框后会登记一个 {@link #CHOICE_TIMEOUT_TICKS} 的截止时间。玩家用任何方式躲掉选择
 * （关界面、掉线、被杀、切维度……只要没发包）→ 到点<b>默认走正常魂师之路</b>，
 * 并且这次机会<b>作废</b>（{@link #consumePending} 之后再来包一律不受理）。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public final class LoneBambooGodPath {

    /** 神位 id（同时是 {@code GodRegistry} 的 key）。 */
    public static final String GOD_ID = "lonebamboo_god";

    /** 神位名。 */
    public static final String GOD_NAME = "孤竹神";

    /** 孤竹魂环的 {@code sourceType}（实体注册名）。 */
    public static final String SOURCE_ID = "kunluncontinent:lone_bamboo";

    /**
     * 参与彩蛋判定时<b>必须没有</b>的武魂。
     *
     * <p>以后要排除新武魂，直接往这里加一行即可（用户要求预留）。</p>
     */
    public static final Set<String> EXCLUDED_WUHUN = Set.of(Wuhunname.tianshi);

    /** 选择框只弹一次（玩家选了正常路之后不再骚扰）。 */
    private static final String NBT_CHOICE_SHOWN = "LoneBambooChoiceShown";

    /**
     * 弹框后多久没做出选择 → 默认走正常魂师之路（20 tick = 1 秒，这里 120 秒）。
     *
     * <p>兜底场景：玩家关不掉界面（已禁 ESC）就用掉线 / 死亡 / 换维度 / 强退来躲，
     * 或者干脆挂机不点。到点由服务端统一判定，不依赖客户端配合。</p>
     */
    public static final int CHOICE_TIMEOUT_TICKS = 120 * 20;

    /** 已弹框、还在等选择的玩家 → 截止的 gameTime。 */
    private static final Map<UUID, Long> PENDING_CHOICE = new HashMap<>();

    private LoneBambooGodPath() {
    }

    // ==================================================================================
    //  判定
    // ==================================================================================

    /** 玩家是否正走在孤竹神位上（考中或已封神）。 */
    public static boolean onPath(PlayerAttributeCapability cap) {
        return cap != null && GOD_ID.equals(cap.getGodName());
    }

    /** 这枚魂环是不是孤竹魂环。 */
    public static boolean isBambooRing(MobAttributeCapability ring) {
        return ring != null && SOURCE_ID.equals(ring.getSourceType());
    }

    /** 玩家唯一那个武魂的第一枚环是不是孤竹环。 */
    public static boolean firstRingIsBamboo(PlayerAttributeCapability cap) {
        if (cap == null) {
            return false;
        }
        List<String> names = cap.getWuhunListsname();
        if (names == null || names.size() != 1) {
            return false;
        }
        List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(names.get(0));
        return rings != null && rings.size() == 1 && isBambooRing(rings.get(0));
    }

    /** 彩蛋的全部触发条件（吸收完第一枚魂环后调用）。 */
    public static boolean isEligible(PlayerAttributeCapability cap) {
        if (cap == null || cap.isGod() || !cap.getGodName().isEmpty()) {
            return false;
        }
        List<String> names = cap.getWuhunListsname();
        // 双生 / 三生不给：这条路要求"唯一的武魂唯一的第一环"
        if (names == null || names.size() != 1) {
            return false;
        }
        for (String name : names) {
            if (EXCLUDED_WUHUN.contains(name)) {
                return false;
            }
        }
        return firstRingIsBamboo(cap);
    }

    // ==================================================================================
    //  吸收回调 / 吸收拦截
    // ==================================================================================

    /**
     * 每吸收成功一枚魂环后调用（{@code PlayerHunhuanAPI#addHunhuan} 末尾）。
     *
     * <p>两种可能：① 已经在孤竹路上 → 推进第 1~8 考；② 满足彩蛋条件 → 弹选择框。</p>
     */
    public static void onRingAbsorbed(ServerPlayer player, PlayerAttributeCapability cap) {
        if (player == null || cap == null) {
            return;
        }

        if (onPath(cap)) {
            advanceByRing(player, cap);
            return;
        }

        if (!isEligible(cap)) {
            return;
        }

        // 只弹一次：玩家选"正常魂师之路"之后不该再被拦第二次
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(NBT_CHOICE_SHOWN)) {
            return;
        }
        data.putBoolean(NBT_CHOICE_SHOWN, true);

        player.sendSystemMessage(Component.literal(
                "§d§l【孤竹之缘】§r §f你以孤竹之环开启了武魂……冥冥中有一道声音在问你：要走哪条路？"));
        markPending(player);
        NetworkHandler.sendToClient(new S2COpenLoneBambooChoicePacket(), player);
    }

    // ==================================================================================
    //  兜底：不选 = 选左边（正常魂师之路）
    // ==================================================================================

    /** 登记截止时间（弹框时调用）。 */
    private static void markPending(ServerPlayer player) {
        PENDING_CHOICE.put(player.getUUID(), player.level().getGameTime() + CHOICE_TIMEOUT_TICKS);
    }

    /** 玩家是否还在"等着选"。 */
    public static boolean isPending(UUID uuid) {
        return PENDING_CHOICE.containsKey(uuid);
    }

    /**
     * <b>一次性领取选择权</b>：拿到 {@code true} 才能接着处理这条选择包。
     *
     * <p>超时之后再来的包、重复点击的包，一律返回 {@code false} —— 防止
     * "超时已默认走正常路，玩家又补点一下孤竹"这种自相矛盾的结果。</p>
     */
    public static boolean consumePending(ServerPlayer player) {
        return player != null && PENDING_CHOICE.remove(player.getUUID()) != null;
    }

    /** 超时：默认正常魂师之路。 */
    private static void defaultToNormal(ServerPlayer player) {
        if (PENDING_CHOICE.remove(player.getUUID()) == null) {
            return;
        }
        player.sendSystemMessage(Component.literal(
                "§7【孤竹之缘】迟迟未作抉择，缘分已散 —— 你走上了§a正常魂师之路§7。")
                .withStyle(ChatFormatting.GRAY));
        // 界面由客户端自己的倒计时关掉（两边时长一致）；这里不再发包，
        // 以免误关玩家当时开着的其它容器界面。
    }

    /** 每 tick 检查超时（集合为空时开销 = 一次 isEmpty）。 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (PENDING_CHOICE.isEmpty() || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        Long deadline = PENDING_CHOICE.get(player.getUUID());
        if (deadline == null) {
            return;
        }
        if (player.level().getGameTime() >= deadline) {
            defaultToNormal(player);
        }
    }

    /**
     * 掉线：直接作废这次机会。
     *
     * <p>{@code NBT_CHOICE_SHOWN} 在弹框时就已置位，所以重新登录不会再弹 ——
     * 等价于"默认走了正常路"，正是用户要的兜底口径。</p>
     */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (PENDING_CHOICE.isEmpty()) {
            return;
        }
        PENDING_CHOICE.remove(event.getEntity().getUUID());
    }

    /**
     * 吸收前的拦截：走上孤竹路之后<b>只能吸孤竹魂环</b>。
     *
     * @return {@code true} = 允许吸收
     */
    public static boolean allowRing(ServerPlayer player, PlayerAttributeCapability cap, HunhuanEntity ringEntity) {
        if (cap == null || !onPath(cap)) {
            return true;
        }
        MobAttributeCapability ring = ringEntity
                .getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                .orElse(null);
        if (ring == null || isBambooRing(ring)) {
            return true;
        }
        player.sendSystemMessage(Component.literal(
                "§c【孤竹之路】§f你已立誓只取孤竹之魂，§7无法吸收其它魂兽的魂环。").withStyle(ChatFormatting.RED));
        return false;
    }

    // ==================================================================================
    //  九考推进
    // ==================================================================================

    /**
     * 第 N 考（N = 1~8）= 吸收第 N+1 枚孤竹魂环。
     *
     * <p>第 9 考是事件考，走 {@link LoneBambooTrial}，这里直接跳过。</p>
     */
    private static void advanceByRing(ServerPlayer player, PlayerAttributeCapability cap) {
        int stage = cap.getCurrentStage();
        if (stage < 1 || stage > 8) {
            return;
        }
        List<String> names = cap.getWuhunListsname();
        if (names == null || names.isEmpty()) {
            return;
        }
        List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(names.get(0));
        if (rings == null || rings.size() != stage + 1) {
            return;   // 环数没对上（比如这次吸收的不是孤竹环，或进度已推进过）
        }
        if (!isBambooRing(rings.get(rings.size() - 1))) {
            return;
        }

        GodExamFeedback.announcePass(player, stage);
        cap.checkTaskCompletion(player);
        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
    }

    /** 事件考（第 9 考）达成时调用。 */
    public static void completeFinalExam(ServerPlayer player, PlayerAttributeCapability cap) {
        if (cap == null || cap.getCurrentStage() != 9) {
            return;
        }
        GodExamFeedback.announcePass(player, 9);
        cap.checkTaskCompletion(player);
        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
    }

    /**
     * 孤竹神封神时补的那一枚环：<b>普通 10 年魂环</b>。
     *
     * <p><b>刻意不走 {@code GodBlessingRing} 那条路</b>：那套会按第九环 ×3 倍给年限、
     * 并打上 {@code shenci} 标记（专属神环贴图 + 额外属性）。孤竹神的彩蛋设定是
     * "一根竹子而已"，所以这里只加一枚最普通的 10 年环，渲染和属性都走普通魂环逻辑。</p>
     *
     * <p>⚠️ 调用时机必须在「所有魂环年限提升」之后，否则刚发的 10 年被一起加上去了。</p>
     */
    public static boolean grantTenYearRing(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return false;
        }
        java.util.Map<String, List<MobAttributeCapability>> allRings = cap.getMonsterCapabilityLists();
        if (allRings == null || allRings.isEmpty()) {
            return false;
        }

        // 环最多的那个武魂 = 主修武魂（并列取先遇到的，这条路本来也只有一个武魂）
        String bestName = null;
        int bestCount = -1;
        for (java.util.Map.Entry<String, List<MobAttributeCapability>> entry : allRings.entrySet()) {
            List<MobAttributeCapability> rings = entry.getValue();
            int count = rings == null ? 0 : rings.size();
            if (count > bestCount) {
                bestCount = count;
                bestName = entry.getKey();
            }
        }
        if (bestName == null) {
            return false;
        }
        List<MobAttributeCapability> target = allRings.get(bestName);
        if (target == null) {
            return false;
        }

        MobAttributeCapability ring = new MobAttributeCapability();
        ring.initNianxian(10L);
        // 不 setShenci(true) / setShenciGod(...)：这枚就是普通魂环
        target.add(ring);

        com.TovidY.kunluncontinent.network.SynsAPI.synsPlayerAttribute(player);
        PlayerHunhuanAPI.broadcastWuhunRings(player, true);

        player.sendSystemMessage(Component.literal(
                "§2【孤竹神赐】§f你的 §b" + bestName + " §f获得了一枚 §e10 年 §f魂环。"));
        return true;
    }

    // ==================================================================================
    //  给神位
    // ==================================================================================

    /**
     * 玩家点了「孤竹神位之路」：直接发神考，<b>无视等级 / 魂环数等一切门槛</b>。
     *
     * <p>用 {@code forceInitializeGodExam} 而不是 {@code initializeGodExam}：
     * 后者会走「天使只认天使神」的校验，而这条路本身就该绕开所有神位规则。</p>
     */
    public static void startGod(ServerPlayer player, PlayerAttributeCapability cap) {
        if (player == null || cap == null) {
            return;
        }
        cap.forceInitializeGodExam(player, GOD_ID);
        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);

        // 只留一句确认 —— 规则/第一考内容面板上都有，别再刷屏
        player.sendSystemMessage(Component.literal("§2你踏上了孤竹神位之路。"));
    }
}
