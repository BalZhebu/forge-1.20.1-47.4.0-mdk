package com.TovidY.kunluncontinent.capability.hunhuanattributes;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Function;

//魂环储存的辅助方法，用于获取武魂属性的加成
public class HunhuanAttributeHelper {
    public static float getWuhunBonus(Player player, Function<MobAttributeCapability, Float> getter) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(cap -> {
            int activeIndex = cap.getHunhuankuaiguan();
            List<String> wuhunNames = cap.getWuhunListsname();
            // 判断武魂是否开启（假设 -1 为关闭状态）
            if (activeIndex >= 0 && wuhunNames != null && activeIndex < wuhunNames.size()) {
                String activeName = wuhunNames.get(activeIndex);
                List<MobAttributeCapability> hunhuans = cap.getMonsterCapabilityLists().get(activeName);
                if (hunhuans != null) {
                    float total = 0;
                    for (MobAttributeCapability h : hunhuans) {
                        total += getter.apply(h);
                    }
                    return total;
                }
            }
            return 0f;
        }).orElse(0f);
    }
}