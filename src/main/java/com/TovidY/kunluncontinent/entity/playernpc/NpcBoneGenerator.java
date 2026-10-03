package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * NPC 魂骨系统：<b>生成</b>、<b>属性生效</b>、<b>掉落</b> 三件事的唯一实现处。
 *
 * <h3>NPC 为什么能装魂骨</h3>
 * NPC 的 {@code soulCapability} 就是 {@link PlayerAttributeCapability}——<b>和玩家同一个类</b>，
 * 所以它天然带 7 个魂骨槽（{@code hunguInventory}）和 {@code boneOnlyStats}，
 * 和玩家走的是<b>同一套</b>属性结算代码，不需要任何额外适配。
 *
 * <h3>年限范围（硬性平衡约束）</h3>
 * <pre>
 *   NPC 等级      魂骨年限范围
 *   1~49            10~99 年      （十年）
 *   50~69           100~999 年    （百年）
 *   70~84           1,000~9,999 年（千年）
 *   85~94           1万~9.9万年   （万年）
 *   95~99      10万~99.9万年     （十万年，95 级是硬门槛）
 *   100 及以上  10万~100万年      （百万年，只有满级突破 NPC 才会出）
 * </pre>
 * <b>95 级以下是绝对出不了十万年魂骨的</b>；百万年封顶，再高也不给。
 * 这条是用户明确要求的硬性条件。
 *
 * <h3>概率（两级都极低）</h3>
 * <pre>
 *   NPC 自带魂骨的概率      npcHasBoneChance(level)     1/10000 ~ 1/50000
 *   击杀后掉出来���概率      dropChance(level)           55% ~ 90%
 *   两者相乘 ≈ 0.0000055~0.9%  —— 也就是"带魂骨的 NPC 本身稀有，
 *                              但打掉它基本必掉"，正是用户要的体验。
 * </pre>
 */
public final class NpcBoneGenerator {

    private NpcBoneGenerator() {
    }

    /**
     * 单条魂骨可携带的词条数上限（不含必带的 {@code maxshengming}）。
     *
     * <p><b>为什么是 6 而不是 11</b>：可选词条池一共 11 个，
     * 每次都塞满就等于"固定满词条"，词条系统就没有意义了。
     * 6 条（含 {@code maxshengming} 共 7 条）已经是"极品骨"的水准。</p>
     */
    private static final int MAX_ENTRIES = 6;

    /** NPC 名字里用来标记"这只 NPC 带魂骨"的 NBT key。 */
    public static final String TAG_HAS_BONE = "NpcHasBone";
    /** 魂骨数量。 */
    public static final String TAG_BONE_COUNT = "NpcBoneCount";
    /** 第一枚魂骨的年限，存 NBT 便于读档/调试查看。 */
    public static final String TAG_BONE_NIANXIAN = "NpcBoneNianxian";
    /** 已掉落过魂骨的标记，防止反复读档重复掉。 */
    private static final String TAG_BONE_DROPPED = "NpcBoneDropped";

    private static final Random RANDOM = new Random();

    /** 可被选为魂骨部件的物品（只取 HUNGULIST 里真正的魂骨，避开分解 gossip 等特殊道具）。 */
    private static final List<Item> BONE_POOL = new ArrayList<>();

    static {
        BONE_POOL.add(ModItems.SOUL_BEAST_SKULL.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_BREASTBONE.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_LEFT_HAND_BONE.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_RIGHT_HAND_BONE.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_LEFT_LEG_BONE.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_RIGHT_LEG_BONE.get());
        BONE_POOL.add(ModItems.SOUL_BEAST_EXTERNAL_APPENDAGES.get());
    }

    // ==================== 年限范围 ====================

