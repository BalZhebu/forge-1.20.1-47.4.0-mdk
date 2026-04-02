package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// 玩家每Tick触发

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PWPlayerTickEvent {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            long gameTime = player.level().getGameTime();
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                playerUpdateServere(player, capability);

                handleSkillCasting(serverPlayer, capability, gameTime);
                handleLieDiLanding(serverPlayer);
                handleLiejinhuEight(serverPlayer);
                handleBahuangNine(serverPlayer);

                handleThunderRealmLightning(serverPlayer,capability,gameTime);

                handleSuiXingField(serverPlayer, gameTime);
                if (gameTime % 20 == 0) {
                    float maxshengming = ModAttributeAPI.getMaxshengming(player);
                    if (Math.abs(maxshengming - player.getMaxHealth()) > 0.1f) {
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxshengming);
                    }
                }
                if (gameTime % 100 == 0) {
                    updatePlayerHealthRegen(player, capability);
                    SynsAPI.synsPlayerAttribute(player);
                }
                if (gameTime % 120 == 0) {
                    updateJingshenliRegen(player, capability);
                }
                updatePlayerFly(player, capability);
                
                handleMeditationLogic(serverPlayer, capability, gameTime);
            });
        }
    }

    private static void handleThunderRealmLightning(ServerPlayer player, PlayerAttributeCapability capability, long gameTime) {
        int playerOffset = Math.abs(player.getUUID().hashCode() % 20);
        if (gameTime % 20 != playerOffset) return;
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(ModDimensions.THUNDER_REALM_LEVEL_KEY)) return;
        if (level.random.nextInt(3) == 0) {
            int count = level.random.nextInt(5) + 1;
            boolean hasProtection = player.getInventory().contains(Items.TOTEM_OF_UNDYING.getDefaultInstance());
            for (int i = 0; i < count; i++) {
                boolean isTargetingPlayer = !hasProtection && level.random.nextFloat() < 0.12f;
                BlockPos strikePos;
                if (isTargetingPlayer) {
                    strikePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightning != null) {
                        lightning.moveTo(Vec3.atBottomCenterOf(strikePos));
                        float damage = capability.getMaxshengming() * 0.05f;
                        player.hurt(level.damageSources().lightningBolt(), damage);
                        level.addFreshEntity(lightning);
                    }
                } else {
                    double radius = 40.0;
                    double offsetX = (level.random.nextDouble() * 2 - 1) * radius;
                    double offsetZ = (level.random.nextDouble() * 2 - 1) * radius;
                    strikePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                            player.blockPosition().offset((int)offsetX, 0, (int)offsetZ));
                    LightningBolt dummyLightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (dummyLightning != null) {
                        dummyLightning.setVisualOnly(true); // 不点火，不伤害，不计入实体碰撞
                        dummyLightning.moveTo(Vec3.atBottomCenterOf(strikePos));
                        level.addFreshEntity(dummyLightning);
                    }
                }
            }
        }
    }

    private static void handleBahuangNine(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("Bahuang9_Active")) return;
        int timer = nbt.getInt("Bahuang9_Timer");
        int remaining = nbt.getInt("Bahuang9_Remaining");
        timer++;
        if (timer >= 30) {
            ServerLevel level = player.serverLevel();
            float damage = nbt.getFloat("Bahuang9_Damage");
            for (int deg = 0; deg < 360; deg += 5) {
                double rad = Math.toRadians(deg);
                for (double r = 1; r < 20; r += 4) { // 扩散感
                    level.sendParticles(ParticleTypes.END_ROD, player.getX() + Math.cos(rad) * r, player.getY() + 0.1, player.getZ() + Math.sin(rad) * r, 1, 0, 0.1, 0, 0);
                }
            }
            level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive()).forEach(t -> {
                t.hurt(player.damageSources().playerAttack(player), damage);
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1)); // 缓慢2
            });
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 0.5f);
            remaining--;
            if (remaining <= 0) nbt.remove("Bahuang9_Active");
            else { nbt.putInt("Bahuang9_Remaining", remaining); nbt.putInt("Bahuang9_Timer", 0); }
        } else nbt.putInt("Bahuang9_Timer", timer);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide && entity.tickCount % 10 == 0 && entity.isAlive()) {
            List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class,
                    entity.getBoundingBox().inflate(0.5),
                    cloud -> cloud.getTags().contains("PohunVoidZone"));
            if (!clouds.isEmpty()) {
                AreaEffectCloud voidCloud = clouds.get(0);
                float damage = voidCloud.getPersistentData().getFloat("VoidDamage");
                entity.invulnerableTime = 0;
                if (voidCloud.getOwner() instanceof Player attacker) {
                    entity.hurt(level.damageSources().playerAttack(attacker), damage);
                } else {
                    entity.hurt(level.damageSources().magic(), damage);
                }
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                            entity.getX(), entity.getY() + 1, entity.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }

    private static void handleLiejinhuEight(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("Liejinhu8_Active")) return;
        int timer = nbt.getInt("Liejinhu8_Timer");
        int remaining = nbt.getInt("Liejinhu8_Remaining");
        float damage = nbt.getFloat("Liejinhu8_Damage");
        ServerLevel level = player.serverLevel();
        timer++;
        if (timer >= 10) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), damage);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.2);
            }
            level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.5f + (remaining * 0.1f));
            remaining--;
            if (remaining <= 0) {
                nbt.remove("Liejinhu8_Active");
                nbt.remove("Liejinhu8_Remaining");
                nbt.remove("Liejinhu8_Timer");
                nbt.remove("Liejinhu8_Damage");
                player.displayClientMessage(Component.literal("§7裂天效果结束"), true);
            } else {
                nbt.putInt("Liejinhu8_Remaining", remaining);
                nbt.putInt("Liejinhu8_Timer", 0);
            }
        } else {
            nbt.putInt("Liejinhu8_Timer", timer);
            if (timer % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 2, 0.5, 0.5, 0.5, 0.01);
            }
        }
    }

    private static void handleSuiXingField(ServerPlayer player, long gameTime) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("SuiXingTimer")) return;
        int timeLeft = nbt.getInt("SuiXingTimer");
        float damage = nbt.getFloat("SuiXingDamage");
        ServerLevel level = player.serverLevel();
        if (timeLeft % 20 == 0) {
            AABB area = player.getBoundingBox().inflate(15.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().indirectMagic(player, player), damage);
                level.sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 4.0, target.getZ(),
                        20, 0.5, 0.5, 0.5, 0.2); // 0.2 的速度让粒子向下“喷射”
                level.sendParticles(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY(), target.getZ(),
                        2, 0.1, 0.1, 0.1, 0.0);
                level.sendParticles(ParticleTypes.FLASH,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        1, 0, 0, 0, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
        if (gameTime % 2 == 0) {
            double angle = (gameTime * 0.2);
            for (int i = 0; i < 4; i++) {
                double rad = angle + (i * Math.PI / 2);
                double px = player.getX() + Math.cos(rad) * 15;
                double pz = player.getZ() + Math.sin(rad) * 15;
                level.sendParticles(ParticleTypes.SOUL, px, player.getY(), pz, 1, 0, 0.1, 0, 0.02);
                level.sendParticles(ParticleTypes.WITCH, px, player.getY() + 0.5, pz, 1, 0.1, 0.5, 0.1, 0.01);
            }
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    player.getX(), player.getY() + 0.1, player.getZ(),
                    5, 0.5, 0, 0.5, 0.02);
        }
        timeLeft--;
        if (timeLeft <= 0) {
            nbt.remove("SuiXingTimer");
            nbt.remove("SuiXingDamage");
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.8f);
        } else {
            nbt.putInt("SuiXingTimer", timeLeft);
        }
    }
    private static void handleLieDiLanding(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        ServerLevel level = player.serverLevel();
        double verticalMomentum = player.getDeltaMovement().y;
        if (nbt.contains("BuZhouQing_Active") && player.onGround() && verticalMomentum <= 0) {
            float dmg = nbt.getFloat("BuZhouQing_Damage");
            AABB bzArea = player.getBoundingBox().inflate(80.0);
            List<LivingEntity> bzTargets = level.getEntitiesOfClass(LivingEntity.class, bzArea,
                    e -> e != player && e.isAlive());
            for (LivingEntity target : bzTargets) {
                target.hurt(player.damageSources().playerAttack(player), dmg);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 2)); // 缓慢3
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));          // 虚弱3
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));         // 反胃
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));            // 饥饿
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY(), player.getZ(), 15, 3, 0.5, 3, 0.2);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 8, 2, 2, 2, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 2.0f, 0.5f);
            nbt.remove("BuZhouQing_Active");
            nbt.remove("BuZhouQing_Damage");
        }
        if (nbt.contains("LieDiActive") && player.onGround() && verticalMomentum <= 0) {
            float finalDamage = nbt.getFloat("LieDiDamage");
            AABB ldArea = player.getBoundingBox().inflate(20.0);
            List<LivingEntity> ldTargets = level.getEntitiesOfClass(LivingEntity.class, ldArea,
                    e -> e != player && e.isAlive());
            for (LivingEntity target : ldTargets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.push(0, 0.8, 0); // 裂地特有的震飞效果
            }
            for (int i = 0; i < 60; i++) {
                double rx = (level.random.nextDouble() - 0.5) * 30;
                double rz = (level.random.nextDouble() - 0.5) * 30;
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX() + rx, player.getY(), player.getZ() + rz, 1, 0, 0.1, 0, 0.02);
                if (i % 6 == 0) {
                    level.sendParticles(ParticleTypes.SONIC_BOOM,
                            player.getX() + rx/2, player.getY(), player.getZ() + rz/2, 1, 0, 0, 0, 0);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.0f, 0.5f);
            nbt.remove("LieDiActive");
            nbt.remove("LieDiDamage");
        }
    }

    private static void handleSkillCasting(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        BaseSkillItem castingSkill = cap.getCurrentCastingSkill();
        if (castingSkill != null) {
            int currentTick = cap.getCastingTick();
            cap.setCastingTick(currentTick + 1);
            if (gameTime % 5 == 0) {
                player.serverLevel().sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 2.2, player.getZ(),
                        3, 0.2, 0.2, 0.2, 0.0
                );
            }
            if (cap.getCastingTick() >= cap.getRequiredCastTick()) {
                String currentWuhun = cap.getWuhunName();
                int selectedSlot = cap.getSelectedSkillSlot();
                int nianxian = 10;
                List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
                if (rings != null && selectedSlot >= 0 && selectedSlot < rings.size()) {
                    nianxian = (int) rings.get(selectedSlot).getNianxian();
                }
                float powerMultiplier = castingSkill.getPowerMultiplier(nianxian);
                float costMultiplier = castingSkill.getCostMultiplier(nianxian);
                castingSkill.executeEffect(player.level(), player, powerMultiplier);
                castingSkill.applyPenalty(player, costMultiplier);
                cap.setSkillLastUsedTime(currentWuhun, selectedSlot, gameTime);
                player.displayClientMessage(Component.translatable("gui.kunluncontinent.cast_shifa"), true);
                cap.stopCasting(player);
                SynsAPI.synsPlayerAttribute(player);
            }
        }
    }

    private static void handleMeditationLogic(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        boolean isMeditating = player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat");
        if (isMeditating) {
            if (gameTime % 20 == 0) {
                float currentJs = cap.getJingshenli();
                float maxJs = cap.getMaxjingshenli();
                if (currentJs < maxJs) {
                    float recoveryAmount = (maxJs * 0.02f) + 5.0f;
                    cap.setJingshenli(Math.min(maxJs, currentJs + recoveryAmount));
                    if (cap.getXiulianTime() <= 0) {
                        SynsAPI.synsPlayerAttribute(player);
                    }
                }
            }

            if (gameTime % 20 == 0) {
                int currentTime = cap.getXiulianTime();
                if (currentTime > 0) {
                    cap.setXiulianTime(currentTime - 1);
                    int level = cap.getDengji();
                    float minutesToLevel = (level <= 30) ? 5f : (level <= 89) ? 7f : 10f;
                    float gain = cap.getMaxjingyan() / (minutesToLevel * 60f);
                    cap.setJingyan(cap.getJingyan() + gain);
                    PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);

                    if (gameTime % 40 == 0) SynsAPI.synsPlayerAttribute(player);

                    if (cap.getXiulianTime() <= 0) {
                        cap.setUsingAll(true);
                        player.sendSystemMessage(Component.translatable("putuan.xiulian.finish"));
                        player.stopRiding();
                    }
                }
            }
        } else {
            int recoverTickRate = cap.isUsingAll() ? 87 : 100;
            if (gameTime % recoverTickRate == 0) {
                if (cap.getXiulianTime() < 600) {
                    cap.setXiulianTime(cap.getXiulianTime() + 1);
                    if (cap.getXiulianTime() >= 600) cap.setUsingAll(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ArmorStand armorStand && armorStand.getTags().contains("putuan_seat")) {
            if (armorStand.getPassengers().isEmpty()) {
                armorStand.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat")) {
                    player.stopRiding();
                    player.sendSystemMessage(Component.translatable("心神受损").withStyle(ChatFormatting.RED));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                }
                    if (cap.getCurrentCastingSkill() != null) {
                    cap.stopCasting(player);
                    player.displayClientMessage(Component.literal("§c魂力紊乱，吟唱中断！"), true);
                }
            });
        }
    }

    private static void updatePlayerHealthRegen(Player player, PlayerAttributeCapability capability) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float regenAttr = ModAttributeAPI.getShengminghuifu(player);
        if (currentHealth < maxHealth && regenAttr > 0) {
            float regenPercent = Math.min(regenAttr * 0.002f, 0.06f);
            float healAmount = maxHealth * regenPercent;
            if (healAmount > 0) {
                player.heal(healAmount);
                SynsAPI.synsPlayerAttribute(player);
            }
        }
    }

    private static void updateJingshenliRegen(Player player, PlayerAttributeCapability capability) {
        float currentJingshenli = capability.getJingshenli();
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
        if (currentJingshenli < maxJingshenli) {
            float regenAmount = maxJingshenli * 0.01f;
            float nextJingshenli = Math.min(currentJingshenli + regenAmount, maxJingshenli);
            capability.setJingshenli(nextJingshenli);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    private static void updatePlayerFly(Player player, @NotNull PlayerAttributeCapability capability) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (player.getPersistentData().contains("knocked_down")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            else if (ModAttributeAPI.getMaxjingshenli(player) > 5000) {
                player.getAbilities().mayfly = true;
            } else {
                player.getAbilities().mayfly = false;
            }
            if (player.getAbilities().flying && player.level().getGameTime() % 10 == 0) {
                capability.setJingshenli(capability.getJingshenli() - 30 + (float) capability.getDengji() / 5);
            }
            if (capability.getWuhunName() != null && player.level().getGameTime() % 20 == 0) {
                int level = capability.getDengji();
                float cost;
                if (level <= 20) {
                    cost = 1.0f + (level * 0.01f);
                }else if (level <= 30) {
                    cost = 3.0f + (level * 0.1f);
                } else if (level <= 89) {
                    cost = 10.0f + (level * 0.2f);
                } else {
                    cost = 15.0f + (level * 0.25f);
                }
                capability.setJingshenli(capability.getJingshenli() - cost);
            }
        }
    }

    private static void playerUpdateServere(Player player, @NotNull PlayerAttributeCapability capability) {
        float jingshenli = ModAttributeAPI.getJingshenli(player);
        if(jingshenli<0){
            player.sendSystemMessage(Component.translatable("精神力不足").withStyle(ChatFormatting.GRAY));
            if(player.getVehicle() instanceof HunhuanEntity){
                player.removeVehicle();
            }
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 2));
            capability.setJingshenli(0);
            capability.setHunhuankuaiguan(-1);
            SynsAPI.synsPlayerAttribute(player);
        }
    }
}