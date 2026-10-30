package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapability;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.godclass.interfac.GodTask;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

//玩家属性

public class PlayerAttributeCapability implements ICapabilitySerializable<CompoundTag> {

    public static final UUID CASTING_SLOWDOWN_ID = UUID.fromString("7f369f4a-8e2b-4f9e-a0e4-522f1c305c6d");

    private int castingTick = 0;
    private int requiredCastTick = 0;
    private BaseSkillItem currentCastingSkill = null;
    /** 正在吟唱的技能所在槽位；-1 = 没在吟唱（见 {@link #getCastingSlot()}）。 */
    private int castingSlot = -1;
    /**
     * 本次吟唱是否带抗打断。
     *
     * <p>抗打断在<b>吟唱开始的那一刻只掷一次骰</b>：掷中就代表整段吟唱全程不可被打断，
     * 掷不中则任意一次实打实的命中都能打断。见 {@code CPacketReleaseSkill}。</p>
     */
    private boolean castingProtected = false;

    private float uiScale = 1.0f;

    private String godName = "";        // 当前进行的神位ID (如 "sea_god")，为空代表无神考
    private int currentStage = 1;       // 当前神考阶段 (1-9)
    private int taskProgress = 0;       // 当前任务的数值进度 (如杀怪数、提交数)
    private boolean isGod = false;      // 是否已完成封神

    private String[] assignedExams = new String[10]; // 存储1-9考抽中的任务描述
    private String[] assignedTargets = new String[10]; // 存储任务目标ID
    private int[] assignedCounts = new int[10]; // 存储需求数量
    private String[] assignedTypes = new String[10]; // 存储任务类型

    private boolean initialized = false;

    private int uiOffsetY = 0;

    // ==================== 【昆仑大陆·爬塔系统字段】 ====================
    private int currentTowerFloor = 0;      // 当前爬塔层数 (0代表第一层)
    private long towerLastActiveTick = 0;   // 爬塔防挂机最后活动Tick
    private boolean isTowerChallenging = false; // 是否处于爬塔挑战中

    private Map<String, BaseSkillItem[]> wuhunSkillsMap = new HashMap<>();
    private Map<String, Integer> selectedSkillIndexMap = new HashMap<>();

    private int xiulianTime = 600;
    private boolean usingAll = false;

    private Map<String, List<MobAttributeCapability>> monsterCapabilityLists = new HashMap<>();
    private List<String> wuhunListsname = new ArrayList<>();

    /**
     * 「已经服下、但还没觉醒」的武魂精华预定名单（顺序 = 吃下的顺序）。
     *
     * <p>武魂精华的意义就在这：0 级还觉醒不了武魂时先吃下去，<b>下次觉醒仪式</b>直接觉醒它
     * （吃两个就一次觉醒两个 = 双生，必天才资质）。见 {@code PlayerHunhuanAPI#awakenAtRitual}。</p>
     */
    private List<String> pendingWuhunEssences = new ArrayList<>();

    private int hunhuankuaiguan;

    private float shengming = 20.0f;  // 当前生命值，默认20.0
    private float maxshengming = 20.0f;  // 最大生命值，默认20.0
    private float jingshenli = 20.0f;
    private float maxjingshenli = 20.0f;
    private float mingzhong = 1.0f;
    private float fangyu = 1.0f;  // 防御力，默认0.0，减少所受伤害
    private float gongji = 1.0f;  // 攻击力，默认1.0，影响攻击伤害
    private float baojilv = 5.0f;  // 暴击率，默认5.0%，提高暴击几率
    private float baojishanghai = 150.0f;  // 暴击伤害，默认150.0%，暴击时的伤害加成
    private float xixue = 1.0f;  // 吸血，默认0.0%，暴击或攻击时回复生命
    private float shanbi = 1.0f;  // 闪避率，默认0.0%，闪避敌人攻击的几率
    private float kangbao = 1.0f;  // 抗暴，默认0.0%，减少被敌人暴击的几率
    private float jingyan = 0;  // 当前经验值，默认0
    private float wuchuan = 1;
    private int dengji = 0;  // 玩家等级，默认0
    private float maxjingyan = 20;  // 最大经验值，默认20，达到该经验后升级
    private float shengminghuifu = 1.0f;
    private float tupochenggonglv = 99.0f;  // 突破成功率，默认99%

    private String forcedTalent = "";
    private int xiantianTalent = 0;

    /**
     * 「神考全过后全部魂环 +5 万年」这个一次性奖励是否已经发过。
     *
     * <p>为什么需要一个字段：封神流程（{@code GodInfo#finalizeAscension}）在积攒型神考、
     * debug 命令、以及以后可能的"重跑奖励"里都可能被重复触发，而年限提升是<b>纯加法</b>，
     * 重跑一次就多 5 万年。用标记保证幂等。</p>
     *
     * <p>存进 NBT 而不放 persistentData：转生时会连同新 cap 一起被重置成 {@code false}，
     * 玩家重修后再次封神可以重新获得 —— 这正是期望行为。</p>
     */
    private boolean godNianxianBonus = false;

    /**
     * 「天使武魂：吸收第一枚魂环后再补发 10 级」是否已经发过。
     *
     * <p>觉醒时（天才资质）已经到 10 级；剩下的 10 级必须等玩家真的吸收第一枚魂环之后才发，
     * 否则会跳过"吸环带来的属性加成"那一段成长（见 {@code TianshiWuhun}）。</p>
     *
     * <p>存 NBT：转生后新 cap 是 {@code false}，重修再走一遍 —— 期望行为。</p>
     */
    private boolean tianshiRingBonus = false;

    /**
     * 天使武魂是不是<b>在 0 级觉醒仪式里</b>得到的。
     *
     * <p>这一条决定了「吸第一枚魂环补 10 级」到底给不给：
     * <ul>
     *   <li>0 级觉醒仪式抽到天使 → 天才资质先给到 10 级，吸第一环再补到 20，{@code true}；</li>
     *   <li>玩家已经觉醒过别的武魂、后来吃“天使武魂精华”/命令拿到天使 → 只多一个武魂，
     *       <b>不补任何等级</b>，这里永远是 {@code false}。</li>
     * </ul>
     * 旧实现只看「身上有没有魂环」，于是已成型的老玩家一吸环就白拿 10 级（BUG）。</p>
     *
     * <p>存 NBT：转生后新 cap 是 {@code false}，重修再走一遍 —— 期望行为。</p>
     */
    private boolean tianshiStartAwaken = false;

    /**
     * 天使之翼（天使武魂第二魂技）是否展开。
     *
     * <p>为什么不像武魂真身那样放 {@code persistentData}：它会影响
     * {@code CombatEventHandler} 的「飞行时伤害削减」判定，而那个事件在<b>客户端也会跑</b>，
     * 客户端读不到 persistentData → 只能放 capability，靠 {@code SPacketPlayerAttribute} 同步。</p>
     */
    private boolean angelWingsActive = false;

