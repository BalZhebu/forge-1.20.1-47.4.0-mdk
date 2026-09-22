package com.TovidY.kunluncontinent.entity.hunhuan;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 吸收魂环时的表现层配置。
 *
 * <p>吸收逻辑完全不在这里：玩家右键骑上魂环实体 → 每秒累积进度 → 到阈值吸收成功。
 * 本类只负责两件事：</p>
 * <ul>
 *   <li>{@link Tier} —— 年限 → 魂环的尺寸与颜色（客户端生成魂环粒子时取用）；</li>
 *   <li>起手 / 完成 / 中断的音效。</li>
 * </ul>
 *
 * <p>魂环的动画本身由客户端完成，见 {@link ClientHunhuanRingFx}。</p>
 */
public final class HunhuanAbsorbFx {

    private HunhuanAbsorbFx() {
    }

    // ==================================================================================
    //  年限档位
    // ==================================================================================

    /** 魂环年限档位：决定魂环多大、什么颜色、用哪张贴图。 */
    public enum Tier {
        /** 十年 ~ 百年：白。 */
        WHITE(0, 1.20F, 0xFFFFFF),
        /** 百年 ~ 千年：黄。 */
        YELLOW(100, 1.30F, 0xFFFF00),
        /** 千年 ~ 万年：紫。 */
        PURPLE(1_000, 1.40F, 0xCC00CC),
        /** 万年 ~ 十万年：黑。 */
        BLACK(10_000, 1.50F, 0x1A1A1A),
        /** 十万年 ~ 百万年：红。 */
        RED(100_000, 1.60F, 0xFF0000),
        /** 百万年 ~ 千万年：橙金。 */
        ORANGE(1_000_000, 1.72F, 0xFF8A00),
        /** 千万年 ~ 亿年：蓝（神赐，用神环贴图）。 */
        BLUE(10_000_000, 1.86F, 0x2E7BFF),
        /** 亿年以上：金绿神辉（神赐，用神环贴图）。 */
        GREEN(100_000_000, 2.00F, 0x22FF55);

        public final int threshold;
        /**
         * 魂环尺寸 —— 即四边形半宽（格）。
         * 贴图里的环占满整张图，所以**显示直径 ≈ 1.9 × 这个值**
         * （1.20 → 约 2.3 格，2.00 → 约 3.8 格）。
         */
        public final float size;
        /** 主题色，与 {@code PWRenderLivingEvent.renderHunhuanAttribute} 保持同一套口径。 */
        public final int rgb;

        Tier(int threshold, float size, int rgb) {
            this.threshold = threshold;
            this.size = size;
            this.rgb = rgb;
        }

        /** 该档位是否使用"神环"贴图。 */
        public boolean shenhuan() {
            return this.ordinal() >= BLUE.ordinal();
        }

        public static Tier of(int nianxian) {
            Tier result = WHITE;
            for (Tier tier : values()) {
                if (nianxian >= tier.threshold) {
                    result = tier;
                }
            }
            return result;
        }

        /**
         * 给魂环粒子用的染色。
         *
         * <p>魂环贴图本身是**纯白**的（实测饱和度 0、亮度 246~255），
         * 所以粒子颜色直接乘主题色就能得到纯正的颜色，不要再往白色方向兑水。</p>
         */
        public float[] tint() {
            return new float[]{
                    ((rgb >> 16) & 0xFF) / 255.0F,
                    ((rgb >> 8) & 0xFF) / 255.0F,
                    (rgb & 0xFF) / 255.0F
            };
        }
    }

    // ==================================================================================
    //  音效
    // ==================================================================================

    /** 开始吸收。 */
    public static void playStart(Level level, Player player, int nianxian) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    /** 吸收成功。越高档位动静越大。 */
    public static void playComplete(Level level, Player player, int nianxian) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4f, 1.0f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2f, 1.2f);

        Tier tier = Tier.of(nianxian);
        if (tier.ordinal() >= Tier.RED.ordinal()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2f, 1.0f);
        }
        if (tier.shenhuan()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }

    /** 吸收被打断。 */
    public static void playCancel(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9f, 0.6f);
    }
}
