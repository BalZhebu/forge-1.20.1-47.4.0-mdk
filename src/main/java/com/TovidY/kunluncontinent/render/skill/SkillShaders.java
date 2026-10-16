package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 技能特效着色器的加载与缓存。
 *
 * <p>着色器放在 {@code assets/kunluncontinent/shaders/core/}，每个特效一组
 * {@code .json + .vsh + .fsh}。顶点格式统一用
 * {@link DefaultVertexFormat#POSITION_COLOR}（位置 + 颜色，不带贴图）。</p>
 *
 * <h3>为什么要自己缓存</h3>
 * <p>{@link ShaderInstance} 的构造要读 json、编译、链接 —— 每帧新建会直接把帧率吃光，
 * 所以按 id 缓存，只建一次。</p>
 *
 * <h3>为什么要单独处理「加载失败」</h3>
 * <p>着色器是唯一一类「写错了会让客户端渲染出问题」的东西。所以这里把异常全部吃掉，
 * 失败就返回 {@code null} 并<b>永久记住</b>（不再反复重试拖慢帧率）。
 * 调用方拿到 {@code null} 就跳过这次绘制 —— 技能原本的粒子表现还在，
 * 不会变成"什么也没有"。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class SkillShaders {

    private static final Map<ResourceLocation, ShaderInstance> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();

    private SkillShaders() {
    }

    /**
     * 取一个已编译的技能特效着色器。首次调用时会编译，之后走缓存。
     *
     * @return 加载 / 编译失败时返回 {@code null}，调用方<b>必须</b>做降级处理
     */
    @Nullable
    public static ShaderInstance get(ResourceLocation id) {
        if (FAILED.contains(id)) {
            return null;
        }
        ShaderInstance cached = CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    Minecraft.getInstance().getResourceManager(),
                    id,
                    DefaultVertexFormat.POSITION_COLOR);
            CACHE.put(id, shader);
            KlMain.LOGGER.info("[昆仑] 技能特效着色器已加载：{}", id);
            return shader;
        } catch (Throwable t) {
            FAILED.add(id);
            KlMain.LOGGER.error("[昆仑] 技能特效着色器加载失败，该特效退回纯粒子表现：{}", id, t);
            return null;
        }
    }

    /**
     * 资源重载后底层 program 会被释放，缓存的实例全部作废。
     *
     * <p>这里<b>不调 {@code close()}</b> —— 重载流程已经释放过底层 program，
     * 再关一次就是二次释放。只要丢掉引用，下一帧渲染时会按需重新构造。</p>
     */
    public static void invalidate() {
        CACHE.clear();
        FAILED.clear();
    }
}
