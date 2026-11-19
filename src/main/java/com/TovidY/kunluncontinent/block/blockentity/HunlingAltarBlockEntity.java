package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitual;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.RitualIngredient;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldSender;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 汇灵台 / 底座共用 —— 存一件「供品」并把它渲染成悬浮在台面之上；同时承载仪式状态机。
 *
 * <h3>为什么只用单个 ItemStack 而不是 ItemStackHandler</h3>
 * <p>需求就是「一次只能放一个」。用 {@code ItemStackHandler(1)} 也能做，但会顺带把
 * {@code ForgeCapabilities.ITEM_HANDLER} 暴露出去 —— 漏斗、管道、其它 mod 的自动化
 * 就能把东西塞进来/抽走，漂浮渲染会跟着瞬移，而且「一次一个」的规则也就形同虚设。
 * 这里只开一个字段，所有进出都必须经过右键。</p>
 *
 * <h3>⚠️ 同步为什么必须自己发包</h3>
 * <p>第一版写的是 {@code level.sendBlockUpdated(pos, state, state, 3)}，结果<b>取回物品后
 * 客户端那团悬浮的东西不消失</b>。翻源码才发现：</p>
 * <pre>
 * // ServerLevel#sendBlockUpdated 在 1.20.1 里只做两件事：
 * //   1. chunkSource.blockChanged(pos)   → 标记脏
 * //   2. 重算 VoxelShape 并让寻路重算     → 只管碰撞
 * // 它从头到尾没有往客户端发任何 BlockEntity 数据包。
 * </pre>
 * <p>所以改成在 {@link #setStored} 里直接把 {@link ClientboundBlockEntityDataPacket}
 * 推给附近玩家 —— 只在放/取/仪式节点发生，开销可以忽略。</p>
 *
 * <h3>仪式状态刻意不写进 NBT</h3>
 * <p>它只持续几十秒到几分钟，服务器重启导致中断可以接受；而持久化要额外存基座坐标、
 * 玩家 UUID、进度、副产物表…… 换来的只是「重启后能接着做」，收益远小于复杂度。
 * 读档后仪式消失，物品原样留在基座与汇灵台上，一件都不会丢。</p>
 */
public class HunlingAltarBlockEntity extends BlockEntity {

    private static final String TAG_ITEM = "StoredItem";

    /** 仪式结束的世界时间（0 = 没有仪式）。只用于客户端把物品越转越快。 */
    private static final String TAG_RITUAL_END = "RitualEnd";

    /** 手动发包的半径（格）。超过这个距离的玩家本来也看不清台面。 */
    private static final double SYNC_RANGE = 64.0D;

    /** 副物品必须在「剩余这么多 tick」之前全部消耗完（3 秒）。 */
    public static final int CONSUME_TAIL_TICKS = 60;

    /** 台面上的物品（永远是 1 个，空表示空台；仪式中是必需品，完成后是成品）。 */
    private ItemStack stored = ItemStack.EMPTY;

    // ==================== 仪式运行时状态（不持久化） ====================

    private boolean ritualActive;
    /** 底座用：被某场仪式占用，禁止取放。 */
    private boolean ritualLocked;
    private int ritualElapsed;
    private int ritualDuration;
    private int ritualSpiritCost;
    private int ritualSpiritPaid;
    private int ritualConsumed;
    private int ritualTotalItems;
    private UUID ritualPlayer;
    private final List<BlockPos> ritualBases = new ArrayList<>();

    /** 当前仪式配方（运行时对象，不持久化）。物品版和召唤生物版都实现 {@link HunlingRitual}。 */
    private HunlingRitual ritualRecipe;
    private List<RitualIngredient> ritualIngredients = new ArrayList<>();

    /** 产物是生物：结束时先进一段「蓄势」前摇，再生成。 */
    private boolean ritualSpawn;
    private int ritualPrespawn;

    /** 蓄势前摇时长（tick）。不占用配方的持续时间。 */
    public static final int SPAWN_PRESPAWN_TICKS = 20;

    /** 仪式结束的世界时间戳；0 表示当前没有仪式。会同步给客户端，用于加速旋转。 */
    private long ritualEndTick;

    public HunlingAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNLING_ALTAR_BE.get(), pos, state);
    }

    // ==================== 台面物品 ====================

    public ItemStack getStored() {
        return stored;
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /** 写入台面物品（传 {@link ItemStack#EMPTY} 表示清空），并立刻同步给周围客户端。 */
    public void setStored(ItemStack stack) {
        this.stored = stack;
        setChanged();
        syncToClients();
    }

    /**
     * 把当前状态推给附近玩家。
     *
     * <p>⚠️ 不能指望 {@code sendBlockUpdated} —— 见类注释，那个方法不发 BE 包。
     * 也不该只靠 {@code setChanged()} 的 chunk 广播：那是「脏区块」机制，
     * 要等下一次区块同步，玩家会看到物品卡在原地才慢慢消失。</p>
     */
    private void syncToClients() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        Packet<ClientGamePacketListener> packet = getUpdatePacket();
        if (packet == null) {
            return;
        }
        double maxSqr = SYNC_RANGE * SYNC_RANGE;
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.5D;
        double z = worldPosition.getZ() + 0.5D;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(x, y, z) <= maxSqr) {
                player.connection.send(packet);
            }
        }
    }

    // ==================== 仪式：查询 ====================

    public boolean isRitualActive() {
        return ritualActive;
    }

    /** 底座：是否被某场仪式占用（禁止取放）。 */
    public boolean isRitualLocked() {
        return ritualLocked;
    }

    public void setRitualLocked(boolean locked) {
        this.ritualLocked = locked;
    }

    /** 仪式结束的世界时间；0 表示没有仪式。客户端渲染器用它算旋转加速。 */
    public long getRitualEndTick() {
        return ritualEndTick;
    }

    // ==================== 仪式：启动 ====================

    /**
     * 开始一场仪式。
     *
     * @param bases 参与仪式的底座坐标（<b>有序</b>，消耗按这个顺序从头到尾）
     */
    public void startRitual(ServerLevel level, ServerPlayer player, HunlingRitual recipe,
                            List<BlockPos> bases) {
        this.ritualActive = true;
        this.ritualElapsed = 0;
        this.ritualDuration = recipe.getDurationTicks();
        this.ritualSpiritCost = recipe.getSpiritCost();
        this.ritualSpiritPaid = 0;
        this.ritualConsumed = 0;
        this.ritualTotalItems = recipe.getTotalItemCount();
        this.ritualPlayer = player.getUUID();
        this.ritualRecipe = recipe;
        this.ritualIngredients = new ArrayList<>(recipe.getRitualIngredients());

        this.ritualSpawn = recipe instanceof HunlingSpawnRitualRecipe;
        this.ritualPrespawn = this.ritualSpawn ? SPAWN_PRESPAWN_TICKS : 0;

        // 客户端要靠这个把物品越转越快（见 HunlingAltarRenderer）
        this.ritualEndTick = level.getGameTime() + recipe.getDurationTicks();

        this.ritualBases.clear();
        for (BlockPos base : bases) {
            this.ritualBases.add(base.immutable());
            if (level.getBlockEntity(base) instanceof HunlingAltarBlockEntity baseBe) {
                baseBe.setRitualLocked(true);
            }
        }
        setChanged();
        // 必须在这里补一次发包：handleUse 里是「先 setStored（发一次，此时 endTick 还是 0）
        // → 再 startRitual」，不发的话客户端永远不知道仪式什么时候结束，加速旋转就没了。
        syncToClients();

        player.displayClientMessage(Component.literal(
                "§6仪式开始 §8| §7需 §b" + recipe.getSpiritCost() + " §7点精神力，耗时 §e"
                        + String.format("%.1f", recipe.getDurationTicks() / 20.0f) + " §7秒")
                .withStyle(ChatFormatting.WHITE), true);

        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6f, 1.4f);
    }

    // ==================== 仪式：每 tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, HunlingAltarBlockEntity be) {
        if (level.isClientSide || !be.ritualActive) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer player = resolvePlayer(serverLevel, be.ritualPlayer);
        if (player == null || !player.isAlive()) {
            // 玩家离线 / 死亡：静默中断
            be.failRitual(serverLevel);
            return;
        }

        // 底座被挖 → 立刻中断（不等消耗到它才失败）
        if (!be.basesIntact(serverLevel)) {
            be.failRitual(serverLevel);
            return;
        }

        be.ritualElapsed++;

        PlayerAttributeCapability cap = player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            be.failRitual(serverLevel);
            return;
        }

        // ── ① 扣精神力：用「应该累计扣多少」减「已经扣了多少」。
        //     这样 10000 / 1200 tick 这种除不尽的，也会在最后一 tick 正好扣满，
        //     不会因为每 tick 取整而少扣或多扣几十点。
        int shouldPaid = (int) Math.round((double) be.ritualSpiritCost * be.ritualElapsed / be.ritualDuration);
        int delta = shouldPaid - be.ritualSpiritPaid;
        if (delta > 0) {
            float current = cap.getJingshenli();
            if (current < delta) {
                player.displayClientMessage(
                        Component.literal("§c精神力枯竭，仪式中断！").withStyle(ChatFormatting.RED), true);
                be.failRitual(serverLevel);
                return;
            }
            cap.setJingshenli(current - delta);
            be.ritualSpiritPaid = shouldPaid;
            if (be.ritualElapsed % 20 == 0) {
                SynsAPI.synsPlayerAttribute(player);
            }
        }

        // ── ② 按时间消耗副物品：在「总时长 - 3 秒」之内均匀消耗完
        int window = Math.max(1, be.ritualDuration - CONSUME_TAIL_TICKS);
        int shouldConsumed = (int) Math.min(be.ritualTotalItems,
                (long) be.ritualElapsed * be.ritualTotalItems / window);
        while (be.ritualConsumed < shouldConsumed) {
            if (!be.consumeNextBaseItem(serverLevel)) {
                be.failRitual(serverLevel);
                return;
            }
            be.ritualConsumed++;
        }

        if (be.ritualElapsed % 4 == 0) {
            // 最后 3 秒副物品已经吃空，注入弧线没东西可画 —— 换成台面物品周围的蓄力
            if (be.ritualElapsed >= be.ritualDuration - CONSUME_TAIL_TICKS) {
                be.drawFinaleCharge(serverLevel, player);
            } else {
                be.drawInjectionArcs(serverLevel, player);
            }
        }

        // ── ⑤ 完成：召唤生物版先进一段蓄势，再真正生成
        if (be.ritualElapsed >= be.ritualDuration) {
            if (be.ritualPrespawn > 0) {
                if (be.ritualPrespawn == SPAWN_PRESPAWN_TICKS) {
                    be.playSpawnCharge(serverLevel);
                }
                be.ritualPrespawn--;
                if (be.ritualPrespawn <= 0) {
                    be.finishRitual(serverLevel, player);
                }
                return;   // 蓄势期间不再扣精神力 / 吃物品（此时其实已经扣完了）
            }
            be.finishRitual(serverLevel, player);
        }
    }

    private static ServerPlayer resolvePlayer(ServerLevel level, UUID uuid) {
        if (uuid == null || level.getServer() == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(uuid);
    }

    /** 所有参演底座是否还在（方块还在、BlockEntity 还是同一类型）。 */
    private boolean basesIntact(ServerLevel level) {
        for (BlockPos basePos : ritualBases) {
            if (!level.hasChunkAt(basePos)) {
                return false;
            }
            if (!(level.getBlockEntity(basePos) instanceof HunlingAltarBlockEntity)) {
                return false;
            }
        }
        return true;
    }

    /** 消耗下一件副物品（按启动时记录的顺序），顺手把副产物弹在底座位置。 */
    private boolean consumeNextBaseItem(ServerLevel level) {
        for (BlockPos basePos : ritualBases) {
            if (!(level.getBlockEntity(basePos) instanceof HunlingAltarBlockEntity base)) {
                return false;
            }
            ItemStack storedOnBase = base.getStored();
            if (storedOnBase.isEmpty()) {
                continue;   // 这一座已经被吃掉了
            }
            ItemStack remainder = findRemainder(storedOnBase);
            if (!remainder.isEmpty()) {
                Containers.dropItemStack(level,
                        basePos.getX() + 0.5D, basePos.getY() + 1.0D, basePos.getZ() + 0.5D,
                        remainder.copy());
            }
            base.setStored(ItemStack.EMPTY);

            // 这一件被「注」进去了：用按钮音做个清脆的消失提示
            level.playSound(null, basePos, SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS, 0.45f, 1.5f);
            return true;
        }
        return false;
    }

    /** 找这一件副物品对应的副产物（如「水桶 → 空桶」）；没有就返回空。 */
    private ItemStack findRemainder(ItemStack consumed) {
        for (RitualIngredient ing : ritualIngredients) {
            if (ing.hasRemainder() && ing.matches(consumed, false)) {
                return ing.remainder();
            }
        }
        return ItemStack.EMPTY;
    }

    // ==================== 仪式：注入表现 ====================

    /**
     * 注入弧线用的粒子：<b>深近黑的暗紫尘埃</b>。
     *
     * <p>原版没有纯黑粒子（{@code SQUID_INK} 会散开下落、{@code ASH} 有重力），
     * 所以用可自定义颜色的 {@link DustParticleOptions}。颜色刻意留了一丝紫
     * （而不是 0,0,0）—— 纯黑在暗处完全看不见，这点紫能让它在夜里也有轮廓。</p>
     */
    private static final ParticleOptions INJECT_PARTICLE =
            new DustParticleOptions(new Vector3f(0.08f, 0.05f, 0.12f), 0.7f);

    /**
     * 从每座<b>还有东西</b>的基座拉一条弯弧到台面，表现「灵气被抽过去」。
     *
     * <h3>为什么用粒子而不是着色器</h3>
     * <p>现有的着色器场域体系（{@code SkillEffect}）只支持「一个锚点 + 半径」，
     * 没有「起点 → 终点」这种两点结构。为了几条弧去扩协议、写新 fsh 不划算 ——
     * 用 {@link ParticleFx#polyline} 沿二次贝塞尔采样，视觉上就是一条会呼吸的弧，
     * 而且服务端发包、客户端零改动。</p>
     *
     * <p>弧的弯曲幅度用正弦随时间浮动，所以多条弧不会像复制粘贴一样整齐。</p>
     */
    private void drawInjectionArcs(ServerLevel level, ServerPlayer player) {
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx == null) {
            return;
        }

        List<Vec3> sources = new ArrayList<>();
        for (BlockPos basePos : ritualBases) {
            if (!(level.getBlockEntity(basePos) instanceof HunlingAltarBlockEntity base)) {
                continue;
            }
            if (base.getStored().isEmpty()) {
                continue;   // 已经被吃空的基座不再拉线
            }
            sources.add(Vec3.atCenterOf(basePos).add(0.0D, 0.9D, 0.0D));
        }
        if (sources.isEmpty()) {
            return;
        }

        Vec3 sink = Vec3.atCenterOf(worldPosition).add(0.0D, 0.95D, 0.0D);
        fx.budget(40 + sources.size() * 26);

        double phase = ritualElapsed * 0.12D;
        for (int i = 0; i < sources.size(); i++) {
            drawArc(fx, sources.get(i), sink, phase + i * 0.7D);
        }

        // 台面上方「正在被灌入」的一点聚集感
        fx.bloom(INJECT_PARTICLE, sink, 2, 0.14D, 0.01D);
    }

    /**
     * 最后 3 秒的「蓄力」：副物品已经全被吃空，台上只剩必需品在那儿悬着 ——
     * 这段时间原来一点动静都没有，看着像卡住了。这里在物品周围铺一圈会收缩的暗紫光环、
     * 几缕灵魂火往上缠，再补一点末地烛微光，越接近完成越密。
     *
     * <p>密度随进度递增（半径同时收缩），视觉上就是「能量被压进台面那颗东西里」，
     * 最后正好接上 {@code finishRitual} 的金色光爆。</p>
     */
    private void drawFinaleCharge(ServerLevel level, ServerPlayer player) {
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx == null) {
            return;
        }

        Vec3 center = Vec3.atCenterOf(worldPosition).add(0.0D, 0.95D, 0.0D);

        // 0 → 1 的进度；窗口至少 1 tick，避免极短配方除零
        int window = Math.max(1, CONSUME_TAIL_TICKS);
        int elapsedInWindow = ritualElapsed - (ritualDuration - window);
        double progress = Math.min(1.0D, Math.max(0.0D, (double) elapsedInWindow / window));

        fx.budget(80 + (int) (progress * 80));

        // ① 环绕物品的光环：半径从 0.55 收到 0.15，越近完成收得越紧
        double ringRadius = 0.55D * (1.0D - progress) + 0.15D;
        int ringPoints = 8 + (int) (progress * 6);
        fx.circle(INJECT_PARTICLE, center, ParticleFx.Axis.Y,
                ringRadius, ringPoints, ritualElapsed * 0.16D, 0.04D);

        // ② 向上缠绕的灵魂火（只在后半段明显，避免一开始就太吵）
        if (progress > 0.35D) {
            fx.spiral(ParticleTypes.SOUL_FIRE_FLAME, center.add(0.0D, -0.35D, 0.0D),
                    ParticleFx.Axis.Y, 0.45D, 0.08D, 1.1D, 2.0D,
                    ritualElapsed * 0.1D, 10 + (int) (progress * 8), 0.02D);
        }

        // ③ 中心微光：数量随进度涨，最后一秒最亮
        int sparks = 1 + (int) (progress * 3.0D);
        fx.bloom(ParticleTypes.END_ROD, center, sparks, 0.07D, 0.01D);

        // ④ 最后一秒再补一圈向内收束的附魔光，和完成光爆做个衔接
        if (progress > 0.66D) {
            fx.burst(ParticleTypes.ENCHANT, center, 4 + (int) (progress * 4), 0.22D, false);
        }
    }

    /** 一条二次贝塞尔弧，控制点抬高 1.5 格并向侧面弯出去。 */
    private static void drawArc(ParticleFx fx, Vec3 from, Vec3 to, double phase) {
        Vec3 dir = to.subtract(from);
        Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
        side = side.lengthSqr() > 1.0E-4D ? side.normalize() : new Vec3(1.0D, 0.0D, 0.0D);

        double bow = 0.85D + Math.sin(phase) * 0.35D;
        Vec3 control = from.add(to).scale(0.5D)
                .add(0.0D, 1.5D, 0.0D)
                .add(side.scale(Math.sin(phase * 0.6D) * bow));

        List<Vec3> points = new ArrayList<>(9);
        for (int i = 0; i <= 8; i++) {
            points.add(bezier(from, control, to, i / 8.0D));
        }
        // step 0.55：点距比之前稀一点，弧线不会糊成一条粗带
        fx.polyline(INJECT_PARTICLE, points, 0.55D, 0.03D);
    }

    private static Vec3 bezier(Vec3 a, Vec3 control, Vec3 b, double t) {
        double u = 1.0D - t;
        return a.scale(u * u)
                .add(control.scale(2.0D * u * t))
                .add(b.scale(t * t));
    }

    // ==================== 仪式：收尾 ====================

    /** 成功：先闪一道光，然后按配方类型落地（物品 → 台面上；生物 → 台子正上方）。 */
    private void finishRitual(ServerLevel level, ServerPlayer player) {
        Vec3 center = Vec3.atCenterOf(worldPosition).add(0.0D, 0.95D, 0.0D);

        // 完成光效：走现有着色器体系（skill_burst，屏幕对齐的金色光爆，8 tick ≈ 0.4 秒）
        // ⚠️ ownerEntityId 传 -1（SkillFieldSender.send 内部就是这么发的），
        //    锚点钉在汇灵台上，不会跟着玩家跑。
        SkillFieldSender.send(player, SkillEffectStyles.BURST_LIETIAN,
                center.x, center.y, center.z, 1.25f, 8);

        // 光爆之外再补一圈向内收束的粒子，把「东西被吸成成品」这件事说清楚
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            fx.budget(120);
            fx.burst(ParticleTypes.END_ROD, center, 18, 0.35D, false);
            fx.bloom(ParticleTypes.ENCHANT, center, 10, 0.35D, 0.0D);
        }

        HunlingRitual recipe = this.ritualRecipe;
        boolean spawned = recipe instanceof HunlingSpawnRitualRecipe spawnRecipe
                && spawnMob(level, spawnRecipe);

        if (spawned) {
            // ⚠️ 召唤成功也要清台面 —— 必需品是被仪式「烧掉」的，不是留着的。
            //    之前只写了下面那个 else-if，导致召完生物后绿宝石还稳稳躺在台上。
            setStored(ItemStack.EMPTY);
        } else if (recipe instanceof HunlingRitualRecipe itemRecipe) {
            setStored(itemRecipe.getResult().copy());
        }

        clearRitual(level);

        level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.9f, 1.2f);
        player.displayClientMessage(
                Component.literal(spawned ? "§a§l召唤完成！" : "§a§l仪式达成！").withStyle(ChatFormatting.GREEN), true);
    }

    /**
     * 在汇灵台正上方生成配方指定的生物。
     *
     * <p>属性完全交给现有的年限体系：{@code initNianxian} 按年限推导三围，
     * 再把 {@code MAX_HEALTH} 的 base 值改成推导出来的血量并回满 ——
     * 和 {@code /kunluncontinent monster <id> <年限> true} 是同一套（那条命令就是范例）。</p>
     *
     * @return 是否成功生成
     */
    private boolean spawnMob(ServerLevel level, HunlingSpawnRitualRecipe recipe) {
        EntityType<?> type = EntityType.byString(recipe.getEntityId().toString()).orElse(null);
        if (type == null) {
            return false;
        }
        Entity entity = type.create(level);
        if (!(entity instanceof Mob mob)) {
            return false;
        }

        Vec3 at = Vec3.atBottomCenterOf(worldPosition.above());
        mob.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.initNianxian(recipe.getNianxian());
            float maxHealth = cap.getMaxshengming();
            var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(maxHealth);
                mob.setHealth(maxHealth);
            }
        });

        level.addFreshEntity(mob);
        SynsAPI.synsEntityAttribute(mob);
        return true;
    }

    /**
     * 蓄势前摇的表现：灵魂状粒子从台下绕上来 + 一声低沉的激活音。
     *
     * <p>{@code recipe.getSpawnEffect()} 是留给后续 BOSS 召唤特效的钩子，
     * 目前只记录不解析 —— 需要接具体特效时在这里按 id 分发即可。</p>
     */
    private void playSpawnCharge(ServerLevel level) {
        Vec3 base = Vec3.atCenterOf(worldPosition).add(0.0D, 0.1D, 0.0D);
        ParticleFx fx = ParticleFx.of(level);
        if (fx != null) {
            fx.budget(260);
            fx.spiral(ParticleTypes.SOUL, base, ParticleFx.Axis.Y,
                    1.1D, 0.25D, 2.2D, 3.0D, 0.0D, 56, 0.03D);
            fx.bloom(ParticleTypes.SOUL_FIRE_FLAME, base.add(0.0D, 1.0D, 0.0D), 12, 0.3D, 0.02D);
        }
        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    /**
     * 失败：台面上的必需品、底座上还没消耗掉的副物品，<b>全部弹成掉落物</b>。
     *
     * <p>已经消耗掉的（包括最后 3 秒前清空的那批）不会返还 —— 这是需求里明确要求的惩罚。
     * 这里不发任何提示：精神力不足时调用方已经播报过，方块被挖的情况按需求也不打扰玩家。</p>
     */
    private void failRitual(ServerLevel level) {
        if (!stored.isEmpty()) {
            Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.8D, worldPosition.getZ() + 0.5D,
                    stored);
            setStored(ItemStack.EMPTY);
        }
        for (BlockPos basePos : ritualBases) {
            if (level.getBlockEntity(basePos) instanceof HunlingAltarBlockEntity base
                    && !base.getStored().isEmpty()) {
                Containers.dropItemStack(level,
                        basePos.getX() + 0.5D, basePos.getY() + 0.8D, basePos.getZ() + 0.5D,
                        base.getStored());
                base.setStored(ItemStack.EMPTY);
            }
        }
        clearRitual(level);
    }

    /** 只清理状态（解锁底座、清计时），不动物品。 */
    private void clearRitual(ServerLevel level) {
        this.ritualActive = false;
        this.ritualElapsed = 0;
        this.ritualDuration = 0;
        this.ritualSpiritCost = 0;
        this.ritualSpiritPaid = 0;
        this.ritualConsumed = 0;
        this.ritualTotalItems = 0;
        this.ritualPlayer = null;
        this.ritualRecipe = null;
        this.ritualSpawn = false;
        this.ritualPrespawn = 0;
        this.ritualEndTick = 0L;
        this.ritualIngredients = new ArrayList<>();

        for (BlockPos basePos : ritualBases) {
            if (level.getBlockEntity(basePos) instanceof HunlingAltarBlockEntity base) {
                base.setRitualLocked(false);
            }
        }
        this.ritualBases.clear();
        setChanged();
        syncToClients();
    }

    /**
     * 方块被拆除时调用：仪式强行中止。
     *
     * <p>此时不需要主动「返还」—— 台面和底座的物品各自由自己的 {@code onRemove} 掉出来，
     * 这里只负责清掉仪式状态、解锁其余底座，避免它们被永久锁死。</p>
     */
    public void abortRitualOnRemoval(ServerLevel level) {
        if (ritualActive) {
            clearRitual(level);
        }
    }

    // ==================== 存档 / 同步 ====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!stored.isEmpty()) {
            tag.put(TAG_ITEM, stored.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        // ⚠️ tag 可能是 null —— 见 getUpdateTag 的注释，这里必须挡住
        if (tag == null) {
            this.stored = ItemStack.EMPTY;
            return;
        }
        super.load(tag);
        this.stored = tag.contains(TAG_ITEM) ? ItemStack.of(tag.getCompound(TAG_ITEM)) : ItemStack.EMPTY;
        this.ritualEndTick = tag.getLong(TAG_RITUAL_END);
    }

    /**
     * 网络同步用的数据。
     *
     * <h3>🚨 为什么这里不能直接复用 {@link #saveAdditional}</h3>
     * <p>{@link ClientboundBlockEntityDataPacket} 的构造器里有一句：</p>
     * <pre>
     * this.tag = tag.isEmpty() ? null : tag;
     * </pre>
     * <p>而 {@code saveAdditional} 在「台上没物品」时<b>什么都不写</b>，返回的 tag 就是空的
     * → 包把 tag 换成 {@code null} → 客户端 {@code onDataPacket(null)} → {@code load(null)}
     * <b>直接空指针</b>（默认实现就一句 {@code load(tag)}）。异常在客户端被打包线程吞掉，
     * 表现就是<b>物品被仪式吃掉了，但渲染的东西还飘在台上</b>。</p>
     *
     * <p>所以这里改成无论如何都写一个 {@code StoredItem} 键（空物品就写空的 {@code ItemStack}），
     * 保证外层 tag 非空；{@code load} 那边再额外挡住 null 作为双保险。</p>
     */
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag itemTag = new CompoundTag();
        stored.save(itemTag);          // 空物品 → 空 tag，但键本身会存在
        tag.put(TAG_ITEM, itemTag);
        tag.putLong(TAG_RITUAL_END, ritualEndTick);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