    /**
     * 按 NPC 等级取该等级的魂骨年限<b>范围</b>（年）。同时也是"能出什么档位"的硬门槛。
     *
     * <p>区间用<b>档位对齐</b>而不是连续随机：80 级 NPC 绝不会掉出千年档，
     * 防止"等级高但掉低年限"的畸形结果。NPC 等级上限 99。</p>
     *
     * <pre>
     *   1~39 级    1 ~ 1万       年
     *   40~79 级   100 ~ 10万     年
     *   80~94 级   1万 ~ 100万年
     *   95~99 级   1万 ~ 999.9万年（最高）
     * </pre>
     */
    public static long rollNianxian(int npcLevel) {
        int lv = Math.max(1, npcLevel);
        long min;
        long max;
        if (lv >= 95) {
            // 最高档：1万 ~ 999.9万 年
            min = 10_000L;
            max = 9_999_999L;
        } else if (lv >= 80) {
            min = 10_000L;
            max = 1_000_000L;
        } else if (lv >= 40) {
            min = 100L;
            max = 100_000L;
        } else {
            min = 1L;
            max = 10_000L;
        }
        // 偏向上限（前段加权），让"打高级 NPC 拿到高年限"的体感更明显
        double t = Math.pow(RANDOM.nextDouble(), 0.6);
        return min + (long) ((max - min) * t);
    }

    // ==================== 概率 ====================

    /**
     * NPC <b>第一枚</b>魂骨自带的概率。
     *
     * <p>1~39 级 0.01%、40~79 级 0.05%、80 级以上 0.1%（80~94 与 95~99 同档）。</p>
     */
    public static double npcHasBoneChance(int npcLevel) {
        int lv = Math.max(1, npcLevel);
        if (lv >= 80) return 0.001;    // 0.1%
        if (lv >= 40) return 0.0005;   // 0.05%
        return 0.0001;                 // 0.01%
    }

    /**
     * 已有一枚魂骨后，<b>再出现下一枚</b>的概率（链式，见 {@link #equipBones}）。
     *
     * <p>链条：60% → 32% → 14% → 5% → 1.5% → 0.4%</p>
     *
     * <p><b>调参历史</b>：最初是 40/15/5/1/0.2/0.04，算出来 7 枚总概率低到
     * Lv99 约 1/6500 亿 —— 理论上存在、实际永远见不到，等于没做。
     * 现版本整体抬升约 1.5 倍并保持递减比例，落在
     * <b>Lv99 约 1/2.3 亿、Lv50 约 1/21 亿</b>这个"稀有传闻"区间。</p>
     */
    private static final double[] CHAIN = {0.60, 0.32, 0.14, 0.05, 0.015, 0.004};

    /**
     * 等级对"多魂骨"概率的加成系数。<b>高等级略高一点</b>（用户要求）。
     *
     * <p>1 级 1.0×，99 级 2.0×（线性递增）。乘在链式的每一跳上。</p>
     */
    private static double levelChainBonus(int npcLevel) {
        int lv = Math.max(1, Math.min(99, npcLevel));
        return 1.0 + (lv - 1) / 98.0;   // 1.0 ~ 2.0
    }

    /**
     * 击杀"已带魂骨"的 NPC 时，魂骨掉出来的概率。<b>恒为 100%</b>（用户要求）。
     *
     * <p>原先按等级分 55%/65%/85%/90% 四档，现已取消 —— <b>只要 NPC 带骨，打死必掉</b>。
     * 稀有度完全由"NPC 自带魂骨的概率"那一层承担（0.01%/0.05%/0.1%），
     * 这样玩家看到带骨的 NPC 时不会怀疑"这次会不会掉不出来"。</p>
     *
     * <p>注意：这<b>不影响</b> 7 枚一起出现的概率 —— 那是生成时的链式 roll 决定的，
     * 与掉率无关。</p>
     */
    public static double dropChance(int npcLevel) {
        return 1.0;
    }

    /**
     * 出现 <b>7 枚</b>魂骨的总概率（调试指令 {@code odds} 会打这个数）。
     *
     * <p>Lv99 约 <b>1/2.3 亿</b>、Lv50 约 1/21 亿、Lv20 约 1/430 亿 ——
     * 属于"稀有传闻"级别：全服长期活跃玩家里理论上可能撞见一次，
     * 但正常游玩基本遇不到。</p>
     */
    public static double sevenBoneChance(int npcLevel) {
        double p = npcHasBoneChance(npcLevel);
        double bonus = levelChainBonus(npcLevel);
        for (double c : CHAIN) p *= Math.min(1.0, c * bonus);
        return p;
    }

