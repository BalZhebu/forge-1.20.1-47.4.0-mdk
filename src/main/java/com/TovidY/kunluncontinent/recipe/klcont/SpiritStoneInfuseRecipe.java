package com.TovidY.kunluncontinent.recipe.klcont;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.spiritstone.SpiritStoneItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

/**
 * 蕴灵石 → 聚魂瓶：把石头里的灵气 <b>1:1</b> 灌进瓶子里。
 *
 * <h3>为什么必须是自定义配方</h3>
 * <p>原版合成表在匹配时<b>不看 NBT</b>，输出也带不了输入的 NBT。而这里的两个关键量都在 NBT 里：
 * 蕴灵石的品质（决定能抽出多少能量）和瓶子已有的 {@code sh_nengliang}。所以只能自己算。</p>
 *
 * <h3>为什么这条配方「一次只能合一颗」，不处理 shift 连点</h3>
 * <p>看清了原版 {@code ResultSlot#onTake} 的机制：每取一次结果，<b>每格非空输入只消耗 1 个</b>；
 * 而 {@code getRemainingItems} 只能「往回加」，不能「多扣」。</p>
 * <p>这里瓶子是<b>输入也是输出</b>：</p>
 * <ul>
 *   <li>让 {@code getRemainingItems} 返还瓶子 → 输出又是一个新瓶子，玩家凭空复制；</li>
 *   <li>不返还 → 第一轮合完瓶子就没了，shift 也只能合一次。</li>
 * </ul>
 * <p>所以合成台这条路只保留「单颗手合」。<b>批量请用 Shift + 右键聚魂瓶</b>
 * （{@code SoulGatheringBottleItem#infuseFromInventory}）—— 一次把背包里的蕴灵石全部灌进去，
 * 比 shift 连点快得多，而且不存在复制问题。</p>
 *
 * <h3>规则</h3>
 * <ul>
 *   <li>一格蕴灵石 + 一格聚魂瓶，瓶子原样保留（等级、自动吸收开关、已有能量）；</li>
 *   <li>转移量 = {@link SpiritStoneItem#getExp(ItemStack)}（阶基数 × 品质倍率），即「1 点修为 = 1 点能量」；</li>
 *   <li>装不下就不匹配（{@code 已有能量 + 这颗的能量 > 瓶子上限}）—— 宁可合不了，也不把灵气默默吞掉。</li>
 * </ul>
 *
 * <p>废品蕴灵石（品质倍率 0）这里不匹配，它走 {@link WasteSpiritStoneRecipe} 分解成粉末。</p>
 */
public class SpiritStoneInfuseRecipe extends CustomRecipe {

    public SpiritStoneInfuseRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    // ==================== 查找输入 ====================

    private static ItemStack findStone(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SpiritStoneItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findBottle(CraftingContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SoulGatheringBottleItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** 瓶子里已有的能量。⚠️ 只读，不能在这里 {@code getOrCreateTag()} —— 匹配阶段不能改物品。 */
    private static int storedEnergy(ItemStack bottle) {
        return bottle.hasTag() ? bottle.getTag().getInt("sh_nengliang") : 0;
    }

    // ==================== 配方本体 ====================

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int filled = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                filled++;
            }
        }
        if (filled != 2) {
            return false;
        }

        ItemStack stone = findStone(container);
        ItemStack bottle = findBottle(container);
        if (stone.isEmpty() || bottle.isEmpty()) {
            return false;
        }
        if (!(stone.getItem() instanceof SpiritStoneItem stoneItem)
                || !(bottle.getItem() instanceof SoulGatheringBottleItem bottleItem)) {
            return false;
        }

        int gain = stoneItem.getExp(stone);
        if (gain <= 0) {
            // 废品：榨不出东西，交给分解配方
            return false;
        }
        return storedEnergy(bottle) + gain <= bottleItem.getMaxnengliang(bottle);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack stone = findStone(container);
        ItemStack bottle = findBottle(container);
        if (stone.isEmpty() || bottle.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!(stone.getItem() instanceof SpiritStoneItem stoneItem)
                || !(bottle.getItem() instanceof SoulGatheringBottleItem bottleItem)) {
            return ItemStack.EMPTY;
        }

        int gain = stoneItem.getExp(stone);
        if (gain <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = bottle.copy();          // 保留等级 / 自动吸收 / 已有能量
        int next = Math.min(bottleItem.getMaxnengliang(result), storedEnergy(result) + gain);
        result.getOrCreateTag().putInt("sh_nengliang", next);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPIRIT_STONE_INFUSE_SERIALIZER.get();
    }

    // ==================== JEI 展示 ====================

    /**
     * JEI 默认会跳过 {@code isSpecial()} 为真的配方，所以这里返回 false。
     *
     * <p>它同时会让这条配方进入配方书索引，但没有 advancement 解锁，玩家不会在配方书里看到它 ——
     * 换句话说只对 JEI 生效，不影响游戏内表现。</p>
     */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(spiritStoneItems()));
        list.add(Ingredient.of(bottleItems()));
        return list;
    }

    /** JEI 的示意产物：等级由放进去的瓶子决定，这里只能用一阶瓶代表。 */
    @Override
    public ItemStack getResultItem(@Nullable RegistryAccess access) {
        return new ItemStack(ModItems.SOUL_GATHERING_BOTTLE_0.get());
    }

    // ==================== 物品集合（JEI 用） ====================

    private static Item[] spiritStoneItems() {
        return new Item[]{
                ModItems.FIRST_SPIRIT_STONE.get(), ModItems.TWO_SPIRIT_STONE.get(),
                ModItems.THREE_SPIRIT_STONE.get(), ModItems.FOUR_SPIRIT_STONE.get(),
                ModItems.FIVE_SPIRIT_STONE.get(), ModItems.SIX_SPIRIT_STONE.get(),
                ModItems.SEVEN_SPIRIT_STONE.get(), ModItems.EIGHT_SPIRIT_STONE.get(),
                ModItems.NINE_SPIRIT_STONE.get()
        };
    }

    private static Item[] bottleItems() {
        return ModItems.JUHUNPING.stream().map(RegistryObject::get).toArray(Item[]::new);
    }
}