    private int damageDisplayMode = 0;
    /** 魂环开启动画样式：0 逐环展开 / 1 天降落位 / 2 魂环升腾。 */
    private int hunhuanOpenAnim = 0;
    /** 飞行速度档位：0  slowest ~ {@link FlySpeedTuning#MAX_LEVEL} 最快。 */
    private int flySpeedLevel = 0;
    private int attributePoints = 0;
    /** 已发放点数对应的等级，老存档补发用，避免重复发。 */
    private int pointsGrantedLevel = -1;
    /**
     * 13 条可加点属性各自已加的点数，下标 = {@link AttributePointSpec#ordinal()}。
     * 长度固定，序列化时按长度写，读档时按当前枚举长度截断/补 0（加属性不用改存档格式）。
     */
    private int[] allocatedPoints = new int[AttributePointSpec.values().length];

    /**
     * 配置面板 {@code configFlags} 的默认值（位掩码，下标见 {@code ConfigScreen} 的 {@code ConfigItemData}）。
     *
     * <p>在原 {@code 0xFFL}（index 1~7 全开、8 以后全关）的基础上，额外把两项置为默认开启：
     * <b>index 11「NPC魂环显示」</b>、<b>index 13「技能特效优化」</b>。
     * 其余 index 8~10、12 保持原状不动。</p>
     *
     * <p>⚠️ index 13 的语义是<b>反向</b>的：<b>开启 = 用粒子特效</b>（默认，即既有表现），
     * <b>关闭 = 用着色器渲染</b>。所以它默认必须是 1，否则老玩家一进游戏地阵就变成着色器版了。</p>
     */
    private static final long DEFAULT_CONFIG_FLAGS =
            0xFFL          // index 1~7 历史默认全开
                    | (1L << 11)   // index 11 NPC魂环显示   ← 默认开启
                    // ⚠️ index 13「技能特效优化」**默认关闭** = 默认走着色器表现。
                    //    这一位语义是反向的（开 = 粒子、关 = 着色器），
                    //    默认开着等于"新玩家第一次进游戏看到的还是老粒子"，
                    //    所以这里不给它默认位 —— 想要粒子的玩家自己去面板打开。
                    | (1L << 14)   // index 14 神环渐变     ← 默认开启
                    | (1L << 15)   // index 15 普通魂环显示 ← 默认开启
                    | (1L << 16);  // index 16 交易自动取币 ← 默认开启（见 BankAccount#CONFIG_AUTO_TRADE）

    private long configFlags = DEFAULT_CONFIG_FLAGS;

    /**
     * {@code configFlags} 的结构版本，用于配置位迁移。
     *
     * <p>加一个新的配置位、且这个位需要"老存档补成某个默认值"时把版本号 +1，
     * 并在 {@link #deserializeNBT} 里加一段对应的补位逻辑。
     * 不这么做的话，老存档那一位是 0，会被当成"玩家主动关掉了"。</p>
     *
     * <ul>
     *   <li>v1 —— 补 index 13「技能特效优化」（该位语义反向：开启 = 粒子、关闭 = 着色器）
     *       <b>已被 v4 取代，代码里的 v1 补位段已删</b></li>
     *   <li>v2 —— 补 index 14「神环渐变」+ index 15「普通魂环显示」</li>
     *   <li>v3 —— 补 index 16「交易自动取币」（不在配置面板里，开关在商人界面右侧）</li>
     *   <li>v4 —— index 13 的默认值<b>整体翻面</b>：默认改为着色器，所以把老存档这一位清 0</li>
     * </ul>
     */
    private static final int CONFIG_FLAGS_VERSION = 4;

    private int zhuanshengshu = 0;

    // ==================== 昆仑银行（个人金库） ====================

    /**
     * 银行里存着的金 / 银 / 铜魂币数量，各自的硬上限 {@code BankAccount.MAX_STORED}（2000 万）。
     *
     * <p><b>为什么存在玩家 capability 而不是方块的 BlockEntity</b>：
     * 银行是「<b>个人账户</b>」—— 结构生成的每一座银行都只是同一个账户的入口，
     * 玩家换一座银行不用"搬家"。同时它随玩家 NBT 走，所以<b>死亡不掉钱</b>。</p>
     *
     * <p><b>为什么转生要显式继承</b>：转生的机制是
     * {@code oldCap.deserializeNBT(newCap.serializeNBT())}，新 cap 里这三个字段是 0，
     * 会把旧值覆盖掉。{@code PlayerHunhuanAPI#zhuansheng} 里已手动拷回，
     * 否则玩家一转生家产就全没了。</p>
     *
     * <p>用 {@code int}：2000 万远小于 {@code Integer.MAX_VALUE}，够用且省存档体积。</p>
     */
    private int bankGold = 0;
    private int bankSilver = 0;
    private int bankCopper = 0;

    /**
     * 当日汇率的<b>显示缓存</b>（1 金 = ? 银 / 1 银 = ? 铜）。
     *
     * <p>汇率本身是 `BankRates` 里由「汇率日序号」算出来的纯函数，不需要存。
     * 这里存一份只是为了<b>让客户端能显示</b>：服务端在「开面板 / 换完钱」时按当前时间写一次，
     * 随 {@code SPacketPlayerAttribute} 一起同步过去。客户端显示时若发现是 0（老存档/没同步过），
     * 会退回用本地 {@code level.getDayTime()} 自己算。</p>
     */
    private int bankRateSilverPerGold = 0;
    private int bankRateCopperPerSilver = 0;

    /**
     * 每日换钱次数：{@link #bankTradeDay} 是这些计数所属的「汇率日」，key = 交易方向名（如 {@code GOLD_TO_SILVER}）。
     *
     * <p>跨日由 `BankExchange` 调 {@link #resetBankTradeCounts()} 清零 —— 这里只负责存，
     * 不自己判断时间（capability 不该知道世界时间）。</p>
     *
     * <p>用 Map 而不是 int[]：以后加交易方向不用改存档结构，旧存档缺的 key 读出来就是 0。</p>
     */
    private long bankTradeDay = Long.MIN_VALUE;
    private final Map<String, Integer> bankTradeCounts = new HashMap<>();

