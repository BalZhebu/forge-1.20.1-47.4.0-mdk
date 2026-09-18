package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkillPohun1 extends BaseSkillItem {

    @Override public float getBaseCost() { return 60f; }

    @Override public float getDamageMultiplier() { return 0f; }

    @Override public int getCastTime() { return 0; }

    @Override public int getCooldownTicks() { return 60; }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.one.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            // 1. 发动技能前，检查并清除玩家身上的旧破魂枪及残留加成
            checkAndRemoveOldPohunqiang(player);

            // 2. 创建并配置新破魂枪
            ItemStack spear = new ItemStack(ModItems.POHUNQIANG.get());
            CompoundTag nbt = spear.getOrCreateTag();
            nbt.putUUID("OwnerUUID", player.getUUID());
            nbt.putString("OwnerName", player.getScoreboardName());
            spear.setHoverName(Component.literal("§6" + player.getName().getString() + "的破魂枪")
                    .withStyle(ChatFormatting.BOLD));

            // 3. 优先放入副手，副手有东西则放入背包；背包满则不给（或按原逻辑掉落/清掉）
            if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, spear);
            } else {
                if (!player.getInventory().add(spear)) {
                    player.drop(spear, false);
                }
            }

            // 4. 技能效果提示与音效粒子
            player.displayClientMessage(Component.literal("§c§l破魂枪，现！"), true);

            // ---- 特效：脚下赤红法阵 → 环身灵魂螺旋 → 枪形光柱收束 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 base = player.position();
                Vec3 eye = player.getEyePosition();
                double rot = serverLevel.getGameTime() * 0.28;

                fx.budget(1500);
                fx.magicCircle(ParticleTypes.CRIMSON_SPORE, ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 2.6, rot);
                fx.dashedRing(ParticleTypes.SOUL, base.add(0, 0.3, 0), ParticleFx.Axis.Y, 3.2, 12, 0.5, -rot * 1.4, 0.03);
                fx.spiral(ParticleTypes.SOUL, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 2.4, 0.25, 2.6, 2.5, rot, 96, 0.05);
                fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, base, 0.55, 2.6, 4, rot, ParticleFx.TAU * 1.2, 0.4);
                fx.cone(ParticleTypes.ENCHANTED_HIT, eye.add(player.getLookAngle().scale(1.2)), player.getLookAngle(), 1.6, 0.35, 8, rot, 4);
                fx.slash(ParticleTypes.SWEEP_ATTACK, eye, player.getLookAngle(), new Vec3(0, 1, 0), 2.4, 220, 2, rot);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }

    /**
     * 检查并清除玩家身上的旧破魂枪，同时重置加成属性
     */
    private void checkAndRemoveOldPohunqiang(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof PohunqiangItem) {
                CompoundTag nbt = stack.getTag();
                if (nbt == null || !nbt.contains("OwnerUUID") || nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
                    if (nbt != null && nbt.contains("PohunBonusValue")) {
                        float lastBonus = nbt.getFloat("PohunBonusValue");
                        if (lastBonus > 0) {
                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                cap.setGongji(Math.max(0, cap.getGongji() - lastBonus));
                                SynsAPI.synsPlayerAttribute(player);
                            });
                        }
                    }
                    stack.setCount(0);
                }
            }
        }
    }
}
