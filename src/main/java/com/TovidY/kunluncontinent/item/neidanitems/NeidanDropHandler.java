package com.TovidY.kunluncontinent.item.neidanitems;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NeidanDropHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    public static void tryDropNeidan(LivingEntity entity, MobAttributeCapability cap, Player player) {
        CompoundTag playerData = player.getPersistentData();
        boolean forceDrop = playerData.getBoolean("KL_Neidan_Prob_Cheat");
        if (!forceDrop && RANDOM.nextDouble() > 0.10) return;
        long nianxian = cap.getNianxian();
        int tier = getTier(nianxian);
        if (tier == 0) return;
        NeidanQuality quality;
        if (playerData.contains("KL_Neidan_Quality_Cheat")) {
            try {
                String cheatVal = playerData.getString("KL_Neidan_Quality_Cheat").replace("PIN", "");
                quality = NeidanQuality.valueOf(cheatVal);
            } catch (Exception e) {
                quality = calculateQuality(tier,player);
            }
        } else {
            quality = calculateQuality(tier,player);
        }

        if (quality == null) return;

        dropItem(entity, tier, quality);
    }

    private static int getTier(long nianxian) {
        if (nianxian >= 100000000) return 8;
        if (nianxian >= 10000000) return 7;
        if (nianxian >= 1000000) return 6;
        if (nianxian >= 100000) return 5;
        if (nianxian >= 10000) return 4;
        if (nianxian >= 1000) return 3;
        if (nianxian >= 100) return 2;
        if (nianxian >= 1) return 1;
        return 0;
    }

    private static NeidanQuality calculateQuality(int tier,Player player) {
        CompoundTag playerData = player.getPersistentData();

        if (playerData.contains("KL_Neidan_Quality_Cheat")) {
            String cheat = playerData.getString("KL_Neidan_Quality_Cheat");
            switch (cheat) {
                case "fanpin":   return NeidanQuality.FAN;
                case "liangpin": return NeidanQuality.LIANG;
                case "shangpin": return NeidanQuality.SHANG;
                case "zhenpin":  return NeidanQuality.ZHEN;
                case "juepin":   return NeidanQuality.JUE;
                case "xianpin":  return NeidanQuality.XIAN;
            }
        }

        boolean canDropXian = tier >= 3;

        double xianProb = 0;
        if (tier == 3) xianProb = 0.003;
        else if (tier == 4) xianProb = 0.004;
        else if (tier == 5) xianProb = 0.005;
        if (canDropXian && RANDOM.nextDouble() <= xianProb) {
            return NeidanQuality.XIAN;
        }
        int totalWeight = 0;
        for (NeidanQuality q : NeidanQuality.values()) {
            if (q == NeidanQuality.XIAN) continue;
            totalWeight += q.baseWeight;
        }

        int r = RANDOM.nextInt(totalWeight);
        int current = 0;
        for (NeidanQuality q : NeidanQuality.values()) {
            if (q == NeidanQuality.XIAN) continue;
            current += q.baseWeight;
            if (r < current) return q;
        }
        return NeidanQuality.FAN;
    }

    private static void dropItem(LivingEntity entity, int tier, NeidanQuality quality) {
        Item item = switch (tier) {
            case 1 -> ModItems.NEIDAN1.get();
            case 2 -> ModItems.NEIDAN2.get();
            case 3 -> ModItems.NEIDAN3.get();
            case 4 -> ModItems.NEIDAN4.get();
            case 5 -> ModItems.NEIDAN5.get();
            case 6 -> ModItems.NEIDAN6.get();
            case 7 -> ModItems.NEIDAN7.get();
            case 8 -> ModItems.NEIDAN8.get();
            default -> null;
        };

        if (item != null) {
            ItemStack stack = new ItemStack(item);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("Quality", quality.name());

            entity.spawnAtLocation(stack);
        }
    }
}
