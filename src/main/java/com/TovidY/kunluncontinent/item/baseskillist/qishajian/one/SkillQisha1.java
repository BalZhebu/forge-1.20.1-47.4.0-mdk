package com.TovidY.kunluncontinent.item.baseskillist.qishajian.one;

import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.server.level.ServerPlayer;
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

/**
 * 第一魂技 · 七杀剑（武魂实体化）
 *
 * <p>与八荒戟的第一魂技同构：把器武魂本体凝结出来交给玩家（优先副手），
 * 同时对同主人的旧剑做一次回收（顺带清掉它残留的攻击加成，避免叠加）。</p>
 *
 * <p>本体 {@link QishajianItem} 自己负责：灵魂绑定、手持加攻、精神力维持、
 * 换人持有或精神力耗尽就溃散。</p>
 */
public class SkillQisha1 extends BaseSkillItem {

    @Override
    public float getBaseCost() { return 60f; }

    /** 召唤类技能没有伤害倍率。 */
    @Override
    public float getDamageMultiplier() { return 0f; }

    @Override
    public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.one.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;

        // 1. 先回收旧剑（含其攻击加成）
        checkAndRemoveOldQishajian(player);

        // 2. 生成本体
        ItemStack sword = new ItemStack(ModItems.QISHAJIAN.get());
        CompoundTag nbt = sword.getOrCreateTag();
        nbt.putUUID("OwnerUUID", player.getUUID());
        nbt.putString("OwnerName", player.getScoreboardName());
        sword.setHoverName(Component.literal("§c§l七杀剑")
                .append(Component.literal(" §7(本命武魂)").withStyle(ChatFormatting.ITALIC)));

        // 3. 优先副手，其次背包，实在塞不下才掉落
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, sword);
        } else if (!player.getInventory().add(sword)) {
            player.drop(sword, false);
        }

        // 4. 特效：七道血色符文环绕 + 中心煞气柱
        boolean shaderFx = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
        if (shaderFx) {
            ServerPlayer sp = (ServerPlayer) player;
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_SUMMON, sp.getX(), sp.getY(), sp.getZ(), 2.4F, 40);
            QishaFx.field(sp, SkillEffectStyles.BURST_QISHA, sp.getX(), sp.getY() + 1.0, sp.getZ(), 1.2F, 15);
        }
        // 着色器模式下 fx 为 null，下面所有 if (fx != null) 分支自动跳过 —— 两种表现不叠加
        ParticleFx fx = shaderFx ? null : ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            double rot = serverLevel.getGameTime() * 0.3;
            fx.budget(1400);
            fx.star(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.5, 0), ParticleFx.Axis.Y, 2.6, 0.9, 7, rot, 0.03);
            fx.magicCircle(ParticleTypes.SOUL, ParticleTypes.CRIMSON_SPORE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 2.2, -rot);
            fx.column(ParticleTypes.CRIMSON_SPORE, ParticleTypes.SOUL, base, 0.4, 2.8, 4, rot, 1.2, 0.6);
            fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.1, 0), 3.2, 1.5, 36);
            fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.8, 0), 20, 0.6, true);
        }

        player.displayClientMessage(Component.literal("§4§l[第一魂技] §c七杀剑，现！"), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    /** 回收玩家身上已有的七杀剑，并退还它加过的攻击力（不清退就会越叠越高）。 */
    private void checkAndRemoveOldQishajian(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof QishajianItem)) {
                continue;
            }
            CompoundTag nbt = stack.getTag();
            if (nbt == null || !nbt.contains("OwnerUUID") || nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
                if (nbt != null && nbt.contains("QishaBonusValue")) {
                    float bonus = nbt.getFloat("QishaBonusValue");
                    if (bonus > 0) {
                        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                            cap.setGongji(Math.max(0, cap.getGongji() - bonus));
                            SynsAPI.synsPlayerAttribute(player);
                        });
                    }
                }
                stack.setCount(0);
            }
        }
    }
}
