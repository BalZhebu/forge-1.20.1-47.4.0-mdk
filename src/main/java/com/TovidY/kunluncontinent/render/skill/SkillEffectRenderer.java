package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能特效渲染器的<b>总入口</b>：持有所有活跃特效，在世界渲染管线里统一绘制。
 *
 * <h3>为什么挂在 {@code AFTER_ENTITIES}</h3>
 * <p>这个阶段实体已经画完、三维投影矩阵还是"世界空间"的那一套，适合画贴地的大型法阵。
 * 项目里魂环的第一人称补渲染用的也是同一套空间口径。</p>
 *
 * <h3>顶点坐标口径（关键，踩过坑）</h3>
 * <p>特效走 {@code BufferUploader.drawWithShader}（和 {@code RenderType} 同一条路），它的内部是：
 * <pre>
 *   vertexbuffer.drawWithShader(RenderSystem.getModelViewMatrix(),
 *                               RenderSystem.getProjectionMatrix(),
 *                               RenderSystem.getShader());
 * </pre>
 * 于是有两条硬性要求：</p>
 * <ol>
 *   <li>顶点写成<b>「世界坐标 - 相机坐标」</b>（相机相对），并且<u>不要</u>再乘 poseStack ——
 *       乘了会变换两次；</li>
 *   <li><b>渲染前必须自己把 ModelViewMat 设成相机视图矩阵。</b>
 *       {@code RenderSystem.getModelViewMatrix()} 不是"永远等于相机矩阵"的：
 *       它只在 {@code applyModelViewMatrix()} 被调用时才从 modelViewStack 同步一次，
 *       而世界渲染期间没人动过它 —— 直接拿到的会是 GUI 阶段
 *       （{@code GameRenderer} 画 HUD 时 {@code setIdentity(); translate(0,0,1000-guiFar)}）
 *       遗留的矩阵，几何体会被推到远裁剪面外，表现为
 *       <b>"只有某个特定视角能看到一小块，其余全不见"</b>。
 *       见 {@link #onRenderLevelStage} 里的 {@code applyCameraMatrix()}。</li>
 * </ol>
 * <p>vanilla 自己也是这么做的：粒子渲染前 {@code ParticleEngine:414-417}、
 * 云和天气前 {@code LevelRenderer:1411-1413}，都是 push + 设矩阵 + apply。</p>
 *
 * <h3>特效怎么产生</h3>
 * <p>服务端发包 → {@link #spawn} → 进列表 → 每帧绘制 → 到点自动丢弃。
 * 所以服务端不需要再发"特效结束"包。</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkillEffectRenderer {

    /** 同一时刻最多保留多少个特效，防止异常情况下无限堆积。 */
    private static final int MAX_ACTIVE = 64;

    private static final List<SkillEffect> ACTIVE = new ArrayList<>();

    /** 上一次渲染时所在的客户端世界，用来在切维度 / 换存档时清空特效。 */
    private static ClientLevel lastLevel;

    private SkillEffectRenderer() {
    }

    /** 服务端包到达时调用。 */
    public static void spawn(@Nullable SkillEffect effect) {
        if (effect == null) {
            return;
        }
        if (ACTIVE.size() >= MAX_ACTIVE) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(effect);
    }

    public static void clear() {
        ACTIVE.clear();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ACTIVE.isEmpty()) {
            lastLevel = level;
            return;
        }
        // 切维度 / 换存档：旧特效的世界坐标已经没意义了
        if (lastLevel != level) {
            ACTIVE.clear();
            lastLevel = level;
            return;
        }

        long gameTime = level.getGameTime();
        float partialTick = event.getPartialTick();
        Vec3 cameraPos = event.getCamera().getPosition();

        ACTIVE.removeIf(effect -> effect.isFinished(gameTime));
        if (ACTIVE.isEmpty()) {
            return;
        }

        // ⚠️ 渲染前必须把 ModelViewMat 设成「相机视图矩阵」，否则几何体全被裁掉。
        // 详见类注释「顶点坐标口径」第 2 条 —— 这是本框架最容易踩的坑。
        PoseStack poseStack = event.getPoseStack();
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        // 用 set 而不是 mulPoseMatrix：完全覆盖栈顶，结果不依赖父级残留是什么
        modelView.last().pose().set(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        try {
            // 渲染途中不能直接动 ACTIVE（增强 for 里 remove 会炸 CME），先收集坏掉的
            List<SkillEffect> broken = null;
            for (SkillEffect effect : ACTIVE) {
                try {
                    effect.render(cameraPos, gameTime, partialTick);
                } catch (Throwable t) {
                    // 单个特效出问题不能连累整帧渲染
                    KlMain.LOGGER.error("[昆仑] 技能特效渲染异常，已跳过：{}", effect.getClass().getSimpleName(), t);
                    if (broken == null) {
                        broken = new ArrayList<>(1);
                    }
                    broken.add(effect);
                }
            }
            if (broken != null) {
                ACTIVE.removeAll(broken);
            }
        } finally {
            // 必须还原：后面的云 / 天气 / 世界边界渲染都依赖这个矩阵栈
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /**
     * 资源重载（F3+T）后底层 shader program 会被释放，缓存的 {@code ShaderInstance} 全部作废。
     * 清掉缓存后，下一帧渲染时会按需重新构造。
     */
    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadHandler {
        @SubscribeEvent
        public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                @Override
                protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                    return null;
                }

                @Override
                protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
                    SkillShaders.invalidate();
                    // 顺手重新武装「特效已创建」日志，方便不重启游戏连续排查
                    SkillEffects.resetLogThrottle();
                }
            });
        }
    }

    /** 给特效用的着色器 id 拼装工具，省得每处都手写 new ResourceLocation。 */
    public static ResourceLocation shader(String name) {
        return new ResourceLocation(KlMain.MOD_ID, name);
    }
}