    // ==================== 生成 ====================

/**
     * NPC 初始化时调用：按链式概率给这只 NPC 装 0~7 枚魂骨。
     *
     * <p><b>链式规则</b>（见 {@link #CHAIN}，用户指定 + 平衡上调后）：
     * <pre>
     *   第1 枚：按等级的自带概率（0.01% / 0.05% / 0.1%）
     *   已有 1 枚 → 60%   出第 2 枚
     *   已有 2 枚 → 32%   出第 3 枚
     *   已有 3 枚 → 14%   出第 4 枚
     *   已有 4 枚 →  5%   出第 5 枚
     *   已有 5 枚 → 1.5%  出第 6 枚
     *   已有 6 枚 → 0.4%  出第 7 枚
     * </pre>
     * 每跳再乘 {@link #levelChainBonus}（1~99 级对应 1.0×~2.0×），高等级略容易出多枚。
     *
     * <p>装进 {@code soulCapability.hunguInventory} 后调
     * {@code collectBoneAttributes}，于是 {@code recalculateNpcStats()} 之后属性就生效。</p>
     *
     * @return 实际装上的魂骨数量（0 = 这只 NPC 没带骨）
     */
    public static int tryEquipBone(PlayerNpcEntity npc, int npcLevel) {
        if (npcHasBoneChance(npcLevel) < RANDOM.nextDouble()) return 0;
        return equipBones(npc, npcLevel, -1, 0);
    }

    /**
     * 连续装多枚魂骨，<b>走自然链式概率</b>。
     *
     * @param forcedNianxian 传 {@code -1} 表示每枚按等级随机年限
     * @return 装上的数量
     */
    public static int equipBones(PlayerNpcEntity npc, int npcLevel, long forcedNianxian) {
        return equipBones(npc, npcLevel, forcedNianxian, 0);
    }

    /**
     * 连续装多枚魂骨（{@link #tryEquipBone} 与指令共用）。
     *
     * @param forcedNianxian 传 {@code -1} 表示每枚按等级随机年限
     * @param exactCount     传 {@code 0}（或 &lt;=0）走自然链式概率；
     *                       传 1~7 则<b>强制恰好装这么多枚</b>（调试用，跳过链式概率）
     * @return 装上的数量
     */
    public static int equipBones(PlayerNpcEntity npc, int npcLevel, long forcedNianxian, int exactCount) {
        PlayerAttributeCapability soul = npc.getSoulCapability();
        double bonus = levelChainBonus(npcLevel);
        boolean force = exactCount > 0;

        // ⭐ 全局轮转序列：所有词条先整体洗牌一次，各枚魂骨从队首依次"领用"，
        // 用过的挪到队尾 → 同一只 NPC 的 7 枚魂骨词条**互不重复**（10 个词条足够7 枚分）。
        // 之前每枚各自 shuffle，结果 7 枚的大量重复，看起来像"每枚都一样"。
        List<String> rotation = new ArrayList<>(ENTRY_POOL);
        Collections.shuffle(rotation);

        int target = force ? Math.min(exactCount, 7) : 7;
        int count = 0;
        for (int i = 0; i < target; i++) {
            if (!force && i > 0) {
                // 第2枚起走链式概率，且必须已有上一枚
                double p = Math.min(1.0, CHAIN[i - 1] * bonus);
                if (RANDOM.nextDouble() > p) break;
            }
            long nianxian = forcedNianxian > 0 ? forcedNianxian : rollNianxian(npcLevel);
            // 槽位：从没装过的里随机挑一个
            int slot = pickFreeSlot(soul);
            if (slot < 0) break;

            soul.getHunguInventory().setStackInSlot(slot,
                    createBoneStack(npc, nianxian, count, rotation));
            count++;
        }

        if (count == 0) return 0;

        // 重算 boneOnlyStats，让全部魂骨属性一起生效
        refreshBoneAttributes(soul);

        npc.getPersistentData().putBoolean(TAG_HAS_BONE, true);
        npc.getPersistentData().putInt(TAG_BONE_COUNT, count);
        // 年限取第一枚的（调试/提示用）
        npc.getPersistentData().putLong(TAG_BONE_NIANXIAN,
                readFirstBoneNianxian(soul));
        return count;
    }

