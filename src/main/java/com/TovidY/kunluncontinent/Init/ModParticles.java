package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 粒子类型注册。
 *
 * <p>这两个粒子不是原版那种"火星"，而是把魂环贴图本身渲染出来的自定义粒子，
 * 用于吸收魂环时让魂环一圈圈绕着玩家飘。客户端的渲染器在
 * {@code HunhuanRingParticle}，贴图定义在
 * {@code assets/kunluncontinent/particles/*.json}。</p>
 */
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, KlMain.MOD_ID);

    /** 普通魂环（贴图 textures/particle/hunhuan_ring.png）。 */
    public static final RegistryObject<SimpleParticleType> HUNHUAN_RING =
            PARTICLE_TYPES.register("hunhuan_ring", () -> new SimpleParticleType(false));

    /** 神赐魂环（贴图 textures/particle/shenhuan_ring.png）。 */
    public static final RegistryObject<SimpleParticleType> SHENHUAN_RING =
            PARTICLE_TYPES.register("shenhuan_ring", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
