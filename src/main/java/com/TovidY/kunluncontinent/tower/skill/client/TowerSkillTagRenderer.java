package com.TovidY.kunluncontinent.tower.skill.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 挑战塔怪物词条行（客户端渲染，始终显示）。
 *
 * <p>第一行（名字+年限）由原版名牌正常渲染；本类在它的<b>正下方</b>用
 * 生物 capability 里的 {@code towerSkillsTag}（随 {@code SPacketEntityAttribute}
 * 同步）画第二行词条——金色=主动、黄色=被动、等级罗马数字，超宽自动换行。</p>
 *
 * <p>渲染钩子必须是 {@link RenderLevelStageEvent}（AFTER_PARTICLES）：
 * 该时点 PoseStack 为相机相对坐标、1 单位 = 1 格（第一人称魂环已验证）。
 * 不要用 {@code RenderLivingEvent.Post}——那个时点还在模型变换链内
 * （16 单位 = 1 格且 Y 翻转），画世界坐标文字会缩成脚边一点。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TowerSkillTagRenderer {

    /** 词条段：颜色码 + 文本。 */
    private static final Pattern SEGMENT = Pattern.compile("§([0-9a-fk-or])([^§]*)");
    /** 名牌缩放（与原版名牌一致：0.025 格/像素）。 */
    private static final float SCALE = 0.025F;
    /** 原版名牌单行占高（9 像素 × 0.025 = 0.225 格）。 */
    private static final float VANILLA_LINE = 0.225F;
    /** 单行最大像素宽，超出自动换行。 */
    private static final float MAX_ROW_WIDTH = 170F;
    private static final float ROW_PX = 9F;
    /** 超过 64 格远不再绘制。 */
    private static final double MAX_DISTANCE_SQR = 64 * 64;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living) || !living.isAlive() || e == mc.player) continue;
            if (living.distanceToSqr(mc.player) > MAX_DISTANCE_SQR) continue;

            // 词条标签来自客户端 capability（SPacketEntityAttribute 已同步）
            String tag = living.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                    .map(MobAttributeCapability::getTowerSkillsTag)
                    .orElse("");
            if (tag.isEmpty()) continue;

            List<MutableComponent> rows = buildRows(tag, mc.font);
            if (rows.isEmpty()) continue;

            // 位置：生物的插值坐标（照抄 LevelRenderer 的实体插值，防止每帧硬跳）
            float partial = event.getPartialTick();
            double px = Mth.lerp(partial, living.xOld, living.getX());
            double py = Mth.lerp(partial, living.yOld, living.getY());
            double pz = Mth.lerp(partial, living.zOld, living.getZ());
            Vec3 cam = event.getCamera().getPosition();

            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(px - cam.x, py - cam.y, pz - cam.z);

            // 词条行锚点：整体塞进"原版名牌（第一行）的正下方"。
            // 原版名牌占 [身高+0.5, 身高+0.725]；词条行从 身高+0.5-0.225*行数 往上堆，
            // 多行时行数越多锚点越低，保证最顶行仍与第一行衔接。
            float anchor = (float) (living.getBbHeight() + 0.5F - rows.size() * VANILLA_LINE);
            anchor = Math.max(anchor, (float) (living.getBbHeight() + 0.02F)); // 防止沉到地面以下
            pose.translate(0, anchor, 0);

            // 公告板：始终朝向镜头
            pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            pose.scale(-SCALE, -SCALE, SCALE);
            Matrix4f matrix = pose.last().pose();

            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            Font font = mc.font;

            for (int j = 0; j < rows.size(); j++) {
                MutableComponent row = rows.get(j);
                float w = font.width(row);
                font.drawInBatch(row,
                        -w / 2F,
                        (rows.size() - 1 - j) * ROW_PX, // 第 0 行在最上
                        0xFFFFFFFF,           // 字体本色由各段样式决定
                        false,                // 无投影（底色已有衬底）
                        matrix, buffer,
                        Font.DisplayMode.NORMAL,
                        0x640B0F17,           // 深色半透明衬底（昆仑面板同款底色）
                        LightTexture.FULL_BRIGHT);
            }
            // 自绘文字立即提交，避免与后续渲染批次混批
            buffer.endBatch();
            pose.popPose();
        }
    }

    /**
     * 把词条标签解析成"按像素宽换行"的行组件列表。
     * 标签格式：{@code §6狂暴III §e壁垒}（颜色码为字面字符）。
     */
    private static List<MutableComponent> buildRows(String tag, Font font) {
        List<MutableComponent> rows = new ArrayList<>();
        MutableComponent current = Component.empty();
        float width = 0;

        Matcher m = SEGMENT.matcher(tag);
        while (m.find()) {
            String text = m.group(2).trim();
            if (text.isEmpty()) continue;
            MutableComponent seg = Component.literal(text).withStyle(colorOf(m.group(1).charAt(0)));
            float segWidth = font.width(seg);
            // 词条之间留一个空格的间隔（标签里的原始空格被 trim 掉了，这里补回来）
            if (width > 0) {
                MutableComponent gap = Component.literal(" ").withStyle(ChatFormatting.GRAY);
                float gapWidth = font.width(gap);
                if (width + gapWidth + segWidth > MAX_ROW_WIDTH) {
                    rows.add(current);
                    current = Component.empty();
                    width = 0;
                } else {
                    current.append(gap);
                    width += gapWidth;
                }
            }
            if (width + segWidth > MAX_ROW_WIDTH && width > 0) {
                rows.add(current);
                current = Component.empty();
                width = 0;
            }
            current.append(seg);
            width += segWidth;
        }
        if (width > 0) rows.add(current);

        // 【】 括住整块词条（首行加左括、末行加右括；换行时括号跟随首末行）
        if (!rows.isEmpty()) {
            MutableComponent open = Component.literal("【").withStyle(ChatFormatting.GRAY);
            MutableComponent close = Component.literal("】").withStyle(ChatFormatting.GRAY);
            rows.set(0, open.append(rows.get(0)));
            int last = rows.size() - 1;
            rows.set(last, rows.get(last).append(close));
        }
        return rows;
    }

    private static ChatFormatting colorOf(char code) {
        return switch (code) {
            case '6' -> ChatFormatting.GOLD;    // 主动词条
            case 'e' -> ChatFormatting.YELLOW;  // 被动词条
            case '7' -> ChatFormatting.GRAY;
            case 'c' -> ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        };
    }
}
