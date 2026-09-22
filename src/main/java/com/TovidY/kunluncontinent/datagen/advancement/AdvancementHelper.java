package com.TovidY.kunluncontinent.datagen.advancement;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.KunlunTrigger;
import com.TovidY.kunluncontinent.advancement.LevelTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

/**
 * 成就 datagen 工具类。
 *
 * <p>把 {@code Advancement.Builder} 那一大串样板（display / translatable / addCriterion / save）
 * 收敛成一行调用，新增成就时只需要提供：父成就、路径名、图标、触发条件。</p>
 *
 * <p>约定：</p>
 * <ul>
 *   <li>成就路径统一为 {@code kunluncontinent:main/<path>}</li>
 *   <li>语言键默认 {@code adv.kunlun.<path>.title} / {@code adv.kunlun.<path>.desc}</li>
 *   <li>框类型：{@link #task} 普通 · {@link #goal} 目标 · {@link #challenge} 挑战</li>
 * </ul>
 */
public final class AdvancementHelper {

    /** 自定义触发器的判定条件名，统一用一个即可。 */
    private static final String CRITERION = "criterion";
    private static final String DEFAULT_LANG_PREFIX = "adv.kunlun.";
    private static final String PATH_PREFIX = "main/";

    private AdvancementHelper() {
    }

    // ==================================================================================
    //  三个档次的便捷入口（默认语言键）
    // ==================================================================================

    public static Advancement task(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                   String path, ItemLike icon, CriterionTriggerInstance criterion) {
        return task(saver, helper, parent, path, DEFAULT_LANG_PREFIX + path, icon, criterion);
    }

    public static Advancement goal(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                   String path, ItemLike icon, CriterionTriggerInstance criterion) {
        return goal(saver, helper, parent, path, DEFAULT_LANG_PREFIX + path, icon, criterion);
    }

    public static Advancement challenge(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                        String path, ItemLike icon, CriterionTriggerInstance criterion) {
        return challenge(saver, helper, parent, path, DEFAULT_LANG_PREFIX + path, icon, criterion);
    }

    // ==================================================================================
    //  指定语言键（兼容项目里已有成就的旧键名）
    // ==================================================================================

    public static Advancement task(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                   String path, String langKey, ItemLike icon, CriterionTriggerInstance criterion) {
        return builder(parent, path, langKey, icon, FrameType.TASK, null, criterion).save(saver, id(path), helper);
    }

    public static Advancement goal(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                   String path, String langKey, ItemLike icon, CriterionTriggerInstance criterion) {
        return builder(parent, path, langKey, icon, FrameType.GOAL, null, criterion).save(saver, id(path), helper);
    }

    public static Advancement challenge(Consumer<Advancement> saver, ExistingFileHelper helper, Advancement parent,
                                        String path, String langKey, ItemLike icon, CriterionTriggerInstance criterion) {
        return builder(parent, path, langKey, icon, FrameType.CHALLENGE, null, criterion).save(saver, id(path), helper);
    }

    /** 根成就：无父级、带背景图。 */
    public static Advancement root(Consumer<Advancement> saver, ExistingFileHelper helper, String path, String langKey,
                                   ItemLike icon, ResourceLocation background, CriterionTriggerInstance criterion) {
        return builder(null, path, langKey, icon, FrameType.TASK, background, criterion).save(saver, id(path), helper);
    }

    // ==================================================================================
    //  条件构造
    // ==================================================================================

    /** 自定义事件条件：配合 {@code AchievementAPI} 里的事件 ID 使用。 */
    public static CriterionTriggerInstance event(String eventId) {
        return KunlunTrigger.Instance.of(eventId);
    }

    /** 背包内出现指定物品（可多个，满足其一即可）。 */
    public static CriterionTriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    /** 达到指定等级。 */
    public static CriterionTriggerInstance levelReached(int level) {
        return LevelTrigger.Instance.levelReached(level);
    }

    /** 进入世界（用于根成就）。 */
    public static CriterionTriggerInstance onJoin() {
        return PlayerTrigger.TriggerInstance.tick();
    }

    // ==================================================================================
    //  语言键
    // ==================================================================================

    public static String titleKey(String langKey) {
        return langKey + ".title";
    }

    public static String descKey(String langKey) {
        return langKey + ".desc";
    }

    // ==================================================================================
    //  内部
    // ==================================================================================

    private static Advancement.Builder builder(Advancement parent, String path, String langKey, ItemLike icon,
                                               FrameType frame, ResourceLocation background, CriterionTriggerInstance criterion) {
        Advancement.Builder builder = Advancement.Builder.advancement()
                .display(icon,
                        Component.translatable(titleKey(langKey)),
                        Component.translatable(descKey(langKey)),
                        background,
                        frame,
                        true,   // 弹出提示
                        true,   // 聊天栏公告
                        false)  // 不隐藏
                .addCriterion(CRITERION, criterion);
        if (parent != null) {
            builder.parent(parent);
        }
        return builder;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(KlMain.MOD_ID, PATH_PREFIX + path);
    }
}
