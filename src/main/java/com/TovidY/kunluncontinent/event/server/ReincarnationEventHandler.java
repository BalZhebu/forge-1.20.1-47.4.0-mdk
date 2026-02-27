package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.klitem.ZhuanShengTestItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReincarnationEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.player;
        CompoundTag data = player.getPersistentData();
        ZhuanShengTestItem.checkReincarnationProgress(player);
        if (data.getBoolean("IsPreparingReincarnation")) {
            int timer = data.getInt("ReincarnationTimer");
            if (timer > 0) {
                data.putInt("ReincarnationTimer", timer - 1);
                if (timer % 20 == 0) {
                    player.sendSystemMessage(Component.literal("天劫倒计时: " + (timer / 20) + "秒").withStyle(ChatFormatting.RED));
                }
            } else {
                data.remove("IsPreparingReincarnation");
                data.putBoolean("IsLightningPhase", true);
                data.putInt("LightningCount", 0);
                player.sendSystemMessage(Component.literal("天威降临，雷劫开始！（天雷会压低血量但不至死，20血以下时将获得转生重修的机会）").withStyle(ChatFormatting.DARK_RED));
            }
        }
        if (data.getBoolean("IsLightningPhase")) {
            if (player.level().getGameTime() % 20 == 0) {
                int count = data.getInt("LightningCount");
                float currentHealth = player.getHealth();
                if (currentHealth <= 20.0f && count > 0) {
                    player.sendSystemMessage(Component.literal("劫难已满，破后而立！").withStyle(ChatFormatting.LIGHT_PURPLE));
                    data.remove("IsLightningPhase");
                    data.remove("LightningCount");
                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        ZhuanShengTestItem.completeReincarnation(player, cap);
                    });
                } else if (count >= 99) {
                    player.sendSystemMessage(Component.literal("雷劫消散，你的实力太过强大，无法堕入轮回。（血量达到20以下时才会进入轮回）").withStyle(ChatFormatting.GRAY));
                    data.remove("IsLightningPhase");
                } else {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
                    if (bolt != null) {
                        bolt.setPos(player.position());
                        bolt.setVisualOnly(true);
                        player.level().addFreshEntity(bolt);
                    }
                    float damageAmount = currentHealth * 0.95f;
                    player.hurt(player.damageSources().lightningBolt(), damageAmount);
                    player.setHealth(Math.max(1.0f, currentHealth * 0.2f));
                    data.putInt("LightningCount", count + 1);
                    player.sendSystemMessage(Component.literal("第 " + (count + 1) + " 重雷劫...").withStyle(ChatFormatting.DARK_PURPLE));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player) {
            // 取消物品转生
            ZhuanShengTestItem.cancelReincarnation(player);

            // 取消重修台转生进度
            CompoundTag data = player.getPersistentData();
            if (data.contains("IsPreparingReincarnation") || data.contains("IsLightningPhase")) {
                data.remove("IsPreparingReincarnation");
                data.remove("IsLightningPhase");
                data.remove("ReincarnationTimer");
                data.remove("LightningCount");
            }
        }
    }
}