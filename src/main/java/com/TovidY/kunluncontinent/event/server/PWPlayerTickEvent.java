package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.FlyCrashBlast;
import com.TovidY.kunluncontinent.capability.playerattributes.FlySpeedTuning;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.capability.playerattributes.XiulianLogic;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.eight.SkillLeijinhu8;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.nine.SkillLeijinhu9;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.ZhenshenState;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import com.TovidY.kunluncontinent.screen.attribute.skill.SkillBatchRelease;
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

                // 一键释放（同键绑多个槽位）：每隔几个 tick 放一个，放完汇总成一条提示
                SkillBatchRelease.tick(serverPlayer, capability, gameTime);

                handleSkillCasting(serverPlayer, capability, gameTime);
                handleLieDiLanding(serverPlayer);
                handleLiejinhuEight(serverPlayer);
                handleLiejinhuNine(serverPlayer);
                handleBahuangNine(serverPlayer);

                handleThunderRealmLightning(serverPlayer, capability, gameTime);

                handleSuiXingField(serverPlayer, gameTime);

                // 成就：首次踏入各维度（每 20 tick 判一次，开销可忽略）
                if (gameTime % 20 == 0) {
                    var dim = serverPlayer.level().dimension();
                    if (dim.equals(ModDimensions.THUNDER_REALM_LEVEL_KEY)) {
                        AchievementAPI.onEnterThunderRealm(serverPlayer);
                    } else if (dim.equals(ModDimensions.CELESTIAL_REALM_LEVEL_KEY)) {
                        AchievementAPI.onEnterCelestialRealm(serverPlayer);
                    } else if (dim.equals(ModDimensions.POLAR_ICE_REALM_LEVEL_KEY)) {
                        AchievementAPI.onEnterPolarIceRealm(serverPlayer);
                    } else if (dim.equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {
                        AchievementAPI.onEnterTowerRealm(serverPlayer);
                    }
                }

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

                if (gameTime % 20 == 0 && ZhenshenState.isActive(serverPlayer)) {
                    ZhenshenState.tickServer(serverPlayer, capability);
                }

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

    // ==================== 睡眠回复精神力 ====================

    /**
     * <b>只有"完成跳过黑夜"</b>（原版把时间推进到第二天早上）才回满精神力。
     *
     * <h3>为什么不能只看 wakeImmediately</h3>
     * <p>1.20.1 里 {@code Player#stopSleepInBed(wakeImmediately, updateLevel)} 的调用点：</p>
     * <pre>
     * 调用点                                             wakeImmediately  updateLevel
     * ServerLevel#wakeUpAllPlayers（跳黑夜成功）              false            false   ← 只有这里算数
     * ServerGamePacketListenerImpl STOP_SLEEPING（点起床）    false            true
     * Player#tick 天亮自然醒                                false            true
     * stopSleeping()（受伤打断）/ 传送 / 断线                 true           true / false
     * </pre>
     * <p>也就是说 <b>"点起床" 传的是 {@code wakeImmediately = false}</b> ——
     * 只看这一个标志，提前起床会被当成"睡满了"（上一版就是栽在这里）。
     * 真正跳黑夜的 {@code wakeUpAllPlayers} 传的是 {@code (false, false)}，
     * 是<b>唯一同时两个 false</b> 的路径，所以判据是
     * {@code !wakeImmediately() && !updateLevel()}。</p>
     *
     * <h3>回多少</h3>
     * <p>直接回满（旧实现是 {@code max * 0.8f}，睡前精神力低于 20% 时会差 0~20%，
     * 表现就是"有时候睡一觉满了、有时候差一截"）。</p>
     */
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;

        // 只有 ServerLevel#wakeUpAllPlayers 这条"跳黑夜"路径会同时给 (false, false)；
        // 提前点起床是 (false, true)，受伤打断 / 传送是 (true, *)
        if (event.wakeImmediately() || event.updateLevel()) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            // 口径与蒲团/自然回复一致：用 ModAttributeAPI（含魂骨/装备/加点加成）
            float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
            if (maxJingshenli <= 0.0f) return;
            capability.setJingshenli(maxJingshenli);
            SynsAPI.synsPlayerAttribute(player);
        });
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

            // 特效：每次脉冲重绘半径二十格的“寂灭地阵”，由内向外旋转。
            // ⚠️ 玩家把「技能特效优化」**关掉**（= 切到着色器版）时，这里**一个粒子都不发** ——
            //    地阵已经交给客户端着色器画了。两种表现必须二选一，同时放会像
            //    "粒子在光圈上乱飘"，比纯粒子版还脏。伤害、缓慢、音效照常。
            if (!SkillFxToggle.useShader(player)) {
                ParticleFx fx = ParticleFx.of(level);
                if (fx != null) {
                    fx.lod(particleOpt ? 0.5 : 1.0).budget(particleOpt ? 1100 : 2600);
                    Vec3 base = player.position();
                    double rot = level.getGameTime() * 0.22;

                    fx.polygon(ParticleTypes.END_ROD, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 20.0, 24, rot, 0.0);
                    fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.18, 0), ParticleFx.Axis.Y, 13.0, 12, -rot, 0.05);
                    fx.dashedRing(ParticleTypes.LAVA, base.add(0, 0.24, 0), ParticleFx.Axis.Y, 6.5, 12, 0.5, rot * 1.5, 0.08);
                    fx.pillars(ParticleTypes.END_ROD, base, 20.0, particleOpt ? 12 : 24, 3.0, rot);
                    fx.burst(ParticleTypes.FLAME, base.add(0, 0.5, 0), particleOpt ? 20 : 40, 1.5, true);
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
                    // 虚空侵蚀：目标脚下浮现反向封印环 + 被撕碎的暗物质
                    ParticleFx vf = ParticleFx.of(serverLevel);
                    if (vf != null) {
                        Vec3 vp = entity.position();
                        double vrot = serverLevel.getGameTime() * 0.35;
                        vf.budget(120).lod(0.6);
                        vf.dashedRing(ParticleTypes.REVERSE_PORTAL, vp.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.0, 8, 0.45, vrot, 0.05);
                        vf.dashedRing(ParticleTypes.SCULK_SOUL, vp.add(0, 0.9, 0), ParticleFx.Axis.Z, 0.85, 8, 0.45, -vrot, 0.05);
                        vf.bloom(ParticleTypes.REVERSE_PORTAL, entity.getEyePosition(), 5, 0.25, 0.02);
                    }
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
        // 粒子 / 着色器二选一：着色器版的金色大阵在技能释放那一刻一次性发了 110 tick，
        // 覆盖整段十轮脉冲；这里每轮不再打粒子（脉冲的"节奏感"由着色器内部的
        // Pulse 循环自己表现）。把 fx 置空，下面所有 if (fx != null) 会自动跳过。
        boolean shaderMode = SkillFxToggle.useShader(player);

        if (timer >= 10) {
            ParticleFx fx = shaderMode ? null : ParticleFx.of(level);
            if (fx != null) {
                fx.lod(particleOpt ? 0.5 : 1.0).budget(particleOpt ? 800 : 1800);
            }
            double rot = level.getGameTime() * 0.4;

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), damage);
                if (shaderMode) {
                    // 着色器版：无论这一刀有没有把它打死，都在它<b>体内</b>炸一次金光。
                    // 锚点是这个生物：它死了 / 消失后 anchor() 会退回这一帧的坐标，
                    // 所以爆点会留在原地，不会跟着飘走。
                    SkillFieldFx.atEntity(target, SkillEffectStyles.BURST_LIETIAN,
                            SkillLeijinhu8.BURST_RADIUS, SkillLeijinhu8.BURST_TICKS);
                } else if (fx != null) {
                    // 每名敌人身上炸开一记金色裂天爪痕
                    Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 to = hp.subtract(player.position()).normalize();
                    Vec3 side = ParticleFx.ortho(to);
                    fx.crossSlash(ParticleTypes.SWEEP_ATTACK, hp, to, side, 1.3, 170, particleOpt ? 1 : 2);
                    fx.burst(ParticleTypes.CRIT, hp, particleOpt ? 6 : 12, 0.4, true);
                }
            }
            if (fx != null) {
                // 以自身为中心的金色裂天光环
                Vec3 base = player.position();
                fx.dashedRing(ParticleTypes.CRIT, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 6.0, 14, 0.5, rot, 0.08);
                fx.star(ParticleTypes.GLOW, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 4.4, 1.8, 6, -rot, 0.06);
                if (!particleOpt) {
                    fx.dot(ParticleTypes.FLASH, base.add(0, 1.0, 0));
                }
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
            // ⚠️ 这里的脉冲间隙粒子也必须跟着「粒子 / 着色器」二选一走。
            //    原来只判了 particleOpt（配置位 10「粒子特效优化」），导致着色器模式下
            //    仍然会零星飘出 END_ROD —— 和"着色器版一个粒子都不发"的约定矛盾，
            //    而且会让玩家误以为"粒子还在、着色器没生效"。
            if (!shaderMode) {
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
    }

    /**
     * 裂金虎 · 第九魂技「猛虎破界」的<b>延迟结算</b>。
     *
     * <h3>为什么要延迟</h3>
     * <p>着色器版的表现是两拍：① 每只生物身后张开黑色旋涡；② 旋涡打出金色光芒。
     * 伤害要跟着第②拍走 —— 否则会变成"怪已经掉血了，旋涡才开始张开"。
     * 释放那一刻（{@code SkillLeijinhu9#executeEffect}）只登记目标 id 和倒计时，
     * 真正结算在这里。</p>
     *
     * <h3>为什么用 NBT 存 id 而不是静态表</h3>
     * <p>跟 {@code Liejinhu8_Active} 一个路子：不引入跨 tick 的静态状态，
     * 玩家退出 / 死亡 / 换维度都不会留下悬挂引用。目标在这一刻已经死了或换了维度，
     * {@code level.getEntity(id)} 取不到就自然跳过。</p>
     */
    private static void handleLiejinhuNine(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        if (!nbt.contains("Liejinhu9_Targets")) return;

        int delay = nbt.getInt("Liejinhu9_Delay") - 1;
        if (delay > 0) {
            nbt.putInt("Liejinhu9_Delay", delay);
            return;
        }

        ServerLevel level = player.serverLevel();
        float damage = nbt.getFloat("Liejinhu9_Damage");
        int[] ids = nbt.getIntArray("Liejinhu9_Targets");

        // 先把登记清掉再做结算：结算里可能触发其它逻辑，别让它在中间被重入
        nbt.remove("Liejinhu9_Delay");
        nbt.remove("Liejinhu9_Damage");
        nbt.remove("Liejinhu9_Targets");

        for (int id : ids) {
            var entity = level.getEntity(id);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) continue;
            // 金光和伤害同一 tick —— "光到即伤"
            SkillFieldFx.atEntity(target, SkillEffectStyles.VORTEX_POJIE_LIGHT,
                    SkillLeijinhu9.LIGHT_RADIUS, SkillLeijinhu9.LIGHT_TICKS);
            SkillLeijinhu9.applyStrike(player, level, target, damage, null, 0.0D);
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
            ParticleFx pulse = ParticleFx.of(level);
            if (pulse != null) {
                pulse.lod(particleOpt ? 0.5 : 1.0).budget(particleOpt ? 1000 : 2400);
            }
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().indirectMagic(player, player), damage);
                if (pulse != null) {
                    Vec3 tp = target.position();
                    // 星辰陨落：头顶垂落的光柱 + 顶点爆发
                    pulse.column(ParticleTypes.END_ROD, ParticleTypes.SOUL, target.position(), 0.6, 4.0, 3, gameTime * 0.1, ParticleFx.TAU, 0.4);
                    pulse.dashedRing(ParticleTypes.WITCH, tp.add(0, 0.15, 0), ParticleFx.Axis.Y, 1.6, 8, 0.5, -gameTime * 0.12, 0.06);
                    pulse.burst(ParticleTypes.END_ROD, tp.add(0, 4.0, 0), particleOpt ? 10 : 22, 0.9, true);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        }

        // 领域边界：每 2/4 tick 重绘双层旋转断环 + 六芒 + 四道环绕星轨
        if (gameTime % (particleOpt ? 4 : 2) == 0) {
            ParticleFx orbit = ParticleFx.of(level);
            if (orbit != null) {
                orbit.lod(particleOpt ? 0.5 : 1.0).budget(particleOpt ? 260 : 600);
                Vec3 base = player.position();
                double rot = gameTime * 0.14;

                orbit.dashedRing(ParticleTypes.SOUL, base.add(0, 0.25, 0), ParticleFx.Axis.Y, 15.0, 16, 0.4, rot, 0.1);
                orbit.dashedRing(ParticleTypes.WITCH, base.add(0, 0.6, 0), ParticleFx.Axis.Y, 15.0, 16, 0.4, -rot * 1.3, 0.1);
                orbit.star(ParticleTypes.END_ROD, base.add(0, 0.4, 0), ParticleFx.Axis.Y, 15.0, 6.0, 6, rot * 0.8, 0.05);
                // 四道环绕上升的星轨
                for (int i = 0; i < 4; i++) {
                    double a = rot * 1.6 + ParticleFx.TAU * i / 4;
                    orbit.dot(ParticleTypes.SOUL, base.add(Math.cos(a) * 15.0, 0.2, Math.sin(a) * 15.0));
                    orbit.dot(ParticleTypes.WITCH, base.add(Math.cos(a) * 15.0, 0.9, Math.sin(a) * 15.0));
                    orbit.dot(ParticleTypes.END_ROD, base.add(Math.cos(a) * 15.0, 1.7, Math.sin(a) * 15.0));
                }
                orbit.bloom(ParticleTypes.ENCHANTED_HIT, base.add(0, 0.5, 0), particleOpt ? 4 : 8, 0.6, 0.02);
            }
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

    /**
     * 不周倾落地冲击波的扩散速度（格/tick）。
     *
     * <p>80 格需要 40 tick（2 秒）铺满。⚠️ 这个值必须和客户端
     * {@code ShockwaveEffect#EXPAND_TICKS} 对得上（那里是 40 tick），
     * 否则光波跑在前面或落在后面，又会出现"没扫到就打到了"的违和感。</p>
     */
    private static final double BZQ_SHOCK_SPEED = 2.0D;

    /** 不周倾落地冲击波的最大半径（格）。与原判定范围一致。 */
    private static final double BZQ_SHOCK_RADIUS = 80.0D;

    /** 客户端光波特效的持续时长（tick）= 扩散 40 + 淡出 18。 */
    private static final int BZQ_SHOCK_TICKS = 58;

    private static void handleLieDiLanding(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        ServerLevel level = player.serverLevel();
        double verticalMomentum = player.getDeltaMovement().y;

        PlayerAttributeCapability landCap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        boolean landOpt = landCap != null && landCap.isConfigOpen(10);
        double landLod = landOpt ? 0.5 : 1.0;

        if (nbt.contains("BuZhouQing_Active") && player.onGround() && verticalMomentum <= 0) {
            // ---- 落地：不再"瞬间打满全场"，而是启动一个向外扩散的冲击波 ----
            // 原来的做法是立刻对 80 格内所有人生效 + 同时打一圈落地粒子，
            // 结果是光波还没扫到远处，远处的怪已经掉血了 —— 很违和。
            // 现在伤害和光波用**同一个速度**往外推（见下面的 BZQ_Shock 分支），
            // 做到"光波扫到哪里，哪里才受伤"。
            nbt.putBoolean("BZQ_Shock", true);
            nbt.putDouble("BZQ_ShockR", 0.0D);
            nbt.putFloat("BZQ_ShockDmg", nbt.getFloat("BuZhouQing_Damage"));

            if (SkillFxToggle.useShader(player)) {
                // ---- 表现 A：着色器版 —— 一道扩散到最大半径的冲击光波 ----
                // 客户端按 durationTicks 自己推进前沿，服务端只发这一次。
                // ⚠️ 用 atPoint 钉在**落地点**，不要用 atEntity —— 光波的圆心必须留在原地，
                //    否则玩家落地后一跑，扩散到一半的圆环会整个跟着人平移，像被拖着走。
                SkillFieldFx.atPoint(player, SkillEffectStyles.SHOCK_BUZHOU,
                        player.getX(), player.getY(), player.getZ(),
                        (float) BZQ_SHOCK_RADIUS, BZQ_SHOCK_TICKS);
            } else {
                // ---- 表现 B：粒子版 —— 落地瞬间的阵法 + 地柱 ----
                // 持续往外推的冲击环由下面的 tick 分支每帧补，这里只打"落地那一下"。
                ParticleFx fx = ParticleFx.of(level);
                if (fx != null) {
                    fx.lod(landLod).budget(landOpt ? 1600 : 3600);
                    Vec3 base = player.position();
                    double rot = level.getGameTime() * 0.2;

                    fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 20.0, 24, rot, 0.3);
                    fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 14.0, 16, -rot, 0.1);
                    fx.magicCircle(ParticleTypes.LAVA, ParticleTypes.WHITE_ASH, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 7.0, rot * 1.6);
                    fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 20.0, landOpt ? 12 : 24, 4.0, rot);
                    fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.LAVA, base, 1.6, 10.0, 4, rot, ParticleFx.TAU * 0.8, 0.5);
                    fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.6, 0), landOpt ? 26 : 56, 2.4, true);
                    fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 1.2, 0), landOpt ? 24 : 48, 4.0, 0.25);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 2.0f, 0.5f);
            nbt.remove("BuZhouQing_Active");
            nbt.remove("BuZhouQing_Damage");
        }

        // ---- 冲击波推进：每 tick 把半径往外推一格，只结算"这一圈环带"里的敌人 ----
        // 用「上一帧半径 ~ 这一帧半径」这个环带做判定，天然不会重复命中同一个目标，
        // 也不用额外记录"打过谁"。想跑出范围的敌人依然跑得掉 —— 这是合理的：
        // 光波没追上它，本来就不该受伤。
        if (nbt.getBoolean("BZQ_Shock")) {
            double r0 = nbt.getDouble("BZQ_ShockR");
            double r1 = Math.min(r0 + BZQ_SHOCK_SPEED, BZQ_SHOCK_RADIUS);
            float shockDamage = nbt.getFloat("BZQ_ShockDmg");
            Vec3 center = player.position();
            double rr0 = r0 * r0;
            double rr1 = r1 * r1;

            List<LivingEntity> ring = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(r1),
                    e -> {
                        if (e == player || !e.isAlive()) {
                            return false;
                        }
                        double d2 = e.position().distanceToSqr(center);
                        return d2 > rr0 && d2 <= rr1;
                    });
            for (LivingEntity target : ring) {
                target.hurt(player.damageSources().playerAttack(player), shockDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));
            }

            // 粒子版：在光波当前所在的位置补一圈粒子，让粒子和判定同步往前走
            if (!SkillFxToggle.useShader(player)) {
                ParticleFx wave = ParticleFx.of(level);
                if (wave != null) {
                    wave.lod(landLod).budget(landOpt ? 120 : 200);
                    wave.shockRing(ParticleTypes.LARGE_SMOKE,
                            center.add(0, 0.08, 0), r1, 1.5, landOpt ? 20 : 34);
                }
            }

            if (r1 >= BZQ_SHOCK_RADIUS) {
                nbt.remove("BZQ_Shock");
                nbt.remove("BZQ_ShockR");
                nbt.remove("BZQ_ShockDmg");
            } else {
                nbt.putDouble("BZQ_ShockR", r1);
            }
        }

        if (nbt.contains("LieDiActive") && player.onGround() && verticalMomentum <= 0) {
            float finalDamage = nbt.getFloat("LieDiDamage");
            List<LivingEntity> ldTargets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(20.0), e -> e != player && e.isAlive());
            for (LivingEntity target : ldTargets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.push(0, 0.8, 0);
            }

            // ---- 裂地落地：大地龟裂 —— 十二道放射裂纹 + 三重冲击环 + 尘柱 ----
            ParticleFx fx = ParticleFx.of(level);
            if (fx != null) {
                fx.lod(landLod).budget(landOpt ? 1300 : 3000);
                Vec3 base = player.position();
                double rot = level.getGameTime() * 0.22;

                // 十二道放射裂纹（自定义长度，营造龟裂感）
                int cracks = landOpt ? 8 : 12;
                for (int i = 0; i < cracks; i++) {
                    double a = rot + ParticleFx.TAU * i / cracks;
                    Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                    fx.lightning(ParticleTypes.CAMPFIRE_COSY_SMOKE, base, base.add(dir.scale(15.0)), 14, 1.4, a);
                    fx.line(ParticleTypes.SOUL_FIRE_FLAME, base, base.add(dir.scale(15.0)), 1.1, 0.35, 0.0, Vec3.ZERO);
                }
                fx.polygon(ParticleTypes.LARGE_SMOKE, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 10.0, 12, -rot, 0.3);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 6.0, 2.5, 42);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.1, 0), 12.0, 2.0, 46);
                fx.shockRing(ParticleTypes.WHITE_ASH, base.add(0, 0.15, 0), 18.0, 1.5, 50);
                fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME, base, 1.1, 8.0, 4, rot, ParticleFx.TAU, 0.5);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.5, 0), landOpt ? 22 : 44, 1.8, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 1.0, 0), landOpt ? 20 : 40, 3.0, 0.2);
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

        // 吟唱期间武魂被关掉 / 换掉 / 换了技能槽 → 这次吟唱已经没有意义。
        // ⚠️ 不判的话，下面「完成结算」会拿**新武魂的槽位**去取年限倍率，算出错误的伤害。
        if (!isStillCastable(cap, castingSkill)) {
            cap.stopCasting(player);
            player.displayClientMessage(Component.literal("§c武魂变化，吟唱中断！"), true);
            SynsAPI.synsPlayerAttribute(player);
            return;
        }

        cap.setCastingTick(cap.getCastingTick() + 1);

        if (gameTime % 5 == 0) {
            // 吟唱中的蓄力法阵：脚下旋转断环 + 身周上升的附魔光点
            ParticleFx castFx = ParticleFx.of(player.serverLevel());
            if (castFx != null) {
                Vec3 base = player.position();
                double rot = gameTime * 0.2;
                castFx.budget(240).lod(0.6);
                castFx.dashedRing(ParticleTypes.ENCHANT, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 1.6, 8, 0.4, rot, 0.06);
                castFx.spiral(ParticleTypes.ENCHANT, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 1.5, 0.3, 2.6, 1.6, -rot, 30, 0.06);
                castFx.bloom(ParticleTypes.ENCHANT, base.add(0, 2.2, 0), 4, 0.2, 0.0);
            }
        }

        if (cap.getCastingTick() >= cap.getRequiredCastTick()) {
            String currentWuhun = cap.getWuhunName();
            // ⚠️ 必须用「吟唱开始时就记下的槽位」，不能用 getSelectedSkillSlot()：
            //    批量释放不改选中槽位，用选中槽位会取到错魂环的年限（倍率算错），
            //    并且会把冷却记到错误的槽位上。
            int castSlot = cap.getCastingSlot();
            int nianxian = 10;
            List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
            if (rings != null && castSlot >= 0 && castSlot < rings.size()) {
                nianxian = (int) rings.get(castSlot).getNianxian();
            }
            float powerMultiplier = castingSkill.getPowerMultiplier(nianxian);
            float costMultiplier = castingSkill.getCostMultiplier(nianxian);
            castingSkill.executeEffect(player.level(), player, powerMultiplier);
            castingSkill.applyPenalty(player, costMultiplier);
            cap.setSkillLastUsedTime(currentWuhun, castSlot, gameTime);
            player.displayClientMessage(Component.translatable("gui.kunluncontinent.cast_shifa"), true);
            cap.stopCasting(player);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    /** 吟唱中的技能是否仍然是「当前武魂、它自己的那个槽位」上的那一个。 */
    private static boolean isStillCastable(PlayerAttributeCapability cap, BaseSkillItem castingSkill) {
        String wuhun = cap.getWuhunName();
        if (wuhun == null) return false;
        BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(wuhun);
        int slot = cap.getCastingSlot();
        return skills != null && slot >= 0 && slot < skills.length && skills[slot] == castingSkill;
    }

/** 神界（天境）维度里，坐在蒲团上的修炼速度倍率。 */
    private static final float CELESTIAL_MEDITATION_MULTIPLIER = 2.0f;
    /** 神界（天境）维度里，修炼时间的自然回复倍率。 */
    private static final int CELESTIAL_XIULIAN_TIME_RECOVER_MULTIPLIER = 2;

    /**
     * 蒲团每秒回复精神力的<b>比例</b>（占精神力上限）。
     *
     * <p>⚠️ 旧实现是 {@code maxJs * 0.02f + 5.0f} —— 那个 {@code +5.0f} 固定值
     * 导致回复量恒定「约 50 秒回满」：上限 1 万时 1 秒回 205 点（顶格），
     * 上限 100 万时 1 秒回 2 万 —— <b>无论多少上限都是 50 秒满</b>，
     * 蒲团等于给了无限精神力，完全丧失「挂机慢慢回」的定位。</p>
     *
     * <p>改为纯百分比后：任何等级都是 {@code 1 / 0.04 = 25} 秒回满，
     * 且<b>上限越高、需要的时间不变</b>，只靠时间换不回高等级的精神力。</p>
     */
    private static final float PUTUAN_JINGSHENLI_REGEN_RATIO = 0.04f;

    /**
     * 修炼时间耗尽后的冷却期内，回复速度的<b>慢化倍数</b>（3 = 慢 3 倍）。
     * <p>冷却仍会存在（原本 3 分钟），但期间也能缓慢回时间，不必干站。</p>
     */
    private static final int XIULIAN_COOLDOWN_RECOVER_PENALTY = 3;

    private static void handleMeditationLogic(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        boolean isMeditating = player.getVehicle() != null
                && player.getVehicle().getTags().contains("putuan_seat");
        // ⭐ 神界（天境）：蒲团修炼速度 ×2、修炼时间回复速度 ×2。
        //    判定用 level.dimension()（服务端权威），与 CelestialTeleport#isInCelestial 同一口径。
        boolean inCelestial = player.level().dimension() == ModDimensions.CELESTIAL_REALM_LEVEL_KEY;
        CompoundTag nbt = player.getPersistentData();

        if (isMeditating) {
            if (gameTime % 20 == 0) {
                boolean needSync = false;
                float currentJs = cap.getJingshenli();
                // ⭐ 口径统一：必须用 ModAttributeAPI（含魂骨/装备/面板加成），
                //    与 updateJingshenliRegen 保持一致。
                //    之前这里用裸的 cap.getMaxjingshenli()，两处算出不同上限，
                //    Math.min 会拿错边界导致精神力溢出或回不满。
                float maxJs = ModAttributeAPI.getMaxjingshenli(player);
                if (maxJs <= 0.0f) maxJs = cap.getMaxjingshenli();

                if (currentJs < maxJs) {
                    // 纯百分比回复（去掉旧的 +5.0f 固定值，理由见常量注释）
                    cap.setJingshenli(Math.min(maxJs, currentJs + maxJs * PUTUAN_JINGSHENLI_REGEN_RATIO));
                    needSync = true;
                }

                int currentTime = cap.getXiulianTime();
                if (currentTime > 0) {
                    cap.setXiulianTime(currentTime - 1);
                    int lvl = cap.getDengji();

                    // 连续公式：分钟数随等级平滑递增，30/70 级不再有断层跳变。
                    // 公式已抽到 XiulianLogic，跳过修炼走同一份，两者收益必然一致。
                    float expGained = cap.getMaxjingyan() / (XiulianLogic.minutesPerLevel(lvl) * 60f);

                    // ⭐ 神界天境：只翻倍"经验获取"，修炼时间消耗（currentTime - 1）保持 1 点不变。
                    //    这样神界挂机 = 同样 10 分钟消耗同样修炼时间，但拿到的经验 ×2，
                    //    既让玩家明显变强，又不会出现"消耗不变→净收益只是刷时长"的歧义。
                    if (inCelestial) {
                        expGained *= CELESTIAL_MEDITATION_MULTIPLIER;
                    }

                    cap.setJingyan(cap.getJingyan() + expGained);
                    PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
                    needSync = true;

                    if (currentTime - 1 <= 0) {
                        cap.setUsingAll(true);
                        player.sendSystemMessage(Component.literal("§c【修炼结束】你的修炼时间已耗尽！进入 3 分钟回复冷却。"));
                        nbt.putInt("XiulianCooldown", XiulianLogic.COOLDOWN_TICKS);
                    }
                }

                if (needSync) SynsAPI.synsPlayerAttribute(player);
            }
        } else {
            // 不在蒲团上：先扣冷却，冷却结束后再恢复修炼时间
            if (nbt.contains("XiulianCooldown")) {
                int cd = nbt.getInt("XiulianCooldown");
                if (cd > 0) {
                    nbt.putInt("XiulianCooldown", cd - 1);
                } else {
                    nbt.remove("XiulianCooldown");
                    player.sendSystemMessage(Component.literal("§a【冥想调息】你的修炼时间恢复冷却已结束，开始逐渐恢复修炼时间。"));
                }
            }

            // ⭐ 冷却期间也允许回复（只是慢一些），避免玩家修炼时间耗尽后
            //    必须干站 3 分钟才能继续 —— 那是纯粹的等待惩罚，没有玩法意义。
            //    冷却中按 1/3 速度回复，即每 300 tick 回 1 秒。
            int baseRate = cap.isUsingAll() ? 87 : 100;
            int tickRate = nbt.contains("XiulianCooldown")
                    ? baseRate * XIULIAN_COOLDOWN_RECOVER_PENALTY   // 冷却中：3 倍间隔
                    : baseRate;

            if (gameTime % tickRate == 0) {
                if (cap.getXiulianTime() < XiulianLogic.TIME_MAX) {
                    // ⭐ 神界天境：回复速度 ×2。
                    //    这里刻意「回复量 ×2」而不是「周期 ÷2」——87/100 都是奇数，
                    //    除 2 会得到 43/50，反倒变成 2.02~2.05 倍的误差。
                    int recoverAmount = inCelestial
                            ? CELESTIAL_XIULIAN_TIME_RECOVER_MULTIPLIER : 1;
                    int next = Math.min(XiulianLogic.TIME_MAX, cap.getXiulianTime() + recoverAmount);
                    cap.setXiulianTime(next);
                    if (next >= XiulianLogic.TIME_MAX) {
                        cap.setUsingAll(false);
                    }
                }
            }
        }
    }


    // ==================== 玩家受伤事件 ====================

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // ⚠️ 先确认「这一下真的打实了」，否则会出现「明明闪避了、吟唱却断了」这种别扭体验。
        //   ① 事件已被取消：本模组的 CombatEventHandler 是 priority = HIGHEST（比本方法早执行），
        //      它在【闪避】和【免伤】生效时会 setCanceled(true)。
        //      而 Forge 取消事件**不会**阻止后面的监听器 —— 必须自己判 isCanceled()。
        //      闪避是核心属性，不判的话玩家看到「闪避了这次攻击！」之后吟唱照断。
        //   ② 伤害为 0：算下来 0 点伤害的攻击不该算「受到攻击」。
        //   ③ 不是生物打的：摔落 / 火焰 / 中毒 / 窒息 / 虚空走的是同一个事件，
        //      玩家只是站在原地被烧，不该算被打断。
        //      远程弹射物取 shooter（getEntity）也能命中，所以两个都查。
        boolean landedHit = !event.isCanceled()
                && event.getAmount() > 0.0F
                && (event.getSource().getEntity() instanceof LivingEntity
                    || event.getSource().getDirectEntity() instanceof LivingEntity);
        if (!landedHit) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat")) {
                player.stopRiding();
                // 修正：literal 用于直接显示中文字符串，translatable 用于 lang key
                player.sendSystemMessage(Component.literal("心神受损").withStyle(ChatFormatting.RED));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
            if (cap.getCurrentCastingSkill() != null) {
                // ⭐ 抗打断在**吟唱开始时就已经掷过骰**（见 CPacketReleaseSkill）：
                //    掷中了就代表整段吟唱打不断，这里直接放行、连提示都不要再发 ——
                //    每次受击都提示会在吟唱期间把 ActionBar 刷屏。
                //    掷不中则任意一次实打实的命中都能打断。
                if (cap.isCastingProtected()) {
                    return;
                }
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
        // 口径与蒲团一致：都用 ModAttributeAPI（含魂骨/装备/面板加成）
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
        // 兜底：上限异常（0 / 负）时直接返回，避免出现「除以 0」或精神力被写负
        if (maxJingshenli <= 0.0f) return;
        if (currentJingshenli < maxJingshenli) {
            capability.setJingshenli(Math.min(currentJingshenli + maxJingshenli * 0.01f, maxJingshenli));
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    // ==================== 飞行逻辑 ====================

    private static void updatePlayerFly(Player player, PlayerAttributeCapability capability, long gameTime) {
        if (player.isCreative() || player.isSpectator()) return;

        boolean canFlyPrev = player.getAbilities().mayfly;

        // 飞行速度档位：写进 abilities（只在值变化时才发包），越快档位越高
        FlySpeedTuning.applyFlyingSpeed(player, capability.getFlySpeedLevel());

        // 撞墙爆炸冷却递减（服务端权威状态，客户端只负责检测并发包）
        FlyCrashBlast.tickCooldown(player);

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

        // 雪帝领域：有仇恨的雪帝 150 格内禁飞。
        // 每 tick 都算 —— 静态集合里通常只有 0~2 个元素，只是一次距离平方比较，
        // 比上面那条"扫 40 格实体"的禁空词条便宜得多。脱离范围后本方法会返回 false，
        // 下面的 else 分支自动把 mayfly 还回去，实现"自动解禁"。
        boolean iceEmpressNoFly = IceEmpressEntity.isFlightSuppressedNear(player);

        if (player.getPersistentData().contains("knocked_down")) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        } else if (isAntiFlyZone || iceEmpressNoFly) {
            if (player.getAbilities().mayfly || player.getAbilities().flying) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                if (gameTime % 40 == 0) {
                    if (iceEmpressNoFly) {
                    } else {
                        player.sendSystemMessage(Component.literal("§4§l【禁空法则】§c 四周有《禁空》词条生物压制，被强制击落！"));
                    }
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
        //    ⚠️ 武魂真身开启时这份维持费 ×10（ZhenshenState.UPKEEP_MULTIPLIER），
        //    这是「真身贵 10 倍」的唯一落地点，别在别处再乘一次。
        //
        //    注意：本方法开头对创造/旁观模式直接 return，所以这两个模式<b>不扣维持费</b>
        //    （改动前的既有行为，保持不动）。因此「真身的状态机」不能写在这里，
        //    否则创造模式玩家会看到「增益 5 秒后凭空消失」—— 见 onPlayerTick 里的 1b。
        if (capability.getWuhunName() != null && gameTime % 20 == 0) {
            float cost = wuhunUpkeepCost(capability.getDengji()) * ZhenshenState.upkeepMultiplier(player);
            capability.setJingshenli(capability.getJingshenli() - cost);
        }

        // 2. 只有在飞行时，每 10 tick 额外扣除飞行消耗（与武魂无关）
        //    消耗随飞行速度档位放大：越快越费精神力
        if (player.getAbilities().flying && gameTime % 10 == 0) {
            float currentJs = capability.getJingshenli();
            float cost = (30.0f - (float) capability.getDengji() / 5.0f)
                    * FlySpeedTuning.costMultiplier(capability.getFlySpeedLevel());
            capability.setJingshenli(currentJs - cost);
        }
    }

    /**
     * 普通开启武魂时的每秒维持费（按等级）。
     *
     * <p>抽成方法是为了让武魂真身的 10 倍倍率有唯一落点 ——
     * 真身不再复制一遍这段公式，见 {@code ZhenshenState#upkeepMultiplier}。</p>
     */
    private static float wuhunUpkeepCost(int lvl) {
        float cost;
        if (lvl <= 40) {
            cost = 3.0f + (lvl * 0.051f);
        } else if (lvl <= 70) {
            cost = 25.0f - 0.088f * (float) Math.pow(lvl - 55, 2);
        } else {
            // 71级时约 5.0，之后每升一级降低 0.12 点
            cost = 5.2f - ((lvl - 70) * 0.12f);
        }
        return Math.max(cost, 1.5f);
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

        // 死亡时把吟唱状态清干净。否则死亡到实体移除之间 onPlayerTick 还在跑，
        // 会把这次吟唱推完并结算（死人放技能）；而且 -70% 移速的吟唱减速修正
        // 会一直挂在属性上（万一以后加了拦截死亡的机制，就会永久减速）。
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> cap.stopCasting(serverPlayer));
        }

        // 一键释放可能还有没放完的槽位，死了就别再续了
        SkillBatchRelease.cancel(player.getUUID());

        ZhuanShengTestItem.cancelReincarnation(player);

        CompoundTag data = player.getPersistentData();
        data.remove("IsPreparingReincarnation");
        data.remove("IsLightningPhase");
        data.remove("ReincarnationTimer");
        data.remove("LightningCount");

        // 裂天（裂金虎8）的十轮脉冲、猛虎破界（裂金虎9）待结算的金光：
        // 死了就一起清掉。理由和上面清吟唱一样 —— 死亡到实体移除之间 onPlayerTick
        // 还在跑，不清的话死亡动画期间还会继续对周围生物结算伤害。
        // （另外 Forge 会把玩家的 persistentData 复制到重生后的实体上，
        //   不清的话会变成"复活 0.5 秒后突然挨一发自己死前放的技能"。）
        data.remove("Liejinhu8_Active");
        data.remove("Liejinhu8_Remaining");
        data.remove("Liejinhu8_Timer");
        data.remove("Liejinhu8_Damage");
        data.remove("Liejinhu9_Delay");
        data.remove("Liejinhu9_Damage");
        data.remove("Liejinhu9_Targets");
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