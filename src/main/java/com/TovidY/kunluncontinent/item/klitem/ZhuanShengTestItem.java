package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ZhuanShengTestItem extends Item{

    private static final String TIMER_KEY = "reincarnation_timer";
    private static final String INTERRUPT_KEY = "reincarnation_interrupted";

    public ZhuanShengTestItem(Item.Properties properties) {
        super(properties.food(
                new FoodProperties.Builder()
                        .nutrition(0)
                        .saturationMod(0)
                        .alwaysEat()
                        .build()
        ));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            CompoundTag data = player.getPersistentData();
            data.putInt(TIMER_KEY, 200); // 10秒计时
            data.putBoolean(INTERRUPT_KEY, false);
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false, true));
            player.sendSystemMessage(Component.literal("转生开始！10秒后完成，死亡将中断。").withStyle(ChatFormatting.GOLD));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    public static void checkReincarnationProgress(Player player) {
        if (player.level().isClientSide) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(TIMER_KEY)) return;
        int timer = data.getInt(TIMER_KEY);
        if (timer > 0) {
            timer--;
            data.putInt(TIMER_KEY, timer);
            if (timer == 0) {
                if (!data.getBoolean(INTERRUPT_KEY)) {
                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        completeReincarnation((ServerPlayer) player, cap);
                    });
                }
                data.remove(TIMER_KEY);
                data.remove(INTERRUPT_KEY);
            }
        }
    }

    public static void cancelReincarnation(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(TIMER_KEY)) {
            data.putBoolean(INTERRUPT_KEY, true); // 标记为中断
            data.remove(TIMER_KEY); // 移除计时，停止逻辑

            player.sendSystemMessage(Component.literal("转生中断！").withStyle(ChatFormatting.RED));
            player.removeEffect(MobEffects.GLOWING);
        }
    }

    public static void completeReincarnation(ServerPlayer player, PlayerAttributeCapability oldCap) {
        for (String wuhunName : oldCap.getWuhunListsname()) {
            ItemStack fruit = WuhunguoshiItem.getWuhunguo(wuhunName);
            if (!fruit.isEmpty() && !player.addItem(fruit)) {
                ItemEntity entity = player.drop(fruit, false);
                if (entity != null) {
                    entity.setNoPickUpDelay();
                    entity.setTarget(player.getUUID());
                }
            }
        }
        addReincarnationDebuffs(player);

        // 执行转生
        PlayerAttributeCapability newCap = new PlayerAttributeCapability();
        PlayerHunhuanAPI.zhuansheng(newCap, oldCap, player);
        oldCap.deserializeNBT(newCap.serializeNBT());
        SynsAPI.synsPlayerAttribute(player);
        player.removeEffect(MobEffects.GLOWING);
        player.sendSystemMessage(Component.literal("转生成功！").withStyle(ChatFormatting.LIGHT_PURPLE));
        AchievementAPI.onReincarnate(player);
    }

    private static void addReincarnationDebuffs(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2, false, true, true));
    }
}
