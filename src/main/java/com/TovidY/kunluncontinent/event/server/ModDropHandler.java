package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ModDropHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    public static void tryExtraDrops(LivingEntity entity, Player player) {
        // 1. 海洋生物掉落深海精华 (6% 概率)
        // 使用 isMarine(entity) 自动判断所有属于水生、鱼类、甚至包含海洋关键词的生物
        handleSimpleDrop(entity, 0.06, ModItems.DEEPSEA_JINGHUA.get(), ModDropHandler::isMarine);

        // --- 以后在这里加新的掉落逻辑 ---
        // 例子：击杀僵尸类生物 10% 掉落某个东西
        // handleSimpleDrop(entity, 0.10, Items.DIAMOND, e -> e instanceof Zombie);
    }

    private static void handleSimpleDrop(LivingEntity entity, double chance, Item item, Predicate<LivingEntity> condition) {
        if (condition.test(entity) && RANDOM.nextDouble() < chance) {
            entity.spawnAtLocation(new ItemStack(item));
        }
    }

    private static boolean isMarine(LivingEntity entity) {
        if (entity.getMobType() == MobType.WATER) return true;
        String registryName = entity.getType().toShortString().toLowerCase();
        if (registryName.contains("fish") ||
                registryName.contains("ocean") ||
                registryName.contains("whale") ||
                registryName.contains("shark")) {
            return true;
        }
        return entity.isInWaterOrBubble();
    }

}
