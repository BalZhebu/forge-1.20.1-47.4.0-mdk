package com.TovidY.kunluncontinent.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KunlunGuiHelper {
    public static String formatLargeNumber(double value) {
        if (value < 100000) {
            if (value == (long) value) return String.format("%d", (long) value);
            return String.format("%.1f", value);
        }
        if (value < 1_000_000) return String.format("%.1fK", value / 1_000.0);
        if (value < 1_000_000_000) return String.format("%.2fM", value / 1_000_000.0);
        if (value < 1_000_000_000_000L) return String.format("%.2fB", value / 1_000_000_000.0);
        return String.format("%.2fT", value / 1_000_000_000_000.0);
    }

    public static void renderKunlunBackground(GuiGraphics gui, int x, int y, int width, int height) {
        gui.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x80000000);
        gui.fill(x, y, x + width, y + height, 0xF012161F);

        gui.renderOutline(x, y, width, height, 0xFFD4AF37);
        gui.renderOutline(x + 2, y + 2, width - 4, height - 4, 0xFF2A3447);
        gui.renderOutline(x + 3, y + 3, width - 6, height - 6, 0xFF8A6D3B);

        int corner = 6;
        gui.fill(x - 1, y - 1, x + corner, y + 2, 0xFFE6B800);
        gui.fill(x - 1, y - 1, x + 2, y + corner, 0xFFE6B800);

        gui.fill(x + width - corner, y - 1, x + width + 1, y + 2, 0xFFE6B800);
        gui.fill(x + width - 2, y - 1, x + width + 1, y + corner, 0xFFE6B800);

        gui.fill(x - 1, y + height - 2, x + corner, y + height + 1, 0xFFE6B800);
        gui.fill(x - 1, y + height - corner, x + 2, y + height + 1, 0xFFE6B800);

        gui.fill(x + width - corner, y + height - 2, x + width + 1, y + height + 1, 0xFFE6B800);
        gui.fill(x + width - 2, y + height - corner, x + width + 1, y + height + 1, 0xFFE6B800);
    }

    public static void renderSlotBackground(GuiGraphics gui, int slotX, int slotY, boolean isHighlight) {
        gui.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF0D1117);
        int borderColor = isHighlight ? 0xFFE6B800 : 0xFF374151;
        gui.renderOutline(slotX, slotY, 18, 18, borderColor);
    }

    public static class HandDrawnEditBox extends EditBox {
        public HandDrawnEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
            this.setBordered(false);
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            if (!this.isVisible()) return;

            gui.fill(this.getX() - 2, this.getY() - 2, this.getX() + this.width + 2, this.getY() + this.height + 2, 0xFF0B0F17);

            int borderColor = this.isFocused() ? 0xFFE6B800 : 0xFF4B5563;
            gui.renderOutline(this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4, borderColor);

            super.renderWidget(gui, mouseX, mouseY, partialTick);
        }
    }

    public static class HandDrawnButton extends AbstractButton {
        private final Consumer<HandDrawnButton> onPressAction;
        private final Supplier<List<Component>> tooltipSupplier;
        private final Font font;

        public HandDrawnButton(Font font, int x, int y, int width, int height, Component message,
                               Consumer<HandDrawnButton> onPressAction, Supplier<List<Component>> tooltipSupplier) {
            super(x, y, width, height, message);
            this.font = font;
            this.onPressAction = onPressAction;
            this.tooltipSupplier = tooltipSupplier;
        }

        @Override
        public void onPress() {
            if (this.onPressAction != null) {
                this.onPressAction.accept(this);
            }
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int bgColor = hovered ? 0xFF2A364F : 0xFF181F2C;
            int borderColor = hovered ? 0xFFE6B800 : 0xFF4A5568;

            gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            gui.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

            if (hovered) {
                gui.fill(this.getX(), this.getY(), this.getX() + 2, this.getY() + 2, 0xFFFFF0A0);
                gui.fill(this.getX() + this.width - 2, this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, 0xFFFFF0A0);
            }

            int textColor = hovered ? 0xFFFFFF : 0xDDDDDD;
            int textX = this.getX() + (this.width - this.font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            gui.drawString(this.font, this.getMessage(), textX, textY, textColor, true);
        }

        public void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
            if (this.tooltipSupplier != null && this.isHoveredOrFocused()) {
                List<Component> tooltip = this.tooltipSupplier.get();
                if (tooltip != null && !tooltip.isEmpty()) {
                    gui.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    public static class KunlunTabButton extends AbstractButton {
        private final ItemStack iconStack;
        private final boolean isSelected;
        private final Consumer<KunlunTabButton> onPressAction;
        private final Font font;

        public KunlunTabButton(Font font, int x, int y, int width, int height, ItemStack iconStack, Component label, boolean isSelected, Consumer<KunlunTabButton> onPressAction) {
            super(x, y, width, height, label);
            this.font = font;
            this.iconStack = iconStack;
            this.isSelected = isSelected;
            this.onPressAction = onPressAction;
        }

        @Override
        public void onPress() {
            if (this.onPressAction != null) {
                this.onPressAction.accept(this);
            }
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            int bgColor = isSelected ? 0xFF2A364F : (hovered ? 0xFF1F2937 : 0xFF111827);
            int borderColor = isSelected ? 0xFFE6B800 : (hovered ? 0xFF9CA3AF : 0xFF4B5563);

            gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            gui.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

            if (isSelected) {
                gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 2, 0xFFFFD700);
            }

            if (iconStack != null && !iconStack.isEmpty()) {
                gui.renderItem(iconStack, this.getX() + (this.width - 16) / 2, this.getY() + (this.height - 16) / 2);
            }

            if (hovered) {
                gui.renderTooltip(this.font, this.getMessage(), mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}