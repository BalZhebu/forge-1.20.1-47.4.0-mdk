package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.item.armor.ModArmorBaseItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 盔甲四件套成就。
 *
 * <p>没有走 tick 轮询，而是挂 {@link LivingEquipmentChangeEvent}：四件套判定本身在
 * {@code ModArmorBaseItem} 那边是「4 个槽都是本模组装备」，
 * 所以玩家穿上第 4 件的那一刻检查一次就够，之后不会再有状态变化。</p>
 *
 * <p>⚠️ 判定口径与游戏内四件套一致（不看材质是否相同、不看耐久），只认类型，
 * 免得成就和实际加成对不上。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorSetAchievementHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorSetAchievementHandler() {
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 只关心盔甲槽；改动的是主手/副手就不用算了
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        if (isFullSet(player)) {
            AchievementAPI.onArmorSetFull(player);
        }
    }

    private static boolean isFullSet(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(player.getItemBySlot(slot).getItem() instanceof ModArmorBaseItem)) {
                return false;
            }
        }
        return true;
    }
}
