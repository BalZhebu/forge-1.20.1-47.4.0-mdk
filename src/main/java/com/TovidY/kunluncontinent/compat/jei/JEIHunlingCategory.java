package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.RitualIngredient;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI 里的汇灵台仪式页（<b>产出物品</b>版）：必需品居中，八座基座围成一圈，下面写精神力与耗时。
 *
 * <h3>布局为什么是「一圈」</h3>
 * <p>实际仪式就是「汇灵台摆中间、基座围一圈把供品送进去」，JEI 照这个形态画，
 * 玩家一眼就知道方块该怎么摆，不用去猜「8 个钻石是摆 8 座基座还是堆在一起」。</p>
 *
 * <h3>布局常量与绘制是共享的</h3>
 * <p>{@link JEIHunlingSpawnCategory}（召唤生物版）用的是<b>同一套坐标</b>，只换了产物槽的内容。
 * 所以这里把布局常量、底板、底部文字都做成 static，两页各画各的产物，其余完全一致。</p>
 */
public class JEIHunlingCategory implements IRecipeCategory<HunlingRitualRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(KlMain.MOD_ID, "hunling_ritual");
    public static final RecipeType<HunlingRitualRecipe> TYPE =
            new RecipeType<>(UID, HunlingRitualRecipe.class);

    // ==================== 共享布局（召唤版同样引用这些） ====================

    public static final int WIDTH = 176;
    public static final int HEIGHT = 180;

    /** 椭圆圆心（也是必需品的槽心）。 */
    public static final int CENTER_X = 88;
    public static final int CENTER_Y = 54;
    public static final int RADIUS_X = 55;
    public static final int RADIUS_Y = 40;

    /** 8 个基座槽的左上角坐标，从正上方开始顺时针。 */
    public static final int[] BASE_X = new int[8];
    public static final int[] BASE_Y = new int[8];

    static {
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(-90 + i * 45);
            BASE_X[i] = (int) Math.round(CENTER_X + Math.cos(angle) * RADIUS_X) - 9;
            BASE_Y[i] = (int) Math.round(CENTER_Y + Math.sin(angle) * RADIUS_Y) - 9;
        }
        // ⚠️ 正下方那一座（i=4）要往下让一截：中心槽下面还压着一个汇灵台方块槽，
        //    不加这一行两者会叠在一起，「汇灵台」看起来就像第九座基座。
        BASE_Y[4] += 12;
    }

    /** 基座方块槽相对物品槽的垂直偏移。 */
    public static final int BASE_ICON_DY = 14;

    public static final int RESULT_X = 79;
    public static final int RESULT_Y = 132;

    // ==================== 共享绘制 ====================

    /** 底板 + 边框 + 一圈基座的虚线环。两页都先画这个。 */
    public static void drawCommonFrame(GuiGraphics graphics) {
        graphics.fill(0, 0, WIDTH, HEIGHT, 0xCC14141C);
        graphics.renderOutline(0, 0, WIDTH, HEIGHT, 0xFF3C3C4E);
        drawBaseRing(graphics);
    }

    /** 成品展示框 + 精神力 / 持续时间文字 + 「产物」标签。 */
    public static void drawFooter(GuiGraphics graphics, int spiritCost, int durationTicks) {
        var font = Minecraft.getInstance().font;

        // 成品展示框：套在产物槽外面一圈（槽在 draw 之后才渲染，框只能画在外围才看得见）
        graphics.fill(RESULT_X - 4, RESULT_Y - 4, RESULT_X + 22, RESULT_Y + 22, 0x55C8A020);
        graphics.renderOutline(RESULT_X - 4, RESULT_Y - 4, 26, 26, 0xFFE8C86A);

        String cost = "总 §5" + spiritCost + " §f精神力消耗";
        String time = "持续时间：§e" + String.format("%.1f", durationTicks / 20.0f) + "秒";
        graphics.drawString(font, cost, 12, 158, 0xFFE0E0E0, false);
        graphics.drawString(font, time, 12, 170, 0xFFE0E0E0, false);

        graphics.drawString(font, "§e产物", RESULT_X + 26, RESULT_Y + 5, 0xFFFFD54A, false);
    }

    /** 在 8 个基座槽之间画短线段，围出一圈。 */
    private static void drawBaseRing(GuiGraphics graphics) {
        for (int i = 0; i < 8; i++) {
            int x1 = BASE_X[i] + 9;
            int y1 = BASE_Y[i] + 9;
            int j = (i + 1) % 8;
            int x2 = BASE_X[j] + 9;
            int y2 = BASE_Y[j] + 9;

            int steps = 6;
            for (int s = 0; s < steps; s++) {
                float t0 = s / (float) steps;
                float t1 = (s + 0.45f) / steps;
                int px0 = (int) (x1 + (x2 - x1) * t0);
                int py0 = (int) (y1 + (y2 - y1) * t0);
                int px1 = (int) (x1 + (x2 - x1) * t1);
                int py1 = (int) (y1 + (y2 - y1) * t1);
                graphics.fill(Math.min(px0, px1), Math.min(py0, py1),
                        Math.max(px0, px1) + 1, Math.max(py0, py1) + 1, 0x55A090FF);
            }
        }
    }

    /**
     * 铺满 8 个基座：每格下面一个真实基座槽（可悬停），上面按顺序摆副物品。
     *
     * <p>副物品会<b>按 count 展开成一格一件</b>；超过 8 件时余量压在最后一格用角标表示。</p>
     */
    public static void fillBaseSlots(IRecipeLayoutBuilder builder, List<RitualIngredient> ingredients,
                                     ItemStack baseIcon) {
        for (int i = 0; i < 8; i++) {
            builder.addSlot(RecipeIngredientRole.CATALYST, BASE_X[i], BASE_Y[i] + BASE_ICON_DY - 1)
                    .addItemStack(baseIcon.copy());
        }

        List<ItemStack> expanded = new ArrayList<>();
        for (RitualIngredient ing : ingredients) {
            for (int i = 0; i < ing.count(); i++) {
                expanded.add(ing.stack().copyWithCount(1));
            }
        }

        for (int i = 0; i < 8; i++) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, BASE_X[i], BASE_Y[i]);
            if (i < expanded.size()) {
                ItemStack stack = expanded.get(i).copy();
                if (i == 7 && expanded.size() > 8) {
                    stack.setCount(expanded.size() - 7);
                }
                slot.addItemStack(stack);
            }
        }
    }

    /** 中心两个槽：汇灵台本体（下）+ 必需品（上）。 */
    public static void fillCenterSlots(IRecipeLayoutBuilder builder, ItemStack catalyst, ItemStack altarIcon) {
        builder.addSlot(RecipeIngredientRole.CATALYST, CENTER_X - 9, CENTER_Y + BASE_ICON_DY - 1)
                .addItemStack(altarIcon.copy());
        builder.addSlot(RecipeIngredientRole.CATALYST, CENTER_X - 9, CENTER_Y - 9)
                .addItemStack(catalyst.copyWithCount(1));
    }

    // ==================== 实例 ====================

    private final IDrawable background;
    private final IDrawable icon;

    /** 底座 / 汇灵台方块图标（构造时建一次，避免每帧 new）。 */
    private final ItemStack baseIcon;
    private final ItemStack altarIcon;

    public JEIHunlingCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
        this.baseIcon = new ItemStack(ModBlocks.HUNLING_ALTAR_BASE.get());
        this.altarIcon = new ItemStack(ModBlocks.HUNLING_ALTAR.get());
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.altarIcon.copy());
    }

    @Override
    public RecipeType<HunlingRitualRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("汇灵台仪式");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(HunlingRitualRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        this.background.draw(graphics, 0, 0);
        drawCommonFrame(graphics);
        drawFooter(graphics, recipe.getSpiritCost(), recipe.getDurationTicks());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HunlingRitualRecipe recipe, IFocusGroup focuses) {
        fillBaseSlots(builder, recipe.getRitualIngredients(), this.baseIcon);
        fillCenterSlots(builder, recipe.getCatalyst(), this.altarIcon);

        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y)
                .addItemStack(recipe.getResult());
    }
}
