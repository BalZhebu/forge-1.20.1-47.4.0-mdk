package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.klitem.DanYaoItem;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.RegistryObject;

import static com.TovidY.kunluncontinent.item.ModItems.JUHUNPING;

public class LiandanluMenu extends AbstractContainerMenu {

    // ===== Menu 槽位布局（注意：与 ItemHandler 下标不同！）=====
    /** 内丹槽 Menu 下标区间 [0, 4]。 */
    public static final int NEIDAN_SLOT_START = 0;
    public static final int NEIDAN_SLOT_END = 5;
    /** 药渣槽 Menu 下标（对应 ItemHandler 的 17）。 */
    public static final int DROSS_MENU_SLOT = 5;
    /** 聚魂瓶槽 Menu 下标（对应 ItemHandler 的 18）。 */
    public static final int BOTTLE_MENU_SLOT = 6;
    /** 产出槽 Menu 下标区间 [7, 18]。 */
    public static final int RESULT_MENU_SLOT_START = 7;
    public static final int RESULT_MENU_SLOT_END = 19;
    /** 玩家背包起始 Menu 下标。 */
    public static final int PLAYER_INV_START = 19;

    private final IItemHandler internal;
    private final ContainerLevelAccess access;
    final BlockEntity blockEntity;
    private final ContainerData data;

