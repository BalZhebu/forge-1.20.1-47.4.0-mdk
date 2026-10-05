package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性点数管理：<b>发放</b>（升级/登录补发）+ <b>消耗</b>（加点/重置）。
 *
 * <p>规则：
 * <ul>
 *   <li>每升1 级得 1 点；</li>
 *   <li>单条属性上限 {@link AttributePointSpec#MAX_POINTS} 点；</li>
 *   <li><b>未重置前只能加不能减</b> —— {@link #allocate} 的 amount 只接受正数，
 *       想要减点必须走 {@link #resetWithScroll}；</li>
 *   <li>重置把 11 条全部清零并把点数全额退还，<b>需要 1 个重置卷轴</b>
 *       （{@code ModItems.RESET_SCROLL}）。两条路——右键卷轴 / 面板重置按钮——
 *       都走 {@link #resetWithScroll} 这一个入口，不要各写一份。</li>
 * </ul>
 */
public final class AttributePoints {

    private AttributePoints() {
    }

    // ==================== 发放 ====================

    /**
     * 登录时按等级补发（老存档一次性返还）。
     * 用 pointsGrantedLevel 记账，已发的不会重复发。
     */
    public static void grantOnLogin(ServerPlayer player, PlayerAttributeCapability cap) {
        if (cap.getPointsGrantedLevel() == Integer.MIN_VALUE) {
            // 老存档首次加载：按当前等级全额补发并记账
            int level = Math.max(0, cap.getDengji());
            cap.setAttributePoints(cap.getAttributePoints() + level);
            cap.setPointsGrantedLevel(level);
            if (level > 0) {
                player.sendSystemMessage(Component.literal(
                        "§b【属性点】§f补偿已发放：§e" + level + " §f点"));
            }
            return;
        }
        // 正常情况：等级已被下调过（转生等）时补差额
        int level = Math.max(0, cap.getDengji());
        if (level > cap.getPointsGrantedLevel()) {
            int delta = level - cap.getPointsGrantedLevel();
            cap.addAttributePoints(delta);
            cap.setPointsGrantedLevel(level);
        }
    }

    /** 升级成功时调用：等级已写入 cap。 */
    public static void grantOnLevelUp(ServerPlayer player, PlayerAttributeCapability cap, int newLevel) {
        int granted = Math.max(0, cap.getPointsGrantedLevel());
        if (newLevel <= granted) return;
        int delta = newLevel - granted;
        cap.addAttributePoints(delta);
        cap.setPointsGrantedLevel(newLevel);
        player.sendSystemMessage(Component.literal(
                "§b【属性点】§f获得 §e" + delta + " §f点（当前 §e" + cap.getAttributePoints() + " §f点）"));
    }

    // ==================== 消耗 ====================

    /**
     * 往某条属性上加点。<b>只加不减</b>。
     *
     * @param amount 加多少点（<=0 一律拒绝）
     * @return 实际加上的点数（0 = 失败）
     */
    public static int allocate(ServerPlayer player, PlayerAttributeCapability cap,
                               AttributePointSpec spec, int amount) {
        if (spec == null || amount <= 0) return 0;

        int current = cap.getAllocatedPoints(spec);
        int room = AttributePointSpec.MAX_POINTS - current;
        int usable = Math.min(Math.min(amount, cap.getAttributePoints()), room);
        if (usable <= 0) {
            // 给出原因，方便玩家理解为什么点不动
            if (cap.getAttributePoints() <= 0) {
                notify(player, "§c可用属性点不足");
            } else {
                notify(player, "§c§l" + spec.getDisplayName() + " §c已达上限（"
                        + AttributePointSpec.MAX_POINTS + " 点）");
            }
            return 0;
        }

        cap.setAllocatedPoints(spec, current + usable);
        cap.addAttributePoints(-usable);
        notify(player, "§b【" + spec.getDisplayName() + "】§f+§e" + usable
                + " §f点（§e" + (current + usable) + "/" + AttributePointSpec.MAX_POINTS + "§f）");
        return usable;
    }

    /**
     * 重置全部 11 条属性点，点数全额退还。<b>不消耗任何道具</b>。
     *
     * <p>玩家主动行为（右键卷轴 / 面板重置按钮）请用
     * {@link #resetWithScroll(ServerPlayer, PlayerAttributeCapability)}，那边会扣卷轴。</p>
     *
     * @return 被清掉的点数；0 = 没点过 / 无需重置
     */
    public static int reset(ServerPlayer player, PlayerAttributeCapability cap) {
        int cleared = cap.clearAllocatedPoints();
        if (cleared <= 0) {
            notify(player, "§7尚未分配任何属性点");
            return 0;
        }
        cap.addAttributePoints(cleared);
        notify(player, "§f已重置，返还 §e" + cleared + " §f点");
        return cleared;
    }

    /**
     * 玩家主动重置：<b>消耗 1 个重置卷轴</b>后清空全部 11 条属性点，点数全额退还。
     *
     * <p>两条路都走这一个入口：① 右键 {@code ModItems.RESET_SCROLL}
     * ② 点属性点面板上的"重置属性点"按钮。</p>
     *
     * <p>卷轴从<b>整个背包</b>里扣（含副手槽），所以玩家拿着卷轴打开面板点按钮也能扣到。
     * 卷轴不足时<b>不扣、不重置</b>，只提示。</p>
     *
     * @return 是否成功重置
     */
    public static boolean resetWithScroll(ServerPlayer player, PlayerAttributeCapability cap) {
        if (cap.getAllocatedPointsTotal() <= 0) {
            notify(player, "§7尚未分配任何属性点，无需重置");
            return false;
        }
        if (!consumeResetScroll(player)) {
            notify(player, "§c需要 1 个§b重置卷轴 §c才能重置属性点");
            return false;
        }
        reset(player, cap);
        return true;
    }

    /**
     * 从玩家背包里扣掉 1 个重置卷轴（主背包 + 主手 + 副手）。
     *
     * <p><b>刻意不用</b> {@code Inventory.clearOrCountMatchingItems(predicate)}：
     * 它的三参数重载会额外去扣 {@code containerMenu.getCarried()}
     * （鼠标正抓着的那一摞），语义不可控。这里只动玩家真正持有的槽位。</p>
     *
     * <p>⚠️ 扣完必须显式 {@code inventoryMenu.broadcastChanges()} ——
     * {@code Inventory.setChanged()} 不同步客户端。</p>
     *
     * @return 是否成功扣到
     */
    public static boolean consumeResetScroll(ServerPlayer player) {
        Inventory inv = player.getInventory();
        List<ItemStack> slots = new ArrayList<>(inv.items);
        // 副手单独补上（armor/offhand 不在 items 里）
        ItemStack offhand = inv.offhand.get(0);
        if (!offhand.isEmpty()) slots.add(offhand);

        for (ItemStack slot : slots) {
            if (!slot.is(ModItems.RESET_SCROLL.get())) continue;
            if (!player.getAbilities().instabuild) {
                slot.shrink(1);
            }
            player.inventoryMenu.broadcastChanges();
            return true;
        }
        return false;
    }

    // ==================== 转生继承 ====================

    /**
     * 转生时的属性点继承：<b>清空所有已加点，按转生前已加总数的 10%~30% 折算成新点数</b>，
     * 直接放进新存档的可用点数池里。
     *
     * <p><b>为什么不按条继承、而是按总数折算</b>：转生是整体重置，11 条属性点全部清零。
     * 若逐条继承，玩家花出去的点数会被"打折退还"，等于惩罚；而按<b>总数</b>折算，
     * 相当于"你之前总共投入了 N 点，转生后返还 N×(10%~30%)"，规则简单也最好解释。</p>
     *
     * <p><b>取整规则（用户明确要求）</b>：点数是正整数，<b>一律向下取整、不能四舍五入</b>。
     * 例：转生前共 7 点，倍率30% → 2.1 → 得 <b>2</b> 点（不是 2.1，也不是四舍五入的 2，
     * 而是在"每一点"的层面各自扣小数：7×0.3=2.1 → 2）。</p>
     *
     * @return 实际继承到的点数（已写入 newCap）
     */
    public static int inheritOnZhuansheng(PlayerAttributeCapability newCap,
                                          PlayerAttributeCapability oldCap) {
        int invested = oldCap.getAllocatedPointsTotal();
        // 无论有没有点过都要清空—— 转生后旧加点一律作废
        oldCap.clearAllocatedPoints();
        oldCap.setAttributePoints(0);

        if (invested <= 0) {
            newCap.clearAllocatedPoints();
            return 0;
        }

        // 10%~30% 随机倍率
        double rate = 0.10 + RANDOM.nextDouble() * 0.20;   // [0.10, 0.30)
        // 向下取整：强转int 直接截断小数，绝不四舍五入
        int inherited = (int) (invested * rate);

        newCap.clearAllocatedPoints();
        newCap.addAttributePoints(inherited);
        return inherited;
    }

    /** 转生继承用的随机源（与本类其它随机分开，避免互相干扰）。 */
    private static final java.util.Random RANDOM = new java.util.Random();

    private static void notify(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}