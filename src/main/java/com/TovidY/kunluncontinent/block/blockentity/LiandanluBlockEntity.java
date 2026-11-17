package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.item.klitem.DanYaoQuality;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class LiandanluBlockEntity extends BlockEntity implements MenuProvider {

    /** 药渣槽在 ItemHandler 中的下标。 */
    public static final int DROSS_SLOT = AlchemicalCalculator.DROSS_SLOT;
    /** 聚魂瓶槽在 ItemHandler 中的下标。 */
    public static final int BOTTLE_SLOT = 18;
    /** 产出槽区间 [5, 16]。 */
    public static final int RESULT_SLOT_START = 5;
    public static final int RESULT_SLOT_END = 16;

    private final int furnaceLevel;
    private int progress = 0;
    private int maxProgress = 200;

    // ==================================================================================
    //  【手动炼丹 · 小游戏状态】全部由服务端持有，客户端只读 ContainerData 渲染
    // ==================================================================================

    /** 自动炼丹开关。false = 手动小游戏模式。 */
    private boolean autoMode = true;

    /** 小游戏阶段：0 = 未开始，1 = 判定中（光标在跑），2 = 已结束（等收丹/已收丹）。 */
    private int manualPhase = PHASE_IDLE;

    /** 光标位置（0 ~ {@link #MANUAL_RANGE}）。 */
    private int cursor = 0;
    private int cursorDir = 1;

    /** 判定区半宽（以光标单位计）。越小越难。 */
    private int zoneHalf = MANUAL_BASE_HALF;

    /** 光标每 tick 移动量。 */
    private int cursorSpeed = MANUAL_BASE_SPEED;

    /** 当前品质：-1 = 还没判定过；0~5 = {@code DanYaoQuality} 的下标。 */
    private int manualQuality = -1;

    /** 判定区中心（0~1000）。以前固定在 500，现在会自己来回漂。 */
    private int zoneCenter = MANUAL_RANGE / 2;
    private int zoneDir = 1;
    private int zoneSpeed = 0;
    /** 判定区随机反向的倒计时（tick）。 */
    private int zoneFlipTimer = 0;

    /** 本炉累计成功次数：每次成功都会让光标更快，掉级也不会退回去。 */
    private int successCount = 0;

    /**
     * 是否进入「仙丹定品」的最后一轮。
     *
     * <p>升到仙丹不再直接收丹，而是<b>再赌一次</b>：这一轮成功才是仙丹，
     * 失败则掉一级（宝丹）并立刻收丹。</p>
     */
    private boolean finalRound = false;

    /** 上一次判定结果：0 = 无，1 = 成功，2 = 失败（客户端用来显示反馈）。 */
    private int lastResult = 0;

    public static final int PHASE_IDLE = 0;
    public static final int PHASE_RUNNING = 1;
    public static final int PHASE_DONE = 2;

    /** 光标活动范围（归一化 0~1000，避免用浮点同步）。 */
    public static final int MANUAL_RANGE = 1000;

    /**
     * 初始判定区半宽：140/1000 = 整条的 14%。
     *
     * <p>原来 36% 太大 —— 光标 8 单位/tick 下要跨 45 tick 才穿过去，闭着眼点都能中。
     * 现在一次穿越只有约 18 tick（0.9 秒），是正常的反应测试强度。</p>
     */
    private static final int MANUAL_BASE_HALF = 72;
    /** 判定区最小半宽（仙丹）。32/1000 ≈ 3.2%，最难一档。 */
    private static final int MANUAL_MIN_HALF = 18;
    /** 每提升一级品质，判定区缩小多少。 */
    private static final int MANUAL_HALF_STEP = 11;
    /**
     * 光标基础速度（单位/tick）。
     *
     * <p>30 → 1000/30 ≈ 33 tick ≈ <b>1.65 秒</b>跑完单程（第一判定）。</p>
     */
    private static final int MANUAL_BASE_SPEED = 30;
    /** 每提升一级品质，光标加快多少。 */
    private static final int MANUAL_SPEED_STEP = 4;
    /** 每成功判定一次，光标再加快多少（掉级不退）。 */
    private static final int MANUAL_SUCCESS_SPEED_STEP = 4;
    /**
     * 光标速度硬上限（单位/tick）。
     *
     * <p>62 → 1000/62 ≈ 16 tick ≈ <b>0.8 秒</b>跑完单程、1.6 秒一个来回
     * —— 这是"仙丹那一发"的强度（用户给的标尺）。</p>
     */
    private static final int MANUAL_MAX_SPEED = 70;
    /** 判定区漂移速度上限（单位/tick）。 */
    private static final int MANUAL_MAX_ZONE_SPEED = 14;
    /**
     * 判定区半宽下限：4/1000 ≈ 半个像素（条宽 108px，1px ≈ 9.3 单位）。
     * <b>刻意不设"看起来还算合理"的下限</b> —— 低等级炉炼高等级丹时就是要难到看不清。
     */
    private static final int MANUAL_HALF_FLOOR = 4;
    /** 判定区半宽上限：整条的三分之二 → 半宽 = 1/3。 */
    private static final int MANUAL_HALF_CEIL = MANUAL_RANGE / 3;
    /** 难度系数吃满后的光标速度硬上限（低阶炉炼高阶丹会撞到这里）。 */
    private static final int MANUAL_SPEED_HARD_MAX = 200;
    /** 判定区随机反向的检查间隔（tick）与概率（%）。 */
    private static final int MANUAL_ZONE_FLIP_MIN = 8;
    private static final int MANUAL_ZONE_FLIP_RANGE = 18;
    private static final int MANUAL_ZONE_FLIP_CHANCE = 40;

    // ----------------------------------------------------------------------------------
    //  面板状态码：**所有玩家反馈都写在炼丹炉面板上**，不再往聊天栏刷屏。
    //  客户端按这个码选一句短文案（见 LiandanluScreen#statusText）。
    // ----------------------------------------------------------------------------------
    public static final int STATUS_NONE = 0;
    public static final int STATUS_READY = 1;              // 就绪：点「开始炼丹」
    public static final int STATUS_RUNNING = 2;            // 判定中
    public static final int STATUS_SUCCESS = 3;            // 判定成功，品质提升
    public static final int STATUS_FAILED = 4;             // 判定失败，已收丹
    public static final int STATUS_DONE = 5;               // 丹成
    public static final int STATUS_NO_RECIPE = 6;          // 配方未匹配
    public static final int STATUS_LEVEL_LOW = 7;          // 炉等级不足
    public static final int STATUS_OUTPUT_FULL = 8;        // 产出槽已满
    public static final int STATUS_NEED_JUDGE = 9;         // 至少判定一次才能收丹
    public static final int STATUS_MATERIAL_CHANGED = 10;  // 材料变了，作废
    public static final int STATUS_TO_AUTO = 11;           // 已切自动
    public static final int STATUS_TO_MANUAL = 12;         // 已切手动
    public static final int STATUS_FINAL_ROUND = 13;       // 仙丹定品：最后一搏
    public static final int STATUS_NO_MP = 14;             // 精神力不足，这一跳都跳不了
    public static final int STATUS_MP_EXHAUSTED = 15;      // 精神力不够下一轮，自动结丹
    public static final int STATUS_BUSY = 16;              // 别人正在用这座炉

    /** 面板当前状态码（同步给客户端渲染）。 */
    private int manualStatus = STATUS_NONE;

    /** 本轮的精神力是否已经扣过（扣过一次就不再重复扣）。 */
    private boolean roundPaid = false;

    /**
     * 手动炼丹的<b>占用者</b>。
     *
     * <p>炼丹炉是公用方块，小游戏状态却只有一份 —— 两个人同时点「判定」会互相踩。
     * 所以开局记下发起者，其他人的按钮一律拒绝（同屏只有一个人能玩）。</p>
     */
    @Nullable
    private java.util.UUID manualOwner = null;

    /** 每成功一次，下一轮的精神力消耗倍率。 */
    public static final double MANUAL_COST_GROWTH = 1.5;

    /** 本轮消耗的缓存值（只给 GUI 显示用，避免每 tick 查一次配方表）。 */
    private int manualCostCache = 0;

    /** 当前这炉的难度系数（= 1 / 炼制速度），炉等级高就 <1、低就 >1。 */
    private float manualDifficulty = 1.0f;

    /** 客户端按钮 id：切换自动/手动。 */
    public static final int BUTTON_TOGGLE_AUTO = 0;
    /** 客户端按钮 id：开始炼丹 / 判定。 */
    public static final int BUTTON_START_OR_JUDGE = 1;
    /** 客户端按钮 id：收丹（提前结束，按当前品质结算）。 */
    public static final int BUTTON_COLLECT = 2;

    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LiandanluBlockEntity.this.progress;
                case 1 -> LiandanluBlockEntity.this.maxProgress;
                case 2 -> {
                    ItemStack stack = LiandanluBlockEntity.this.itemHandler.getStackInSlot(BOTTLE_SLOT);
                    yield stack.isEmpty() ? 0 : 1;
                }
                // ---- 手动炼丹小游戏（客户端只读）----
                case 3 -> LiandanluBlockEntity.this.autoMode ? 1 : 0;
                case 4 -> LiandanluBlockEntity.this.manualPhase;
                case 5 -> LiandanluBlockEntity.this.cursor;
                case 6 -> LiandanluBlockEntity.this.zoneHalf;
                case 7 -> LiandanluBlockEntity.this.cursorSpeed;
                case 8 -> LiandanluBlockEntity.this.manualQuality + 1;   // -1 → 0（0 表示"还没判定"）
                case 9 -> LiandanluBlockEntity.this.lastResult;
                case 10 -> LiandanluBlockEntity.this.zoneCenter;
                case 11 -> LiandanluBlockEntity.this.successCount;
                case 12 -> LiandanluBlockEntity.this.manualStatus;
                case 13 -> LiandanluBlockEntity.this.manualCostCache;
                case 14 -> Math.round(LiandanluBlockEntity.this.manualDifficulty * 100.0f);
                // 客户端拿来把光标外推 1 tick（否则看到的总是上一 tick 的位置，高速时必然"看着在判定区却算失败"）
                case 15 -> LiandanluBlockEntity.this.cursorDir > 0 ? 1 : 0;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LiandanluBlockEntity.this.progress = value;
                case 1 -> LiandanluBlockEntity.this.maxProgress = value;
            }
        }
        @Override
        public int getCount() { return 16; }
    };

    public LiandanluBlockEntity(BlockPos pPos, BlockState pBlockState, int tier) {
        super(ModBlockEntities.LIANDANLU_BE.get(), pPos, pBlockState);
        this.furnaceLevel = tier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LiandanluBlockEntity entity) {
        // 手动模式：只跑小游戏，完全不碰自动炼丹的配方/能量/进度逻辑
        if (!entity.autoMode) {
            entity.validateOwner();
            entity.tickManual();
            return;
        }

        // 1. 获取配方
        SimpleContainer container = new SimpleContainer(AlchemicalCalculator.MATERIAL_SLOTS);
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            container.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<LiandanRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level);

        if (recipeOptional.isEmpty()) {
            entity.progress = 0;
            return;
        }

        LiandanRecipe recipe = recipeOptional.get();
        ItemStack bottleStack = entity.itemHandler.getStackInSlot(BOTTLE_SLOT);

        if (!(bottleStack.getItem() instanceof SoulGatheringBottleItem bottle)
                || bottle.getNengliang(null, bottleStack) < recipe.getEnergyCost()
                || !canInsertResult(entity.itemHandler)) {
            // 材料/能量/输出任一不满足 → 进度清零，避免「攒了一半进度换材料」的白嫖
            entity.progress = 0;
            return;
        }

        entity.maxProgress = entity.getAdjustedCookingTime(recipe);
        entity.progress++;

        if (entity.progress >= entity.maxProgress) {
            executeCraft(entity, recipe, bottle, bottleStack);
            entity.progress = 0;
        }
    }

    // ==================================================================================
    //  【手动炼丹 · 小游戏】
    //
    //  规则（第一版雏形）：
    //    · 光标在 0~1000 之间匀速来回，判定区固定在正中间；
    //    · 点「判定」时光标停在哪，落在判定区内算成功、否则失败；
    //    · 第一次判定成功 → 药散（DanYaoQuality=1）；失败 → 破碎/丹渣，直接结束；
    //    · 之后每成功一次升一级、判定区缩小、光标加速；失败降一级，掉到破碎就结束；
    //    · 升到最高品质（仙丹）也结束。
    // ==================================================================================

    /**
     * 本轮的精神力消耗：<b>丹方能量消耗 × 1.5^已成功次数</b>。
     *
     * <p>用自动炼丹的「聚魂瓶能量」当基准值，成功一次之后每轮 ×1.5 ——
     * 想冲仙丹就得烧掉一条精神条。</p>
     *
     * <p>⚠️ 以后要加"炉等级影响消耗"，改这一个方法即可（乘一个炉等级系数）。</p>
     */
    public int manualCost(LiandanRecipe recipe) {
        if (recipe == null) {
            return 0;
        }
        double raw = recipe.getEnergyCost() * Math.pow(MANUAL_COST_GROWTH, Math.max(0, successCount));
        this.manualCostCache = (int) Math.ceil(raw);
        return this.manualCostCache;
    }

    /**
     * 检查并扣除<b>一轮</b>的精神力。
     *
     * @return 精神力不够 → {@code false}（一分不扣）
     */
    private boolean chargeRound(Player player, LiandanRecipe recipe) {
        PlayerAttributeCapability cap = player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return false;
        }
        int cost = manualCost(recipe);
        if (cost <= 0) {
            return true;
        }
        if (cap.getJingshenli() < cost) {
            return false;
        }
        cap.setJingshenli(cap.getJingshenli() - cost);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            SynsAPI.synsPlayerAttribute(sp);   // 让面板/精神条立刻刷新
        }
        return true;
    }

    /**
     * 占用者校验：掉线 / 换维度 / 走远（>12 格）→ 这局作废。
     *
     * <p>不清材料也不扣费：材料只在结丹时结算，MP 是每轮实扣的，
     * 所以中途离场只是"放弃这一局"，不会产生白嫖或卡死状态。</p>
     */
    private void validateOwner() {
        if (manualPhase != PHASE_RUNNING || manualOwner == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getServer() == null) {
            return;
        }
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(manualOwner);
        if (owner == null
                || owner.level() != level
                || owner.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5) > 144.0) {
            resetManual();
        }
    }

    /**
     * 炉等级 → 难度系数（<b>与自动炼丹的炼制速度成反比</b>）。
     *
     * <p>自动炼丹里 {@code getAdjustedCookingTime} 用的是同一个"炉相对丹方"的档差：
     * 炉等级 ≥ 丹方等级 → 每高一级快 35%（{@code speed = 1 + diff*0.35}）；
     * 炉等级 < 丹方等级 → 每低一级慢 1.35 倍（{@code speed = 1/1.35^diff}）。</p>
     *
     * <p>这里取 {@code 难度 = 1 / 速度}：<b>炼得快 = 光标慢 + 判定区大</b>（简单），
     * <b>炼得慢 = 光标快 + 判定区小</b>（难）。</p>
     */
    private float difficultyForFurnace(LiandanRecipe recipe) {
        if (recipe == null) {
            return 1.0f;
        }
        int danLevel = recipe.getRecipeLevel();
        float speed;
        if (furnaceLevel >= danLevel) {
            speed = 1.0f + (furnaceLevel - danLevel) * 0.35f;
        } else {
            speed = (float) (1.0 / Math.pow(1.35, Math.min(danLevel - furnaceLevel, 8)));
        }
        return 1.0f / speed;
    }

    /**
     * 内丹品质 → 难度系数。
     *
     * <p>取 5 个材料槽的<b>平均药性</b>（{@code AlchemicalCalculator} 里同一套 0~6 曲线），
     * 归一化后线性映射到 {@code 1.25 ~ 0.65}：</p>
     * <ul>
     *   <li>全凡品 / 只有草药（药性 0）→ ×1.25（更难）</li>
     *   <li>5 颗仙品（药性 6.0）→ ×0.65（明显更好炼）</li>
     * </ul>
     * <p>幅度刻意做得比炉等级温和：内丹是"锦上添花"，炉等级才是主要变量。</p>
     */
    private float innerDifficulty() {
        ItemStack[] mats = new ItemStack[AlchemicalCalculator.MATERIAL_SLOTS];
        for (int i = 0; i < mats.length; i++) {
            mats[i] = itemHandler.getStackInSlot(i);
        }
        double avg = AlchemicalCalculator.averagePotentialOf(mats);   // 0 ~ 6
        double ratio = Math.max(0.0, Math.min(1.0, avg / 6.0));
        return (float) (1.25 - 0.60 * ratio);
    }

    /** 判定区半宽：品质曲线 ÷ 炉难度，再夹到 [半像素, 整条 2/3]。 */
    private int halfForQuality(int quality) {
        int base = Math.max(MANUAL_MIN_HALF, MANUAL_BASE_HALF - quality * MANUAL_HALF_STEP);
        int scaled = Math.round(base / manualDifficulty);
        return Math.max(MANUAL_HALF_FLOOR, Math.min(MANUAL_HALF_CEIL, scaled));
    }

    /**
     * 光标速度：品质越高越快，<b>而且每成功一次都会再快一档</b>（掉级不退）。
     *
     * <p>之所以把"累计成功"单独算：玩家靠失败掉级把品质压回药散之后，
     * 如果速度只看品质，就会变成"掉级 = 回到简单模式"，
     * 可以无限刷低品质。现在失败降级但速度不回退，越玩越难。</p>
     */
    private int speedForQuality(int quality, int successes) {
        int q = Math.max(0, quality);
        int base = Math.min(MANUAL_MAX_SPEED,
                MANUAL_BASE_SPEED + q * MANUAL_SPEED_STEP + successes * MANUAL_SUCCESS_SPEED_STEP);
        int scaled = Math.round(base * manualDifficulty);
        return Math.max(2, Math.min(MANUAL_SPEED_HARD_MAX, scaled));
    }

    /**
     * 判定区漂移速度（单位/tick）。品质 0~1 时不动，之后越来越快。
     *
     * <p>让判定区自己走起来是难度跃迁的关键：判定区固定时玩家只要记住
     * "光标到中间就点"，加上漂移之后必须同时预判两个运动。</p>
     */
    private static int zoneSpeedFor(int quality, int successes) {
        // 判定区第一轮不动（让玩家先看清玩法），之后每升一级明显加快
        if (quality < 1) {
            return 0;
        }
        int raw = 2 + quality * 2 + successes;
        return Math.min(MANUAL_MAX_ZONE_SPEED, raw);
    }

    /** 每 tick 推进光标（只有服务端会走到这里）。 */
    private void tickManual() {
        if (manualPhase != PHASE_RUNNING) {
            return;
        }

        // ① 光标扫动
        cursor += cursorDir * cursorSpeed;
        if (cursor > MANUAL_RANGE) {
            cursor = MANUAL_RANGE - (cursor - MANUAL_RANGE);
            cursorDir = -1;
        } else if (cursor < 0) {
            cursor = -cursor;
            cursorDir = 1;
        }

        // ② 判定区漂移（随机左右 + 边界反弹，不越出条外）
        if (zoneSpeed > 0) {
            // 随机反向：每隔一小段时间掷一次骰，让判定区"抽风"式变向，不能靠背节奏
            if (--zoneFlipTimer <= 0) {
                zoneFlipTimer = MANUAL_ZONE_FLIP_MIN + level.random.nextInt(MANUAL_ZONE_FLIP_RANGE);
                if (level.random.nextInt(100) < MANUAL_ZONE_FLIP_CHANCE) {
                    zoneDir = -zoneDir;
                }
            }
            zoneCenter += zoneDir * zoneSpeed;
            int lo = zoneHalf;
            int hi = MANUAL_RANGE - zoneHalf;
            if (hi < lo) {          // 兜底：判定区比整条还宽（理论上不会）
                zoneCenter = MANUAL_RANGE / 2;
            } else if (zoneCenter > hi) {
                zoneCenter = hi - (zoneCenter - hi);
                zoneDir = -1;
            } else if (zoneCenter < lo) {
                zoneCenter = lo + (lo - zoneCenter);
                zoneDir = 1;
            }
        }
    }

    /** 找当前材料对应的配方；没有返回 null。 */
    @Nullable
    private LiandanRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        SimpleContainer container = new SimpleContainer(AlchemicalCalculator.MATERIAL_SLOTS);
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level)
                .orElse(null);
    }

    /** 点「开始炼丹」：**只做校验**，不产出任何东西。 */
    public void startManual(Player player) {
        LiandanRecipe recipe = findRecipe();
        if (recipe == null) {
            fail(STATUS_NO_RECIPE);
            return;
        }
        // ⚠️ 不再拒绝"炉等级 < 丹方等级"——
        //    自动炼丹对这种情况是"烧得更慢"，手动就对应"光标更快 + 判定区更小"，
        //    炼得出来但极难。硬拒绝会让 difficultyForFurnace 的高难度分支变成死代码。
        if (!canInsertResult(itemHandler)) {
            fail(STATUS_OUTPUT_FULL);
            return;
        }

        manualQuality = -1;
        lastResult = 0;
        successCount = 0;
        finalRound = false;
        roundPaid = false;
        // ⚠️ 顺序要紧：half/speed 都要乘这个系数，必须先算出来再赋值
        // 总难度 = 炉等级难度 × 内丹品质难度
        manualDifficulty = difficultyForFurnace(recipe) * innerDifficulty();
        zoneHalf = halfForQuality(0);              // 第一次判定：判定区最大
        cursorSpeed = speedForQuality(0, 0);        // 第一次判定：光标最慢
        zoneSpeed = zoneSpeedFor(0, 0);             // 第一次判定：判定区不动
        zoneCenter = MANUAL_RANGE / 2;
        zoneDir = 1;
        zoneFlipTimer = 0;
        cursor = 0;
        cursorDir = 1;
        manualPhase = PHASE_RUNNING;
        manualOwner = player.getUUID();
        manualCost(recipe);   // 先算一次，GUI 一开始就能显示本轮消耗

        setStatus(STATUS_RUNNING);
    }

    /** 点「判定」：按光标当前位置给结果。 */
    public void judge(Player player) {
        if (manualPhase != PHASE_RUNNING) {
            return;
        }
        LiandanRecipe recipe = findRecipe();
        if (recipe == null) {
            resetManual();
            fail(STATUS_MATERIAL_CHANGED);
            return;
        }

        // ① 本轮还没付费 → 点判定的这一刻才检查并扣除（精神不够连跳都不让跳）
        if (!roundPaid) {
            if (!chargeRound(player, recipe)) {
                setStatus(STATUS_NO_MP);
                return;
            }
            roundPaid = true;
        }

        // ⚠️ 容差 = 半个 tick 的位移。
        //    光标是按 tick 离散前进的（高速时一 tick 就跨 60+ 单位），玩家看到的位置
        //    与服务器判定的那一 tick 之间必然有不到 1 tick 的错位；
        //    不加容差就会出现"屏幕上明明压在判定区里，却判失败"。
        int tolerance = zoneHalf + Math.max(0, cursorSpeed / 2);
        boolean hit = Math.abs(cursor - zoneCenter) <= tolerance;

        // ② 失败：掉一级后立刻收丹（仙丹失败 → 宝丹，同样收丹）
        if (!hit) {
            manualQuality = (manualQuality <= 0) ? 0 : manualQuality - 1;
            lastResult = 2;
            finishManual(player, recipe, STATUS_FAILED);
            return;
        }

        successCount++;      // 累计成功：速度只会越来越快，掉级不退
        lastResult = 1;

        // ② 仙丹定品的最后一轮：这一下中了才真的成仙
        if (finalRound) {
            finishManual(player, recipe, STATUS_DONE);
            return;
        }

        manualQuality = (manualQuality < 0)
                ? 1
                : Math.min(DanYaoQuality.values().length - 1, manualQuality + 1);

        // ③ 刚升到仙丹 → 不结束，再赌最后一轮
        if (manualQuality >= DanYaoQuality.values().length - 1) {
            finalRound = true;
        }

        // ③ 成功之后**立刻**检查下一轮的精神力：不够就直接结丹，够才开下一轮
        if (!chargeRound(player, recipe)) {
            finishManual(player, recipe, STATUS_MP_EXHAUSTED);
            return;
        }
        roundPaid = true;

        startNextRound();
        // 刚进入"仙丹定品"那一轮要单独提示，否则玩家以为已经成丹了
        setStatus(finalRound ? STATUS_FINAL_ROUND : STATUS_SUCCESS);
    }

    /**
     * 开下一轮：按当前品质重算判定区大小 / 光标速度 / 漂移速度，并把判定区摆到新位置。
     *
     * <p>成功满 2 次之后判定区不再固定居中，而是<b>随机落在条上</b>；
     * 但绝不贴左侧 —— 光标每轮都从最左（0）出发，判定区贴左等于白送。</p>
     */
    private void startNextRound() {
        zoneHalf = halfForQuality(manualQuality);
        cursorSpeed = speedForQuality(manualQuality, successCount);
        zoneSpeed = zoneSpeedFor(manualQuality, successCount);
        zoneCenter = randomZoneCenter();
        zoneDir = level.random.nextBoolean() ? 1 : -1;
        zoneFlipTimer = 0;
        cursor = 0;
        cursorDir = 1;
    }

    /** 随机判定区中心：前两次成功之前固定居中，之后随机但避开最左侧（光标起点）。 */
    private int randomZoneCenter() {
        if (successCount < 2) {
            return MANUAL_RANGE / 2;
        }
        int lo = Math.max(MANUAL_RANGE / 4, zoneHalf + 120);   // 离左侧至少 120 + 半宽
        int hi = MANUAL_RANGE - zoneHalf;
        if (lo >= hi) {
            return MANUAL_RANGE / 2;
        }
        return lo + level.random.nextInt(hi - lo + 1);
    }

    /**
     * <b>提前收丹</b>：玩到一半不想再赌了，按当前品质结算。
     *
     * <p>没有这个按钮的话，玩家只有"升到仙丹"或"被打回破碎"两条路能拿到丹药，
     * 中间品质（药丹/灵丹…）永远取不出来。</p>
     */
    public void collect(Player player) {
        if (manualPhase != PHASE_RUNNING) {
            return;
        }
        if (manualQuality < 0) {
            setStatus(STATUS_NEED_JUDGE);
            return;
        }
        LiandanRecipe recipe = findRecipe();
        if (recipe == null) {
            resetManual();
            fail(STATUS_MATERIAL_CHANGED);
            return;
        }
        // 玩家主动收丹：不是失败，按"丹成"结算
        finishManual(player, recipe, STATUS_DONE);
    }

    /** 收丹：按最终品质产出（手动模式不消耗聚魂瓶能量，因为瓶槽已从面板移除）。 */
    private void finishManual(Player player, LiandanRecipe recipe, int endStatus) {
        int quality = Math.max(0, Math.min(DanYaoQuality.values().length - 1, manualQuality));
        manualPhase = PHASE_DONE;
        roundPaid = false;

        IItemHandlerModifiable handler = (IItemHandlerModifiable) itemHandler;
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        result.getOrCreateTag().putInt("DanYaoQuality", quality);

        // 扣材料：内丹沿用自动炼丹的"保住"概率，其它材料照常消耗 1 个
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.getItem() instanceof NeidanItem
                    && level.getRandom().nextFloat() < AlchemicalCalculator.NEIDAN_KEEP_CHANCE) {
                continue;
            }
            handler.extractItem(i, 1, false);
        }

        ItemStack remaining = result;
        for (int i = RESULT_SLOT_START; i <= RESULT_SLOT_END; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break;
        }

        // 结束原因由调用方决定：可能是"判定失败降级"，也可能是"升到仙丹/主动收丹"。
        // ⚠️ 不能按 quality 猜 —— 之前"连成 3 次后失败"会掉到药散(q>0)，
        //    于是被误判成"丹成"，面板提示"丹成！可再次开始炼丹"，与实际相反。
        lastResult = endStatus == STATUS_FAILED ? 2 : 1;
        setStatus(endStatus);
        cursor = MANUAL_RANGE / 2;
        setChanged();
    }

    /** 品质中文名（-1 时显示"未判定"）。 */
    private String qualityName() {
        if (manualQuality < 0) {
            return "未判定";
        }
        int idx = Math.min(manualQuality, DanYaoQuality.values().length - 1);
        return DanYaoQuality.values()[idx].name;
    }

    /** 清空小游戏状态（切模式 / 材料变化时）。 */
    public void resetManual() {
        manualPhase = PHASE_IDLE;
        manualOwner = null;
        manualCostCache = 0;
        manualQuality = -1;
        lastResult = 0;
        successCount = 0;
        finalRound = false;
        roundPaid = false;
        finalRound = false;
        cursor = 0;
        cursorDir = 1;
        zoneHalf = MANUAL_BASE_HALF;
        cursorSpeed = MANUAL_BASE_SPEED;
        zoneCenter = MANUAL_RANGE / 2;
        zoneDir = 1;
        zoneFlipTimer = 0;
        zoneSpeed = 0;
    }

    private void fail(int status) {
        resetManual();
        setStatus(status);
    }

    /** 写状态码并同步（不往聊天栏发任何东西）。 */
    private void setStatus(int status) {
        this.manualStatus = status;
        setChanged();
    }


    /**
     * 面板按钮统一入口（{@code LiandanluMenu#clickMenuButton} 转发到这里）。
     *
     * @return 是否消费了这次点击
     */
    public boolean handleButton(Player player, int buttonId) {
        if (level == null || level.isClientSide || player == null) {
            return false;
        }
        // 联机兜底：包可以伪造，必须自己再验一次距离（4 格内才认）
        if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) > 64.0) {
            return false;
        }

        if (buttonId == BUTTON_TOGGLE_AUTO) {
            // 有人正在玩手动时，别人不能把模式切走（会把他的局直接清掉）
            if (manualPhase == PHASE_RUNNING && !isOwner(player)) {
                setStatus(STATUS_BUSY);
                return true;
            }
            // 切换模式只改开关，两个槽位的物品原地保留 ——
            // 手动模式把它们整体隐藏（不可见/不可点），切回自动就原样出现。
            autoMode = !autoMode;
            resetManual();
            progress = 0;
            setStatus(autoMode ? STATUS_TO_AUTO : STATUS_TO_MANUAL);
            return true;
        }

        if (buttonId == BUTTON_START_OR_JUDGE) {
            if (autoMode) {
                return false;
            }
            if (manualPhase == PHASE_RUNNING) {
                if (!isOwner(player)) {
                    setStatus(STATUS_BUSY);
                    return true;
                }
                judge(player);
            } else {
                startManual(player);
            }
            return true;
        }

        if (buttonId == BUTTON_COLLECT) {
            if (autoMode) {
                return false;
            }
            if (manualPhase == PHASE_RUNNING && !isOwner(player)) {
                setStatus(STATUS_BUSY);
                return true;
            }
            collect(player);
            return true;
        }
        return false;
    }

    /** 当前玩家是不是这局手动炼丹的发起者。 */
    private boolean isOwner(Player player) {
        return manualOwner == null || manualOwner.equals(player.getUUID());
    }

    // 关于那两个隐藏槽位：
    //   用户定的是「整体隐藏、切回自动原样出现」—— 药渣块 / 聚魂瓶在手动模式下属于
    //   "不使用"的东西，不该塞回玩家背包。所以只做读取层屏蔽：
    //   界面不画（LiandanluScreen）、点不到（LiandanluMenu#clicked），
    //   ItemHandler 里的物品原地不动，切回自动就照常显示。
    public boolean isAutoMode() {
        return autoMode;
    }

    // 在 LiandanluBlockEntity 类中添加
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            // 强制通知附近的所有观察者（包括打开界面的玩家）该方块数据已更新
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    private static void executeCraft(LiandanluBlockEntity entity, LiandanRecipe recipe, SoulGatheringBottleItem bottle, ItemStack bottleStack) {
        if (bottle.getNengliang(null, bottleStack) < recipe.getEnergyCost()) return;

        IItemHandlerModifiable handler = (IItemHandlerModifiable) entity.itemHandler;

        // 先算概率再扣能量：随机只消耗一次，且失败不会白扣
        double[] weights = AlchemicalCalculator.computeWeights(
                handler, recipe.isSpecial(), entity.furnaceLevel, recipe.getRecipeLevel());
        int finalQuality = AlchemicalCalculator.roll(weights, entity.level.getRandom());

        bottle.setNengliang(null, bottleStack, bottle.getNengliang(null, bottleStack) - recipe.getEnergyCost());

        ItemStack resultStack = recipe.getResultItem(entity.level.registryAccess()).copy();
        resultStack.getOrCreateTag().putInt("DanYaoQuality", finalQuality);

        int keptNeidan = 0;
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.getItem() instanceof NeidanItem
                    && entity.level.getRandom().nextFloat() < AlchemicalCalculator.NEIDAN_KEEP_CHANCE) {
                keptNeidan++;
                continue;
            }
            handler.extractItem(i, 1, false);
        }
        if (!handler.getStackInSlot(DROSS_SLOT).isEmpty()) handler.extractItem(DROSS_SLOT, 1, false);

        ItemStack remaining = resultStack;
        for (int i = RESULT_SLOT_START; i <= RESULT_SLOT_END; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break;
        }

        if (entity.level != null && !entity.level.isClientSide) {
            Player crafter = entity.level.getNearestPlayer(
                    entity.getBlockPos().getX() + 0.5D,
                    entity.getBlockPos().getY() + 0.5D,
                    entity.getBlockPos().getZ() + 0.5D,
                    8.0D, false);
            if (crafter != null) {
                AchievementAPI.onAlchemySuccess(crafter, finalQuality);
            }
        }

        entity.setChanged();
    }

    /**
     * 炉温对耗时的影响：炉等级 ≥ 丹方等级 → 加速；低于 → 惩罚。
     *
     * <p>旧实现惩罚是 {@code 2^levelDiff}（最高 2^8=256 倍，配 72000 tick 上限 = 整整一小时）。
     * 这里改成温和的线性加速 + 对数惩罚，最高约 4 倍（400 → 1600 tick ≈ 80 秒）。</p>
     */
    private int getAdjustedCookingTime(LiandanRecipe recipe) {
        int baseTicks = Math.max(20, recipe.getCookTime());
        if (recipe.isSpecial()) {
            return baseTicks;
        }

        int danLevel = recipe.getRecipeLevel();
        int luLevel = this.furnaceLevel;

        if (luLevel >= danLevel) {
            float speedMultiplier = 1.0f + (luLevel - danLevel) * 0.35f;
            return (int) Math.max(baseTicks / speedMultiplier, 20);
        }
        int levelDiff = danLevel - luLevel;
        double penalty = Math.pow(1.35, Math.min(levelDiff, 8));
        return Math.min((int) (baseTicks * penalty), 3600);
    }

    private static boolean canInsertResult(IItemHandler handler) {
        for (int i = RESULT_SLOT_START; i <= RESULT_SLOT_END; i++) {
            if (handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }


    @Override public Component getDisplayName() { return Component.literal("炼丹炉"); }
    @Nullable @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LiandanluMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("FurnaceLevel", this.furnaceLevel);
        pTag.putInt("Progress", this.progress);
        // 手动炼丹：开关与游戏进度一起存，避免关界面/重启把玩家正在进行的判定弄丢
        pTag.putBoolean("AutoMode", this.autoMode);
        pTag.putInt("ManualPhase", this.manualPhase);
        pTag.putInt("ManualQuality", this.manualQuality);
        pTag.putInt("ManualSuccess", this.successCount);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("inventory")) {
            this.itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        }
        this.progress = pTag.getInt("Progress");
        if (pTag.contains("AutoMode")) {
            this.autoMode = pTag.getBoolean("AutoMode");
        }
        this.manualPhase = pTag.getInt("ManualPhase");
        this.manualQuality = pTag.getInt("ManualQuality");
        this.successCount = pTag.getInt("ManualSuccess");
        // 重进世界时不要接着跑半截手势：进行中的判定重置为"待开始"
        if (this.manualPhase == PHASE_RUNNING) {
            this.manualPhase = PHASE_IDLE;
        }
        this.zoneHalf = halfForQuality(Math.max(0, this.manualQuality));
        this.cursorSpeed = speedForQuality(Math.max(0, this.manualQuality), this.successCount);
        this.zoneSpeed = zoneSpeedFor(Math.max(0, this.manualQuality), this.successCount);
        this.zoneCenter = MANUAL_RANGE / 2;
        this.zoneDir = 1;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    // 在 LiandanluBlockEntity.java
    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public int getFurnaceLevel() {
        return this.furnaceLevel;
    }


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}