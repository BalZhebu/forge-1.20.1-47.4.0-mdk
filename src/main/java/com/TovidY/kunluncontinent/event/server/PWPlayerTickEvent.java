package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.klitem.ZhuanShengTestItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.SyncNpcWuhunPacket;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent.scanCompoundForNianxian;
import static com.TovidY.kunluncontinent.item.ModItems.THUNDER_PROTECTION_LIST;

// 玩家每Tick触发

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PWPlayerTickEvent {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            long gameTime = player.level().getGameTime();
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                playerUpdateServere(player, capability, gameTime);

                handleReincarnationLogic(serverPlayer, gameTime);

                handleSkillCasting(serverPlayer, capability, gameTime);
                handleLieDiLanding(serverPlayer);
                handleLiejinhuEight(serverPlayer);
                handleBahuangNine(serverPlayer);

                handleThunderRealmLightning(serverPlayer, capability, gameTime);

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

                updatePlayerFly(player, capability, gameTime);

                handleMeditationLogic(serverPlayer, capability, gameTime);
            });
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof PlayerNpcEntity npc && !event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            List<Integer> nianxianList = new ArrayList<>();
            PlayerAttributeCapability npcCap = npc.getSoulCapability();
            if (npcCap != null && npcCap.getWuhunList() != null) {
                for (MobAttributeCapability wuhun : npcCap.getWuhunList()) {
                    if (wuhun != null && wuhun.getNianxian() > 0) {
                        nianxianList.add((int) wuhun.getNianxian());
                    }
                }
            }
            if (nianxianList.isEmpty()) {
                scanCompoundForNianxian(npc.getPersistentData(), nianxianList);
            }
            if (!nianxianList.isEmpty()) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncNpcWuhunPacket(npc.getId(), nianxianList)
                );
            }
        }
    }

    // ==================== 完整睡眠回复精神力 ====================

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;
        if (!event.wakeImmediately()) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
                float regenAmount = maxJingshenli * 0.8f;
                float nextJingshenli = Math.min(maxJingshenli, capability.getJingshenli() + regenAmount);
                capability.setJingshenli(nextJingshenli);
                SynsAPI.synsPlayerAttribute(player);
            });
        }
    }

    // ==================== 雷界闪电 ====================

    private static void handleThunderRealmLightning(ServerPlayer player, PlayerAttributeCapability capability, long gameTime) {
        int playerOffset = Math.abs(player.getUUID().hashCode() % 20);
        if (gameTime % 20 != playerOffset) return;
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(ModDimensions.THUNDER_REALM_LEVEL_KEY)) return;

        ItemStack protectionItem = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (THUNDER_PROTECTION_LIST.stream().anyMatch(reg -> stack.is(reg.get()))) {
                protectionItem = stack;
                break;
            }
        }

        boolean hasProtection = !protectionItem.isEmpty();
        if (hasProtection && gameTime % 100 == playerOffset) {
            protectionItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        if (level.random.nextInt(3) == 0) {
            int count = level.random.nextInt(5) + 1;
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
                            player.blockPosition().offset((int) offsetX, 0, (int) offsetZ));
                    LightningBolt dummyLightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (dummyLightning != null) {
                        dummyLightning.setVisualOnly(true);
                        dummyLightning.moveTo(Vec3.atBottomCenterOf(strikePos));
                        level.addFreshEntity(dummyLightning);
                    }
                }
            }
        }
    }

    // ==================== 八荒第九技能 ====================

    private static void handleBahuangNine(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("Bahuang9_Active")) return;

        int timer = nbt.getInt("Bahuang9_Timer");
        int remaining = nbt.getInt("Bahuang9_Remaining");
        timer++;

        if (timer >= 30) {
            ServerLevel level = player.serverLevel();
            float damage = nbt.getFloat("Bahuang9_Damage");
            PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
            boolean particleOpt = cap != null && cap.isConfigOpen(10);

            // 粒子优化：步长加倍，360度只需一半的粒子环（72→36圈，半径采样也减半）
            double step = particleOpt ? 10 : 5;
            for (int deg = 0; deg < 360; deg += (int) step) {
                double rad = Math.toRadians(deg);
                double maxR = particleOpt ? 13 : 20;
                double rStep = particleOpt ? 5 : 4;
                for (double r = 1; r < maxR; r += rStep) {
                    level.sendParticles(ParticleTypes.END_ROD,
                            player.getX() + Math.cos(rad) * r, player.getY() + 0.1,
                            player.getZ() + Math.sin(rad) * r, 1, 0, 0.1, 0, 0);
                }
            }

            level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20.0),
                    e -> e != player && e.isAlive()).forEach(t -> {
                t.hurt(player.damageSources().playerAttack(player), damage);
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            });

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 0.5f);

            remaining--;
            if (remaining <= 0) {
                nbt.remove("Bahuang9_Active");
                nbt.remove("Bahuang9_Timer");
                nbt.remove("Bahuang9_Remaining");
                nbt.remove("Bahuang9_Damage");
            } else {
                nbt.putInt("Bahuang9_Remaining", remaining);
                nbt.putInt("Bahuang9_Timer", 0);
            }
        } else {
            nbt.putInt("Bahuang9_Timer", timer);
        }
    }

    // ==================== 虚空区域伤害 + 蒲团座位清理（合并为同一事件） ====================

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        // 蒲团座位：无人乘坐时自动销毁
        if (entity instanceof ArmorStand armorStand
                && armorStand.getTags().contains("putuan_seat")
                && armorStand.getPassengers().isEmpty()) {
            armorStand.discard();
            return; // 已销毁，不继续处理后续逻辑
        }

        // 虚空区域持续伤害
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
                            entity.getX(), entity.getY() + 1, entity.getZ(),
                            5, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }

    // ==================== 裂金虎第八技能 ====================

    private static void handleLiejinhuEight(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("Liejinhu8_Active")) return;

        int timer = nbt.getInt("Liejinhu8_Timer");
        int remaining = nbt.getInt("Liejinhu8_Remaining");
        float damage = nbt.getFloat("Liejinhu8_Damage");
        ServerLevel level = player.serverLevel();
        timer++;
        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        boolean particleOpt = cap != null && cap.isConfigOpen(10);

        if (timer >= 10) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), damage);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + 1, target.getZ(),
                        particleOpt ? 3 : 5, 0.3, 0.3, 0.3, 0.1);
                level.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1, target.getZ(),
                        particleOpt ? 5 : 10, 0.5, 0.5, 0.5, 0.2);
            }
            if (!particleOpt) {
                level.sendParticles(ParticleTypes.FLASH,
                        player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            }
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
            // 优化：每 4 tick 发一次，原来每 2 tick
            if (!particleOpt && timer % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(), 2, 0.5, 0.5, 0.5, 0.01);
            } else if (particleOpt && timer % 4 == 0) {
                level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(), 1, 0.5, 0.5, 0.5, 0.01);
            }
        }
    }

    // ==================== 碎星领域 ====================

    private static void handleSuiXingField(ServerPlayer player, long gameTime) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("SuiXingTimer")) return;

        int timeLeft = nbt.getInt("SuiXingTimer");
        float damage = nbt.getFloat("SuiXingDamage");
        ServerLevel level = player.serverLevel();
        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        boolean particleOpt = cap != null && cap.isConfigOpen(10);

        if (timeLeft % 20 == 0) {
            AABB area = player.getBoundingBox().inflate(15.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().indirectMagic(player, player), damage);
                level.sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 4.0, target.getZ(),
                        particleOpt ? 10 : 20, 0.5, 0.5, 0.5, 0.2);
                level.sendParticles(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY(), target.getZ(),
                        particleOpt ? 1 : 2, 0.1, 0.1, 0.1, 0.0);
                if (!particleOpt) {
                    level.sendParticles(ParticleTypes.FLASH,
                            target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        }

        // 粒子优化：每 4 tick 而非每 2 tick 发送轨道粒子，频率减半
        if (gameTime % (particleOpt ? 4 : 2) == 0) {
            double angle = gameTime * 0.2;
            for (int i = 0; i < 4; i++) {
                double rad = angle + (i * Math.PI / 2);
                double px = player.getX() + Math.cos(rad) * 15;
                double pz = player.getZ() + Math.sin(rad) * 15;
                level.sendParticles(ParticleTypes.SOUL, px, player.getY(), pz, 1, 0, 0.1, 0, 0.02);
                level.sendParticles(ParticleTypes.WITCH, px, player.getY() + 0.5, pz, 1, 0.1, 0.5, 0.1, 0.01);
            }
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    player.getX(), player.getY() + 0.1, player.getZ(),
                    particleOpt ? 3 : 5, 0.5, 0, 0.5, 0.02);
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

    // ==================== 裂地 / 布洲倾落地 ====================

    private static void handleLieDiLanding(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        ServerLevel level = player.serverLevel();
        double verticalMomentum = player.getDeltaMovement().y;

        if (nbt.contains("BuZhouQing_Active") && player.onGround() && verticalMomentum <= 0) {
            float dmg = nbt.getFloat("BuZhouQing_Damage");
            List<LivingEntity> bzTargets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(80.0), e -> e != player && e.isAlive());
            for (LivingEntity target : bzTargets) {
                target.hurt(player.damageSources().playerAttack(player), dmg);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM,
                    player.getX(), player.getY(), player.getZ(), 15, 3, 0.5, 3, 0.2);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY(), player.getZ(), 8, 2, 2, 2, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 2.0f, 0.5f);
            nbt.remove("BuZhouQing_Active");
            nbt.remove("BuZhouQing_Damage");
        }

        if (nbt.contains("LieDiActive") && player.onGround() && verticalMomentum <= 0) {
            float finalDamage = nbt.getFloat("LieDiDamage");
            List<LivingEntity> ldTargets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());
            for (LivingEntity target : ldTargets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.push(0, 0.8, 0);
            }
            for (int i = 0; i < 60; i++) {
                double rx = (level.random.nextDouble() - 0.5) * 30;
                double rz = (level.random.nextDouble() - 0.5) * 30;
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX() + rx, player.getY(), player.getZ() + rz, 1, 0, 0.1, 0, 0.02);
                if (i % 6 == 0) {
                    level.sendParticles(ParticleTypes.SONIC_BOOM,
                            player.getX() + rx / 2, player.getY(), player.getZ() + rz / 2, 1, 0, 0, 0, 0);
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

    // ==================== 技能施法 ====================

    private static void handleSkillCasting(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        BaseSkillItem castingSkill = cap.getCurrentCastingSkill();
        if (castingSkill == null) return;

        cap.setCastingTick(cap.getCastingTick() + 1);

        if (gameTime % 5 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 2.2, player.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
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

    // ==================== 冥想 / 修炼 ====================

    private static void handleMeditationLogic(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        boolean isMeditating = player.getVehicle() != null
                && player.getVehicle().getTags().contains("putuan_seat");

        if (isMeditating) {
            if (gameTime % 20 == 0) {
                boolean needSync = false;

                // 精神力恢复
                float currentJs = cap.getJingshenli();
                float maxJs = cap.getMaxjingshenli();
                if (currentJs < maxJs) {
                    cap.setJingshenli(Math.min(maxJs, currentJs + maxJs * 0.02f + 5.0f));
                    needSync = true;
                }

                // 修炼时间 + 经验增长
                int currentTime = cap.getXiulianTime();
                if (currentTime > 0) {
                    cap.setXiulianTime(currentTime - 1);
                    int lvl = cap.getDengji();
                    float minutesToLevel = lvl <= 30 ? 5f : lvl <= 89 ? 7f : 10f;
                    cap.setJingyan(cap.getJingyan() + cap.getMaxjingyan() / (minutesToLevel * 60f));
                    PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
                    needSync = true;

                    if (currentTime - 1 <= 0) {
                        cap.setUsingAll(true);
                        player.sendSystemMessage(Component.translatable("putuan.xiulian.finish"));
                        player.stopRiding();
                    }
                }

                if (needSync) SynsAPI.synsPlayerAttribute(player);
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

    // ==================== 玩家受伤事件 ====================

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat")) {
                player.stopRiding();
                // 修正：literal 用于直接显示中文字符串，translatable 用于 lang key
                player.sendSystemMessage(Component.literal("心神受损").withStyle(ChatFormatting.RED));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
            if (cap.getCurrentCastingSkill() != null) {
                cap.stopCasting(player);
                player.displayClientMessage(Component.literal("§c魂力紊乱，吟唱中断！"), true);
            }
        });
    }

    // ==================== 生命值回复 ====================

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

    // ==================== 精神力自然回复 ====================

    private static void updateJingshenliRegen(Player player, PlayerAttributeCapability capability) {
        float currentJingshenli = capability.getJingshenli();
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
        if (currentJingshenli < maxJingshenli) {
            capability.setJingshenli(Math.min(currentJingshenli + maxJingshenli * 0.01f, maxJingshenli));
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    // ==================== 飞行逻辑 ====================

    private static void updatePlayerFly(Player player, PlayerAttributeCapability capability, long gameTime) {
        if (player.isCreative() || player.isSpectator()) return;

        boolean canFlyPrev = player.getAbilities().mayfly;

        // 禁空扫描：每 10 tick 一次，用 UUID 错开不同玩家的扫描时机，减轻服务器单 tick 压力
        boolean isAntiFlyZone = false;
        int flyOffset = Math.abs(player.getUUID().hashCode() % 10);
        if (gameTime % 10 == flyOffset
                && !player.level().isClientSide()
                && player.level() instanceof ServerLevel serverLevel) {
            AABB checkArea = player.getBoundingBox().inflate(40.0D);
            isAntiFlyZone = serverLevel.getEntitiesOfClass(Mob.class, checkArea, Mob::isAlive)
                    .stream()
                    .anyMatch(mob -> mob.getPersistentData().getBoolean("Skill_JinKong_Available"));
        }

        if (player.getPersistentData().contains("knocked_down")) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        } else if (isAntiFlyZone) {
            if (player.getAbilities().mayfly || player.getAbilities().flying) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                if (gameTime % 40 == 0) {
                    player.sendSystemMessage(Component.literal("§4§l【禁空法则】§c 四周有《禁空》词条生物压制，被强制击落！"));
                }
            }
        } else if (ModAttributeAPI.getMaxjingshenli(player) > 5000) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }

        if (canFlyPrev != player.getAbilities().mayfly) {
            player.onUpdateAbilities();
        }

// 1. 只要开启了武魂（无论飞不飞），每 20 tick (1秒) 必定扣除武魂维持消耗
        if (capability.getWuhunName() != null && gameTime % 20 == 0) {
            int lvl = capability.getDengji();
            float cost;
            if (lvl <= 40) {
                cost = 3.0f + (lvl * 0.051f);
            }
            else if (lvl <= 70) {
                cost = 25.0f - 0.088f * (float)Math.pow(lvl - 55, 2);
            }
            else {
                // 71级时约 5.0，之后每升一级降低 0.12 点
                cost = 5.2f - ((lvl - 70) * 0.12f);
            }
            if (cost < 1.5f) {
                cost = 1.5f;
            }
            capability.setJingshenli(capability.getJingshenli() - cost);
        }

        // 2. 只有在飞行时，每 10 tick 额外扣除飞行消耗（与武魂无关）
        if (player.getAbilities().flying && gameTime % 10 == 0) {
            float currentJs = capability.getJingshenli();
            float cost = 30.0f - (float) capability.getDengji() / 5.0f;
            capability.setJingshenli(currentJs - cost);
        }
    }

    // ==================== 转生逻辑 ====================

    private static void handleReincarnationLogic(ServerPlayer player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        ZhuanShengTestItem.checkReincarnationProgress(player);

        // 1. 天劫倒计时阶段
        if (data.getBoolean("IsPreparingReincarnation")) {
            int timer = data.getInt("ReincarnationTimer");
            if (timer > 0) {
                data.putInt("ReincarnationTimer", timer - 1);
                if (timer % 20 == 0) {
                    player.sendSystemMessage(Component.literal("天劫倒计时: " + (timer / 20) + "秒")
                            .withStyle(ChatFormatting.RED));
                }
            } else {
                data.remove("IsPreparingReincarnation");
                data.putBoolean("IsLightningPhase", true);
                data.putInt("LightningCount", 0);
                player.sendSystemMessage(Component.literal(
                                "天威降临，雷劫开始！（天雷会压低血量但不至死，20血以下时将获得转生重修的机会）")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }

        // 2. 雷劫轰顶阶段
        if (data.getBoolean("IsLightningPhase") && gameTime % 20 == 0) {
            int count = data.getInt("LightningCount");
            float currentHealth = player.getHealth();

            if (currentHealth <= 20.0f && count > 0) {
                // 转生成功
                player.sendSystemMessage(Component.literal("劫难已满，破后而立！")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                data.remove("IsLightningPhase");
                data.remove("LightningCount");
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap ->
                        ZhuanShengTestItem.completeReincarnation(player, cap));

            } else if (count >= 99) {
                // 雷劫消散
                player.sendSystemMessage(Component.literal(
                                "雷劫消散，你的实力太过强大，无法堕入轮回。（血量达到20以下时才会进入轮回）")
                        .withStyle(ChatFormatting.GRAY));
                data.remove("IsLightningPhase");

            } else {
                // 天雷降击：绕过 LivingHurtEvent，直接操作血量，避免触发 CombatEventHandler 的
                // 暴击/特效判定和吸血回复，确保天劫伤害行为纯粹可预期
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
                if (bolt != null) {
                    bolt.setPos(player.position());
                    bolt.setVisualOnly(true);
                    player.level().addFreshEntity(bolt);
                }
                // 直接将血量压到当前的 20%，保底 1 点，不至死
                float nextHealth = Math.max(1.0f, currentHealth * 0.2f);
                player.setHealth(nextHealth);
                data.putInt("LightningCount", count + 1);
                player.sendSystemMessage(Component.literal("第 " + (count + 1) + " 重雷劫...")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    // ==================== 玩家死亡事件 ====================

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ZhuanShengTestItem.cancelReincarnation(player);

        CompoundTag data = player.getPersistentData();
        data.remove("IsPreparingReincarnation");
        data.remove("IsLightningPhase");
        data.remove("ReincarnationTimer");
        data.remove("LightningCount");
    }

    @SubscribeEvent
    public static void onVoidDamage(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getSource().equals(player.damageSources().fellOutOfWorld())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTickVoid(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.getY() >= player.level().getMinBuildHeight()) return;
        long gameTime = player.level().getGameTime();
        int offset = Math.abs(player.getUUID().hashCode() % 10);
        if (gameTime % 10 != offset) return;
        float maxHealth = player.getMaxHealth();
        float damage = maxHealth * 0.1f;
        float nextHealth = player.getHealth() - damage;
        if (nextHealth <= 0) {
            player.hurt(player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        } else {
            player.setHealth(nextHealth);
            serverPlayer.serverLevel().sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.3, 0.5, 0.3, 0.05
            );
            player.playSound(SoundEvents.GENERIC_HURT, 0.8f, 0.5f);
        }
    }

    // ==================== 精神力耗尽惩罚 ====================

    private static void playerUpdateServere(Player player, PlayerAttributeCapability capability, long gameTime) {
        float jingshenli = ModAttributeAPI.getJingshenli(player);
        if (jingshenli < 0) {
            // 每 40 tick（2 秒）提示一次，避免每 tick 刷屏
            if (gameTime % 40 == 0) {
                player.sendSystemMessage(Component.translatable("精神力不足").withStyle(ChatFormatting.GRAY));
            }
            if (player.getVehicle() instanceof HunhuanEntity) {
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