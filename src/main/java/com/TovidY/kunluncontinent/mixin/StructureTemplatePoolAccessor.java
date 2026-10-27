package com.TovidY.kunluncontinent.mixin;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 读 {@code StructureTemplatePool} 里那张「已按权重展开」的元素表。
 *
 * <p>{@code getRandomTemplate} 就是 {@code templates.get(random.nextInt(size))} ——
 * 所以往这个 list 里追加 N 份同一个元素，等价于给它 N 点权重。</p>
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> kl$getTemplates();
}
