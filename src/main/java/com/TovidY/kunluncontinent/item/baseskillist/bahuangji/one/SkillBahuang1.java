package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one;


import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
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

public class SkillBahuang1 extends BaseSkillItem {

    @Override
    public float getBaseCost() { return 60f; }

    @Override
    public float getDamageMultiplier() { return 0f; } // 召唤类技能无伤害倍率

    @Override
    public int getCastTime() { return 0; } // 瞬发

    @Override
    public int getCooldownTicks() { return 60; } // 3秒

    @Override
    public String getDescriptionKey() {
        return "skill.bakuangji.one.description";
    }

    /**
     * [核心更新]：将逻辑写在 4 参数版本中，
     * 这样无论瞬发还是吟唱（虽然目前是0），都会走这个分发管道。
     */
    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            // 0. 发动技能前，先检查并清除玩家身上的旧八荒戟及其残留属性加成
            checkAndRemoveOldBahuangji(player);

            // 1. 生成并设置八荒戟物品
            ItemStack bahuangji = new ItemStack(ModItems.BAHUANGJI.get());
            CompoundTag nbt = bahuangji.getOrCreateTag();
            nbt.putUUID("OwnerUUID", player.getUUID());
            nbt.putString("OwnerName", player.getScoreboardName());

            bahuangji.setHoverName(Component.literal("§c§l八荒戟")
                    .append(Component.literal(" §7(本命武魂)").withStyle(ChatFormatting.ITALIC)));

            // 2. 装备逻辑：优先副手，副手占用则尝试放入主背包；背包满了则掉落
            if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, bahuangji);
            } else {
                if (!player.getInventory().add(bahuangji)) {
                    player.drop(bahuangji, false);
                }
            }

            // 3. 视觉特效
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.3, 0.8, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);

            // 4. 提示信息
            player.displayClientMessage(Component.literal("§4§l[第一魂技] §c八荒戟，出！"), true);

            // 5. 播放霸气音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5f, 1.2f);
        }
    }

    /**
     * 检查并清除玩家身上已有的八荒戟，同时扣除其先前赋予的攻击力加成
     */
    private void checkAndRemoveOldBahuangji(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BahuangjiItem) {
                CompoundTag nbt = stack.getTag();
                if (nbt == null || !nbt.contains("OwnerUUID") || nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
                    if (nbt != null && nbt.contains("BahuangBonusValue")) {
                        float lastBonus = nbt.getFloat("BahuangBonusValue");
                        if (lastBonus > 0) {
                            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                                cap.setGongji(Math.max(0, cap.getGongji() - lastBonus));
                                SynsAPI.synsPlayerAttribute(player);
                            });
                        }
                    }
                    // 销毁旧物品
                    stack.setCount(0);
                }
            }
        }
    }
}