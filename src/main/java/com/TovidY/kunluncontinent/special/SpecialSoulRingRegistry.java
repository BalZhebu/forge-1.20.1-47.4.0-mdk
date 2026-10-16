package com.TovidY.kunluncontinent.special;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.SkillLibrary;
import com.TovidY.kunluncontinent.special.lonebamboo.LoneBambooSoulRing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 特殊生物魂环注册表。
 *
 * <p>两张查阅表：</p>
 * <ul>
 *   <li><b>按实体类型</b>查（{@link #get}）—— 击杀时判定「这只生物是不是特殊生物、要不要必掉魂环 / 不给魂骨」；</li>
 *   <li><b>按来源 ID</b>查（{@link #getBySourceId}）—— 吸收时判定「这个魂环来自哪只特殊生物、给什么技能」。
 *       魂环是独立实体，拿不到原生物对象，所以来源 ID 由
 *       {@code MobAttributeCapability#setSourceType} 随能力一起复制过来。</li>
 * </ul>
 */
public final class SpecialSoulRingRegistry {

    private static final Map<ResourceLocation, SpecialSoulRing> BY_TYPE = new LinkedHashMap<>();
    private static final Map<String, SpecialSoulRing> BY_ID = new LinkedHashMap<>();
    private static boolean initialized = false;

    private SpecialSoulRingRegistry() {
    }

    /**
     * 注册一个特殊生物。
     *
     * @throws IllegalStateException 实体类型未注册，或同一生物类型重复注册
     *                               （宁可炸掉也不要静默覆盖 —— 后者会让「为什么没生效」非常难查）
     */
    public static void register(SpecialSoulRing ring) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(ring.entityType());
        if (id == null) {
            throw new IllegalStateException("特殊魂环注册失败：实体类型尚未注册 -> " + ring.getClass().getName());
        }
        if (BY_TYPE.putIfAbsent(id, ring) != null) {
            throw new IllegalStateException("特殊魂环重复注册：" + id);
        }
        BY_ID.put(id.toString(), ring);
    }

    /** 按实体类型查（击杀掉落判定用）。未注册返回 {@code null}。 */
    @Nullable
    public static SpecialSoulRing get(EntityType<?> type) {
        ensureInit();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return id == null ? null : BY_TYPE.get(id);
    }

    /** 按「来源实体 ID」查（魂环吸收判定用，形如 {@code kunluncontinent:lone_bamboo}）。 */
    @Nullable
    public static SpecialSoulRing getBySourceId(@Nullable String sourceEntityId) {
        ensureInit();
        return sourceEntityId == null || sourceEntityId.isEmpty() ? null : BY_ID.get(sourceEntityId);
    }

    /**
     * 技能分配的<b>唯一入口</b>：特殊魂环优先，否则回落到该武魂的正常技能池。
     *
     * <p>调用点只有一个（{@code HunhuanEntity#handleAbsorbed}），所以任何
     * 「分配魂技」的规则变化都只需要改这里或各 {@link SpecialSoulRing} 实现。</p>
     *
     * @param sourceEntityId 魂环携带的来源实体 ID，可能为 {@code null} / 空串
     */
    @Nullable
    public static BaseSkillItem resolveSkill(@Nullable String sourceEntityId,
                                             String wuhun,
                                             int ringIndex,
                                             RandomSource random) {
        SpecialSoulRing special = getBySourceId(sourceEntityId);
        if (special != null) {
            BaseSkillItem skill = special.createSkill(wuhun, ringIndex, random);
            if (skill != null) {
                return skill;
            }
        }
        return SkillLibrary.getRandomSkill(wuhun, ringIndex, random);
    }

    /**
     * 懒初始化。
     *
     * <p><b>不能在静态块里直接注册</b>：那会在类初始化阶段就去读
     * {@code EntityInit} 里的 {@code EntityType}，可能早于注册表完成填充。
     * 本类第一次被调用一定发生在游戏运行期（击杀 / 吸收），此时再注册万无一失。</p>
     */
    private static void ensureInit() {
        if (initialized) {
            return;
        }
        initialized = true;
        // ↓↓↓ 以后新增特殊生物，在这里加一行 ↓↓↓
        register(new LoneBambooSoulRing());
    }
}
