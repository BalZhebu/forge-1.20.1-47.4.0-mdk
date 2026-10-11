package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
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
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class LiandanluScreen extends AbstractContainerScreen<LiandanluMenu> {

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

    public LiandanluScreen(LiandanluMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 211;
        this.imageHeight = 170;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
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

        if (this.menu.hasDrossBlock()) {
            guiGraphics.drawString(this.font, "✔药渣", this.leftPos + 130, yPos - 12, 0x55FF55, true);
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