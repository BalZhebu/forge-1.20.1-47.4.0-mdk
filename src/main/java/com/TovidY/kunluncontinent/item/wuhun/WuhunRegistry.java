package com.TovidY.kunluncontinent.item.wuhun;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 武魂注册表：<b>新增武魂的唯一入口</b>。
 *
 * <h3>加一个武魂要做什么</h3>
 * <ol>
 *   <li>在 {@code Wuhunname} 加一个名字常量（其它地方引用它）；</li>
 *   <li>写 9 个魂技类（或者用 {@code SkillSpec} 声明）；</li>
 *   <li>在 {@code ModItems} 里写<b>一个</b> {@link #define} 块（见 {@link WuhunDef} 的示例）；</li>
 *   <li>放 9 张贴图（{@code textures/item/skill_<id>_<n>.png}）+ 果实贴图；</li>
 *   <li>{@code runData}。</li>
 * </ol>
 * <p>以前还要额外改 {@code HUNJILIST}、{@code SkillLibrary}、{@code wuhunListsnameall}、
 * 语言文件共 36 行，现在这些都从本表推导，<b>不用再改</b>。</p>
 *
 * <h3>与旧写法共存</h3>
 * <p>老的 {@code ModItems.SKILL_XXX_N} 字段与 {@code SkillLibrary} 里的手工 register 仍然有效：
 * 查表逻辑是"先查本注册表，查不到再回落到旧表"，所以可以逐个武魂迁移，不影响没迁移的。</p>
 */
public final class WuhunRegistry {

    /** 按显示名索引（{@code Wuhunname} 里的常量就是显示名）。 */
    private static final Map<String, WuhunDef> BY_NAME = new LinkedHashMap<>();

    private WuhunRegistry() {
    }

    /** 开始定义一个武魂。 */
    public static Builder define(String id, String displayName, WuhunDef.Category category, String color) {
        return new Builder(new WuhunDef(id, displayName, category, color));
    }

    /** 链式定义器。 */
    public static final class Builder {

        private final WuhunDef def;

        private Builder(WuhunDef def) {
            this.def = def;
        }

        /**
         * 声明一个魂技槽（物品 id 自动生成 {@code skill_<武魂id>_<槽位>}）。
         *
         * @param slot      第几槽（1~9）
         * @param name      技能中文名（自动生成语言文件用）
         * @param suppliers 该槽的魂技；传多个 = 随机池，id 自动加 b/c/d… 后缀
         */
        @SafeVarargs
        public final Builder skill(int slot, String name, Supplier<? extends BaseSkillItem>... suppliers) {
            return registerSlot(slot, "skill_" + def.id() + "_" + slot, name, suppliers);
        }

        /**
         * 声明一个魂技槽，但<b>物品 id 用手写的</b>。
         *
         * <p>迁移老武魂时用：它们的历史 id 不统一（{@code skill_pohun_1}、
         * {@code skill_bahuang_1} 这类没有统一前缀），改 id 会让玩家背包里的旧物品变空气。</p>
         */
        @SafeVarargs
        public final Builder skillAs(String itemId, int slot, String name, Supplier<? extends BaseSkillItem>... suppliers) {
            return registerSlot(slot, itemId, name, suppliers);
        }

        @SafeVarargs
        private Builder registerSlot(int slot, String baseId, String name, Supplier<? extends BaseSkillItem>... suppliers) {
            List<RegistryObject<Item>> owned = new ArrayList<>(suppliers.length);
            for (int i = 0; i < suppliers.length; i++) {
                String itemId = baseId + (i == 0 ? "" : String.valueOf((char) ('a' + i)));
                Supplier<? extends BaseSkillItem> supplier = suppliers[i];
                owned.add(ModItems.ITEMS.register(itemId, supplier::get));
            }
            def.putSlot(slot, new WuhunDef.SkillSlot(name, List.copyOf(owned), List.copyOf(owned)));
            return this;
        }

        /**
         * 往已有槽位<b>追加既有魂技</b>（只进随机池，不再注册、不生成模型/语言）。
         *
         * <p>给裂金虎 / 磐石巨猿这种"第一魂技五选一"用：四个变体是早年用
         * {@code skillVariant(...)} 注册的（图标复用主技能），迁到本注册表时
         * 只需挂进池子，避免重复注册。</p>
         */
        @SafeVarargs
        public final Builder extraSkill(int slot, RegistryObject<? extends Item>... existing) {
            WuhunDef.SkillSlot current = def.slot(slot);
            List<RegistryObject<Item>> owned = new ArrayList<>(current == null ? List.of() : current.owned());
            List<RegistryObject<Item>> pool = new ArrayList<>(current == null ? List.of() : current.pool());
            for (RegistryObject<? extends Item> ref : existing) {
                @SuppressWarnings("unchecked")
                RegistryObject<Item> casted = (RegistryObject<Item>) (RegistryObject<?>) ref;
                pool.add(casted);
            }
            def.putSlot(slot, new WuhunDef.SkillSlot(
                    current == null ? "魂技" : current.name(), List.copyOf(owned), List.copyOf(pool)));
            return this;
        }

        /**
         * 接管一个<b>已经注册好</b>的魂技物品（迁移老武魂时用）。
         *
         * <p>物品 id、贴图都保持不变；模型与语言文件仍由本注册表生成（算作 owned）。
         * 典型场景：裂金虎 / 磐石巨猿的第一魂技，既要被变体当图标来源，又要有名字。</p>
         */
        public Builder skillRef(int slot, String name, RegistryObject<? extends BaseSkillItem> existing) {
            @SuppressWarnings("unchecked")
            RegistryObject<Item> ref = (RegistryObject<Item>) (RegistryObject<?>) existing;
            def.putSlot(slot, new WuhunDef.SkillSlot(name, List.of(ref), List.of(ref)));
            return this;
        }

        /**
         * 声明武魂果实（可选，{@code itemId} 用完整注册名，如 {@code guoshi_tianshi}）。
        public Builder fruit(String itemId, Supplier<Item> supplier) {
            def.setFruit(ModItems.ITEMS.register(itemId, supplier));
            return this;
        }

        /**
         * 声明这个武魂<b>不参与普通觉醒池</b>（走自己的独立概率）。
         *
         * <p>天使必须调这个：它已有一套 10% 的独立掷骰，再进池子等于抽两次。</p>
         */
        public Builder independentAwaken() {
            def.setPoolAwaken(false);
            return this;
        }

        /** 完成注册，加入全局表。 */
        public WuhunDef build() {
            BY_NAME.put(def.displayName(), def);
            return def;
        }
    }

    /** 全部武魂（按定义顺序）。 */
    public static List<WuhunDef> all() {
        return new ArrayList<>(BY_NAME.values());
    }

    /** 按显示名取（没有则 null）。 */
    public static WuhunDef byName(String displayName) {
        return BY_NAME.get(displayName);
    }

    /** 参与普通觉醒池的武魂名（独立概率的武魂不在这里）。 */
    public static List<String> names() {
        List<String> pool = new ArrayList<>();
        for (WuhunDef def : BY_NAME.values()) {
            if (def.poolAwaken()) {
                pool.add(def.displayName());
            }
        }
        return pool;
    }

    /** 本注册表里所有魂技物品（模型生成用）。 */
    public static List<RegistryObject<Item>> allSkillItems() {
        List<RegistryObject<Item>> all = new ArrayList<>();
        for (WuhunDef def : BY_NAME.values()) {
            all.addAll(def.allSkills());
        }
        return all;
    }

    /**
     * 取某个武魂第 {@code slot} 槽的随机魂技。
     *
     * @return 本表没登记这个武魂 / 没这个槽 → {@code null}（调用方回落到旧逻辑）
     */
    public static BaseSkillItem randomSkill(String wuhunName, int slot, RandomSource random) {
        WuhunDef def = BY_NAME.get(wuhunName);
        if (def == null) {
            return null;
        }
        WuhunDef.SkillSlot s = def.slot(slot);
        return s == null ? null : s.random(random);
    }
}
