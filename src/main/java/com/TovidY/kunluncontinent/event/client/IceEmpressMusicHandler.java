package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.Init.ModSounds;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

/**
 * 雪帝战斗音乐（100 秒循环）的客户端播放器。
 *
 * <h3>为什么不用服务端发包</h3>
 * <p>战斗音乐对每个玩家应该是"我附近有雪帝就响"的<b>本地判断</b>，
 * 不需要跨端同步：服务端只要保证实体本身是同步的就够了（血量、动画都走实体包）。
 * 这样既没有额外的网络协议，也不会出现"玩家 A 进战斗把 B 的音乐也拉起来"的问题。</p>
 *
 * <h3>起停规则</h3>
 * <ul>
 *   <li><b>开</b>：客户端 150 格内出现活着的雪帝 → 开始播放（循环、非定位、不衰减）；</li>
 *   <li><b>停</b>：离开 150 格 / 雪帝死亡 / 卸载 / 换维度 / 退出世界 → <b>立刻</b>停
 *       （每 tick 校验一次跟踪目标，不等下一次扫描）。</li>
 * </ul>
 *
 * <h3>为什么是「非定位」</h3>
 * <p>{@code relative = true} + {@code Attenuation.NONE}：声音跟着听众走、恒定音量，
 * 不会因为跑远而变小声（音乐本来就不该有距离衰减）。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT)
public final class IceEmpressMusicHandler {

    /** 听觉半径：和禁飞领域同一个数（150 格）。 */
    private static final double HEAR_RADIUS = IceEmpressEntity.FLIGHT_SUPPRESS_RADIUS;

    /** 没有目标时，每隔多少 tick 扫一次附近有没有雪帝。 */
    private static final int RESCAN_TICKS = 20;

    /** 音量：满音量太吵，压低一点给打击音效让位（实际还受「敌对生物」滑块缩放）。 */
    private static final float MUSIC_VOLUME = 0.85F;

    /** 当前跟的雪帝（客户端实体）。 */
    private static IceEmpressEntity tracked;

    /** 正在播放的音乐实例。 */
    private static IceEmpressMusic music;

    /** 下一次扫描的倒计时。 */
    private static int rescanTimer;

    private IceEmpressMusicHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            tracked = null;
            stop(mc);
            return;
        }

        // 每 tick 校验一次：离开 150 格、死亡、换维度 → 立即停
        if (tracked != null && !isValid(mc, tracked)) {
            tracked = null;
            stop(mc);
        }

        if (tracked == null) {
            if (--rescanTimer > 0) {
                return;
            }
            rescanTimer = RESCAN_TICKS;
            tracked = findBoss(mc);
        }

        // 1.20.1 的 SoundInstance 没有 isStopped()，用 SoundManager#isActive 判断是否还在播
        if (tracked != null && (music == null || !mc.getSoundManager().isActive(music))) {
            start(mc);
        }
    }

    private static boolean isValid(Minecraft mc, IceEmpressEntity boss) {
        if (boss.isRemoved() || !boss.isAlive()) {
            return false;
        }
        if (boss.level() != mc.player.level()) {
            return false;
        }
        return mc.player.distanceToSqr(boss) <= HEAR_RADIUS * HEAR_RADIUS;
    }

    /** 找最近的、还活着的雪帝（客户端实体列表，已经在同步范围内）。 */
    private static IceEmpressEntity findBoss(Minecraft mc) {
        return mc.level
                .getEntitiesOfClass(IceEmpressEntity.class,
                        mc.player.getBoundingBox().inflate(HEAR_RADIUS),
                        IceEmpressEntity::isAlive)
                .stream()
                .min(Comparator.comparingDouble(mc.player::distanceToSqr))
                .orElse(null);
    }

    private static void start(Minecraft mc) {
        stop(mc);
        music = new IceEmpressMusic(ModSounds.ICE_EMPRESS_MUSIC.get());
        mc.getSoundManager().play(music);
        // 让原版背景音乐让位，避免两首音乐叠在一起
        mc.getMusicManager().stopPlaying();
    }

    private static void stop(Minecraft mc) {
        if (music != null) {
            mc.getSoundManager().stop(music);
            music = null;
        }
    }

    /**
     * 循环、非定位、无衰减的战斗音乐实例。
     *
     * <p>{@code relative = true} 让它跟随听者（不按坐标算衰减），
     * {@code Attenuation.NONE} 才是真正决定"恒定音量"的那个 —— 两个都要设。</p>
     *
     * <p><b>为什么走 {@link SoundSource#HOSTILE}</b>：很多玩家会把「音乐」音量拖到 0，
     * 战斗音乐就彻底听不见了。归到「敌对生物」通道后，音量由生物音效滑块控制，
     * 和雪帝的叫声/脚步共用一个开关，更符合它的定位。</p>
     */
    private static final class IceEmpressMusic extends AbstractSoundInstance {

        private IceEmpressMusic(SoundEvent sound) {
            super(sound, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = MUSIC_VOLUME;
            this.pitch = 1.0F;
        }

        /** 玩家把音乐音量拖到 0 时也照常"播放"（只是听不到），避免反复 isStopped 重启。 */
        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
