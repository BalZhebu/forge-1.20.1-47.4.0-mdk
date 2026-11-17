package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 吟唱时的人物姿势 —— 让「站着不动走进度条」至少看起来在施法。
 *
 * <h3>为什么用 {@code ArmPose} 而不是直接改手臂角度</h3>
 * <p>{@code RenderPlayerEvent.Pre} 发生在 {@code setModelProperties} <b>之后</b>、
 * {@code setupAnim} <b>之前</b>。也就是说这一帧里</p>
 * <ul>
 *   <li>直接改 {@code model.rightArm.xRot} → 会被紧接着的 {@code setupAnim} 整个覆盖，白改；</li>
 *   <li>改 {@code rightArmPose} → 它是 {@code setupAnim} 的<b>输入</b>，会被采纳。</li>
 * </ul>
 * <p>而下一帧 {@code setModelProperties} 又会把它重置回按物品决定 —— 既生效、又不会漏到别的动作上。
 * 同理见 {@code PoleVaultRenderHandler}。</p>
 *
 * <p>{@code TOOT_HORN} 是原版唯一「双手收到胸前/脸前」的姿势，也是所有内置姿势里最接近
 * 结印念咒的一个。想要更精确的「掌心朝上托举」只能 Mixin {@code PlayerModel#setupAnim} 尾部，
 * 那属于客户端模型改动，收益不值这个风险，所以没做。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT)
public class CastPoseHandler {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player == null || !CastClientState.isCasting(player.getId())) {
            return;
        }

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        if (model == null) {
            return;
        }

        model.rightArmPose = HumanoidModel.ArmPose.TOOT_HORN;
        model.leftArmPose = HumanoidModel.ArmPose.TOOT_HORN;
    }
}
