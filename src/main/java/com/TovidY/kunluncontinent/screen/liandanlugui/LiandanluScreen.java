package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.screen.KunlunContainerScreen;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class LiandanluScreen extends KunlunContainerScreen<LiandanluMenu> {

    private static final ResourceLocation FLAME_EMPTY =
            new ResourceLocation(KlMain.MOD_ID, "textures/screens/huoyan.png");
    private static final ResourceLocation FLAME_FULL =
            new ResourceLocation(KlMain.MOD_ID, "textures/screens/huoyanmax.png");

    private static final int ORIGINAL_FLAME_SIZE = 14;
    private static final int FLAME_SCALE = 2;
    private static final int DISPLAY_FLAME_SIZE = ORIGINAL_FLAME_SIZE * FLAME_SCALE;

    private final SimpleContainer lastContainer = new SimpleContainer(AlchemicalCalculator.MATERIAL_SLOTS);
    private Optional<LiandanRecipe> cachedRecipe = Optional.empty();
    private int lastSlotHash = 0;

    /** 本帧的 partialTick：用来把光标外推到"当前"画面时刻。 */
    private float partialTicks = 0f;

    /** 上一次看到的模式/阶段，用来决定是否重建按钮（模式是小游戏里随时切的）。 */
    private boolean lastAutoMode = true;
    private int lastPhase = 0;

    public LiandanluScreen(LiandanluMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 211;
        this.imageHeight = 170;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.lastAutoMode = this.menu.isAutoMode();
        this.lastPhase = this.menu.getManualPhase();
        buildButtons();
    }

    /**
     * 左侧两个按钮：自动/手动切换 + 开始炼丹（手动模式下才有）。
     *
     * <p>按钮放在<b>面板外侧左边</b>：面板内部已经被 5 个内丹槽 + 12 个产出槽塞满了，
     * 再挤进去会压到槽位。屏幕太窄时往回贴到 4px。</p>
     */
    private void buildButtons() {
        this.clearWidgets();
        int x = Math.max(4, this.leftPos - 74);

        this.addRenderableWidget(Button.builder(
                Component.literal("自动炼丹：" + (this.menu.isAutoMode() ? "§a开" : "§c关")),
                b -> sendButton(LiandanluBlockEntity.BUTTON_TOGGLE_AUTO)
        ).bounds(x, this.topPos + 8, 70, 18).build());

        if (!this.menu.isAutoMode()) {
            boolean running = this.menu.getManualPhase() == LiandanluBlockEntity.PHASE_RUNNING;
            this.addRenderableWidget(Button.builder(
                    Component.literal(running ? "§e判 定" : "§a开始炼丹"),
                    b -> sendButton(LiandanluBlockEntity.BUTTON_START_OR_JUDGE)
            ).bounds(x, this.topPos + 32, 70, 18).build());

            // 判定中才给「收丹」：升到仙丹会自动收，其余品质想拿走就靠它
            if (running) {
                this.addRenderableWidget(Button.builder(
                        Component.literal("§6收 丹"),
                        b -> sendButton(LiandanluBlockEntity.BUTTON_COLLECT)
                ).bounds(x, this.topPos + 56, 70, 18).build());
            }
        }
    }

    private void sendButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    private void updateRecipeCache() {
        if (this.minecraft == null || this.minecraft.level == null) return;

        // 哈希必须包含品质 NBT —— 否则玩家把凡品内丹换成绝品内丹（物品 ID 相同、数量相同）
        // 时不会重算配方与概率显示
        int currentHash = 1;
        ItemStack[] materials = this.menu.getMaterialStacks();
        for (ItemStack stack : materials) {
            currentHash = 31 * currentHash + stack.getItem().hashCode() + stack.getCount();
            if (stack.getItem() instanceof NeidanItem neidan) {
                currentHash = 31 * currentHash + neidan.getQuality(stack).ordinal();
            }
        }

        if (currentHash != lastSlotHash) {
            this.lastSlotHash = currentHash;
            for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
                this.lastContainer.setItem(i, materials[i]);
            }
            this.cachedRecipe = this.minecraft.level.getRecipeManager()
                    .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), this.lastContainer, this.minecraft.level);
        }
    }

    @Override
    protected void renderPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        updateRecipeCache();
        this.partialTicks = partialTicks;


        // 模式 / 阶段变了要重建按钮（面板里可以随时切自动/手动，阶段也会在判定后跳变）
        if (this.menu.isAutoMode() != this.lastAutoMode || this.menu.getManualPhase() != this.lastPhase) {
            this.lastAutoMode = this.menu.isAutoMode();
            this.lastPhase = this.menu.getManualPhase();
            buildButtons();
        }

        this.renderBackground(guiGraphics);
        super.renderPanel(guiGraphics, mouseX, mouseY, partialTicks);
        renderAlchmenyInfo(guiGraphics);

        // 手动模式下悬停到被隐藏的两个槽位时不要弹 tooltip（否则会看到槽里的物品名）
        boolean hiddenHover = !this.menu.isAutoMode()
                && this.hoveredSlot != null && isManualHiddenSlot(this.hoveredSlot);
        if (!hiddenHover) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // 1. 渲染古风通用手绘背景主面板
        KunlunGuiHelper.renderKunlunBackground(guiGraphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        boolean auto = this.menu.isAutoMode();

        // 2. 统一手绘 Slot 衬底（手动模式不画药渣槽 / 聚魂瓶槽）
        for (Slot slot : this.menu.slots) {
            if (!auto && isManualHiddenSlot(slot)) {
                continue;
            }
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;

            // 只要不属于玩家背包/快捷栏（Y < 80 区域），全部渲染统一金色边框
            boolean isMachineSlot = slot.y < 80;
            KunlunGuiHelper.renderSlotBackground(guiGraphics, slotX, slotY, isMachineSlot);
        }

        // 手动模式：不画火焰进度条（进度条位置让给小游戏）
        if (!auto) {
            return;
        }

        // 3. 渲染炼丹火焰进度条
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        int flameX = this.leftPos + 98;
        int flameY = this.topPos + 28;

        guiGraphics.blit(FLAME_EMPTY, flameX, flameY, DISPLAY_FLAME_SIZE, DISPLAY_FLAME_SIZE, 0, 0, ORIGINAL_FLAME_SIZE, ORIGINAL_FLAME_SIZE, ORIGINAL_FLAME_SIZE, ORIGINAL_FLAME_SIZE);

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            float ratio = Math.min(1.0F, (float) progress / maxProgress);

            int scaledSourceHeight = (int)(ratio * ORIGINAL_FLAME_SIZE);
            int scaledDisplayHeight = scaledSourceHeight * FLAME_SCALE;

            if (scaledSourceHeight > 0) {
                int textureVOffset = ORIGINAL_FLAME_SIZE - scaledSourceHeight;
                int screenYOffset = DISPLAY_FLAME_SIZE - scaledDisplayHeight;

                guiGraphics.blit(FLAME_FULL,
                        flameX,
                        flameY + screenYOffset,
                        DISPLAY_FLAME_SIZE,
                        scaledDisplayHeight,
                        0,
                        (float) textureVOffset,
                        ORIGINAL_FLAME_SIZE,
                        scaledSourceHeight,
                        ORIGINAL_FLAME_SIZE,
                        ORIGINAL_FLAME_SIZE
                );
            }
        }
        RenderSystem.disableBlend();
    }

    private void renderAlchmenyInfo(GuiGraphics guiGraphics) {
        if (!this.menu.isAutoMode()) {
            renderManualUi(guiGraphics);
            return;
        }

        renderEnergyStatus(guiGraphics);

        if (cachedRecipe.isPresent()) {
            LiandanRecipe recipe = cachedRecipe.get();
            ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
            Component resultName = Component.literal("预测：").append(resultStack.getHoverName());
            guiGraphics.drawString(this.font, resultName, this.leftPos + 8, this.topPos + 28, 0xFFD700, true);

            renderProbabilities(guiGraphics, recipe);
        } else {
            guiGraphics.drawString(this.font, "等待投放材料...", this.leftPos + 8, this.topPos + 28, 0xAAAAAA, true);
        }
    }

    private void renderEnergyStatus(GuiGraphics guiGraphics) {
        int hasBottle = this.menu.getData().get(2);
        String statusText;
        int color;

        ItemStack bottleStack = this.menu.getBottleStack();

        if (hasBottle == 0) {
            statusText = "缺少聚魂瓶";
            color = 0xFF5555;
        } else if (bottleStack.isEmpty()) {
            statusText = "同步中...";
            color = 0xAAAAAA;
        } else {
            if (!AlchemicalCalculator.hasAnyMaterial(this.menu.getMaterialStacks())) {
                statusText = "请放入内丹...";
                color = 0xFFCC00;
            } else if (cachedRecipe.isEmpty()) {
                statusText = "配方未匹配";
                color = 0xFF5555;
            } else if (bottleStack.getItem() instanceof SoulGatheringBottleItem bottle) {
                int cost = cachedRecipe.get().getEnergyCost();
                if (bottle.getNengliang(null, bottleStack) < cost) {
                    statusText = "能量不足: " + cost;
                    color = 0xFF5555;
                } else {
                    statusText = "消耗能量: " + cost;
                    color = 0x55FF55;
                }
            } else {
                statusText = "槽位异常";
                color = 0xFF5555;
            }
        }
        guiGraphics.drawString(this.font, statusText, this.leftPos + 8, this.topPos + 62, color, true);
    }

    /**
     * 概率显示 —— 与服务端共用 {@link AlchemicalCalculator}，
     * 这里的数字就是实际开炉的概率（旧实现是客户端独立复算的药渣88% / 搬移5层，已与服务端漂移）。
     */
    private void renderProbabilities(GuiGraphics guiGraphics, LiandanRecipe recipe) {
        double[] weights = AlchemicalCalculator.computeWeights(
                this.menu.getMaterialStacks(),
                this.menu.hasDrossBlock(),
                recipe.isSpecial(),
                this.menu.getFurnaceLevel(),
                recipe.getRecipeLevel());
        double[] percent = AlchemicalCalculator.toPercent(weights);

        int startX = this.leftPos + 8;
        int yPos = this.topPos + 73;

        for (int i = 0; i < AlchemicalCalculator.TIER_COUNT; i++) {
            double chance = percent[i];
            int currentX = startX + (i * 33);
            int color = (chance > 0.01) ? AlchemicalCalculator.TIER_COLORS[i] : 0x444444;
            String percentText = (chance > 0 && chance < 1) ? "<1%" : String.format("%.0f%%", chance);

            guiGraphics.drawString(this.font, AlchemicalCalculator.TIER_LABELS[i], currentX, yPos, color, true);
            guiGraphics.drawString(this.font, percentText, currentX + 10, yPos, 0xFFFFFF, true);
        }
    }

    /**
     * 手动炼丹界面：丹方 / 品质 / 横向判定条 / 状态行。
     *
     * <p>位置沿用自动模式「预测 + 概率百分比」那块空白（y ≈ topPos+28 ~ +70），
     * 不挡内丹槽，也不挡右侧产出槽。</p>
     */
    private void renderManualUi(GuiGraphics guiGraphics) {
        int x = this.leftPos + 8;
        int y = this.topPos + 28;

        guiGraphics.drawString(this.font, "§6手动炼丹", x, y, 0xFFD700, true);
        int furnaceLevel = this.menu.getFurnaceLevel();
        float difficulty = this.menu.getManualDifficulty();
        String diffText = String.format("§8×%.2f", difficulty);
        guiGraphics.drawString(this.font, "§7炉§e" + furnaceLevel + " §7难度" + diffText,
                x + 58, y, 0xFFFFFF, true);

        String pillName = cachedRecipe
                .map(r -> r.getResultItem(this.minecraft.level.registryAccess()).getHoverName().getString())
                .orElse("未识别配方");
        guiGraphics.drawString(this.font, "§7丹方：§f" + pillName, x, y + 11, 0xFFFFFF, true);

        int quality = this.menu.getManualQuality();
        if (quality < 0) {
            guiGraphics.drawString(this.font, "§7品质：§8未判定", x, y + 22, 0xFFFFFF, true);
        } else {
            var q = com.TovidY.kunluncontinent.item.klitem.DanYaoQuality.values()[quality];
            guiGraphics.drawString(this.font, "§7品质：" + q.color + q.name, x, y + 22, 0xFFFFFF, true);
        }
        int successes = this.menu.getSuccessCount();
        if (successes > 0) {
            guiGraphics.drawString(this.font, "§8连成 " + successes + " 次", x + 62, y + 22, 0xFFFFFF, true);
        }

        // ---- 横向判定条 ----
        int barX = x;
        int barY = y + 36;
        int barW = 108;
        int barH = 9;

        guiGraphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF20242C);
        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF101319);

        // 判定区（会随难度漂移）
        int half = this.menu.getZoneHalf();
        int center = this.menu.getZoneCenter();
        int zoneX0 = barX + (int) ((center - half) / (float) LiandanluBlockEntity.MANUAL_RANGE * barW);
        int zoneX1 = barX + (int) ((center + half) / (float) LiandanluBlockEntity.MANUAL_RANGE * barW);
        guiGraphics.fill(zoneX0, barY, zoneX1, barY + barH, 0x90E6B800);
        guiGraphics.fill(zoneX0, barY, zoneX0 + 1, barY + barH, 0xFFFFE066);
        guiGraphics.fill(zoneX1 - 1, barY, zoneX1, barY + barH, 0xFFFFE066);

        // 光标：把上一 tick 的位置沿当前方向外推 partialTick，画出来的才是"此刻"的位置。
        // 不做这一步的话，玩家看到永远落后服务器 1 tick —— 高速时就是"明明压在判定区上却失败"。
        int shownCursor = advanceCursor(this.menu.getCursor(), this.menu.getCursorDir(),
                this.menu.getCursorSpeed(), this.partialTicks);
        int cursorX = barX + (int) (shownCursor / (float) LiandanluBlockEntity.MANUAL_RANGE * barW);
        guiGraphics.fill(cursorX - 1, barY - 2, cursorX + 2, barY + barH + 2, 0xFFFFFFFF);

        // 状态行：所有反馈都画在这里（不往聊天栏刷屏）
        String status = statusText(this.menu.getManualStatus(), this.menu.getManualPhase());
        guiGraphics.drawString(this.font, status, x, barY + 14, 0xFFFFFF, true);
    }

    /** 把光标沿方向外推 {@code fraction} 个 tick（含两端反弹），返回 0~1000 的位置。 */
    private static int advanceCursor(int cursor, int dir, int speed, float fraction) {
        int range = LiandanluBlockEntity.MANUAL_RANGE;
        int pos = cursor + Math.round(dir * speed * fraction);
        if (pos > range) {
            pos = range - (pos - range);
        } else if (pos < 0) {
            pos = -pos;
        }
        return Math.max(0, Math.min(range, pos));
    }

    /** 手动模式下要隐藏的槽位：药渣槽 + 聚魂瓶槽（按 Menu 下标取，不靠 {@code slot.index}）。 */
    private boolean isManualHiddenSlot(Slot slot) {
        return slot == this.menu.slots.get(LiandanluMenu.DROSS_MENU_SLOT)
                || slot == this.menu.slots.get(LiandanluMenu.BOTTLE_MENU_SLOT);
    }

    /**
     * 状态码 → 面板上的一行短文案。
     *
     * <p>服务端只同步 {@code int}（{@code LiandanluBlockEntity.STATUS_*}），
     * 文案全部在客户端拼好 —— 不往网络包里塞字符串，也不往聊天栏发消息。</p>
     */
    private String statusText(int status, int phase) {
        return switch (status) {
            case LiandanluBlockEntity.STATUS_READY -> "§7点左侧「开始炼丹」开炉";
            case LiandanluBlockEntity.STATUS_RUNNING -> "§e光标进金色区时点「判定」§7（耗神 §b"
                    + this.menu.getManualCost() + "§7）";
            case LiandanluBlockEntity.STATUS_SUCCESS -> "§a判定成功，品质提升！";
            case LiandanluBlockEntity.STATUS_FAILED -> "§c判定失败，丹药已降级——本炉结束";
            case LiandanluBlockEntity.STATUS_DONE -> "§a丹成，已取出——可再次「开始炼丹」";
            case LiandanluBlockEntity.STATUS_NO_RECIPE -> "§c配方未匹配，请放入正确的内丹";
            case LiandanluBlockEntity.STATUS_LEVEL_LOW -> "§c炉等级不足，炼不了这枚丹";
            case LiandanluBlockEntity.STATUS_OUTPUT_FULL -> "§c产出槽已满，先取走丹药";
            case LiandanluBlockEntity.STATUS_NEED_JUDGE -> "§c至少判定一次才能收丹";
            case LiandanluBlockEntity.STATUS_MATERIAL_CHANGED -> "§c材料已变，本次炼丹作废";
            case LiandanluBlockEntity.STATUS_TO_AUTO -> "§a已切换为自动炼丹";
            case LiandanluBlockEntity.STATUS_TO_MANUAL -> "§7已切换为手动炼丹";
            case LiandanluBlockEntity.STATUS_FINAL_ROUND -> "§6仙丹定品：最后一搏，失手即降为宝丹";
            case LiandanluBlockEntity.STATUS_NO_MP -> "§c精神力不足，无法开炉判定";
            case LiandanluBlockEntity.STATUS_MP_EXHAUSTED -> "§e精神力不足，已自动结丹";
            case LiandanluBlockEntity.STATUS_BUSY -> "§c另一位魂师正在用这座丹炉";
            default -> phase == LiandanluBlockEntity.PHASE_RUNNING
                    ? "§e光标进金色区时点「判定」§7（可随时收丹）"
                    : "§7点左侧「开始炼丹」开炉";
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}