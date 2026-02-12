package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

// 生成属性赋予
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class MobAttributeInit {

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();

        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            if (attr.getNianxian() == 0) {
                long nianxian = MobAttributeLogic.calculateNianxian(
                        mob.level().dimension(),
                        mob.blockPosition()
                );
                attr.initNianxian(nianxian);
            }
            applyAttributesToEntity(mob, attr);
        });
    }

    @SubscribeEvent
    public static void onSlimeSplit(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Slime babySlime && !event.getLevel().isClientSide()) {
            List<Slime> parents = event.getLevel().getEntitiesOfClass(
                    Slime.class,
                    babySlime.getBoundingBox().inflate(2.0D),
                    parent -> parent != babySlime && parent.isDeadOrDying()
            );

            if (!parents.isEmpty()) {
                Slime parent = parents.get(0);
                parent.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(parentAttr -> {
                    babySlime.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(babyAttr -> {
                        babyAttr.initNianxian(parentAttr.getNianxian());
                        applyAttributesToEntity(babySlime, babyAttr);
                        SynsAPI.synsEntityAttribute(babySlime);
                    });
                });
            }
        }
    }

    public static void applyAttributesToEntity(Mob mob, MobAttributeCapability attr) {
        AttributeInstance maxHealthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(attr.getMaxshengming());
            if (mob.tickCount < 2) {
                mob.setHealth((float)attr.getMaxshengming());
            }
        }
    }
}