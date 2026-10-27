package com.TovidY.kunluncontinent.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 读 {@code SinglePoolElement} 的模板 id —— 用来判断「这个池元素是不是我们的 bank」。
 *
 * <p>字段是 {@code protected final Either<ResourceLocation, StructureTemplate>}：
 * 左边是模板 id（数据包加载的常态），右边是已经解析好的 StructureTemplate。</p>
 */
@Mixin(SinglePoolElement.class)
public interface SinglePoolElementAccessor {

    @Accessor("template")
    Either<ResourceLocation, StructureTemplate> kl$getTemplate();
}
