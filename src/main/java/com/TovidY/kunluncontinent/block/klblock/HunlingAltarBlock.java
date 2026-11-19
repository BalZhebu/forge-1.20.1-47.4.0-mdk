package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.blockentity.HunlingAltarBlockEntity;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitual;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 汇灵台 —— 仪式的主体。
 *
 * <h3>怎么开始一场仪式</h3>
 * <ol>
 *   <li>在周围的底座上摆好副物品（8 颗钻石就摆 8 座，每座 1 颗）；</li>
 *   <li><b>手里拿着必需品</b>（比如钻石）右键汇灵台；</li>
 *   <li>台子以自己为中心扫 {@link #SCAN_RADIUS} 格内的底座，
 *       把「必需品 + 底座上的所有物品」拿去和配方表比对：
 *       <ul>
 *         <li>匹配 → 必需品放上台、仪式开始；</li>
 *         <li>不匹配 → 提示一句，什么都不消耗。</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>台面物品不再是「想放就放」的：否则玩家会先把必需品摆上去，仪式永远启动不了。</p>
 *
 * <h3>为什么只在启动时扫一次</h3>
 * <p>范围是 10×10×10（中心 ±5），一次扫描是 1331 个格子。只在右键那一下扫，
 * 之后每 tick 只遍历记下来的那十几座底座，不会每次都扫全区。</p>
 */
public class HunlingAltarBlock extends AbstractHunlingAltarBlock {

    /** 扫描半径：10×10×10 → ±5。嫌卡就调小这个。 */
    public static final int SCAN_RADIUS = 5;

    /** 与模型元素一一对应，栏杆之间的缝是真镂空。 */
    protected static final VoxelShape SHAPE = Stream.of(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D),   // 底盘
            Block.box(3.0D, 1.0D, 3.0D, 13.0D, 8.0D, 13.0D),   // 主体
            Block.box(2.0D, 8.0D, 2.0D, 14.0D, 9.0D, 14.0D),   // 台面
            Block.box(1.0D, 9.0D, 5.0D, 3.0D, 10.0D, 10.0D),   // 西侧栏
            Block.box(13.0D, 9.0D, 5.0D, 15.0D, 10.0D, 10.0D), // 东侧栏
            Block.box(6.0D, 9.0D, 13.0D, 10.0D, 10.0D, 15.0D), // 南侧栏
            Block.box(6.0D, 6.0D, 1.0D, 10.0D, 9.0D, 2.0D),    // 北侧柱
            Block.box(1.0D, 6.0D, 5.0D, 2.0D, 9.0D, 10.0D),    // 西侧柱
            Block.box(14.0D, 6.0D, 5.0D, 15.0D, 9.0D, 10.0D),  // 东侧柱
            Block.box(6.0D, 6.0D, 14.0D, 10.0D, 9.0D, 15.0D),  // 南侧柱
            Block.box(6.0D, 9.0D, 1.0D, 10.0D, 10.0D, 3.0D)    // 北侧栏
    ).reduce(Shapes::or).get();

    /** 扫描结果的一条：底座坐标 + 上面摆的东西。 */
    private record BaseSlot(BlockPos pos, ItemStack stack) {
    }

    public HunlingAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ==================== 交互 ====================

    @Override
    protected InteractionResult handleUse(Level level, BlockPos pos, Player player,
                                          InteractionHand hand, HunlingAltarBlockEntity altar) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // 台上有东西（成品 / 上次残留）→ 空手取回，其余一律拒绝
        if (!altar.isEmpty()) {
            if (held.isEmpty()) {
                takeItem(level, pos, player, altar);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(
                    Component.literal("§7空手右键可取回。"), true);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }

        // 扫描底座
        List<BaseSlot> slots = scanBases(serverLevel, pos);
        if (slots.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§7配方错误，请检查配方材料"), true);
            return InteractionResult.CONSUME;
        }

        List<ItemStack> baseStacks = new ArrayList<>(slots.size());
        for (BaseSlot slot : slots) {
            baseStacks.add(slot.stack());
        }

        HunlingRitual recipe = findRecipe(serverLevel, held, baseStacks);
        if (recipe == null) {
            player.displayClientMessage(
                    Component.literal("§c没有与之匹配的仪式：检查必需品与基座上的供品"), true);
            return InteractionResult.CONSUME;
        }

        // 召唤生物版：启动前校验实体 ID。写错 id / 模组被删时直接拒绝，
        // 而不是让玩家先烧掉精神力与材料才发现「召不出来」。
        if (recipe instanceof HunlingSpawnRitualRecipe spawn && !spawn.isEntityAvailable()) {
            player.displayClientMessage(
                    Component.literal("§c配方里的生物不存在：" + spawn.getEntityId()), true);
            return InteractionResult.CONSUME;
        }

        // 必需品放上台面，然后开跑
        altar.setStored(held.copyWithCount(1));
        if (!player.isCreative()) {
            held.shrink(1);
        }
        List<BlockPos> basePositions = new ArrayList<>(slots.size());
        for (BaseSlot slot : slots) {
            basePositions.add(slot.pos());
        }
        altar.startRitual(serverLevel, serverPlayer, recipe, basePositions);
        return InteractionResult.CONSUME;
    }

    // ==================== 底座扫描 ====================

    /**
     * 扫出周围「放着东西」的底座，按坐标排序保证消耗顺序稳定。
     *
     * <p>只扫一次（启动瞬间），之后 tick 里遍历的是记下来的坐标列表。</p>
     */
    private static List<BaseSlot> scanBases(ServerLevel level, BlockPos center) {
        List<BaseSlot> slots = new ArrayList<>();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    // ⚠️ 先判区块加载，否则 getBlockState 会强制加载远处区块
                    if (!level.hasChunkAt(p)) {
                        continue;
                    }
                    if (!(level.getBlockState(p).getBlock() instanceof HunlingAltarBaseBlock)) {
                        continue;
                    }
                    if (!(level.getBlockEntity(p) instanceof HunlingAltarBlockEntity be)) {
                        continue;
                    }
                    if (be.getStored().isEmpty()) {
                        continue;
                    }
                    slots.add(new BaseSlot(p.immutable(), be.getStored()));
                }
            }
        }
        // 固定顺序 = 固定消耗顺序，避免同一份供品两次仪式吃法不同
        slots.sort(Comparator.comparingLong(slot -> slot.pos().asLong()));
        return slots;
    }

    /**
     * 找一条能成立的仪式配方：先查「产出物品」那批，再查「召唤生物」那批。
     *
     * <p>⚠️ 1.20.1 的 {@code getAllRecipesFor} 直接返回配方本体列表（不是 {@code RecipeHolder}）。</p>
     */
    private static HunlingRitual findRecipe(ServerLevel level, ItemStack catalyst,
                                            List<ItemStack> baseStacks) {
        for (HunlingRitualRecipe recipe
                : level.getRecipeManager().getAllRecipesFor(ModRecipes.HUNLING_RITUAL_TYPE.get())) {
            if (recipe.matchesRitual(catalyst, baseStacks)) {
                return recipe;
            }
        }
        for (HunlingSpawnRitualRecipe recipe
                : level.getRecipeManager().getAllRecipesFor(ModRecipes.HUNLING_SPAWN_TYPE.get())) {
            if (recipe.matchesRitual(catalyst, baseStacks)) {
                return recipe;
            }
        }
        return null;
    }
}