    private final ItemStackHandler hunguInventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
        }
    };

    public boolean debugIgnoreTianfu = false;
    public boolean debugForceSuccess = false;

    private final Map<String, Float> boneOnlyStats = new HashMap<>();
    public Map<String, Float> getBoneOnlyStats() { return boneOnlyStats; }

    private final Map<String, Float> wuhunPermanentStats = new HashMap<>();
    public Map<String, Float> getWuhunPermanentStats() { return wuhunPermanentStats; }


    public PlayerAttributeCapability(){
        super();
        this.hunhuankuaiguan = 0;

        this.assignedExams = new String[10];
        this.assignedTargets = new String[10];
        this.assignedCounts = new int[10];
        this.assignedTypes = new String[10];
        this.godName = ""; // 默认为空
    }

    public PlayerAttributeCapability(int hunhuankuaiguan) {
        this.hunhuankuaiguan = hunhuankuaiguan;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Initialized", initialized);
        tag.putFloat("Shengming", shengming);
        tag.putFloat("MaxShengming", maxshengming);
        tag.putFloat("Jingshenli", jingshenli);
        tag.putFloat("Maxjingshenli", maxjingshenli);
        tag.putFloat("Mingzhong", mingzhong);
        tag.putFloat("Fangyu", fangyu);
        tag.putFloat("Gongji", gongji);
        tag.putFloat("Baojilv", baojilv);
        tag.putFloat("Baojishanghai", baojishanghai);
        tag.putFloat("Xixue", xixue);
        tag.putFloat("Shanbi", shanbi);
        tag.putFloat("Kangbao", kangbao);
        tag.putFloat("Jingyan", jingyan);
        tag.putFloat("Wuchuan", wuchuan);
        tag.putInt("Dengji", dengji);
        tag.putFloat("MaxJingyan", maxjingyan);
        tag.putFloat("TupoChenggonglv", tupochenggonglv);
        tag.putFloat("ShengmingHuifu", shengminghuifu);
        tag.putInt("xiulianTime", this.xiulianTime);
        tag.putBoolean("usingAll", this.usingAll);
        tag.put("HunguSlots", hunguInventory.serializeNBT());
        tag.putInt("Zhuanshengshu", zhuanshengshu);
        tag.putInt("Hunhuankuaiguan", hunhuankuaiguan);

        tag.putInt("DamageDisplayMode", damageDisplayMode);
        tag.putInt("HunhuanOpenAnim", hunhuanOpenAnim);
        tag.putInt("FlySpeedLevel", flySpeedLevel);
        tag.putBoolean("GodNianxianBonus", godNianxianBonus);
        tag.putBoolean("TianshiRingBonus", tianshiRingBonus);
        tag.putBoolean("TianshiStartAwaken", tianshiStartAwaken);
        tag.putBoolean("AngelWingsActive", angelWingsActive);
        tag.putInt("AttributePoints", attributePoints);
        tag.putInt("PointsGrantedLevel", pointsGrantedLevel);
        tag.putIntArray("AllocatedPoints", allocatedPoints);
        tag.putLong("configFlags", configFlags);
        tag.putInt("ConfigFlagsVersion", CONFIG_FLAGS_VERSION);

        tag.putInt("UiOffsetY", this.uiOffsetY);

        tag.putFloat("UiScale", this.uiScale);

        // 保存神祇系统数据
        tag.putString("GodName", godName);
        tag.putInt("CurrentGodStage", currentStage);
        tag.putInt("GodTaskProgress", taskProgress);
        tag.putBoolean("IsGod", isGod);

        tag.putInt("XiantianTalent", this.xiantianTalent);

        // 保存爬塔系统数据
        tag.putInt("CurrentTowerFloor", currentTowerFloor);
        tag.putLong("TowerLastActiveTick", towerLastActiveTick);
        tag.putBoolean("IsTowerChallenging", isTowerChallenging);

        // 在 serializeNBT 中增加：
        ListTag taskTag = new ListTag();
        for (int i = 1; i <= 9; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("desc", assignedExams[i] != null ? assignedExams[i] : "");
            entry.putString("target", assignedTargets[i] != null ? assignedTargets[i] : "");
            entry.putInt("count", assignedCounts[i]);
            entry.putString("type", assignedTypes[i] != null ? assignedTypes[i] : "");
            taskTag.add(entry);
        }
        tag.put("GodTasks", taskTag);

        ListTag wuhunOrderTag = new ListTag();
        for (String name : wuhunListsname) {
            wuhunOrderTag.add(StringTag.valueOf(name));
        }
        tag.put("WuhunOrderList", wuhunOrderTag);

        ListTag pendingTag = new ListTag();
        for (String pendingName : pendingWuhunEssences) {
            pendingTag.add(StringTag.valueOf(pendingName));
        }
        tag.put("PendingWuhunEssences", pendingTag);

        for (Map.Entry<String, List<MobAttributeCapability>> entry : monsterCapabilityLists.entrySet()) {
            String wuhunName = entry.getKey();
            tag.putBoolean("iswuhun" + wuhunName, true);
            int ringIndex = 0;
            for (MobAttributeCapability ringCap : entry.getValue()) {
                tag.put(wuhunName + ringIndex, ringCap.serializeNBT());
                ringIndex++;
            }
        }
        CompoundTag boneTag = new CompoundTag();
        for (Map.Entry<String, Float> entry : boneOnlyStats.entrySet()) {
            boneTag.putFloat(entry.getKey(), entry.getValue());
        }
        tag.put("BoneOnlyStats", boneTag);

        CompoundTag permanentTag = new CompoundTag();
        for (Map.Entry<String, Float> entry : wuhunPermanentStats.entrySet()) {
            permanentTag.putFloat(entry.getKey(), entry.getValue());
        }
        tag.put("WuhunPermanentStats", permanentTag);

        CompoundTag allSkillsTag = new CompoundTag();
        for (Map.Entry<String, BaseSkillItem[]> entry : wuhunSkillsMap.entrySet()) {
            CompoundTag singleWuhunTag = new CompoundTag();
            BaseSkillItem[] skills = entry.getValue();
            for (int i = 0; i < 9; i++) {
                if (skills[i] != null) {
                    singleWuhunTag.putString("Skill_" + i, ForgeRegistries.ITEMS.getKey(skills[i]).toString());
                }
            }
            allSkillsTag.put(entry.getKey(), singleWuhunTag);
        }
        tag.put("WuhunSkillsData", allSkillsTag);

        // 银行余额（同时用于客户端面板显示：SPacketPlayerAttribute 传的就是这份 NBT）
        tag.putInt("BankGold", bankGold);
        tag.putInt("BankSilver", bankSilver);
        tag.putInt("BankCopper", bankCopper);
        tag.putInt("BankRateSilver", bankRateSilverPerGold);
        tag.putInt("BankRateCopper", bankRateCopperPerSilver);

        tag.putLong("BankTradeDay", bankTradeDay);
        CompoundTag tradeCounts = new CompoundTag();
        bankTradeCounts.forEach(tradeCounts::putInt);
        tag.put("BankTradeCounts", tradeCounts);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.initialized = nbt.getBoolean("Initialized");
        this.shengming = nbt.getFloat("Shengming");
        this.maxshengming = nbt.getFloat("MaxShengming");
        this.jingshenli = nbt.getFloat("Jingshenli");
        this.maxjingshenli = nbt.getFloat("Maxjingshenli");
        this.mingzhong = nbt.getFloat("Mingzhong");
        this.fangyu = nbt.getFloat("Fangyu");
        this.gongji = nbt.getFloat("Gongji");
        this.baojilv = nbt.getFloat("Baojilv");
        this.baojishanghai = nbt.getFloat("Baojishanghai");
        this.xixue = nbt.getFloat("Xixue");
        this.shanbi = nbt.getFloat("Shanbi");
        this.wuchuan = nbt.getFloat("Wuchuan");
        this.kangbao = nbt.getFloat("Kangbao");
        this.jingyan = nbt.getFloat("Jingyan");
        this.dengji = nbt.getInt("Dengji");
        this.maxjingyan = nbt.getFloat("MaxJingyan");
        this.tupochenggonglv = nbt.getFloat("TupoChenggonglv");
        this.shengminghuifu = nbt.getFloat("ShengmingHuifu");
        this.zhuanshengshu = nbt.getInt("Zhuanshengshu");
        this.xiulianTime = nbt.getInt("xiulianTime");
        this.usingAll = nbt.getBoolean("usingAll");
        this.hunhuankuaiguan = nbt.getInt("Hunhuankuaiguan");

        this.damageDisplayMode = nbt.getInt("DamageDisplayMode");
        setHunhuanOpenAnim(nbt.getInt("HunhuanOpenAnim"));
        setFlySpeedLevel(nbt.getInt("FlySpeedLevel"));
        this.godNianxianBonus = nbt.getBoolean("GodNianxianBonus");
        this.tianshiRingBonus = nbt.getBoolean("TianshiRingBonus");
        this.tianshiStartAwaken = nbt.getBoolean("TianshiStartAwaken");
        this.angelWingsActive = nbt.getBoolean("AngelWingsActive");
        this.attributePoints = Math.max(0, nbt.getInt("AttributePoints"));
        this.pointsGrantedLevel = nbt.contains("PointsGrantedLevel")
                ? nbt.getInt("PointsGrantedLevel") : Integer.MIN_VALUE;

        // 已加点：按当前枚举长度裁剪 / 补 0，加属性不用改存档格式
        int[] saved = nbt.getIntArray("AllocatedPoints");
        this.allocatedPoints = new int[AttributePointSpec.values().length];
        System.arraycopy(saved, 0, this.allocatedPoints, 0, Math.min(saved.length, this.allocatedPoints.length));

        if (nbt.contains("UiOffsetY")) {
            this.uiOffsetY = nbt.getInt("UiOffsetY");
        }

        if (nbt.contains("configFlags")) {
            this.configFlags = nbt.getLong("configFlags");
        } else {
            // 老存档没有这个字段：用同一个默认值，别让「读档回退路径」与「新号路径」默认值不一致
            this.configFlags = DEFAULT_CONFIG_FLAGS;
        }

        // ── 配置位迁移 ──────────────────────────────────────────────
        // （v1 曾经把 index 13 补成 1「默认粒子」，v4 已经把这个默认值整体翻面成
        //   「默认着色器」并把它清 0 —— 所以 v1 那段补位是多余的，删掉免得误导。）
        // v2：index 14「神环渐变」、index 15「普通魂环显示」—— 都是后加的位，默认开。
        //     同样只对旧存档补，玩家自己关掉后重进不会被强行打开。
        if (nbt.getInt("ConfigFlagsVersion") < 2) {
            this.configFlags |= (1L << 14);
            this.configFlags |= (1L << 15);
        }
        // v3：index 16「交易自动取币」—— 同样是后加的位，默认开。
        if (nbt.getInt("ConfigFlagsVersion") < 3) {
            this.configFlags |= (1L << 16);
        }
        // v4：index 13 的默认值整体翻面 —— 现在「默认 = 着色器」，所以把这一位清掉。
        //     老存档里它基本是 1（= 粒子），不清的话升级后还是老表现，玩家会以为新特效没做。
        //     只跑一次；之后再被玩家手动打开（切回粒子）就不会被这条覆盖了。
        if (nbt.getInt("ConfigFlagsVersion") < 4) {
            this.configFlags &= ~(1L << 13);
        }

        // 读取神祇系统数据
        this.godName = nbt.getString("GodName");
        this.currentStage = nbt.getInt("CurrentGodStage");
        this.taskProgress = nbt.getInt("GodTaskProgress");
        this.isGod = nbt.getBoolean("IsGod");

        if (nbt.contains("XiantianTalent")) {
            this.xiantianTalent = nbt.getInt("XiantianTalent");
        }

        // 读取爬塔系统数据
        this.currentTowerFloor = nbt.getInt("CurrentTowerFloor");
        this.towerLastActiveTick = nbt.getLong("TowerLastActiveTick");
        this.isTowerChallenging = nbt.getBoolean("IsTowerChallenging");

        // 在 deserializeNBT 中增加：
        if (nbt.contains("GodTasks")) {
            ListTag taskTag = nbt.getList("GodTasks", 10);
            for (int i = 0; i < taskTag.size() && i < 9; i++) {
                CompoundTag entry = taskTag.getCompound(i);
                assignedExams[i+1] = entry.getString("desc");
                assignedTargets[i+1] = entry.getString("target");
                assignedCounts[i+1] = entry.getInt("count");
                assignedTypes[i+1] = entry.getString("type");
            }
        }

        if (nbt.contains("UiScale")) {
            this.uiScale = nbt.getFloat("UiScale");
        } else {
            this.uiScale = 1.0f;
        }

        if (nbt.contains("HunguSlots")) {
            hunguInventory.deserializeNBT(nbt.getCompound("HunguSlots"));
        }

        if (nbt.contains("WuhunOrderList") || (!wuhunListsnameall.isEmpty() && nbt.contains("iswuhun" + wuhunListsnameall.get(0)))) {
            List<String> tempNames = new ArrayList<>();
            Map<String, List<MobAttributeCapability>> tempMonsters = new HashMap<>();
            if (nbt.contains("WuhunOrderList")) {
                ListTag orderList = nbt.getList("WuhunOrderList", 8);
                for (int i = 0; i < orderList.size(); i++) {
                    String name = orderList.getString(i);
                    tempNames.add(name);
                }
            }
        }
        if (nbt.contains("WuhunSkillsData")) {
            CompoundTag allSkillsTag = nbt.getCompound("WuhunSkillsData");
            Map<String, BaseSkillItem[]> tempSkillsMap = new HashMap<>(); // 临时 Map
            for (String wuhunName : allSkillsTag.getAllKeys()) {
                CompoundTag singleWuhunTag = allSkillsTag.getCompound(wuhunName);
                BaseSkillItem[] skills = new BaseSkillItem[9];
                for (int i = 0; i < 9; i++) {
                    if (singleWuhunTag.contains("Skill_" + i)) {
                        String registryName = singleWuhunTag.getString("Skill_" + i);
                        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(registryName));
                        if (item instanceof BaseSkillItem baseSkill) {
                            skills[i] = baseSkill;
                        }
                    }
                }
                tempSkillsMap.put(wuhunName, skills);
            }
            // ⚠️ 必须**无条件赋值**（含空表）。原来写的是 `if (!tempSkillsMap.isEmpty())`，
            //    于是"新存档序列化出来的空表"不会覆盖旧表 —— 转生走的就是这条路
            //    （newCap 是全新对象 → 空表 → oldCap.deserializeNBT 时被跳过），
            //    结果旧武魂技能残留，玩家重新觉醒同名武魂直接白得技能。
            this.wuhunSkillsMap = tempSkillsMap;
        }
        this.monsterCapabilityLists.clear();
        this.wuhunListsname.clear();
        this.pendingWuhunEssences.clear();
        if (nbt.contains("PendingWuhunEssences")) {
            ListTag pendingTag = nbt.getList("PendingWuhunEssences", 8);
            for (int i = 0; i < pendingTag.size(); i++) {
                String pendingName = pendingTag.getString(i);
                if (!pendingName.isEmpty() && !this.pendingWuhunEssences.contains(pendingName)) {
                    this.pendingWuhunEssences.add(pendingName);
                }
            }
        }
        if (nbt.contains("WuhunOrderList")) {
            ListTag orderList = nbt.getList("WuhunOrderList", 8);
            for (int i = 0; i < orderList.size(); i++) {
                loadWuhunData(nbt, orderList.getString(i));
            }
        }

        if (nbt.contains("BoneOnlyStats")) {
            CompoundTag boneTag = nbt.getCompound("BoneOnlyStats");
            this.boneOnlyStats.clear();
            for (String key : boneTag.getAllKeys()) {
                this.boneOnlyStats.put(key, boneTag.getFloat(key));
            }
        }

        // 旧存档没有这个 tag，wuhunPermanentStats 保持空即可（不会白给属性）
        if (nbt.contains("WuhunPermanentStats")) {
            CompoundTag permanentTag = nbt.getCompound("WuhunPermanentStats");
            this.wuhunPermanentStats.clear();
            for (String key : permanentTag.getAllKeys()) {
                float v = permanentTag.getFloat(key);
                // ⚠️ 读档做一次"净化"：非正数/非有限值直接丢弃，
                // 防止存档被外部改坏后导致属性异常膨胀
                if (Float.isFinite(v) && v > 0f) {
                    this.wuhunPermanentStats.put(key, v);
                }
            }
        }

        // 银行余额：老存档没有这几个 tag → 保持 0（不是"清空"，新对象本来就是 0）
        this.bankGold = Math.max(0, nbt.getInt("BankGold"));
        this.bankSilver = Math.max(0, nbt.getInt("BankSilver"));
        this.bankCopper = Math.max(0, nbt.getInt("BankCopper"));
        this.bankRateSilverPerGold = Math.max(0, nbt.getInt("BankRateSilver"));
        this.bankRateCopperPerSilver = Math.max(0, nbt.getInt("BankRateCopper"));

        // 每日换钱次数：老存档没有这两个 tag → day 为 MIN_VALUE，等第一次换钱时再初始化
        this.bankTradeDay = nbt.contains("BankTradeDay") ? nbt.getLong("BankTradeDay") : Long.MIN_VALUE;
        this.bankTradeCounts.clear();
        if (nbt.contains("BankTradeCounts")) {
            CompoundTag counts = nbt.getCompound("BankTradeCounts");
            for (String key : counts.getAllKeys()) {
                this.bankTradeCounts.put(key, Math.max(0, counts.getInt(key)));
            }
        }
    }

    private void loadWuhunData(CompoundTag nbt, String name) {
        ArrayList<MobAttributeCapability> list = new ArrayList<>();
        int index = 0;
        while (nbt.contains(name + index)) {
            MobAttributeCapability ringCap = new MobAttributeCapability();
            ringCap.deserializeNBT(nbt.getCompound(name + index));
            list.add(ringCap);
            index++;
        }
        this.monsterCapabilityLists.put(name, list);
        if (!this.wuhunListsname.contains(name)) {
            this.wuhunListsname.add(name);
        }
    }

    public void setWuhunSkill(String wuhunName, int slot, BaseSkillItem skill) {
        BaseSkillItem[] skills = wuhunSkillsMap.computeIfAbsent(wuhunName, k -> new BaseSkillItem[9]);
        if (slot >= 0 && slot < 9) {
            skills[slot] = skill;
        }
    }

    public String getGodName() { return godName; }

    public void startGodExam(String godId) {
        this.godName = godId;
        this.currentStage = 1;
        this.taskProgress = 0;
        this.isGod = false;
    }

    public int getCurrentStage() { return currentStage; }

    public void nextGodStage() {
        if (this.currentStage < 9) {
            this.currentStage++;
            this.taskProgress = 0;
        } else {
            this.isGod = true;
        }
    }

    public int getCurrentTowerFloor() { return this.currentTowerFloor; }
    public void setCurrentTowerFloor(int floor) { this.currentTowerFloor = Math.max(0, floor); }

    public long getTowerLastActiveTick() { return this.towerLastActiveTick; }
    public void setTowerLastActiveTick(long tick) { this.towerLastActiveTick = tick; }

    public boolean isTowerChallenging() { return this.isTowerChallenging; }
    public void setTowerChallenging(boolean challenging) { this.isTowerChallenging = challenging; }

    public int getGodTaskProgress() { return taskProgress; }
    public void setGodTaskProgress(int progress) { this.taskProgress = progress; }
    public void addGodTaskProgress(int amount) { this.taskProgress += amount; }

    public boolean isGod() { return isGod; }

    public boolean isConfigOpen(int index) {
        return ((configFlags >> index) & 1) == 1;
    }

    public void toggleConfig(int index) {
        this.configFlags ^= (1L << index);
    }

    public void refreshBoneAttributes(Player player) {
        collectBoneAttributes(this);
        SynsAPI.synsPlayerAttribute(player);
    }


    // 当玩家开启神考时调用（比如右键祭坛或神的瞥视）
    public void initializeGodExam(Player player, String godId) {
        // 天使武魂只认天使神：其它神位一律拒掉（规则见 TianshiWuhun#isAllowedGod）
        if (!TianshiWuhun.isAllowedGod(this, godId)) {
            player.sendSystemMessage(Component.literal("§c【天使武魂】你只遵循天使神的召唤，无法接受其它神位。")
                    .withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }
        forceInitializeGodExam(player, godId);
    }

    /**
     * <b>跳过「天使只认天使神」的校验</b>，直接写入神考数据。
     *
     * <p>只给 debug / 管理命令用（{@code /shenwei}、{@code GodExamDebug}），
     * 玩法路径请一律调 {@link #initializeGodExam}，否则天使玩家能拿到别的神位。</p>
     */
    public void forceInitializeGodExam(Player player, String godId) {
        GodInfo info = GodRegistry.GODS.get(godId);
        if (info == null) return;
        this.godName = godId;
        this.currentStage = 1;
        this.taskProgress = 0;
        RandomSource random = player.level().random;
        for (int i = 1; i <= 9; i++) {
            GodTask task = info.getRandomTask(i, random);
            if (task != null) {
                this.assignedExams[i] = task.description;
                this.assignedTargets[i] = task.targetId;
                this.assignedCounts[i] = task.requiredCount;
                this.assignedTypes[i] = task.type.name();
            }
        }
    }

    public void checkTaskCompletion(Player player) {
        GodInfo info = GodRegistry.GODS.get(this.godName);
        if (info == null) return;

        // 成就：通过第 1 考 / 第 5 考（必须在推进阶段之前取 stage）
        AchievementAPI.onGodExamStagePassed(player, this.currentStage);

        info.executeRewards(this.currentStage, player);
        this.nextGodStage();

        if (this.isGod()) {
            player.sendSystemMessage(Component.literal("§6恭喜你，成就" + info.name + "之位！"));
        }
    }

    public String[] getAssignedExams() {
        return this.assignedExams;
    }

    public String[] getAssignedTargets() {
        return this.assignedTargets;
    }

    public int[] getAssignedCounts() {
        return this.assignedCounts;
    }

    public String[] getAssignedTypes() {
        return this.assignedTypes;
    }

    public boolean hasActiveTask() {
        return !godName.isEmpty() && currentStage >= 1 && currentStage <= 9;
    }

    /**
     * 按 key 从魂骨属性里取值。玩家和 <b>NPC</b> 共用（NPC 的魂骨槽也是本类的
     * {@code hunguInventory}），两边口径必须一致，别各写一份switch。
     */
    public static float getAttrValueByKey(ItemAttributeCapability attr, String key) {
        return switch (key) {
            case "gongji" -> attr.getGongji();
            case "fangyu" -> attr.getFangyu();
            case "maxshengming" -> attr.getMaxshengming();
            case "baojilv" -> attr.getBaojilv();
            case "baojishanghai" -> attr.getBaojishanghai();
            case "xixue" -> attr.getXixue();
            case "shanbi" -> attr.getShanbi();
            case "mingzhong" -> attr.getMingzhong();
            case "wuchuan" -> attr.getWuchuan();
            case "kangbao" -> attr.getKangbao();
            case "shengminghuifu" -> attr.getShengminghuifu();
            default -> 0f;
        };
    }

    /**
     * 遍历 7 个魂骨槽，把词条属性累加进 {@code boneOnlyStats}。<b>玩家与 NPC 共用</b>。
     *
     * <p>只做"清空 + 遍历累加"，<b>不负责发同步包</b>——玩家侧的
     * {@link #refreshBoneAttributes(Player)} 会在后面补发，NPC 不需要发包。</p>
     */
    public static void collectBoneAttributes(PlayerAttributeCapability cap) {
        cap.boneOnlyStats.clear();
        for (int i = 0; i < 7; i++) {
            ItemStack stack = cap.hunguInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                for (String key : attr.getActiveAttributes()) {
                    float val = getAttrValueByKey(attr, key);
                    if (val > 0) {
                        cap.boneOnlyStats.merge(key, val, Float::sum);
                    }
                }
            });
        }
    }

    public ItemStackHandler getHunguInventory() {
        return hunguInventory;
    }


    public void setForcedTalent(String talent) { this.forcedTalent = talent; }
    public String getForcedTalent() { return this.forcedTalent; }

    public void setXiantianTalent(int talent) { this.xiantianTalent = talent; }
    public int getXiantianTalent() { return this.xiantianTalent; }

    /** 天使武魂「吸第一枚魂环补发 10 级」是否已发过（见 {@code TianshiWuhun}）。 */
    public boolean isTianshiRingBonusGiven() { return tianshiRingBonus; }
    public void setTianshiRingBonusGiven(boolean given) { this.tianshiRingBonus = given; }

    /** 天使武魂是不是在 0 级觉醒仪式里得到的（决定那 10 级补不补，见 {@code TianshiWuhun}）。 */
    public boolean isTianshiStartAwaken() { return tianshiStartAwaken; }
    public void setTianshiStartAwaken(boolean start) { this.tianshiStartAwaken = start; }

    /** 天使之翼（第二魂技）是否展开（见 {@code AngelWings}）。 */
    public boolean isAngelWingsActive() { return angelWingsActive; }
    public void setAngelWingsActive(boolean active) { this.angelWingsActive = active; }

    public int getXiulianTime() {
        return this.xiulianTime;
    }
    public void setXiulianTime(int time) {
        this.xiulianTime = Math.max(0, Math.min(600, time));
    }

    public boolean isUsingAll() {
        return this.usingAll;
    }
    public void setUsingAll(boolean usingAll) {
        this.usingAll = usingAll;
    }

    public int getHunhuankuaiguan() {
        return hunhuankuaiguan;
    }

    public void setHunhuankuaiguan(int hunhuankuaiguan) {
        this.hunhuankuaiguan = hunhuankuaiguan;
    }

    public List<MobAttributeCapability> getWuhunList() {
        if(wuhunListsname.size()-1<hunhuankuaiguan||hunhuankuaiguan<0)return null;
        return monsterCapabilityLists.get(wuhunListsname.get(hunhuankuaiguan));
    }

    public int getDamageDisplayMode() { return damageDisplayMode; }
    public void setDamageDisplayMode(int mode) { this.damageDisplayMode = mode; }

    /** 魂环开启动画的样式数量：0 逐环展开 / 1 天降落位 / 2 魂环升腾。 */
    public static final int HUNHUAN_ANIM_COUNT = 3;

    public int getHunhuanOpenAnim() { return hunhuanOpenAnim; }

    public void setHunhuanOpenAnim(int value) {
        // 越界直接夹回合法区间，避免旧存档/异常包把渲染分支带到 default
        this.hunhuanOpenAnim = Math.max(0, Math.min(HUNHUAN_ANIM_COUNT - 1, value));
    }

    /** 飞行速度档位（0 ~ {@link FlySpeedTuning#MAX_LEVEL}）。 */
    public int getFlySpeedLevel() { return flySpeedLevel; }

    /** 「神考全过后全部魂环 +5 万年」是否已发放（幂等标记，见 {@code HunhuanNianxianUpgrader}）。 */
    public boolean isGodNianxianBonus() { return godNianxianBonus; }

    public void setGodNianxianBonus(boolean value) { this.godNianxianBonus = value; }

    /** 调整飞行档位，自动夹回合法区间。 */
    public void setFlySpeedLevel(int value) {
        this.flySpeedLevel = Math.max(0, Math.min(FlySpeedTuning.MAX_LEVEL, value));
    }

    /** 相对当前档位增减飞行速度（技能面板的 ± 按钮走这里）。 */
    public void adjustFlySpeedLevel(int delta) {
        setFlySpeedLevel(flySpeedLevel + delta);
    }

    public int getAttributePoints() { return attributePoints; }

    public void setAttributePoints(int v) { this.attributePoints = Math.max(0, v); }

    public void addAttributePoints(int v) { setAttributePoints(attributePoints + v); }

    public int getPointsGrantedLevel() { return pointsGrantedLevel; }

    public void setPointsGrantedLevel(int v) { this.pointsGrantedLevel = v; }

    // ==================== 属性加点 ====================

    /** 某条属性已加的点数。 */
    public int getAllocatedPoints(AttributePointSpec spec) {
        return allocatedPoints[spec.ordinal()];
    }

    /**
     * 写入某条属性的点数（已clamp 到 {@link AttributePointSpec#MAX_POINTS}）。
     * <b>只能由服务端调用</b>，客户端只是显示。
     */
    public void setAllocatedPoints(AttributePointSpec spec, int value) {
        allocatedPoints[spec.ordinal()] = Math.max(0, Math.min(AttributePointSpec.MAX_POINTS, value));
    }

    /** 13 条属性已加点之和。 */
    public int getAllocatedPointsTotal() {
        int sum = 0;
        for (int v : allocatedPoints) sum += v;
        return sum;
    }

    /** 13 条全部清零（重置按钮用），返回被清掉的点数总数。 */
    public int clearAllocatedPoints() {
        int cleared = getAllocatedPointsTotal();
        for (AttributePointSpec spec : AttributePointSpec.values()) {
            allocatedPoints[spec.ordinal()] = 0;
        }
        return cleared;
    }

    public int getZhuanshengshu() {return zhuanshengshu;}
    public void setZhuanshengshu(int zhuanshengshu) {
        this.zhuanshengshu = zhuanshengshu;
    }

    // ==================== 银行余额读写 ====================

    public int getBankGold() { return bankGold; }
    public int getBankSilver() { return bankSilver; }
    public int getBankCopper() { return bankCopper; }

    /**
     * 直接写入余额（内部用，<b>不做上限/负数校验</b>）。
     *
     * <p>业务逻辑一律走 {@code BankAccount}，那里会钳制到 0 ~ {@code MAX_STORED}。</p>
     */
    public void setBankGold(int v) { this.bankGold = Math.max(0, v); }
    public void setBankSilver(int v) { this.bankSilver = Math.max(0, v); }
    public void setBankCopper(int v) { this.bankCopper = Math.max(0, v); }

    /** 汇率显示缓存（服务端写，客户端读）。 */
    public int getBankRateSilverPerGold() { return bankRateSilverPerGold; }
    public void setBankRateSilverPerGold(int v) { this.bankRateSilverPerGold = Math.max(0, v); }
    public int getBankRateCopperPerSilver() { return bankRateCopperPerSilver; }
    public void setBankRateCopperPerSilver(int v) { this.bankRateCopperPerSilver = Math.max(0, v); }

    // ---------- 每日换钱次数 ----------

    public long getBankTradeDay() { return bankTradeDay; }
    public void setBankTradeDay(long day) { this.bankTradeDay = day; }

    /** 某个交易方向今天已经换了几次（没换过 → 0）。 */
    public int getBankTradeCount(String tradeKey) {
        return bankTradeCounts.getOrDefault(tradeKey, 0);
    }

    public void setBankTradeCount(String tradeKey, int count) {
        bankTradeCounts.put(tradeKey, Math.max(0, count));
    }

    /** 跨日清零（由 `BankExchange` 在日期变化时调用）。 */
    public void resetBankTradeCounts() {
        this.bankTradeCounts.clear();
    }

    public float getShengming() { return shengming; }
    public void setShengming(float shengming) { this.shengming = shengming; }

    public float getJingshenli() {
        return jingshenli;
    }

    public void setJingshenli(float jingshenli) { this.jingshenli = jingshenli; }

    public float getMaxjingshenli() { return maxjingshenli; }
    public void setMaxjingshenli(float maxjingshenli) { this.maxjingshenli = maxjingshenli; }

    public float getMingzhong() { return mingzhong; }
    public void setMingzhong(float mingzhong) { this.mingzhong = mingzhong; }

    public float getShengmingHuifu() { return shengminghuifu; }
    public void setShengmingHuifu(float shengminghuifu) { this.shengminghuifu = shengminghuifu; }

    public float getMaxshengming() { return maxshengming; }
    public void setMaxshengming(float maxshengming) { this.maxshengming = maxshengming; }

    public float getFangyu() { return fangyu; }
    public void setFangyu(float fangyu) { this.fangyu = fangyu; }

    public float getWuchuan() { return wuchuan; }
    public void setWuchuan(float wuchuan) { this.wuchuan = wuchuan; }

    public float getGongji() { return gongji; }
    public void setGongji(float gongji) { this.gongji = gongji; }

    public float getBaojilv() { return baojilv; }
    public void setBaojilv(float baojilv) { this.baojilv = baojilv; }

    public long getConfigFlags() { return configFlags; }
    public void setConfigFlags(long flags) { this.configFlags = flags; }

    public float getBaojishanghai() { return baojishanghai; }

    public void setBaojishanghai(float baojishanghai) {
        this.baojishanghai = baojishanghai;
    }

    public float getXixue() { return xixue; }
    public void setXixue(float xixue) { this.xixue = xixue; }

    public float getShanbi() { return shanbi; }
    public void setShanbi(float shanbi) { this.shanbi = shanbi; }

    public float getKangbao() { return kangbao; }
    public void setKangbao(float kangbao) { this.kangbao = kangbao; }

    public float getJingyan() { return jingyan; }
    public void setJingyan(float jingyan) { this.jingyan = jingyan; }

    public int getDengji() { return dengji; }
    public void setDengji(int dengji) { this.dengji = dengji; }

    public float getMaxjingyan() { return maxjingyan; }
    public void setMaxjingyan(float maxjingyan) { this.maxjingyan = maxjingyan; }
    
    public float getTupochenggonglv() { return tupochenggonglv; }
    public void setTupochenggonglv(float tupochenggonglv) { this.tupochenggonglv = tupochenggonglv; }
    
    public boolean isInitialized() { return initialized; }
    public void setInitialized(boolean initialized) { this.initialized = initialized; }

    public int getUiOffsetY() {
        return this.uiOffsetY;
    }

    public void setUiOffsetY(int value) {
        this.uiOffsetY = value;
    }

    public void startCasting(BaseSkillItem skill, int slot, int time, boolean protectedCast) {
        this.currentCastingSkill = skill;
        this.castingSlot = slot;
        this.requiredCastTick = time;
        this.castingTick = 0;
        this.castingProtected = protectedCast;
    }

    /**
     * 正在吟唱的技能所在槽位；{@code -1} = 没在吟唱。
     *
     * <p><b>为什么必须记住槽位</b>：吟唱完成时要按「该槽位魂环的年限」算威力 / 消耗倍率，
     * 并把冷却记回该槽位。以前用的是「当前选中槽位」，于是有两个问题：</p>
     * <ul>
     *   <li>吟唱期间动一下选中槽位，这次吟唱就会被判成"武魂变化"直接中断；</li>
     *   <li>批量释放（同一个键绑多个槽位）根本不改选中槽位，于是批量里的吟唱
     *       <b>开局第一 tick 就被判定中断</b>，永远放不出来。</li>
     * </ul>
     */
    public int getCastingSlot() {
        return this.castingSlot;
    }

    /** 本次吟唱是否带抗打断（开局掷中的话整段都打不断）。 */
    public boolean isCastingProtected() {
        return this.castingProtected;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == PlayerAttributeCapabilityProvider.CAPABILITY) {
            return LazyOptional.of(() -> (T) this);
        }
        return LazyOptional.empty();
    }

    public Map<String, BaseSkillItem[]> getWuhunSkillsMap() {
        return wuhunSkillsMap;
    }

    /**
     * 清空「每个武魂当前选中的技能槽」。
     *
     * <p>{@code selectedSkillIndexMap} <b>不写进 NBT</b>（选了第几槽属于运行时手感状态），
     * 所以转生时 {@code newCap → oldCap} 的存档覆盖<b>带不走它</b>，必须单独清。</p>
     */
    public void clearSelectedSkillSlots() {
        this.selectedSkillIndexMap.clear();
    }

    public int getSelectedSkillSlot() {
        String currentWuhun = getWuhunName();
        if (currentWuhun == null) return 0;
        return selectedSkillIndexMap.getOrDefault(currentWuhun, 0);
    }

    public float getUiScale() {
        return this.uiScale;
    }

    public void setUiScale(float scale) {
        // 限制在 0.1 ~ 5.0 之间，防止太小看不到或太大爆屏
        this.uiScale = Math.max(0.1f, Math.min(5.0f, scale));
    }

    public void setSelectedSkillSlot(int slot) {
        String currentWuhun = getWuhunName();
        if (currentWuhun != null && slot >= 0 && slot < 9) {
            selectedSkillIndexMap.put(currentWuhun, slot);
        }
    }

    public static ArrayList<String> wuhunListsnameall= new ArrayList<>();
    static {
        wuhunListsnameall.add(Wuhunname.pohunqiang);
        wuhunListsnameall.add(Wuhunname.bahuangji);
        wuhunListsnameall.add(Wuhunname.panshijuyuan);
        wuhunListsnameall.add(Wuhunname.liejinhu);
        // ⚠️ 天使<b>不</b>在这个池子里：它是「独一无二」武魂，觉醒走独立的 10% 抽签
        //    （见 TianshiWuhun#tryIndependentAwaken + PlayerHunhuanAPI#addWuHun）。
        //    放进来会被平均分配（4 个武魂时 25%），和它的稀有度定位不符。

        Collections.sort(wuhunListsnameall);
    }

    public void resetGodSystem() {
        this.godName = "";           // 移除神位ID
        this.currentStage = 1;       // 回到第一考
        this.taskProgress = 0;       // 进度清零
        this.isGod = false;          // 剥夺神位状态
        this.assignedExams = new String[10];
        this.assignedTargets = new String[10];
        this.assignedCounts = new int[10];
        this.assignedTypes = new String[10];
    }

    public String getWuhunName() {
        if(wuhunListsname.size()-1<hunhuankuaiguan||hunhuankuaiguan<0)return null;
        return wuhunListsname.get(hunhuankuaiguan);
    }

    public BaseSkillItem getCurrentCastingSkill() {
        return this.currentCastingSkill;
    }

    public int getCastingTick() {
        return this.castingTick;
    }

    public void setCastingTick(int tick) {
        this.castingTick = tick;
    }

    public int getRequiredCastTick() {
        return this.requiredCastTick;
    }

    public void stopCasting(Player player) {
        this.currentCastingSkill = null;
        this.castingSlot = -1;
        this.castingTick = 0;
        this.requiredCastTick = 0;
        this.castingProtected = false;

        removeCastingSlowdown(player);
    }

    public void removeCastingSlowdown(Player player) {
        if (player != null) {
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.hasModifier(CASTING_SLOWDOWN_MODIFIER)) {
                speedAttr.removeModifier(CASTING_SLOWDOWN_ID);
            }
        }
    }

    public boolean isBrandNewConfig() {
        return this.gongji == 1.0f && this.maxshengming == 20.0f && this.dengji == 0;
    }

    public void initDefaultAttributes() {
        this.shengming = 20.0f;
        this.maxshengming = 20.0f;
        this.jingshenli = 20.0f;
        this.maxjingshenli = 20.0f;
        this.mingzhong = 1.0f;
        this.fangyu = 1.0f;
        this.gongji = 1.0f;
        this.baojilv = 5.0f;
        this.baojishanghai = 150.0f;
        this.xixue = 1.0f;
        this.shanbi = 1.0f;
        this.kangbao = 1.0f;
        this.jingyan = 0.0f;
        this.dengji = 0;
        this.maxjingyan = 20.0f;
        this.shengminghuifu = 1.0f;
        this.wuchuan = 1.0f;
        this.xiulianTime = 600;
    }

    private Map<String, Long> skillCooldowns = new HashMap<>();

    public long getSkillLastUsedTime(String wuhun, int slot) {
        return skillCooldowns.getOrDefault(wuhun + "_" + slot, 0L);
    }

    public void setSkillLastUsedTime(String wuhun, int slot, long time) {
        skillCooldowns.put(wuhun + "_" + slot, time);
    }

    public static final AttributeModifier CASTING_SLOWDOWN_MODIFIER = new AttributeModifier(CASTING_SLOWDOWN_ID, "Casting skill slowdown", -0.7, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public Map<String, List<MobAttributeCapability>> getMonsterCapabilityLists() {
        return monsterCapabilityLists;
    }

    public List<String> getWuhunListsname() {
        return wuhunListsname;
    }

    /** 已服下、但还没觉醒的武魂精华预定名单（可修改，顺序 = 吃下的顺序）。 */
    public List<String> getPendingWuhunEssences() {
        return pendingWuhunEssences;
    }

    /** 预定一个武魂精华；已存在则忽略。 */
    public void addPendingWuhunEssence(String wuhunName) {
        if (wuhunName != null && !wuhunName.isEmpty() && !pendingWuhunEssences.contains(wuhunName)) {
            pendingWuhunEssences.add(wuhunName);
        }
    }

    /** 觉醒仪式消费掉预定（不管有没有真的觉醒成，都给清了，避免无限滞留）。 */
    public void clearPendingWuhunEssences() {
        pendingWuhunEssences.clear();
    }


}