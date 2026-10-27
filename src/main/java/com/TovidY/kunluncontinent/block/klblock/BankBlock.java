package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.bank.BankExchange;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.screen.bank.BankMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 银行方块：<b>只有正面</b>用 {@code bank_block.png}，其余五面用 {@code bank_block1.png}。
 *
 * <h3>「正面」是哪一面</h3>
 * <p>指玩家放下方块时<b>朝向玩家自己</b>的那一面。{@code FACING} 存的就是「正面朝向」：</p>
 * <pre>
 *   context.getHorizontalDirection()          // 玩家视线方向（朝外）
 *        .getOpposite()                       // 取反 = 由方块指向玩家 = 正面朝向
 * </pre>
 * <p>所以玩家站在南边朝北放，{@code FACING=NORTH}，正面就是北面 —— 正好面对玩家。</p>
 *
 * <h3>模型怎么对上</h3>
 * <p>用原版 {@code block/orientable} 模型：{@code front} 是<b>北面</b>，{@code side} 是其余三个水平面，
 * {@code top}/{@code bottom} 各一张。blockstate 里按 {@code facing} 转 y 轴
 * （north 0° / east 90° / south 180° / west 270°），这样任何朝向正面都朝外。</p>
 *
 * <h3>不可挖掘</h3>
 * <p>完全照搬基岩的数值：{@code strength(-1F, 3600000F)} ——
 * 生存模式挖不动（{@code getDestroyProgress} 直接返回 0）、爆炸也炸不掉，
 * 但<b>创造模式可以正常破坏</b>（和基岩一致）。这里不要额外加
 * {@code onDestroyedByPlayer} 之类的拦截，否则创造模式也拆不掉，会变成卡死的方块。</p>
 */
public class BankBlock extends HorizontalDirectionalBlock {

    public BankBlock(Properties properties) {
        super(properties);
        // 默认朝北，避免未初始化的 state 出现 null facing
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // ⚠️ HorizontalDirectionalBlock 只提供了 rotate/mirror，FACING 要自己登记
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 取反 → FACING 指向「面向玩家」的那一面
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * 右键打开昆仑银行面板。
     *
     * <p>银行<b>没有 BlockEntity</b>：余额存在玩家的 capability 里（个人账户），
     * 所以这里只是"开一个界面"，方块本身不存任何数据 —— 拆了重建也不会丢钱。</p>
     *
     * <p>开界面前先 {@code synsPlayerAttribute}：面板直接读客户端 capability 显示余额，
     * 不先同步一次的话，刚上线/刚交易过的玩家会看到旧数字。</p>
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            // 开面板前把「当日汇率」和「今日换钱次数」推到最新，再同步一次属性 —— 面板上的数字才是当前值
            BankAccount.refreshRates(serverPlayer);
            BankExchange.ensureTradeDay(serverPlayer);
            SynsAPI.synsPlayerAttribute(serverPlayer);
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inv, p) -> new BankMenu(id, inv, pos),
                    Component.translatable("container.kunluncontinent.bank")), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kunluncontinent.bank_block")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
