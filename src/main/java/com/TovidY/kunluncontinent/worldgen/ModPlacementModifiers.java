package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.worldgen.placement.DryGroundFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 本模组自定义的 {@link PlacementModifierType}。
 *
 * <p>放置修饰符必须是「可序列化」的：数据包（worldgen JSON）里写的是类型 id + 参数，
 * 所以每个自定义修饰符都要在 {@code minecraft:placement_modifier_type} 注册表里登记一份
 * {@link PlacementModifierType}（本质上就是一个 {@code MapCodec}）。</p>
 *
 * <p>目前只有 {@link DryGroundFilter}，用于把「脚下是水」的树位置剔除掉。</p>
 */
public final class ModPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, KlMain.MOD_ID);

    /**
     * 修饰符类型实例。
     *
     * <p>⚠️ 必须先落成一个具体类型的字段，再交给 {@code DeferredRegister.register}。
     * 写成内联的 {@code () -> () -> DryGroundFilter.CODEC} 会因为目标类型是
     * {@code PlacementModifierType<?>}（通配符）而无法推断泛型，编译报
     * "无法推断类型变量"。</p>
     */
    public static final PlacementModifierType<DryGroundFilter> DRY_GROUND_TYPE =
            () -> DryGroundFilter.CODEC;

    /**
     * JSON 里写 {@code {"type": "kunluncontinent:dry_ground"}}。
     *
     * <p>⚠️ 先用 {@link #DRY_GROUND_TYPE} 落成具体类型，再交给 {@code register}。
     * 直接把 {@code () -> DryGroundFilter.CODEC} 内联进去，会因为目标类型是通配符
     * {@code PlacementModifierType<?>} 而推断失败。</p>
     */
    public static final RegistryObject<PlacementModifierType<DryGroundFilter>> DRY_GROUND =
            PLACEMENT_MODIFIERS.register("dry_ground", () -> DRY_GROUND_TYPE);

    private ModPlacementModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        PLACEMENT_MODIFIERS.register(modEventBus);
    }
}
