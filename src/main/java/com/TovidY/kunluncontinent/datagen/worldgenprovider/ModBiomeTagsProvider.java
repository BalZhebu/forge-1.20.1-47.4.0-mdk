package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.worldgen.ModBiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 本模组的生物群系标签。
 *
 * <h3>为什么需要自定义标签</h3>
 * <p>1.20.1 的原版 {@link BiomeTags} 里<b>没有</b> {@code #minecraft:is_desert}，
 * 也没有任何「排除法」机制（标签只能做并集，不能做差集）。
 * 所以「除沙漠海洋以外的所有主世界群系」只能靠自定义标签表达。</p>
 *
 * <p>这里刻意做成<b>黑名单</b>：主世界群系有 40+ 个，白名单要逐个列举、
 * 以后 Mojang 加了新群系（或玩家装了加群系的 mod）就会漏掉；
 * 黑名单只列要排除的几类，新群系自动纳入。</p>
 */
public class ModBiomeTagsProvider extends BiomeTagsProvider {

    public ModBiomeTagsProvider(PackOutput output,
                                CompletableFuture<HolderLookup.Provider> lookupProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, KlMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 孤竹不生成的群系：海洋（含深海，is_deep_ocean 是 is_ocean 的子集，两个都写更直观）、
        // 沙滩、河流，以及沙漠本体。
        this.tag(ModBiomeTags.LONE_BAMBOO_BLOCKED)
                .addTag(BiomeTags.IS_OCEAN)
                .addTag(BiomeTags.IS_DEEP_OCEAN)
                .addTag(BiomeTags.IS_BEACH)
                .addTag(BiomeTags.IS_RIVER)
                .add(Biomes.DESERT);
    }
}
