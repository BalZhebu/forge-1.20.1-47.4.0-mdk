package com.TovidY.kunluncontinent.screen.playernpc.shoumai;

import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * NPC 售卖界面容器。
 *
 * 槽位布局：
 *   0 ~ 13  输入槽（玩家放入可售卖物品）
 *  14 ~ 16  输出槽（铜/银/金魂币，不可取走）
 *  17 ~ 43  玩家背包（27 格）
 *  44 ~ 52  玩家快捷栏（9 格）
 */

public class SellMenu extends AbstractContainerMenu {

    private static final int INPUT_COUNT     = 14;
    private static final int PLAYER_INV_OFF  = INPUT_COUNT; // 14

    public final Level world;
    public final Player entity;
    @Nullable public final PlayerNpcEntity npc;

    // 内部存放出售物品的 14 个格子容器
    private final Container sellContainer = new SimpleContainer(INPUT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            SellMenu.this.slotsChanged(this);
        }
    };

    private int outputCopper = 0;
    private int outputSilver = 0;
    private int outputGold   = 0;
    private boolean sellConfirmed = false;

    // ── 构造函数 ───────────────────────────

    public SellMenu(int id, Inventory inv) {
        this(id, inv, (PlayerNpcEntity) null, null);
    }

    public SellMenu(int id, Inventory inv, @Nullable PlayerNpcEntity npc) {
        this(id, inv, npc, null);
    }

    public SellMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getEntityFromBuf(inv.player.level(), buf), buf);
    }

    // 主构造函数
    private SellMenu(int id, Inventory inv, @Nullable PlayerNpcEntity npc, @Nullable FriendlyByteBuf buf) {
        super(ModMenuTypes.SELL_MENU.get(), id);
        this.entity = inv.player;
        this.world  = inv.player.level();
        this.npc    = npc;

        // 1. 注册按钮确认状态同步
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() { return sellConfirmed ? 1 : 0; }
            @Override
            public void set(int val) { sellConfirmed = (val == 1); }
        });

        // 2. 输入槽 0~13
        int inX0 = 12, inY0 = 15;
        for (int i = 0; i < INPUT_COUNT; i++) {
            addSlot(new Slot(this.sellContainer, i, inX0 + (i % 7) * 18, inY0 + (i / 7) * 18) {
                @Override
                public boolean mayPlace(ItemStack s) {
                    return !s.isEmpty() && SellPriceRegistry.getPrice(s.getItem()) != null;
                }
                @Override public boolean mayPickup(Player p)   { return true; }
                @Override public int getMaxStackSize()         { return 64; }
            });
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlot(new Slot(inv, c + (r + 1) * 9, 30 + c * 18, 60 + r * 18));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlot(new Slot(inv, c, 30 + c * 18, 118));
        }
    }

    // ── 公开 API ────────────────────────────────────────

    public String getTotalSummary() {
        calcTotal();
        return SellPriceRegistry.formatPrice(
                new SellPriceRegistry.PriceEntry(outputCopper, outputSilver, outputGold));
    }

    public void toggleSell(ServerPlayer player) {
        if (!sellConfirmed) {
            sellConfirmed = true;
        } else {
            sell(player);
        }
        this.broadcastChanges();
    }

    public boolean isSellConfirmed() { return sellConfirmed; }
    public int    getOutputCopper()  { return outputCopper; }
    public int    getOutputSilver()  { return outputSilver; }
    public int    getOutputGold()    { return outputGold;   }

    // ── 私有逻辑 ────────────────────────────────────────

    private void calcTotal() {
        int c = 0, s = 0, g = 0;
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = sellContainer.getItem(i);
            if (stack.isEmpty()) continue;
            SellPriceRegistry.PriceEntry entry = SellPriceRegistry.getPrice(stack.getItem());
            if (entry == null) continue;
            c += entry.copper * stack.getCount();
            s += entry.silver * stack.getCount();
            g += entry.gold   * stack.getCount();
        }
        this.outputCopper = c;
        this.outputSilver = s;
        this.outputGold   = g;
    }

    private void sell(ServerPlayer player) {
        calcTotal();

        if (outputCopper > 0 || outputSilver > 0 || outputGold > 0) {
            giveCoinsToPlayer(player, ModItems.COPPER_SOUL_COIN.get(), outputCopper);
            giveCoinsToPlayer(player, ModItems.SILVER_SOUL_COIN.get(), outputSilver);
            giveCoinsToPlayer(player, ModItems.GOLDEN_SOUL_COIN.get(), outputGold);
            for (int i = 0; i < INPUT_COUNT; i++) {
                ItemStack stack = sellContainer.getItem(i);
                if (!stack.isEmpty() && SellPriceRegistry.getPrice(stack.getItem()) != null) {
                    sellContainer.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        outputCopper = outputSilver = outputGold = 0;
        sellConfirmed = false;
        this.broadcastChanges();
    }

    private void giveCoinsToPlayer(ServerPlayer player, Item coinItem, int totalAmount) {
        while (totalAmount > 0) {
            int count = Math.min(totalAmount, 64);
            ItemStack stack = new ItemStack(coinItem, count);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            totalAmount -= count;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (npc != null) {
            return npc.isAlive() && player.distanceToSqr(npc) < 64.0;
        }
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        calcTotal();
        this.broadcastChanges();
    }

    // 【修补 1】：把 me.removed 纠正为正确的 removed 重写方法
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!this.world.isClientSide) {
            this.clearContainer(player, this.sellContainer);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack clicked = slot.getItem();
        ItemStack copy = clicked.copy();

        if (index < INPUT_COUNT) {
            // 点击出售输入槽 → 退回到背包/快捷栏
            if (!moveItemStackTo(clicked, PLAYER_INV_OFF, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_OFF) {
            // 点击背包 → 放入出售槽
            if (!moveItemStackTo(clicked, 0, INPUT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (clicked.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (clicked.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, clicked);
        return copy;
    }

    @Nullable
    private static PlayerNpcEntity getEntityFromBuf(Level level, FriendlyByteBuf buf) {
        if (buf == null || !buf.isReadable()) return null;
        int entityId = buf.readInt();
        Entity entity = level.getEntity(entityId);
        return entity instanceof PlayerNpcEntity npcEntity ? npcEntity : null;
    }
}