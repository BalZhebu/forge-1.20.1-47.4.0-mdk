package com.TovidY.kunluncontinent.potion.specialeffects.coldeffect;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.klitem.ThermalAmuletItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ExtremeColdHandler {
    private static final String COLD_TICK_KEY = "kunlun_stay_ticks";
    private static final String LAST_LEVEL_KEY = "kunlun_last_level";

    public static final ResourceKey<DamageType> EXTREME_COLD_TYPE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "extreme_cold")
    );

    private static DamageSource getExtremeColdDamageSource(LivingEntity entity) {
        return new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(EXTREME_COLD_TYPE)
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || player.tickCount % 20 != 0) return;
        Level level = player.level();
        ResourceKey<Level> currentDim = level.dimension();
        boolean isInPolarIce = currentDim == ModDimensions.POLAR_ICE_REALM_LEVEL_KEY;
        ItemStack amulet = getThermalAmulet(player);
        boolean hasProtection = !amulet.isEmpty();
        if (hasProtection || !isInPolarIce) {
            player.getPersistentData().putInt(COLD_TICK_KEY, 0);
            if (hasProtection) {
                if (player.hasEffect(ModEffects.EXTREME_COLD.get())) {
                    player.removeEffect(ModEffects.EXTREME_COLD.get());
                }
                if (isInPolarIce) {
                    handleAmuletConsumption(player, amulet, level);
                }
            }
            if (!isInPolarIce) return;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.EXTREME_COLD.get());
        if (effect != null) {
            handleBuffLogic(player, effect);
            return;
        }
        if (isInPolarIce && !player.isCreative() && !player.isSpectator()) {
            int currentTicks = player.getPersistentData().getInt(COLD_TICK_KEY);
            currentTicks += 20;
            int maxTicksToStay = getMaxStayTicks(player.experienceLevel);
            if (currentTicks >= maxTicksToStay) {
                player.addEffect(new MobEffectInstance(ModEffects.EXTREME_COLD.get(), 200, 0));
                player.getPersistentData().putInt(COLD_TICK_KEY, 0);
                player.displayClientMessage(Component.literal("寒气侵入了你的身体...").withStyle(ChatFormatting.RED), true);
                player.playSound(SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 0.8F);
            } else {
                player.getPersistentData().putInt(COLD_TICK_KEY, currentTicks);
                if (maxTicksToStay - currentTicks <= 200 && maxTicksToStay - currentTicks > 180) {
                    player.displayClientMessage(Component.literal("你感到四肢开始发僵...").withStyle(ChatFormatting.YELLOW), true);
                    player.playSound(SoundEvents.SNOW_BREAK, 1.0F, 0.5F);
                }
            }
        }
    }

    private static void handleAmuletConsumption(Player player, ItemStack amulet, Level level) {
        int interval = 100;
        if (level.isRaining() && level.canSeeSky(player.blockPosition())) {
            interval = 60;
            if (player.tickCount % 120 == 0) {
                player.displayClientMessage(Component.literal("暴风雪正在加速侵蚀魂导器...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            }
        }
        if (player.tickCount % interval == 0) {
            consumeAmuletDurability(player, amulet);
        }
    }

    private static void handleBuffLogic(Player player, MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();
        int duration = effect.getDuration();
        float maxHealth = player.getMaxHealth();
        float damage = 0;

        switch (amplifier) {
            case 0: damage = player.getMaxHealth() * 0.01f; break;
            case 1: damage = player.getMaxHealth() * 0.05f; break;
            case 2: damage = player.getMaxHealth() * 0.10f; break;
            case 3: damage = player.getMaxHealth() * 0.60f; break;
        }

        switch (amplifier) {
            case 0:
                damage = maxHealth * 0.01f;
                if (duration <= 20) upgradeBuff(player, 1, 400, "你感到越来越冷了...");
                break;
            case 1:
                damage = maxHealth * 0.05f;
                if (duration <= 20) upgradeBuff(player, 2, 600, "你似乎要被这寒冷永久冰封...");
                break;
            case 2:
                damage = maxHealth * 0.10f;
                if (duration <= 20) {
                    upgradeBuff(player, 3, 1200, "你感到全身都没有知觉了...");
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 9));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1200, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
                }
                break;
            case 3:
                damage = maxHealth * 0.60f;
                if (duration <= 20 && duration > 0) {
                    upgradeBuff(player, 3, 1728000, null);
                }
                break;
        }
        if (damage > 0) {
            player.hurt(getExtremeColdDamageSource(player), damage);
        }
    }

    private static void upgradeBuff(Player player, int nextAmp, int nextDuration, String msg) {
        player.removeEffect(ModEffects.EXTREME_COLD.get());
        player.addEffect(new MobEffectInstance(ModEffects.EXTREME_COLD.get(), nextDuration, nextAmp));
        if (msg != null) {
            player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.AQUA), true);
        }
    }

    private static int getMaxStayTicks(int level) {
        if (level <= 1) return 600;
        if (level >= 99) return 36000;
        float ticksPerLevel = 35400f / 98f;
        return 600 + Math.round((level - 1) * ticksPerLevel);
    }

    private static ItemStack getThermalAmulet(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ThermalAmuletItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void consumeAmuletDurability(Player player, ItemStack stack) {
        if (stack.getItem() instanceof ThermalAmuletItem amulet && !amulet.isInfinite()) {
            stack.hurtAndBreak(1, player, (p) -> {
                p.displayClientMessage(Component.literal("你的魂导器已损坏，寒气正在逼近！").withStyle(ChatFormatting.DARK_RED), false);
                p.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
            });
        }
    }
}