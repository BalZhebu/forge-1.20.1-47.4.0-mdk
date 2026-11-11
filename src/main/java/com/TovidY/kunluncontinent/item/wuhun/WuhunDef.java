package com.TovidY.kunluncontinent.item.wuhun;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 一个武魂的<b>完整声明</b>：名字 + 九个魂技槽 + 武魂果实。
 *
 * <h3>它解决的问题</h3>
 * <p>以前新增一个武魂要改 5 处样板：{@code Wuhunname} 加常量、{@code ModItems} 加 9 个
 * {@code RegistryObject} 字段、{@code HUNJILIST} 加 9 行、{@code SkillLibrary} 加 9 行
 * {@code register(...)}、{@code ModZhCnLangProvider} 加 9 行文案 —— 40 行纯拷贝粘贴，
 * 漏一处就出现"魂技拿到了但没有名字/没有模型"这类问题。</p>
 *
 * <p>现在只需要在 {@code ModItems} 里写<b>一个定义块</b>：</p>
 * <pre>{@code
 * public static final WuhunDef WUHUN_XXX = WuhunRegistry.define("xxx", "某武魂", WuhunDef.Category.BEAST, "§b")
 *         .skill(1, "第一魂技名", SkillXxx1::new)
 *         ...
 *         .skill(9, "第九魂技名", SkillXxx9::new)
 *         .fruit("guoshi_xxx", () -> new WuhunguoshiItem(...).setWuhunname("某武魂"))
 *         .build();
 * }</pre>
 * <p>结果：物品注册、魂技池（{@link com.TovidY.kunluncontinent.item.baseskillist.SkillLibrary}）、
 * 觉醒池（{@code PlayerHunhuanAPI#getWuhunNamesAll}）、模型生成、语言文件
 * <b>全部自动接上</b>，不需要再逐个文件加行。</p>
 *
 * <h3>特殊武魂</h3>
 * <p>有独特机制的武魂（天使的独立觉醒、孤竹的专属路线…）仍然单独写一个类，
 * 由本类只管"数据与注册"这一层，两者不冲突。见 {@code TianshiWuhun}。</p>
 */
public final class WuhunDef {

    /** 武魂分类，只用于面板分组/排序与后续扩展，不影响判定。 */
    public enum Category {
        /** 器武魂（破魂枪、八荒戟）。 */
        WEAPON,
        /** 兽武魂（裂金虎、磐石巨猿）。 */
        BEAST,
        /** 特殊武魂（天使等，走独立规则）。 */
        SPECIAL
    }

    /**
     * 一个魂技槽。
     *
     * @param name  技能中文名（生成语言文件用）
     * @param owned 由本定义<b>自己注册</b>的魂技（模型 + 语言文件自动生成，只算这些）
     * @param pool  该槽的完整随机池（含用 {@code extraSkill} 挂进来的既有魂技，如裂金虎的变体）
     */
    public record SkillSlot(String name, List<RegistryObject<Item>> owned, List<RegistryObject<Item>> pool) {

        /** 从池里随机取一个魂技（池为空返回 null）。 */
        public BaseSkillItem random(RandomSource random) {
            if (pool.isEmpty()) {
                return null;
            }
            Item item = pool.get(random.nextInt(pool.size())).get();
            return item instanceof BaseSkillItem skill ? skill : null;
        }

        /** 池里第一个（展示/图标用）。 */
        public Item item() {
            return pool.isEmpty() ? null : pool.get(0).get();
        }
    }

    private final String id;
    private final String displayName;
    private final Category category;
    private final String color;
    /** 下标 0 = 第 1 魂技槽。 */
    private final SkillSlot[] slots = new SkillSlot[9];
    private RegistryObject<Item> fruitItem = null;

    /**
     * 是否参与「普通觉醒池」的等概率分配。
     *
     * <p>天使这类<b>独立概率</b>的武魂必须设为 {@code false}：它自己有 10% 的单独掷骰
     * （见 {@code TianshiWuhun}），再进池子就等于抽两次，其它武魂的概率全被稀释。</p>
     */
    private boolean poolAwaken = true;

    WuhunDef(String id, String displayName, Category category, String color) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.color = color;
    }

    // ==================================================================================
    //  读取
    // ==================================================================================

    /** 注册名（{@code tianshi}），同时是物品 id 前缀。 */
    public String id() {
        return id;
    }

    /** 显示名（{@code 天使}），与 {@code Wuhunname} 里的常量一致。 */
    public String displayName() {
        return displayName;
    }

    public Category category() {
        return category;
    }

    /** 该武魂的主色（{@code §e} 这类），用于自动生成语言文件。 */
    public String color() {
        return color;
    }

    /**
     * 取第 {@code slot}（1~9）个魂技槽。
     *
     * @return 未定义该槽时返回 {@code null}
     */
    public SkillSlot slot(int slot) {
        if (slot < 1 || slot > 9) {
            return null;
        }
        return slots[slot - 1];
    }

    /** 第 {@code slot} 槽的随机池（未定义返回空列表）。 */
    public List<RegistryObject<Item>> skills(int slot) {
        SkillSlot s = slot(slot);
        return s == null ? List.of() : s.pool();
    }

    /** 第 {@code slot} 槽<b>自己注册</b>的魂技（语言文件用，不含 extraSkill 挂进来的）。 */
    public List<RegistryObject<Item>> ownedSkills(int slot) {
        SkillSlot s = slot(slot);
        return s == null ? List.of() : s.owned();
    }

    /** 第 {@code slot} 槽的技能名（未定义返回 null）。 */
    public String skillName(int slot) {
        SkillSlot s = slot(slot);
        return s == null ? null : s.name();
    }

    /** 武魂果实（可能为 null）。 */
    public Item fruit() {
        return fruitItem == null ? null : fruitItem.get();
    }

    /** 是否参与普通觉醒池（见 {@link #poolAwaken}）。 */
    public boolean poolAwaken() {
        return poolAwaken;
    }

    void setPoolAwaken(boolean value) {
        this.poolAwaken = value;
    }

    /** 本武魂<b>自己注册</b>的魂技物品（模型生成/语言文件用，不含 extraSkill 挂进来的）。 */
    public List<RegistryObject<Item>> allSkills() {
        List<RegistryObject<Item>> all = new ArrayList<>();
        for (SkillSlot s : slots) {
            if (s != null) {
                all.addAll(s.owned());
            }
        }
        return all;
    }

    // ==================================================================================
    //  写入（只允许 WuhunRegistry 在 build() 时调用）
    // ==================================================================================

    void putSlot(int slot, SkillSlot value) {
        if (slot >= 1 && slot <= 9) {
            slots[slot - 1] = value;
        }
    }

    void setFruit(RegistryObject<Item> fruit) {
        this.fruitItem = fruit;
    }
}
