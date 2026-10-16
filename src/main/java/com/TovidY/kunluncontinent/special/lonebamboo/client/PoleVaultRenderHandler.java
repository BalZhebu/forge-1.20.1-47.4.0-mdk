package com.TovidY.kunluncontinent.special.lonebamboo.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooModel;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 撑杆跳的显示层：<b>双手举杆 + 一根插在地上的竹竿</b>。
 *
 * <h3>为什么不需要 Mixin、也不需要换掉玩家模型</h3>
 * <p>{@code PlayerRenderer.render} 的顺序是（见 1.20.1 源码）：</p>
 * <pre>
 *   setModelProperties(player);                       ← 每帧设置 rightArmPose / leftArmPose
 *   if (MinecraftForge.EVENT_BUS.post(RenderPlayerEvent.Pre)) return;   ← 钩子在这里
 *   super.render(...);                                ← setupAnim() 才真正摆姿
 * </pre>
 * <p>也就是说 <b>Pre 事件在 {@code setModelProperties} 之后</b>，我们在这里改
 * {@code rightArmPose} 会被紧随其后的 {@code setupAnim} 采纳；而下一帧
 * {@code setModelProperties} 又会把它重置回原样 —— 既生效、又不会泄漏。</p>
 *
 * <p>而 {@code HumanoidModel.ArmPose.BOW_AND_ARROW} 的原版摆姿恰好就是
 * 「双臂前伸、与视线同向」：</p>
 * <pre>
 *   rightArm.xRot = leftArm.xRot = -π/2 + head.xRot;   // 水平前伸
 *   rightArm.yRot = -0.1 + head.yRot;
 *   leftArm.yRot  =  0.5 + head.yRot;                  // 略向内合拢
 * </pre>
 * <p>正好是「双手在身前握住杆子」的姿势，一分钱美术资源都不用花。</p>
 *
 * <h3>竹竿</h3>
 * <p>直接复用孤竹的 {@link LoneBambooModel}（{@code bakeLayer} 拿一份），
 * 按「插在<b>起跳点</b>不动、玩家沿杆升上去」的方式渲染 —— 这比把杆子绑在身上更像撑杆跳。
 * 空间口径与 {@code PWRenderPlayerEvent} 里渲染魂环的那套一致：
 * {@code RenderPlayerEvent.Post} 时 pose stack 的原点在<b>玩家脚底</b>、坐标轴与世界对齐，
 * 所以「起跳点」直接用两者位置相减即可。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PoleVaultRenderHandler {

    // ==================================================================================
    //  竹竿的外观参数 —— 进游戏看完再微调这几个数就够
    // ==================================================================================

    /** 竹竿整体缩放。孤竹模型高约 1.9 格，×3 ≈ 5.7 格，刚好配 6 格的跳跃高度。 */
    private static final float POLE_SCALE = 3.0F;

    /** 竹竿底部相对地面的高度（负值 = 埋进地里一点，看起来像"插在地上"）。 */
    private static final float POLE_BASE_Y = -0.2F;

    /**
     * 竹竿相对玩家面朝方向的偏移（格）。
     * <p>孤竹模型左右基本对称（竹叶前后各一簇），所以这个值只影响"杆在身前还是身后"。
     * 如果进游戏发现杆子跑到背后了，把符号反过来即可。</p>
     */
    private static final float POLE_FORWARD = 0.45F;

    /** 缓存的竹竿模型（客户端资源，烘一次就够）。 */
    private static LoneBambooModel<Entity> poleModel;

    private PoleVaultRenderHandler() {
    }

    /**
     * 双手举杆：把两个手臂的姿势换成原版的「双手持械」。
     *
     * <p>⚠️ 必须挂在 {@code RenderPlayerEvent.Pre}（而不是 Post）——
     * Post 时模型已经画完了，改什么都来不及。</p>
     */
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!PoleVaultClientState.isVaulting(player)) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        model.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
    }

    /** 在起跳点画竹竿。挂在 Post：此时玩家模型已经画完，竹竿会正确盖在玩家身上。 */
    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Vec3 launch = PoleVaultClientState.launchPos(player);
        if (launch == null) {
            return;
        }
        LoneBambooModel<Entity> model = poleModel();
        if (model == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // 1. 从"玩家脚底"位移到"起跳点" —— 竹竿因此留在原地，玩家沿杆上升
        poseStack.translate(launch.x - player.getX(), launch.y - player.getY(), launch.z - player.getZ());
        // 2. 转到玩家朝向，让"身前"这个偏移跟着玩家转（竹竿左右对称，这一步主要是为了朝向自然）
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - player.getViewYRot(event.getPartialTick())));
        poseStack.translate(0.0D, POLE_BASE_Y, -POLE_FORWARD);
        // 3. 与前两种渲染器同样的口径：模型坐标系要翻 Y、翻 X 才是"正着站"
        poseStack.scale(-POLE_SCALE, -POLE_SCALE, POLE_SCALE);

        model.renderToBuffer(
                poseStack,
                event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(LoneBambooRenderer.TEXTURE)),
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    /** 惰性烘焙竹竿模型（用的是渲染层注册表里已经注册好的 {@code LAYER_LOCATION}）。 */
    private static LoneBambooModel<Entity> poleModel() {
        if (poleModel == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            if (modelSet == null) {
                return null;
            }
            poleModel = new LoneBambooModel<>(modelSet.bakeLayer(LoneBambooModel.LAYER_LOCATION));
        }
        return poleModel;
    }
}