    public LiandanluMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(16));
    }

    public LiandanluMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.LIANDANLU_MENU.get(), id);

        // 0 进度 / 1 总进度 / 2 是否有聚魂瓶 / 3-9 手动小游戏（见 LiandanluBlockEntity#data）
        checkContainerDataCount(data, 16);
        this.blockEntity = entity;
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), entity.getBlockPos());

        this.addDataSlots(data);

        // 获取 BlockEntity 的物品处理器
        this.internal = ((LiandanluBlockEntity) entity).getItemHandler();

        // 1. 注册内丹槽 (Menu 0-4 → ItemHandler 0-4)
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            this.addSlot(new SlotItemHandler(internal, i, 8 + i * 17, 8) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // 只允许内丹或草药，避免玩家把石头塞进材料槽却毫无提示
                    return stack.getItem() instanceof NeidanItem
                            || stack.is(ModItems.GUYUANCAO_ITEM.get())
                            || stack.is(ModItems.FANQICAO_ITEM.get());
                }
            });
        }

        // 2. 注册药渣槽 (Menu 5 → ItemHandler 17)
        this.addSlot(new SlotItemHandler(internal, AlchemicalCalculator.DROSS_SLOT, 8, 41) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 手动炼丹没有药渣这个玩法，直接禁放
                if (!LiandanluMenu.this.isAutoMode()) return false;
                return stack.is(ModBlocks.DROSS_BLOCK.get().asItem());
            }

            // ---- 手动模式整体隐藏：不画、不悬停、不参与任何搬运 ----
            // isActive() 控制"这个槽位是否存在于界面上"（AbstractContainerScreen 渲染前会查它）
            @Override
            public boolean isActive() {
                return LiandanluMenu.this.isAutoMode();
            }

            // 物品本体留在 ItemHandler 里不动，只是这个槽对界面报告"空"
            @Override
            public ItemStack getItem() {
                return LiandanluMenu.this.isAutoMode() ? super.getItem() : ItemStack.EMPTY;
            }
        });

        // 3. 聚魂瓶槽 (Menu 6 → ItemHandler 18)
        this.addSlot(new SlotItemHandler(internal, LiandanluBlockEntity.BOTTLE_SLOT, 34, 41) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 手动炼丹不消耗精神力，瓶槽在界面上也隐藏了
                if (!LiandanluMenu.this.isAutoMode()) return false;
                for (RegistryObject<Item> bottle : JUHUNPING) {
                    if (stack.is(bottle.get())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean isActive() {
                return LiandanluMenu.this.isAutoMode();
            }

            @Override
            public ItemStack getItem() {
                return LiandanluMenu.this.isAutoMode() ? super.getItem() : ItemStack.EMPTY;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int index = LiandanluBlockEntity.RESULT_SLOT_START + (row * 4) + col;
                this.addSlot(new SlotItemHandler(internal, index, 131 + col * 18, 12 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

        addPlayerInventory(inv);
    }

    public ItemStack getBottleStack() {
        return this.slots.get(BOTTLE_MENU_SLOT).getItem();
    }

    // ==================================================================================
    //  手动炼丹：开关 / 小游戏状态 / 按钮
    // ==================================================================================

    /** true = 自动炼丹（原逻辑）；false = 手动小游戏。 */
    public boolean isAutoMode() {
        return this.data.get(3) == 1;
    }

    /** 0 = 未开始，1 = 判定中，2 = 已结束。 */
    public int getManualPhase() {
        return this.data.get(4);
    }

    /** 光标位置 0~1000。 */
    public int getCursor() {
        return this.data.get(5);
    }

    /** 判定区半宽（光标单位）。 */
    public int getZoneHalf() {
        return this.data.get(6);
    }

    /** 当前品质：-1 = 未判定，0~5 = {@code DanYaoQuality} 下标。 */
    public int getManualQuality() {
        return this.data.get(8) - 1;
    }

    /** 上次判定结果：0 无，1 成功，2 失败。 */
    public int getLastResult() {
        return this.data.get(9);
    }

    /** 判定区中心（0~1000），会自己漂移。 */
    public int getZoneCenter() {
        return this.data.get(10);
    }

    /** 本炉累计成功次数（影响光标速度）。 */
    public int getSuccessCount() {
        return this.data.get(11);
    }

    /** 面板状态码（见 {@code LiandanluBlockEntity.STATUS_*}），所有反馈都画在面板上。 */
    public int getManualStatus() {
        return this.data.get(12);
    }

    /** 本轮判定需要的精神力。 */
    public int getManualCost() {
        return this.data.get(13);
    }

    /** 炉难度系数（1.00 = 与丹方同级；<1 更好炼，>1 更难炼）。 */
    public float getManualDifficulty() {
        return this.data.get(14) / 100.0f;
    }

    /** 光标速度（单位/tick），客户端外推用。 */
    public int getCursorSpeed() {
        return this.data.get(7);
    }

    /** 光标当前方向：+1 向右，-1 向左（客户端用来外推显示位置）。 */
    public int getCursorDir() {
        return this.data.get(15) == 1 ? 1 : -1;
    }

    /**
     * 手动模式下药渣槽 / 聚魂瓶槽是<b>整体隐藏</b>的，这里把点击也一起挡掉。
     *
     * <p>只在界面上不画是不够的：槽位还在 Menu 里，玩家点那个位置照样能拿起/放下，
     * 会出现"凭空拿起一个看不见的瓶子"。物品本身留在 ItemHandler 里不动，
     * 切回自动模式就原样出现。</p>
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!isAutoMode() && (slotId == DROSS_MENU_SLOT || slotId == BOTTLE_MENU_SLOT)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    /**
     * 面板按钮 → 方块实体。
     *
     * <p>用 {@code clickMenuButton} 而不是新开网络包：vanilla 的
     * {@code ServerboundContainerButtonClickPacket} 已经带容器 id 与按钮 id，
     * 服务端也会校验玩家确实开着这个界面。</p>
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.blockEntity instanceof LiandanluBlockEntity liandanlu) {
            return liandanlu.handleButton(player, buttonId);
        }
        return false;
    }

    /** 取5 个内丹槽的物品快照，供 GUI 计算概率显示。 */
    public ItemStack[] getMaterialStacks() {
        ItemStack[] materials = new ItemStack[AlchemicalCalculator.MATERIAL_SLOTS];
        for (int i = 0; i < materials.length; i++) {
            materials[i] = this.slots.get(NEIDAN_SLOT_START + i).getItem();
        }
        return materials;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = slot.getItem();
        itemstack = sourceStack.copy();

        // Menu 槽位布局：0-4 内丹 / 5 药渣 / 6 聚魂瓶 / 7-18 产出 / 19+ 玩家背包
        // 旧代码误用 ItemHandler 下标（17/18）当 Menu 下标，导致 shift+点击药渣/聚魂瓶永远失败
        if (index < PLAYER_INV_START) {
            // 机器区 → 玩家背包
            if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = this.moveItemStackTo(sourceStack, NEIDAN_SLOT_START, NEIDAN_SLOT_END, false);
            if (!moved) {
                if (sourceStack.is(ModBlocks.DROSS_BLOCK.get().asItem())) {
                    moved = this.moveItemStackTo(sourceStack, DROSS_MENU_SLOT, DROSS_MENU_SLOT + 1, false);
                } else if (isSoulGatheringBottle(sourceStack)) {
                    moved = this.moveItemStackTo(sourceStack, BOTTLE_MENU_SLOT, BOTTLE_MENU_SLOT + 1, false);
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return itemstack;
    }

    private boolean isSoulGatheringBottle(ItemStack stack) {
        for (RegistryObject<Item> bottle : JUHUNPING) {
            if (stack.is(bottle.get())) return true;
        }
        return false;
    }

    private void addPlayerInventory(Inventory inv) {
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(inv, j + (i + 1) * 9, 25 + j * 18, 86 + i * 18));
        for (int i = 0; i < 9; ++i)
            this.addSlot(new Slot(inv, i, 25 + i * 18, 144));
    }

    public boolean hasDrossBlock() {
        return this.slots.get(DROSS_MENU_SLOT).getItem()
                .is(ModBlocks.DROSS_BLOCK.get().asItem());
    }

    /** 炉等级会影响品质加成，GUI 必须拿到它才能和服务端算出同一份概率。 */
    public int getFurnaceLevel() {
        return ((LiandanluBlockEntity) this.blockEntity).getFurnaceLevel();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }

    public ContainerData getData() {
        return this.data;
    }

}