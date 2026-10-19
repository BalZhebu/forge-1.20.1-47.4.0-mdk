package com.TovidY.kunluncontinent.render;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 雪帝专属 BOSS 血条：用两张自定义贴图替换掉原版那根。
 *
 * <h3>为什么不用 Mixin</h3>
 * <p>Forge 1.20.1 在 {@code BossHealthOverlay#render} 里给每根血条都发了一次
 * {@link CustomizeGuiOverlayEvent.BossEventProgress}，事件<b>在绘制之前</b>触发、
 * 且是 {@code @Cancelable}：取消后原版的底条与名字都不会画，同时可以用
 * {@code setIncrement} 告诉原版「下一根血条往下挪多少」。所以这里既不用碰
 * {@code BossHealthOverlay} 的私有方法，也不用自己维护血条列表/可见性
 * —— 那些仍然由 {@code ServerBossEvent} 的 {@code startSeenByPlayer} 负责。</p>
 *
 * <h3>两张贴图怎么用</h3>
 * <ul>
 *   <li>{@code iceempress_health_min.png}（暗色）→ 整条先铺一张，作为空槽；</li>
 *   <li>{@code iceempress_health_max.png}（亮色）→ 按血量比例<b>只取左侧那么多列</b>覆盖上去。</li>
 * </ul>
 * <p>两张图的外框、雪花装饰是逐像素对齐的，所以裁剪后左半亮、右半暗，
 * 交界处就是一根干净的竖线，不需要额外的遮罩。</p>
 *
 * <h3>识别哪根是雪帝的</h3>
 * <p>网络包里只带 {@code BossEvent} 的 name / 颜色 / 进度，客户端拿到的一律是
 * {@code LerpingBossEvent}，{@code instanceof} 判断不了子类。所以改用
 * <b>血条名字的翻译键</b>做标记：服务端构造血条时固定用
 * {@link IceEmpressEntity#BOSS_BAR_KEY}，客户端只比对
 * {@link TranslatableContents#getKey()}。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT)
public final class IceEmpressBossBarRenderer {

    private static final ResourceLocation TEX_EMPTY =
            new ResourceLocation(KlMain.MOD_ID, "textures/gui/iceempress_health_min.png");
    private static final ResourceLocation TEX_FILL =
            new ResourceLocation(KlMain.MOD_ID, "textures/gui/iceempress_health_max.png");

    /** 贴图原始尺寸（两张都是 256×27）。 */
    private static final int TEX_W = 256;
    private static final int TEX_H = 27;

    /** 血条下方留给下一根血条的间距。 */
    private static final int BAR_GAP = 8;

    /** 相对原版位置向上抬多少（贴图上沿有 6 行装饰透明区，抬一点才不会离名字太远）。 */
    private static final int BAR_RAISE = 4;

    private IceEmpressBossBarRenderer() {
    }

    @SubscribeEvent
    public static void onBossBarProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent bar = event.getBossEvent();
        if (!isIceEmpressBar(bar.getName())) {
            return;   // 别的 BOSS（比如魔鲸）继续走原版血条
        }

        // 屏蔽原版底条与名字，并把自己画的高度告诉原版
        event.setCanceled(true);
        event.setIncrement(TEX_H + BAR_GAP);

        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();

        int screenWidth = graphics.guiWidth();
        int x = (screenWidth - TEX_W) / 2;

        // 名字行沿用原版口径（事件 y 上方 9~10px）；条本身上移 4px，
        // 因为贴图上沿有 6 行是雪花装饰的透明区，直接贴原版位置会显得离名字很远。
        int nameY = event.getY() - 10;
        int barY = event.getY() - BAR_RAISE;

        // 1. 空槽（整条）
        graphics.blit(TEX_EMPTY, x, barY, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

        // 2. 血量填充：从贴图左侧裁掉 progress 宽度
        float progress = Mth.clamp(bar.getProgress(), 0.0F, 1.0F);
        int filled = Math.round(TEX_W * progress);
        if (filled > 0) {
            graphics.blit(TEX_FILL, x, barY, 0, 0, filled, TEX_H, TEX_W, TEX_H);
        }

        // 3. 标题
        Component name = bar.getName();
        int nameWidth = mc.font.width(name);
        graphics.drawString(mc.font, name, screenWidth / 2 - nameWidth / 2,
                nameY, 0xFFFFFF, true);
    }

    /** 翻译键比对；玩家用命名牌改名时血条名字仍是固定键，所以不会漏判。 */
    private static boolean isIceEmpressBar(Component name) {
        return name.getContents() instanceof TranslatableContents contents
                && IceEmpressEntity.BOSS_BAR_KEY.equals(contents.getKey());
    }
}
