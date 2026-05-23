package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    // 这里的 Key 必须与你 Handler 里引用的一致
    public static final ResourceKey<DamageType> EXTREME_COLD = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "extreme_cold")
    );

    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(EXTREME_COLD, new DamageType("extreme_cold", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F
        ));
    }
}
