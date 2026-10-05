package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShenkaoMenu extends AbstractContainerMenu {

    public ShenkaoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv);
    }

    public ShenkaoMenu(int id, Inventory inv) {
        super(ModMenuTypes.SHENKAO_MENU.get(), id);

        // ⭐ 神祇面板**不显示物品栏槽位**。
        //   这里原本添加了 36 个玩家背包槽（9×3+9），会导致：
        //     ① 面板上出现 36 个空槽 + 可以把背包物品塞进来
        //     ② ShenkaoScreen#renderBg 里那圈"槽位衬底"也白画
        //   神考/神祇面板只展示信息，不需要任何槽位，故全部不添加。
        //   quickMoveStack 也因此永不触发（保持返回 EMPTY 即可）。
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("神祇试炼");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new ShenkaoMenu(id, inv);
        }
    }
}