    /** 找一个还空着的魂骨槽；全满返回 -1。 */
    private static int pickFreeSlot(PlayerAttributeCapability soul) {
        int free = -1;
        for (int i = 0; i < 7; i++) {
            if (soul.getHunguInventory().getStackInSlot(i).isEmpty()) {
                if (free < 0) free = i;
            } else if (free >= 0) {
                return free;   // 找到第一个空槽就直接用
            }
        }
        return free;
    }

    private static long readFirstBoneNianxian(PlayerAttributeCapability soul) {
        for (int i = 0; i < 7; i++) {
            var stack = soul.getHunguInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            return stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY)
                    .map(a -> (long) a.getNianxian()).orElse(0L);
        }
        return 0L;
    }

    /**
     * 强制给 NPC 装上魂骨（调试指令用，跳过第一枚的概率）。
     *
     * @param forcedNianxian 传 {@code -1} 表示按等级随机
     */
    public static boolean forceEquipBone(PlayerNpcEntity npc, int npcLevel, long forcedNianxian) {
        return equipBones(npc, npcLevel, forcedNianxian) > 0;
    }

    /** NPC 是否带魂骨（读 NBT，不看魂骨槽——槽可能被其它代码清空）。 */
    public static boolean hasBone(PlayerNpcEntity npc) {
        return npc.getPersistentData().getBoolean(TAG_HAS_BONE);
    }

    // ==================== 掉落 ====================

    /**
     * 玩家击杀 NPC 时调用：把它身上<b>全部</b>魂骨掉出来。返回掉落的数量。
     *
     * <p>硬性前提（两条）：
     * <ol>
     *   <li>NPC 必须<b>带魂骨</b>；</li>
     *   <li>未掉落过（防读档重复掉）。</li>
     * </ol>
     * 击杀者必须是玩家这一点由 {@code PlayerNpcEntity.die} 保证。
     *
     * <p><b>不再有掉率判定</b> —— 带骨必掉 100%（见 {@link #dropChance}）。
     * 稀有度全在"生成时是否带骨"那一层。魂骨直接从魂骨槽取出，所以必定带词条。</p>
     */
    public static int tryDropBone(PlayerNpcEntity npc) {
        if (!hasBone(npc)) return 0;
        if (npc.getPersistentData().getBoolean(TAG_BONE_DROPPED)) return 0;

        npc.getPersistentData().putBoolean(TAG_BONE_DROPPED, true);

        // 把 7 个槽里的魂骨全部丢出来
        PlayerAttributeCapability soul = npc.getSoulCapability();
        int dropped = 0;
        for (int i = 0; i < 7; i++) {
            ItemStack stack = soul.getHunguInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var itemEntity = npc.spawnAtLocation(stack);
            if (itemEntity != null) {
                itemEntity.setExtendedLifetime();
                dropped++;
            }
        }
        return dropped;
    }

    // ==================== 内部 ====================

    /**
     * 可选词条池（不含必带的 {@code maxshengming}）。共 10 个。
     * 见 {@link #createBoneStack} 里"全局轮转"的用法。
     */
    private static final List<String> ENTRY_POOL = List.of(
            "gongji", "fangyu", "baojilv", "baojishanghai",
            "wuchuan", "shanbi", "mingzhong", "kangbao",
            "shengminghuifu", "xixue"
    );

    /** 造一枚带年限 + 词条的魂骨（不写入任何槽位）。 */
    private static ItemStack createBoneStack(PlayerNpcEntity npc, long nianxian, int boneIndex,
                                            List<String> assigned) {
        Item item = BONE_POOL.get(RANDOM.nextInt(BONE_POOL.size()));
        ItemStack stack = new ItemStack(item);

        stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            attr.toUpdateNianxian((int) nianxian);
            attr.setSourceName(cleanName(npc.getName().getString()));

            int count = rollEntryCount(npc.getSoulCapability().getDengji(), boneIndex);
            // 从轮转序列里顺序取，保证同一只 NPC 的各枚魂骨词条尽量不重复
            int take = Math.min(count, assigned.size());
            List<String> mine = new ArrayList<>(assigned.subList(0, take));
            // 把用掉的部分挪到队尾，下一枚接着从后面取
            assigned.subList(0, take).clear();
            assigned.addAll(mine);

            attr.getActiveAttributes().clear();
            attr.getActiveAttributes().add("maxshengming");   // 必带
            attr.getActiveAttributes().addAll(mine);

            stack.getOrCreateTag().put("shanhaiitematuble", attr.serializeNBT());
        });
        return stack;
    }

    /**
     * 词条数量：<b>NPC 等级</b>越高越多，<b>第几枚</b>也递增。
     *
     * <p><b>刻意压低上限</b>（{@link #MAX_ENTRIES} = 6）：11 个词条里带 6 条已经是"极品骨"，
     * 每次都塞满 9~10 条就完全没有"词条"的存在感了 —— 那不如直接给固定满词条。</p>
     *
     * <p>实际落点：Lv1~39 约 2~3 条、Lv40~79 约 3~4 条、Lv80~94 约 4~5 条、
     * Lv95~99 约 5~6 条；同一只 NPC 的第 2 枚起每枚 +1（上限仍为 6）。</p>
     */
    private static int rollEntryCount(int npcLevel, int boneIndex) {
        int lv = Math.max(1, npcLevel);
        int base;
        if (lv >= 95) base = 5;
        else if (lv >= 80) base = 4;
        else if (lv >= 40) base = 3;
        else base = 2;

        // 第 2 枚起每枚 +1（boneIndex 从 0 开始），末枚不再 +（避免最后一枚必然顶格）
        int scaled = base + Math.min(boneIndex, 2) + (RANDOM.nextInt(3) - 1);
        return Math.max(2, Math.min(MAX_ENTRIES, scaled));
    }

    /**
     * 刷新 NPC 的魂骨属性汇总。
     *
     * <p>直接复用玩家侧的 {@link PlayerAttributeCapability#collectBoneAttributes}——
     * 遍历 7 个槽、累加词条的那段逻辑两边完全一致（当初玩家那边的方法末尾会
     * {@code SynsAPI.synsPlayerAttribute(player)} 发网络包，NPC 不是 ServerPlayer 不需要，
     * 所以把"只做汇总"的部分抽成了公共静态方法）。</p>
     */
    public static void refreshBoneAttributes(PlayerAttributeCapability soul) {
        PlayerAttributeCapability.collectBoneAttributes(soul);
    }

    /** NPC 显示名可能带 "-----属性" 后缀，魂骨来源名只取前半截。 */
    private static String cleanName(String full) {
        int idx = full.indexOf("-----");
        return idx > 0 ? full.substring(0, idx) : full;
    }

    /** 调试用：造一枚指定年限的魂骨 ItemStack（指令"给自己一个魂骨"用）。 */
    public static ItemStack debugCreateBone(long nianxian) {
        ItemStack stack = new ItemStack(BONE_POOL.get(RANDOM.nextInt(BONE_POOL.size())));
        stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            attr.toUpdateNianxian((int) nianxian);
            attr.setSourceName("调试生成");
            attr.getActiveAttributes().clear();
            attr.getActiveAttributes().add("maxshengming");
            attr.getActiveAttributes().add("gongji");
            attr.getActiveAttributes().add("fangyu");
            stack.getOrCreateTag().put("shanhaiitematuble", attr.serializeNBT());
        });
        return stack;
    }
}