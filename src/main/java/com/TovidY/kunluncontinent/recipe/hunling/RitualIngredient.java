package com.TovidY.kunluncontinent.recipe.hunling;

import com.google.gson.JsonObject;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 一条副物品需求（摆在基座上的那种），物品 / 物品牌仪式共用。
 *
 * <h3>为什么用 ItemStack 而不是原版 Ingredient</h3>
 * <p>原版 {@code Ingredient} 的 {@code ItemValue} <b>只有 item 字段、不带 NBT</b>，
 * 而 {@code strict_nbt} 开关需要连 NBT 一起比。所以这里直接存 {@link ItemStack}，
 * 匹配交给自己写的 {@link #matches(ItemStack, boolean)}。</p>
 *
 * <p>⚠️ 这里也放 JSON 的读写工具：两个配方序列化器都要用，
 * 放在任一方类里都会让另一方产生无谓的依赖。</p>
 */
public record RitualIngredient(ItemStack stack, int count, ItemStack remainder) {

    /** 这一件能不能顶替目标物品。 */
    public boolean matches(ItemStack target, boolean strictNbt) {
        if (target.isEmpty()) {
            return false;
        }
        if (strictNbt) {
            return ItemStack.isSameItemSameTags(stack, target);
        }
        return stack.is(target.getItem());
    }

    public boolean hasRemainder() {
        return !remainder.isEmpty();
    }

    // ==================== JSON ====================

    /** 一条副物品写成 JSON。 */
    public static JsonObject toJson(RitualIngredient ing) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", ForgeRegistries.ITEMS.getKey(ing.stack().getItem()).toString());
        obj.addProperty("count", ing.count());
        if (ing.stack().hasTag()) {
            obj.addProperty("nbt", ing.stack().getTag().toString());
        }
        if (ing.hasRemainder()) {
            obj.addProperty("remainder",
                    ForgeRegistries.ITEMS.getKey(ing.remainder().getItem()).toString());
            if (ing.remainder().hasTag()) {
                obj.addProperty("remainder_nbt", ing.remainder().getTag().toString());
            }
        }
        return obj;
    }

    /** 解析一条副物品。 */
    public static RitualIngredient fromJson(JsonObject obj) {
        int count = Math.max(1, GsonHelper.getAsInt(obj, "count", 1));
        ItemStack stack = parseStack(obj, "item", "nbt", 1);
        ItemStack remainder = obj.has("remainder")
                ? parseStack(obj, "remainder", "remainder_nbt", 1)
                : ItemStack.EMPTY;
        return new RitualIngredient(stack, count, remainder);
    }

    /** 解析一件带可选 NBT 的物品。 */
    public static ItemStack parseStack(JsonObject obj, String itemKey, String nbtKey, int count) {
        ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(obj, itemKey));
        Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            throw new IllegalArgumentException("未知物品: " + itemId);
        }
        ItemStack stack = new ItemStack(item, count);
        if (obj.has(nbtKey)) {
            String snbt = GsonHelper.getAsString(obj, nbtKey);
            try {
                stack.setTag(TagParser.parseTag(snbt));
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                throw new IllegalArgumentException("无效的 NBT: " + snbt, e);
            }
        }
        return stack;
    }

    /** 单件物品写成 JSON（{@code item} + 可选 {@code count} / {@code nbt}）。 */
    public static JsonObject stackToJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.getCount() != 1) {
            obj.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            obj.addProperty("nbt", stack.getTag().toString());
        }
        return obj;
    }
}
