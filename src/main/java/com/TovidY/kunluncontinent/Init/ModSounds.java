package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 声音事件注册。
 *
 * <p><b>名字必须和 {@code assets/kunluncontinent/sounds.json} 里的键逐字一致</b>，
 * 否则游戏里只会静音、日志里也不会报错（最容易漏的一步）。</p>
 *
 * <p>本模组目前只有两条：</p>
 * <ul>
 *   <li>{@link #ICE_EMPRESS_MUSIC} —— 雪帝战斗音乐，客户端本地播放（非定位、不衰减），
 *       见 {@code event/client/IceEmpressMusicHandler}；</li>
 *   <li>{@link #MUSIC_DISC_THRONE_OF_FROZEN_STONE} —— 同一段音频做的唱片，
 *       由 {@link com.TovidY.kunluncontinent.item.ModItems#THRONE_OF_FROZEN_STONE} 使用。</li>
 * </ul>
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, KlMain.MOD_ID);

    /** 雪帝战斗音乐（{@code sounds/music/throne_of_frozen_stone.ogg}）。 */
    public static final RegistryObject<SoundEvent> ICE_EMPRESS_MUSIC =
            variable("music.ice_empress");

    /** 唱片《冰封王座》——和战斗音乐共用同一个音频文件，只是换了播放来源。 */
    public static final RegistryObject<SoundEvent> MUSIC_DISC_THRONE_OF_FROZEN_STONE =
            variable("music_disc.throne_of_frozen_stone");

    private ModSounds() {
    }

    /**
     * 变距声音：范围随音量放大，唱机播放时用的是 4.0 音量 → 约 64 格。
     *
     * <p>不用 {@code createFixedRangeEvent}：固定范围会让唱机把声音限制在 16 格内，
     * 而原版唱片是靠"4.0 音量 + 变距"传得更远的。</p>
     */
    private static RegistryObject<SoundEvent> variable(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(KlMain.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
