package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class LiandanluScreen extends AbstractContainerScreen<LiandanluMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/liandanlu.png");

    public LiandanluScreen(LiandanluMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 211;
        this.imageHeight = 170;
        // 将标题和背包标签移出屏幕（如果你不想显示它们）
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    private static final ResourceLocation FLAME_EMPTY =
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/huoyan.png");
    private static final ResourceLocation FLAME_FULL =
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/huoyanmax.png");

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderAlchmenyInfo(guiGraphics);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderAlchmenyInfo(GuiGraphics guiGraphics) {
        // 匹配配方名
        SimpleContainer container = new SimpleContainer(5);
        for (int i = 0; i < 5; i++) {
            container.setItem(i, this.menu.getSlot(i).getItem());
        }

        if (this.minecraft.level != null) {
            var recipeManager = this.minecraft.level.getRecipeManager();
            Optional<LiandanRecipe> match = recipeManager.getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, this.minecraft.level);

            if (match.isPresent()) {
                ItemStack resultStack = match.get().getResultItem(this.minecraft.level.registryAccess());
                Component resultName = Component.literal("预测产出：").append(resultStack.getHoverName());
                guiGraphics.drawString(this.font, resultName, this.leftPos + 8, this.topPos + 28, 0xFFD700, true);

                // 只有匹配到配方才显示概率
                renderProbabilities(guiGraphics);
            } else {
                guiGraphics.drawString(this.font, "等待投放材料...", this.leftPos + 8, this.topPos + 28, 0xAAAAAA, true);
            }
        }
    }

    private void renderProbabilities(GuiGraphics guiGraphics) {
        double[] weights = calculateWeightsForDisplay();
        double totalWeight = 0;
        for (double w : weights) totalWeight += w;

        String[] labels = {"碎", "散", "丹", "灵", "宝", "仙"};
        int[] colors = {0x777777, 0x55FF55, 0x5555FF, 0xAA00AA, 0xFFAA00, 0xFF5555};

        // 绘制起始位置：玩家背包上方空位
        int startX = this.leftPos + 10;
        int yPos = this.topPos + 75;
        int horizontalSpacing = 33;

        for (int i = 0; i < labels.length; i++) {
            double chance = (weights[i] / totalWeight) * 100;
            int currentX = startX + (i * horizontalSpacing);

            // 如果概率为0，调暗颜色显示
            int color = (chance > 0) ? colors[i] : 0x444444;
            String percentText = String.format("%.0f%%", chance); // 取整显示更整洁

            guiGraphics.drawString(this.font, labels[i], currentX, yPos, color, true);
            guiGraphics.drawString(this.font, percentText, currentX + 10, yPos, 0xFFFFFF, true);
        }

        // 药渣加持标志
        if (!this.menu.getSlot(17).getItem().isEmpty()) {
            guiGraphics.drawString(this.font, "✔药渣加持", this.leftPos + 145, yPos - 12, 0x55FF55, true);
        }
    }

    private double[] calculateWeightsForDisplay() {
        // 这里的逻辑与 AlchemicalCalculator 必须完全一致
        double[] weights = {40.0, 30.0, 15.0, 10.0, 4.0, 1.0};

        // 丹渣块稳定
        if (!this.menu.getSlot(17).getItem().isEmpty()) {
            double brokenWeight = weights[0];
            weights[0] = 0;
            weights[1] += brokenWeight;
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

        for (int i = 0; i < 4; i++) {
            double shift = Math.min(weights[i], pressure);
            weights[i] -= shift;
            weights[i + 1] += shift;
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        int originalSize = 14;
        int scale = 2;
        int displaySize = originalSize * scale;
        int flameX = this.leftPos + 95;
        int flameY = this.topPos + 30;
        guiGraphics.blit(FLAME_EMPTY, flameX, flameY, displaySize, displaySize, 0, 0, originalSize, originalSize, originalSize, originalSize);

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            float ratio = Math.min(1.0F, (float) progress / maxProgress);
            int scaledSourceHeight = Math.round(ratio * originalSize);
            int scaledDisplayHeight = scaledSourceHeight * scale;
            if (scaledSourceHeight > 0) {
                guiGraphics.blit(FLAME_FULL,
                        flameX,
                        flameY + (displaySize - scaledDisplayHeight),
                        displaySize,
                        scaledDisplayHeight,
                        0,
                        (float) (originalSize - scaledSourceHeight),
                        originalSize,
                        scaledSourceHeight,
                        originalSize,
                        originalSize
                );
            }
        }
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