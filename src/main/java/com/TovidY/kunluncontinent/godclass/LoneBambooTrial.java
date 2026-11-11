package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 孤竹神 <b>第九考</b>：幽冥登天。
 *
 * <h3>规则</h3>
 * <ul>
 *   <li>只能在<b>主世界</b>、且站在 <b>Y ≤ -60</b> 的地方开启；</li>
 *   <li>开启后只能用孤竹魂技（撑杆跳）往上爬，<b>达到 Y ≥ 500</b> 即通过；</li>
 *   <li>期间<b>触发飞行</b>（创造飞行 / 鞘翅 / 漂浮效果）→ 立即失败，提示"禁止飞行"；</li>
 *   <li>期间<b>落地</b>（起跳后再次踩到地面）→ 立即失败；</li>
 *   <li>失败可重来（再点一次「开启试炼考核」）。</li>
 * </ul>
 *
 * <h3>为什么必须"开启"才判定</h3>
 * <p>按 tick 检查高度/飞行/落地，如果对所有玩家常开，就是每 tick 每玩家一次 AABB 级的判定成本。
 * 所以做成<b>显式开启</b>：只有 {@link #ACTIVE} 里的玩家才会被 tick，
 * 集合为空时整个监听器第一行就 return，稳态开销 ≈ 0。</p>
 *
 * <h3>状态</h3>
 * <p>只放内存（{@link #ACTIVE}）+ 玩家 persistentData 里的"是否离开过地面"标记。
 * 玩家掉线/换维度 = 本次试炼直接作废，不跨存档续接（试炼本来就是一次性的）。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public final class LoneBambooTrial {

    /** 通关所需高度（绝对 Y，用户指定 500）。 */
    public static final int REQUIRED_Y = 500;

    /** 开启试炼要求的最高高度（必须在这个值<b>之下</b>，含）。 */
    public static final int START_MAX_Y = -60;

    /** 已开启试炼的玩家。 */
    private static final Set<UUID> ACTIVE = new HashSet<>();

    /** 「起跳后是否真的离过地」——不记这个的话，开启的那一瞬间站在地上就会被判落地失败。 */
    private static final String NBT_AIRBORNE = "LoneBambooTrialAirborne";

    private LoneBambooTrial() {
    }

    /** 玩家是否正在试炼中（面板/命令可用）。 */
    public static boolean isActive(UUID uuid) {
        return ACTIVE.contains(uuid);
    }

    // ==================================================================================
    //  开启
    // ==================================================================================

    /**
     * 点「开启试炼考核」时调用（第 9 考）。
     *
     * @return 是否成功开启
     */
    public static boolean start(ServerPlayer player) {
        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return false;
        }

        // 只有在孤竹路、且正卡在第 9 考时才受理
        if (!LoneBambooGodPath.onPath(cap)) {
            player.sendSystemMessage(Component.literal("§c【孤竹试炼】你并不在孤竹神的考核中。").withStyle(ChatFormatting.RED));
            return false;
        }
        if (cap.isGod()) {
            player.sendSystemMessage(Component.literal("§a你已经成就孤竹神位，无需再试。").withStyle(ChatFormatting.GREEN));
            return false;
        }
        if (cap.getCurrentStage() != 9) {
            player.sendSystemMessage(Component.literal("§c【孤竹试炼】请先完成前八考（吸收到第 9 枚孤竹魂环）。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // 【成神门槛】必须 99 级才能开启第九考 —— 与其它神位「第 9 考要求 99 级」对齐，
        // 这样通过后 +1 级刚好 100 级成神。
        if (cap.getDengji() < 99) {
            player.sendSystemMessage(Component.literal("§c【孤竹试炼】登天之前需先立于凡尘之巅 —— 当前 §e"
                    + cap.getDengji() + " §c级，需 §e99 §c级。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            player.sendSystemMessage(Component.literal("§c【孤竹试炼】试炼只在主世界生效。").withStyle(ChatFormatting.RED));
            return false;
        }
        if (player.getY() > START_MAX_Y) {
            player.sendSystemMessage(Component.literal("§c【孤竹试炼】你必须身处 §eY ≤ " + START_MAX_Y
                    + " §c的幽冥深处才能开启试炼（当前 §e" + (int) player.getY() + "§c）。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (ACTIVE.contains(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§e试炼已经在进行中，继续向上！").withStyle(ChatFormatting.YELLOW));
            return true;
        }

        ACTIVE.add(player.getUUID());
        player.getPersistentData().putBoolean(NBT_AIRBORNE, false);

        player.sendSystemMessage(Component.literal("§2§l【孤竹试炼·第九考】§r §f幽冥登天，目标 §eY ≥ " + REQUIRED_Y));
        player.sendSystemMessage(Component.literal("§7禁止飞行、禁止落地，违者本次失败（可重开）"));
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    // ==================================================================================
    //  每 tick 检查（只在有活跃试炼时执行）
    // ==================================================================================

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // ⚠️ 第一行就排除：没人开试炼时这里的开销就是一次 isEmpty()
        if (ACTIVE.isEmpty() || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (!ACTIVE.contains(player.getUUID())) {
            return;
        }
        tick(player);
    }

    private static void tick(ServerPlayer player) {
        // 1. 换维度 = 试炼作废（主世界之外没有 Y=500 的意义）
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            fail(player, "你离开了主世界");
            return;
        }

        // 2. 禁止一切飞行：创造飞行 / 鞘翅滑翔 / 漂浮药水
        if (player.getAbilities().flying || player.isFallFlying() || player.hasEffect(MobEffects.LEVITATION)) {
            fail(player, "禁止飞行");
            return;
        }

        CompoundTag data = player.getPersistentData();

        // 3. 还没起跳：站在地上等着，不判落地
        if (!data.getBoolean(NBT_AIRBORNE)) {
            if (!player.onGround()) {
                data.putBoolean(NBT_AIRBORNE, true);
            }
            return;
        }

        // 4. 起跳之后又踩到地面 = 失败
        if (player.onGround()) {
            fail(player, "你落地了");
            return;
        }

        // 5. 到高度即通过
        if (player.getY() >= REQUIRED_Y) {
            succeed(player);
        }
    }

    private static void fail(ServerPlayer player, String reason) {
        ACTIVE.remove(player.getUUID());
        player.getPersistentData().putBoolean(NBT_AIRBORNE, false);

        player.sendSystemMessage(Component.literal("§c§l【孤竹试炼】§r §c本次试炼失败：" + reason + "。")
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("§7回到 §eY ≤ " + START_MAX_Y + "§7 处可重新开启。")
                .withStyle(ChatFormatting.GRAY));
        player.playNotifySound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private static void succeed(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        player.getPersistentData().putBoolean(NBT_AIRBORNE, false);

        player.sendSystemMessage(Component.literal("§2§l【孤竹试炼·完成】§r §f你凭一根孤竹，登临九天！"));
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 0.8F);

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> LoneBambooGodPath.completeFinalExam(player, cap));
    }

    // ==================================================================================
    //  掉线清理
    // ==================================================================================

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            ACTIVE.remove(player.getUUID());
        }
    }
}
