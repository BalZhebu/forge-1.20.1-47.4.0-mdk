package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * 本模组自定义的生物群系标签。
 *
 * <p>标签内容一律由 datagen 生成（见 {@code datagen.worldgenprovider.ModBiomeTagsProvider}），
 * 这里只放 {@link TagKey} 常量。不要在别处硬编码群系清单 —— 1.20.1 没有
 * {@code #minecraft:is_desert} 这类标签，「除某类群系以外」只能用自定义标签表达。</p>
 */
public final class ModBiomeTags {

    /**
     * 孤竹<b>不</b>生成的群系：沙漠、海洋（含深海）、沙滩、河流。
     *
     * <p>走「黑名单」而不是白名单：主世界群系共 40+ 个，白名单要逐个列举且以后
     * 加了新群系就漏；黑名单只需列出要排除的几类，新群系自动纳入。</p>
     */
    public static final TagKey<Biome> LONE_BAMBOO_BLOCKED =
            TagKey.create(Registries.BIOME, new ResourceLocation(KlMain.MOD_ID, "lone_bamboo_blocked"));

    private ModBiomeTags() {
    }
}
