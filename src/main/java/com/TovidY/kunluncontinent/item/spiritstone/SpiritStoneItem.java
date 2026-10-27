package com.TovidY.kunluncontinent.item.spiritstone;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 蕴灵石：右键炼化其中的灵气，转化为玩家修为（经验值 {@code jingyan}）。
 *
 * <h3>两个维度</h3>
 * <ul>
 *   <li><b>阶</b>：由物品本身决定（{@code first_spirit_stone} = 1 阶 … {@code nine_spirit_stone} = 9 阶），
 *       修为基数查表 {@code SpiritStoneQuality.BASE_EXP}（1 阶 = 4，逐阶自定义）；</li>
 *   <li><b>品质</b>：存在 {@code ItemStack} 的 NBT 里，见 {@link SpiritStoneQuality}，决定倍率。</li>
 * </ul>
 *
 * <p>修为 = 各阶基数（见 {@code SpiritStoneQuality.BASE_EXP}）× 品质倍率。
 * 基数（上品基准）：1阶 4 / 2阶 16 / 3阶 36 / 4阶 64 / 5阶 100 /
 * 6阶 144 / 7阶 196 / 8阶 256 / 9阶 324。
 * 品质再乘效率：下品 ×0.4、中品 ×0.7、上品 ×1.0（±0%）、极品 ×1.5、绝品 ×2.0、仙品 ×3.0，
 * 九阶仙品 = 972 点。</p>
 *
 * <p><b>废品</b>（倍率 0）右键不会消耗，只提示"毫无灵气"。</p>
 *
 * <p>物品名由 {@link #getName(ItemStack)} 动态拼成「【品质】X阶蕴灵石」，
 * 所以同一件物品在不同品质下显示的名字不同。</p>
 *
 * <p>⚠️ 品质的产出逻辑还没做：读不到 NBT 时按 {@link SpiritStoneQuality#DEFAULT}（上品，基准 ±0%）处理，
 * 以后在掉落 / 合成里调 {@link SpiritStoneQuality#set(ItemStack, SpiritStoneQuality)} 写入即可。</p>
 */
public class SpiritStoneItem extends Item {

    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 9;

    /** 这一颗是几阶（1~9）。 */
    private final int tier;

    public SpiritStoneItem(Properties properties, int tier) {
        super(properties);
        this.tier = Mth.clamp(tier, MIN_TIER, MAX_TIER);
    }

    public int getTier() {
        return tier;
    }

    /** 这颗石头当前能提供的修为（按它自己携带的品质算）。 */
    public int getExp(ItemStack stack) {
        return SpiritStoneQuality.expOf(tier, stack);
    }

    // ==================== 物品名 ====================

    /**
     * 名字动态拼成「【仙品】九阶蕴灵石」。
     *
     * <p>基础名仍然走语言文件的 {@code item.kunluncontinent.xxx}，所以以后改阶数名（或加英文）
     * 只改 lang；品质前缀由品质枚举的颜色决定。</p>
     */
    @Override
    public Component getName(ItemStack stack) {
        SpiritStoneQuality quality = SpiritStoneQuality.get(stack);
        return Component.literal("【")
                .append(Component.literal(quality.displayName()).withStyle(quality.color()))
                .append(Component.literal("】"))
                .append(Component.translatable(this.getDescriptionId(stack)));
    }

    // ==================== 右键炼化 ====================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpiritStoneQuality quality = SpiritStoneQuality.get(stack);
        int exp = SpiritStoneQuality.expOf(tier, quality);

        // ---- 废品：一点修为都没有，石头也不吃 ----
        if (exp <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("§8这块蕴灵石空空如也，一丝灵气都没有。"), true);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5f, 0.7f);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerAttributeCapability cap = serverPlayer
                    .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);

            // 199 级是真正的尽头，经验吸了也用不上
            if (cap != null && cap.getDengji() >= 199) {
                serverPlayer.displayClientMessage(
                        Component.literal("§7修为已至尽头，这块蕴灵石对你毫无用处。"), true);
                return InteractionResultHolder.fail(stack);
            }

            // 真正加修为。addJingyan 内部会在经验满时触发突破，
            // 所以这里只报"投入了多少"，升级本身由 PlayerUpgradeSystem 自己发消息。
            PlayerHunhuanAPI.addJingyan(serverPlayer, exp);

            // 吸收表现：青色音效 + 一圈上升的末地烛粒子
            level.playSound(null, player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.35f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 0.9, player.getZ(),
                        18, 0.35, 0.55, 0.35, 0.015);
            }

            stack.shrink(1);

            int cur = cap != null ? (int) cap.getJingyan() : 0;
            int max = cap != null ? (int) cap.getMaxjingyan() : 0;
            player.displayClientMessage(Component.literal(
                    "§b炼化 §e" + String.format("%,d", exp) + " §b点修为"
                            + " §7(" + quality.coloredName() + "§7 · " + tier + " 阶)"
                            + " §8[" + cur + "/" + max + "]"), true);
        }

        return InteractionResultHolder.consume(stack);
    }

    // ==================== 描述 ====================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        SpiritStoneQuality quality = SpiritStoneQuality.get(stack);
        int exp = SpiritStoneQuality.expOf(tier, quality);

        tooltip.add(Component.literal("§7品阶: §f" + tier + " 阶")
                .append(Component.literal("  §8|  §7品质: "))
                .append(Component.literal(quality.displayName()).withStyle(quality.color())));

        // 效率：以「上品」为基准（±0%），低于它的削弱、高于它的增幅
        tooltip.add(Component.literal("§7发挥效率: " + quality.efficiencyText())
                .append(Component.literal("  §8(上品为基准)")));

        if (exp <= 0) {
            tooltip.add(Component.literal("§8内里空空如也，无法炼化。"));
        } else {
            tooltip.add(Component.literal("§7可炼化修为: §b" + String.format("%,d", exp) + " §7点"));
            tooltip.add(Component.literal("§8右键吸收，化为自身修为。"));
        }

        if (quality.isPrecious()) {
            tooltip.add(Component.literal("§d※ 灵气凝而不散，隐有宝光").withStyle(ChatFormatting.ITALIC));
        }

        // 风味描述
        tooltip.add(Component.literal("§8§o灵石之髓，凝天地灵机，纳之为己用。"));
    }

    /** 绝品 / 仙品带附魔光效，一眼能看出不是凡品。 */
    @Override
    public boolean isFoil(ItemStack stack) {
        return SpiritStoneQuality.get(stack).isPrecious();
    }
}
