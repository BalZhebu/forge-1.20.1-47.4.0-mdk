package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.blockentity.HunlingAltarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 汇灵台与底座的公共部分：台面能摆一件物品、能取回、拆掉会掉出来，还负责跑仪式 tick。
 *
 * <h3>交互总规则</h3>
 * <ul>
 *   <li>仪式进行中 / 底座被仪式占用 → <b>一律拒绝</b>（右键提示一句，不打开任何界面）；</li>
 *   <li>其余情况交给 {@link #handleUse}，由子类决定语义：底座是「放一件」，汇灵台是「启动仪式」。</li>
 * </ul>
 *
 * <h3>为什么要把两个方块抽成一个基类</h3>
 * <p>它们各自占一格、各有模型和碰撞箱，但摆放/掉落/同步逻辑一模一样。复制一份就意味着
 * 以后改规则要改两处 —— 之前吟唱那种「两处逻辑迟早写歪」的坑不想再踩。</p>
 */
public abstract class AbstractHunlingAltarBlock extends BaseEntityBlock {

    protected AbstractHunlingAltarBlock(Properties properties) {
        super(properties);
    }

    /** BaseEntityBlock 默认是 INVISIBLE（只靠 BER 画），这里要正常渲染方块模型。 */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HunlingAltarBlockEntity(pos, state);
    }

    /** 仪式 tick 只在服务端跑。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ModBlockEntities.HUNLING_ALTAR_BE.get(), HunlingAltarBlockEntity::serverTick);
    }

    // ==================== 交互 ====================

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            // 客户端只负责挥手动画，真正的判定在服务端
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof HunlingAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        // 仪式进行中：台上是必需品，禁止取；底座是耗材，禁止取放
        if (altar.isRitualActive()) {
            player.displayClientMessage(Component.literal("§7仪式进行中，无法取放供品。"), true);
            return InteractionResult.CONSUME;
        }
        if (altar.isRitualLocked()) {
            player.displayClientMessage(Component.literal("§7这座基座正被仪式占用。"), true);
            return InteractionResult.CONSUME;
        }

        return handleUse(level, pos, player, hand, altar);
    }

    /** 子类实现具体的放取语义。 */
    protected abstract InteractionResult handleUse(Level level, BlockPos pos, Player player,
                                                   InteractionHand hand, HunlingAltarBlockEntity altar);

    /** 默认语义：空台 + 手持 → 放一个；有物品 + 空手 → 取回。底座直接用这个。 */
    protected InteractionResult handleSimplePlace(Level level, BlockPos pos, Player player,
                                                  InteractionHand hand, HunlingAltarBlockEntity altar) {
        ItemStack held = player.getItemInHand(hand);

        if (altar.isEmpty()) {
            if (held.isEmpty()) {
                return InteractionResult.PASS;
            }
            altar.setStored(held.copyWithCount(1));
            if (!player.isCreative()) {
                held.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            takeItem(level, pos, player, altar);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(
                Component.literal("§7空手右键可取回。"), true);
        return InteractionResult.CONSUME;
    }

    /** 把台面上的物品还给玩家（塞不下就掉在脚下）。 */
    protected static void takeItem(Level level, BlockPos pos, Player player, HunlingAltarBlockEntity altar) {
        ItemStack stored = altar.getStored().copy();
        altar.setStored(ItemStack.EMPTY);
        if (!player.getInventory().add(stored)) {
            player.drop(stored, false);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kunluncontinent.hunling_altar")
                .withStyle(ChatFormatting.GRAY));
    }

    // ==================== 拆除 ====================

    /**
     * 方块被拆掉时：中止仪式 + 掉出台面物品。
     *
     * <p>⚠️ 必须挂在 {@code onRemove}：{@code Level#destroyBlock} 的顺序是
     * {@code playerWillDestroy → removeBlock（触发本方法）→ playerDestroy}，
     * 挂 {@code playerDestroy} 时 BlockEntity 已经被移除了，取不到物品。</p>
     *
     * <p>按需求，强行拆除时<b>已消耗的副物品不返还、必需品照常掉出</b>，且不额外发提示。</p>
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof HunlingAltarBlockEntity altar) {
                if (level instanceof ServerLevel serverLevel) {
                    altar.abortRitualOnRemoval(serverLevel);
                }
                if (!altar.getStored().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                            altar.getStored());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
