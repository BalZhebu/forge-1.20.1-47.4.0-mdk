package com.TovidY.kunluncontinent.mixin;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 给「不能改 customName」的怪物补上「-----X年」的<b>显示名</b>。
 *
 * <h3>为什么需要这个 Mixin</h3>
 * <p>正常情况下年限是写进 {@code entity.setCustomName(...)} 的，但有些 mod 把
 * 「有没有自定义名」写进了持久化判定里 —— 典型是 Alex's Mobs 的宇宙鳕鱼：</p>
 * <pre>
 *   requiresCustomPersistence() = super || hasCustomName() || fromBucket()
 *   removeWhenFarAway(d)        = !fromBucket() && !hasCustomName()
 * </pre>
 * <p>给它改名会同时导致「永不despawn」+「不参与刷怪上限统计」，
 * 而 {@code NaturalSpawner.createState} 正是靠
 * {@code if (isPersistenceRequired() || requiresCustomPersistence()) continue;} 跳过统计的 ——
 * 于是末地会以每区块每 tick 的速度无限刷宇宙鳕鱼。</p>
 *
 * <p>所以 {@code CapabilityRegistryHandler} 在命名后会复核持久化语义，翻转就回滚。
 * 但回滚之后头顶就看不到年限了。本 Mixin 负责把这部分补回来：</p>
 * <ul>
 *   <li><b>只改显示</b>：注入 {@link Entity#getDisplayName()} 的返回值，
 *       不写 NBT、不碰 {@code customName}，对去刷/上限/驯服判定零影响；</li>
 *   <li><b>只兜底</b>：{@code hasCustomName()} 为真时直接放行（普通怪早就把年限写进
 *       customName 了，再拼一次会变成「宇宙鳕鱼-----12年-----12年」）；</li>
 *   <li><b>两端都生效</b>：客户端实体的 {@code MobAttributeCapability} 由
 *       {@code SPacketEntityAttribute} 同步（{@code ClientPacketHandlers#entityAttribute}），
 *       所以渲染时拿得到年限；服务端的死亡消息、聊天、BossBar 同样走 getDisplayName()。</li>
 * </ul>
 *
 * <h3>性能</h3>
 * <p>{@code getDisplayName()} 每帧会被渲染层调用多次，所以只缓存拼好的<b>字符串</b>
 * （年限变了才重拼），每次调用新建一个 literal —— 和原版「每次都 copy 一份」的开销同级，
 * 而且返回的是全新对象，不用担心调用方修改污染缓存。</p>
 */
@Mixin(Entity.class)
public abstract class MixinEntityDisplayName {

    /** 缓存的显示文本（只缓存字符串，避免返回可变的共享 Component）。 */
    @Unique
    private String kunlun$labelText;

    /** 缓存对应的年限，变了就重拼。 */
    @Unique
    private long kunlun$labelNianxian = -1L;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void kunlun$appendNianxian(CallbackInfoReturnable<Component> cir) {
        Entity self = (Entity) (Object) this;
        // 只处理「被动过手脚、没敢写 customName」的那类怪
        if (!(self instanceof Mob mob) || mob.hasCustomName()) {
            return;
        }

        MobAttributeCapability cap = mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return;
        }
        long nianxian = cap.getNianxian();
        if (nianxian <= 0) {
            return;
        }

        if (kunlun$labelText == null || kunlun$labelNianxian != nianxian) {
            kunlun$labelNianxian = nianxian;
            kunlun$labelText = mob.getName().getString() + "-----" + kunlun$colorPrefix(nianxian) + nianxian + "年";
        }
        cir.setReturnValue(Component.literal(kunlun$labelText));
    }

    /** 与 {@code CapabilityRegistryHandler} 里的分档完全一致，避免两处颜色对不上。 */
    @Unique
    private static String kunlun$colorPrefix(long nianxian) {
        if (nianxian >= 10000000L) return "§c§l";
        if (nianxian >= 100000L) return "§c";
        if (nianxian >= 10000L) return "§0";
        if (nianxian >= 1000L) return "§5";
        if (nianxian >= 100L) return "§e";
        return "§f";
    }
}
