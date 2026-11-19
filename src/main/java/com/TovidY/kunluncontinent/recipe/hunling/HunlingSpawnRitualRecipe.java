package com.TovidY.kunluncontinent.recipe.hunling;

import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 汇灵台仪式 · <b>召唤生物</b>版：仪式走完后，台面上的必需品被「烧掉」，台子正上方冒出一只生物。
 *
 * <h3>和物品版唯一的区别是产物</h3>
 * <p>前面一整套流程（必需品摆台、副物品摆基座、按时间扣精神力、最后 3 秒前吃空副物品）完全一样，
 * 所以两边都实现 {@link HunlingRitual}，状态机只认接口。这里只多三个参数：</p>
 * <ul>
 *   <li><b>entity</b>：生物 ID（如 {@code minecraft:zombie}）。<b>启动前会校验</b>，
 *       写错或模组缺失时不让开仪式，避免白吞材料；</li>
 *   <li><b>nianxian</b>：年限。生物的具体属性不写在配方里 —— 交给现有的
 *       {@code MobAttributeCapability#initNianxian}，按年限推导，和 {@code /kunluncontinent monster} 一致；</li>
 *   <li><b>spawn_effect</b>：召唤特效 ID（预留）。生物生成前有一段不占持续时间的「蓄势」，
 *       后续接 BOSS 特效时从这里读，现在留空也能正常跑。</li>
 * </ul>
 *
 * <h3>产物是野生的</h3>
 * <p>召唤出来的生物不绑定主人，和自然刷出来的一样会对玩家动手 —— 仪式招怪就是招怪。</p>
 */
public class HunlingSpawnRitualRecipe implements Recipe<Container>, HunlingRitual {

    private final ResourceLocation id;
    private final ItemStack catalyst;
    private final List<RitualIngredient> ingredients;
    private final int spiritCost;
    private final int durationTicks;
    private final boolean strictNbt;

    private final ResourceLocation entityId;
    private final long nianxian;

    /** 成品展示物（刷怪蛋 / 召唤石之类）。JEI 与提示文案都读它，不再靠实体 id 反推。 */
    private final ItemStack displayItem;

    @Nullable
    private final String spawnEffect;

    public HunlingSpawnRitualRecipe(ResourceLocation id, ItemStack catalyst, List<RitualIngredient> ingredients,
                                    int spiritCost, int durationTicks, boolean strictNbt,
                                    ResourceLocation entityId, long nianxian, ItemStack displayItem,
                                    @Nullable String spawnEffect) {
        this.id = id;
        this.catalyst = catalyst;
        this.ingredients = List.copyOf(ingredients);
        this.spiritCost = Math.max(0, spiritCost);
        this.durationTicks = Math.max(1, durationTicks);
        this.strictNbt = strictNbt;
        this.entityId = entityId;
        this.nianxian = Math.max(0, nianxian);
        this.displayItem = displayItem;
        this.spawnEffect = spawnEffect;
    }

    // ==================== 仪式参数 ====================

    @Override
    public ItemStack getCatalyst() {
        return catalyst;
    }

    @Override
    public List<RitualIngredient> getRitualIngredients() {
        return ingredients;
    }

    @Override
    public int getSpiritCost() {
        return spiritCost;
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public boolean isStrictNbt() {
        return strictNbt;
    }

    // ==================== 产物 ====================

    public ResourceLocation getEntityId() {
        return entityId;
    }

    public long getNianxian() {
        return nianxian;
    }

    /**
     * 成品展示物：JEI 产物槽、以及以后任何「这个仪式产出什么」的展示都用它。
     *
     * <p>为什么不从 {@link #getEntityId()} 反推刷怪蛋：</p>
     * <ul>
     *   <li>末影龙、凋灵、巨人这些<b>没有刷怪蛋</b>，反推只能退化成鸡蛋，看着像产物是鸡蛋；</li>
     *   <li>自己注册的刷怪蛋如果没继承 {@code SpawnEggItem}，{@code SpawnEggItem.byId} 也找不到；</li>
     *   <li>有时产物想展示成「召唤石」而不是蛋，反推做不到。</li>
     * </ul>
     * <p>所以直接把展示物写成必填参数，想让 JEI 显示什么就写什么。</p>
     */
    public ItemStack getDisplayItem() {
        return displayItem;
    }

    @Nullable
    public String getSpawnEffect() {
        return spawnEffect;
    }

    /**
     * 配方里的生物 ID 现在能不能解析成实体类型。
     *
     * <p>启动仪式前会调这个：写错 id 或装了的模组被删掉时直接拒绝，
     * 而不是让玩家先烧掉 10 秒的精神力和材料才发现「召不出来」。</p>
     */
    public boolean isEntityAvailable() {
        return EntityType.byString(entityId.toString()).isPresent();
    }

    // ==================== Recipe 接口（不被容器使用） ====================

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(catalyst));
        for (RitualIngredient ing : ingredients) {
            list.add(Ingredient.of(ing.stack()));
        }
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HUNLING_SPAWN_SERIALIZER.get();
    }

    @Override
    public RecipeType<HunlingSpawnRitualRecipe> getType() {
        return ModRecipes.HUNLING_SPAWN_TYPE.get();
    }
}
