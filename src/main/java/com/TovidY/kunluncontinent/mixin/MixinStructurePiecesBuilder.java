package com.TovidY.kunluncontinent.mixin;

import com.TovidY.kunluncontinent.worldgen.village.VillageBankPools;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>一个村庄最多一个银行。</b>
 *
 * <h3>为什么需要它</h3>
 * <p>jigsaw 的池子只会「按权重随机抽」，<b>没有</b>「每个结构实例唯一」这种机制。
 * 银行权重调高以后，一个村庄抽到 2~3 次是完全正常的。</p>
 *
 * <h3>为什么挂在 {@code StructurePiecesBuilder#addPiece}</h3>
 * <ul>
 *   <li>一个 {@code StructurePiecesBuilder} 实例 = <b>一次结构生成</b>（一个村庄）——
 *       {@code JigsawPlacement} 里 {@code list.forEach(builder::addPiece)} 最终落到这里，
 *       所以 {@code @Unique} 字段天然是「本村庄是否已放过银行」的计数器，不需要 ThreadLocal；</li>
 *   <li>比 {@code @Redirect} 到 {@code List#forEach} 稳：那个 target 是接口调用，
 *       混淆后 owner 可能是 {@code java/util/List} 或 {@code java/lang/Iterable}，容易匹配不上。</li>
 * </ul>
 *
 * <p>被 cancel 的片段不会加入本村庄，村庄只是少一栋房 —— 比出现两座银行可接受得多。</p>
 */
@Mixin(StructurePiecesBuilder.class)
public class MixinStructurePiecesBuilder {

    /** 本次生成（= 这个村庄）是否已经放了一枚银行。 */
    @Unique
    private boolean kl$bankPlaced;

    @Inject(method = "addPiece", at = @At("HEAD"), cancellable = true)
    private void kl$limitVillageBank(StructurePiece piece, CallbackInfo ci) {
        if (!VillageBankPools.isBankPiece(piece)) {
            return;
        }
        if (this.kl$bankPlaced) {
            ci.cancel();
            return;
        }
        this.kl$bankPlaced = true;
    }
}
