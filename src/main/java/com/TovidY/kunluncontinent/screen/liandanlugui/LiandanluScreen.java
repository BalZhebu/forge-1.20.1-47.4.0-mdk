package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class LiandanluScreen extends AbstractContainerScreen<LiandanluMenu> {

    private static final ResourceLocation FLAME_EMPTY =
            new ResourceLocation(KlMain.MOD_ID, "textures/screens/huoyan.png");
    private static final ResourceLocation FLAME_FULL =
            new ResourceLocation(KlMain.MOD_ID, "textures/screens/huoyanmax.png");

    private static final int ORIGINAL_FLAME_SIZE = 14;
    private static final int FLAME_SCALE = 2;
    private static final int DISPLAY_FLAME_SIZE = ORIGINAL_FLAME_SIZE * FLAME_SCALE;

    private final SimpleContainer lastContainer = new SimpleContainer(5);
    private Optional<LiandanRecipe> cachedRecipe = Optional.empty();
    private int lastSlotHash = 0;

    public LiandanluScreen(LiandanluMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 211;
        this.imageHeight = 170;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    private void updateRecipeCache() {
        if (this.minecraft == null || this.minecraft.level == null) return;

        int currentHash = 1;
        for (int i = 0; i < 5; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            currentHash = 31 * currentHash + stack.getItem().hashCode() + stack.getCount();
        }

        if (currentHash != lastSlotHash) {
            this.lastSlotHash = currentHash;
            for (int i = 0; i < 5; i++) {
                this.lastContainer.setItem(i, this.menu.getSlot(i).getItem());
            }
            this.cachedRecipe = this.minecraft.level.getRecipeManager()
                    .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), this.lastContainer, this.minecraft.level);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        updateRecipeCache();
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderAlchmenyInfo(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // 1. 渲染古风通用手绘背景主面板
        KunlunGuiHelper.renderKunlunBackground(guiGraphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        // 2. 统一手绘 Slot 衬底
        for (Slot slot : this.menu.slots) {
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;

            // 判定逻辑修改：只要不属于玩家背包/快捷栏（Y < 80 区域），全部渲染统一金色边框！
            // 只有下方的玩家背包区域 (Y >= 80) 才渲染深色标准槽位
            boolean isMachineSlot = slot.y < 80;

            KunlunGuiHelper.renderSlotBackground(guiGraphics, slotX, slotY, isMachineSlot);
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

        LiandanluBlockEntity be = (LiandanluBlockEntity) this.menu.blockEntity;

        ItemStack bottleStack = be.getItemHandler().getStackInSlot(18);
        if (bottleStack.isEmpty()) {
            bottleStack = this.menu.getSlot(18).getItem();
        }

        if (hasBottle == 0) {
            statusText = "缺少聚魂瓶";
            color = 0xFF5555;
        } else if (bottleStack.isEmpty()) {
            statusText = "同步中...";
            color = 0xAAAAAA;
        } else {
            if (!hasAnyNeidan()) {
                statusText = "请放入内丹...";
                color = 0xFFCC00;
            } else if (cachedRecipe.isEmpty()) {
                statusText = "配方未匹配";
                color = 0xFF5555;
            } else {
                boolean isBottle = false;
                for (RegistryObject<Item> bottleReg : ModItems.JUHUNPING) {
                    if (bottleStack.is(bottleReg.get())) {
                        isBottle = true;
                        break;
                    }
                }

                if (isBottle) {
                    if (bottleStack.getItem() instanceof SoulGatheringBottleItem bottle) {
                        int currentEnergy = bottle.getNengliang(null, bottleStack);
                        int cost = cachedRecipe.get().getEnergyCost();
                        statusText = (currentEnergy < cost) ? "能量不足: " + cost : "消耗能量: " + cost;
                        color = (currentEnergy < cost) ? 0xFF5555 : 0x55FF55;
                    } else {
                        statusText = "加载中...";
                        color = 0xAAAAAA;
                    }
                } else {
                    statusText = "槽位异常";
                    color = 0xFF5555;
                }
            }
        }
        guiGraphics.drawString(this.font, statusText, this.leftPos + 8, this.topPos + 62, color, true);
    }

    private boolean hasAnyNeidan() {
        for (int i = 0; i < 5; i++) {
            if (!this.menu.getSlot(i).getItem().isEmpty()) return true;
        }
        return false;
    }

    private void renderProbabilities(GuiGraphics guiGraphics, LiandanRecipe recipe) {
        String[] labels = {"碎", "散", "丹", "灵", "宝", "仙"};
        int[] colors = {0x777777, 0x55FF55, 0x5555FF, 0xAA00AA, 0xFFAA00, 0xFF5555};

        double[] weights = calculateWeightsForDisplay();

        if (recipe.isSpecial()) {
            double brokenWeight = weights[0];
            if (brokenWeight > 0) {
                weights[0] = 0;
                double remainingTotal = 0;
                for (double w : weights) remainingTotal += w;
                if (remainingTotal > 0) {
                    for (int i = 1; i < weights.length; i++) {
                        double ratio = weights[i] / remainingTotal;
                        weights[i] += brokenWeight * ratio;
                    }
                } else {
                    weights[1] = 100.0;
                }
            }
        }

        drawWeights(guiGraphics, labels, colors, weights);
    }

    private void drawWeights(GuiGraphics guiGraphics, String[] labels, int[] colors, double[] weights) {
        double totalWeight = 0;
        for (double w : weights) totalWeight += w;
        int startX = this.leftPos + 8;
        int yPos = this.topPos + 73;
        int horizontalSpacing = 33;

        for (int i = 0; i < labels.length; i++) {
            double chance = (weights[i] / totalWeight) * 100;
            int currentX = startX + (i * horizontalSpacing);
            int color = (chance > 0.01) ? colors[i] : 0x444444;
            String percentText = (chance > 0 && chance < 1) ? "<1%" : String.format("%.0f%%", chance);

            guiGraphics.drawString(this.font, labels[i], currentX, yPos, color, true);
            guiGraphics.drawString(this.font, percentText, currentX + 10, yPos, 0xFFFFFF, true);
        }

        if (!this.menu.getSlot(17).getItem().isEmpty()) {
            guiGraphics.drawString(this.font, "✔药渣", this.leftPos + 130, yPos - 12, 0x55FF55, true);
        }
    }

    private double[] calculateWeightsForDisplay() {
        double[] weights = {40.0, 30.0, 15.0, 10.0, 4.0, 1.0};
        if (this.menu.hasDrossBlock()) {
            double currentBrokenWeight = weights[0];
            double reduction = currentBrokenWeight * 0.88;
            weights[0] -= reduction;
            weights[1] += reduction;
        }
        double pressure = 0;
        boolean hasXian = false;
        boolean hasJue = false;

        for (int i = 0; i < 5; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (stack.getItem() instanceof NeidanItem) {
                String q = stack.getOrCreateTag().getString("Quality");
                pressure += switch (q) {
                    case "XIAN" -> { hasXian = true; yield 60.0; }
                    case "JUE"  -> { hasJue = true; yield 40.0; }
                    case "ZHEN" -> 20.0;
                    case "SHANG"-> 10.0;
                    default     -> 0.0;
                };
            }
        }

        for (int i = 0; i < 5; i++) {
            double shift = Math.min(weights[i], pressure);
            weights[i] -= shift;
            if (i + 1 < weights.length) {
                weights[i + 1] += shift;
            }
            pressure -= shift;
            if (pressure <= 0) break;
        }

        if (hasXian) {
            weights[0]=0; weights[1]=0; weights[2]=0;
            weights[3]=35.0; weights[4]=60.0; weights[5]=5.0;
        } else if (hasJue && weights[2] <= 0) {
            weights[0]=0; weights[1]=0; weights[2]=0;
            weights[3]=84.0; weights[4]=15.0; weights[5]=1.0;
        }

        return weights;
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