package com.TovidY.kunluncontinent.tower.skill.client;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * 与极简血量（Neat，modid = "neat"）的共存兼容层（纯客户端，纯事件，无需改 Neat）。
 *
 * <p><b>重叠根因：</b>Neat 的血条牌顶会原样画出生物的完整自定义名
 * （名字+年限+词条，读 {@code getCustomName()}），位置在 {@code 身高 + 0.6}；
 * 原版名牌（同一串文字）画在 {@code 身高 + 0.5} 附近 → 两块叠字。</p>
 *
 * <p><b>策略：</b>玩家装了 Neat 时，对带词条的塔怪取消原版名牌——
 * 名字/年限/词条全部由 Neat 牌独占显示（它读的正是同一串自定义名）；
 * 没装 Neat 的玩家完全不受影响，原版名牌照常显示。</p>
 *
 * <p>只在 Neat 默认显示范围（24 格）内取消；范围外 Neat 本来就不画牌，
 * 原版名牌照常出现，保证远处也认得出塔怪。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TowerNamePlateCompat {

    /** Neat 是否安装（客户端各自判断，服务器无关）。 */
    private static final boolean NEAT_LOADED = ModList.get().isLoaded("neat");
    /** Neat 默认血条牌最大显示距离（config: max_distance，默认 24）。 */
    private static final double NEAT_MAX_DISTANCE_SQR = 24 * 24;

    @SubscribeEvent
    public static void onNameTag(RenderNameTagEvent event) {
        if (!NEAT_LOADED) return;

        Entity entity = event.getEntity();
        if (entity.getCustomName() == null) return;
        // 只处理塔怪（名字里带词条标签的），NPC / 其它命名生物不受影响
        if (!entity.getCustomName().getString().contains("[词条: ")) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 24 格内：Neat 会画血条牌（含完整名字）→ 取消原版名牌防叠字
        // 24 格外：Neat 牌本来就不渲染 → 原版名牌照常，远处也认得出塔怪
        if (entity.distanceToSqr(mc.player) <= NEAT_MAX_DISTANCE_SQR) {
            event.setCanceled(true);
        }
    }
}
