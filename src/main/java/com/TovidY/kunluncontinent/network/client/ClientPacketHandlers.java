package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent;
import com.TovidY.kunluncontinent.event.client.CastClientState;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.render.skill.SkillEffect;
import com.TovidY.kunluncontinent.render.skill.SkillEffectRenderer;
import com.TovidY.kunluncontinent.render.skill.SkillEffects;
import com.TovidY.kunluncontinent.screen.attribute.XiulianSelectionScreen;
import com.TovidY.kunluncontinent.screen.playernpc.NpcDialogScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

/**
 * 数据包在<b>客户端</b>要做的事，全部集中在这里。
 *
 * <h3>为什么必须单独拆一个类（而不是写在包类里）</h3>
 * <p>{@code NetworkHandler.register} 在<b>模组构造期</b>就会把每个包类 {@code Class} 传给
 * {@code registerMessage}，等于让 JVM <b>链接并校验</b>这些类。只要包类自己的方法体里出现
 * {@code Minecraft} / {@code Screen} / {@code ClientLevel} 这类客户端类，服务端在校验阶段就要加载它们，
 * 而 Forge 的 {@code RuntimeDistCleaner} 会直接抛</p>
 * <pre>
 * Attempted to load class net/minecraft/client/gui/screens/Screen for invalid dist DEDICATED_SERVER
 * </pre>
 * <p>—— 整个模组在专用服务器上起不来。把客户端代码挪进这个类之后，包类只剩
 * {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandlers::xxx)}，
 * 服务端既不会加载本类，也就不会碰到客户端类。</p>
 *
 * <p>⚠️ <b>新增"收到包要开界面 / 读 Minecraft"的包时，客户端逻辑一律加到本类</b>，
 * 不要在包类里直接写 {@code Minecraft.getInstance()}，否则又会把专用服务器打掉。</p>
 *
 * <p>⚠️ <b>这里必须用 {@code unsafeRunWhenOn}，不要用 {@code safeRunWhenOn}</b>：
 * Forge 的 {@code safe*} 系列会在<b>开发环境</b>调 {@code validateSafeReferent}，
 * 它要求传进去的 referent 是<b>普通类实例</b>（不能是 lambda）——
 * 我们写的 {@code () -> () -> ClientPacketHandlers.xxx()} 里，内层 lambda 的
 * "捕获类"和"实现类"是同一个包类，于是被判成 unsafe，
 * 每个包都刷一条 {@code [FATAL] Detected unsafe referent usage} 并抛异常
 * （只在开发期出现，正式包不刷，但会把后台日志淹掉）。
 * 客户端代码已经隔离在本类里，{@code unsafeRunWhenOn} 在服务端根本不会执行到这个 lambda，安全性没有区别。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    /** 打开 NPC 对话界面。 */
    public static void openNpcDialog(int entityId) {
        Minecraft.getInstance().setScreen(new NpcDialogScreen(entityId));
    }

    /** 打开「孤竹之缘」二选一对话框（彩蛋）。 */
    public static void openLoneBambooChoice() {
        Minecraft.getInstance().setScreen(new com.TovidY.kunluncontinent.screen.lonebamboo.LoneBambooChoiceScreen());
    }

    /** 切换视角（0 第一人称 / 1 第三人称背面 / 2 第三人称正面）。 */
    public static void changeCamera(int mode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        CameraType[] values = CameraType.values();
        mc.options.setCameraType(values[Math.floorMod(mode, values.length)]);
    }

    /** 打开修炼选择界面。 */
    public static void openXiulianScreen(int xiulianTime) {
        Minecraft.getInstance().setScreen(new XiulianSelectionScreen(xiulianTime));
    }

    /** 把服务端算好的属性整份覆盖到本地玩家的 capability 上。 */
    public static void syncShenciAttributes(PlayerAttributeCapability attributes) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap ->
                cap.deserializeNBT(attributes.serializeNBT()));
    }

    /** 别人的武魂/魂环数据同步到客户端渲染缓存。 */
    public static void syncWuhunData(UUID playerUUID, List<Integer> wuhunNianxianList,
                                     List<String> shenciGodList, boolean divine,
                                     boolean playingAnimation, long animationStartTime, int animStyle) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Player player = level.getPlayerByUUID(playerUUID);
        if (player != null) {
            PWRenderPlayerEvent.updatePlayerWuhunData(player, wuhunNianxianList, shenciGodList,
                    divine, playingAnimation, animationStartTime, animStyle);
        }
    }

    /** 生成一次技能特效。 */
    public static void skillField(int styleId, int ownerEntityId, double x, double y, double z,
                                  float radius, int durationTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        SkillEffect effect = SkillEffects.create(styleId, ownerEntityId, new Vec3(x, y, z),
                radius, level.getGameTime(), durationTicks);
        SkillEffectRenderer.spawn(effect);
    }

    /** 实体属性同步到客户端 capability。 */
    public static void entityAttribute(int entityId, CompoundTag nbt) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        if (entity == null) {
            return;
        }
        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> cap.deserializeNBT(nbt));
    }

    /** 玩家属性同步到客户端 capability。 */
    public static void playerAttribute(int entityId, CompoundTag nbt) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        if (entity instanceof Player player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> cap.deserializeNBT(nbt));
        }
    }

    /** NPC 的魂环数据写入客户端缓存。 */
    public static void npcWuhun(int entityId, List<Integer> nianxianList) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        if (entity instanceof PlayerNpcEntity npc) {
            PWRenderPlayerEvent.updateNpcWuhunCache(npc.getUUID(), nianxianList);
        }
    }

    /**
     * 吟唱状态同步。
     *
     * <p>{@code totalTicks <= 0} 表示这次吟唱收尾（唱完或被打断）。</p>
     */
    public static void castingSync(int entityId, int totalTicks) {
        if (totalTicks <= 0) {
            CastClientState.stop(entityId);
        } else {
            CastClientState.start(entityId, totalTicks);
        }
    }
}
