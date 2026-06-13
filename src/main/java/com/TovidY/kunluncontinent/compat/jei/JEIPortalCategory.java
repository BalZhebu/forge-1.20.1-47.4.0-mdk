package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import javax.annotation.Nullable;

public class JEIPortalCategory implements IRecipeCategory<PortalJeiRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(KlMain.MOD_ID, "portal_structure");
    public static final RecipeType<PortalJeiRecipe> TYPE = new RecipeType<>(UID, PortalJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private float yawRotation = 0f;
    private float pitchRotation = 22f;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    public JEIPortalCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(166, 115);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.FLINT_AND_STEEL));
    }

    @Override
    public RecipeType<PortalJeiRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.literal("传送门多方块结构"); }

    @Override
    public int getWidth() { return 166; }

    @Override
    public int getHeight() { return 115; }

    @Override
    public @Nullable IDrawable getIcon() { return this.icon; }

    @Override
    public void draw(PortalJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 0);

        var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, "§6多结构方块", 90, 15, 0x404040, false);
        guiGraphics.drawString(font, "§74x5框架结构", 90, 30, 0x707070, false);
        guiGraphics.drawString(font, "§b激活物品->", 90, 55, 0x00AAAA, false);

        boolean isLeftClickPressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
        boolean mouseIn3DZone = mouseX >= 0 && mouseX <= 85 && mouseY >= 0 && mouseY <= 115;

        if (isLeftClickPressed) {
            if (!isDragging && mouseIn3DZone) {
                isDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            } else if (isDragging) {
                double deltaX = mouseX - lastMouseX;
                double deltaY = mouseY - lastMouseY;

                yawRotation -= (float) deltaX * 1.2f;
                pitchRotation += (float) deltaY * 1.2f;

                if (pitchRotation > 85f) pitchRotation = 85f;
                if (pitchRotation < -85f) pitchRotation = -85f;

                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        } else {
            isDragging = false;
        }

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(44, 58, 100);

        float scale = 9.5f;
        pose.scale(scale, -scale, scale);

        pose.mulPose(Axis.XP.rotationDegrees(pitchRotation));

        float baseTimeRot = 0f;
        if (!isDragging) {
            baseTimeRot = (float) (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime()) * 0.7f;
        }
        pose.mulPose(Axis.YP.rotationDegrees(yawRotation + baseTimeRot));

        pose.translate(-2.0, -2.5, -0.5);

        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        BlockState frameState = recipe.getFrameBlock().defaultBlockState();
        BlockState portalState = recipe.getPortalBlock().defaultBlockState();

        int[][] map = {
                {1, 1, 1, 1},
                {1, 0, 0, 1},
                {1, 0, 0, 1},
                {1, 0, 0, 1},
                {1, 1, 1, 1}
        };

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                pose.pushPose();
                pose.translate(col, row, 0);

                BlockState drawState = (map[row][col] == 1) ? frameState : portalState;

                brd.renderSingleBlock(
                        drawState,
                        pose,
                        buffer,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        null
                );
                pose.popPose();
            }
        }

        buffer.endBatch();
        pose.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PortalJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 135, 75)
                .addItemStack(recipe.getIgniter());
    }
}