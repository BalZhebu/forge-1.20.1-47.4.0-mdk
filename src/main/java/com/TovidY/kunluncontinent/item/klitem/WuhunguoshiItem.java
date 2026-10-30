package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.Wuhunname;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 武魂精华：吃下去就能「指定」下一次觉醒的武魂。
 *
 * <h3>两种用法，按玩家当前状态自动区分</h3>
 * <ul>
 *   <li><b>还没觉醒过武魂</b>（0 级，魂力未觉醒）→ 记入<b>预定名单</b>，<b>不立刻觉醒</b>；
 *       下次突破 0→1 的觉醒仪式直接觉醒它（吃两枚 = 双生、吃三枚 = 三生，都是天才资质）；</li>
 *   <li><b>已经觉醒过武魂</b> → 立刻觉醒该武魂（老行为，最多 3 个武魂）。</li>
 * </ul>
 *
 * <h3>为什么 0 级也能吃</h3>
 * <p>以前要求「必须已觉醒武魂才能吃」，等于逼玩家先随机觉醒一个不想要的武魂再吃精华 ——
 * 精华完全失去了「指定」的意义。现在 0 级就能吃，选择权在玩家手里。</p>
 *
 * <p>预定上限见 {@link PlayerHunhuanAPI#MAX_PENDING_ESSENCES}（= 3，也就是一次觉醒最多能出几个武魂）。
 * 吃满 3 枚 → 觉醒直接就是「三生武魂」，到顶了不再追加。</p>
 */
public class WuhunguoshiItem extends Item {
    private String wuhunname;
    public WuhunguoshiItem(Properties properties) {
        super(properties);
    }

    public static ItemStack getWuhunguo(String s) {
        ItemStack itemStack = ItemStack.EMPTY;
        switch (s){
            case Wuhunname.pohunqiang:
                itemStack = new ItemStack(ModItems.GUOSHI_POHUNQIANG.get());
                break;
            case Wuhunname.bahuangji:
                itemStack = new ItemStack(ModItems.GUOSHI_BAHUANGJI.get());
                break;
            case Wuhunname.liejinhu:
                itemStack = new ItemStack(ModItems.GUOSHI_LEIJINHU.get());
                break;
            case Wuhunname.panshijuyuan:
                itemStack = new ItemStack(ModItems.GUOSHI_PANSHIJUYUAN.get());
                    break;
            case Wuhunname.tianshi:
                itemStack = new ItemStack(ModItems.GUOSHI_TIANSHI.get());
        }
        return itemStack;
    }


    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide || !(livingEntity instanceof ServerPlayer serverPlayer)) {
            return itemStack;
        }
        serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            String name = this.getWuhunname();

            if (cap.getWuhunListsname().contains(name)) {
                serverPlayer.sendSystemMessage(Component.literal("你已经觉醒过该武魂了。")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }

            // ── 已经觉醒过武魂：立刻觉醒（保持原有行为）──
            if (!cap.getWuhunListsname().isEmpty()) {
                if (cap.getWuhunListsname().size() >= 3) {
                    serverPlayer.sendSystemMessage(Component.literal("最多只能觉醒 3 个武魂！")
                            .withStyle(ChatFormatting.DARK_RED));
                    return;
                }
                PlayerHunhuanAPI.addWuHun(serverPlayer, name);
                serverPlayer.sendSystemMessage(Component.literal("成功觉醒武魂：" + name)
                        .withStyle(ChatFormatting.GREEN));
                consumeOne(itemStack, serverPlayer);
                return;
            }

            if (cap.getPendingWuhunEssences().contains(name)) {
                serverPlayer.sendSystemMessage(Component.literal("你已经服下过这枚武魂精华了。")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }
            if (cap.getPendingWuhunEssences().size() >= PlayerHunhuanAPI.MAX_PENDING_ESSENCES) {
                serverPlayer.sendSystemMessage(Component.literal(
                                "最多只能服用 " + PlayerHunhuanAPI.MAX_PENDING_ESSENCES + " 枚武魂精华。")
                        .withStyle(ChatFormatting.DARK_RED));
                return;
            }

            cap.addPendingWuhunEssence(name);
            int count = cap.getPendingWuhunEssences().size();

            serverPlayer.sendSystemMessage(Component.literal("§d§l【武魂精华】§f你已炼化 §b" + name + " §f精华。"));
            serverPlayer.sendSystemMessage(Component.literal(
                    "§7下次觉醒时它必定是第一个武魂 §8（已预定 " + count + " / "
                            + PlayerHunhuanAPI.MAX_PENDING_ESSENCES + "）"));
            if (count >= 3) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "§7武魂槽位已满，觉醒武魂时必定为天才资质。"));
            } else if (count == 2) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "§7武魂槽位定下两枚，觉醒武魂时必定为天才资质。"));
            }
            consumeOne(itemStack, serverPlayer);
            SynsAPI.synsPlayerAttribute(serverPlayer);
        });
        return itemStack;
    }

    /** 吃掉一枚（创造模式不消耗）。 */
    private static void consumeOne(ItemStack stack, ServerPlayer player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    public String getWuhunname() {
        return wuhunname;
    }

    public WuhunguoshiItem setWuhunname(String wuhunname) {
        this.wuhunname = wuhunname;
        return this;
    }

    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("服用后觉醒武魂", wuhunname).withStyle(ChatFormatting.DARK_PURPLE));
        list.add(Component.literal("§8尚未觉醒武魂时服用将会觉醒时的第一个武魂"));
        list.add(Component.literal("§8已拥有武魂时服用将只会觉醒指定武魂"));
    }
}
