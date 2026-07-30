package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class NpcSkinRegistry {
    // 存储皮肤配置的内部类
    public record SkinEntry(String name, ResourceLocation texture) {}
    // 存储 ID 与皮肤配置的映射表
    private static final Map<Integer, SkinEntry> SKIN_MAP = new HashMap<>();
    // 史蒂夫默认备用配置
    private static final SkinEntry DEFAULT_ENTRY = new SkinEntry(
            "未知旅人",
            new ResourceLocation("textures/entity/player/wide/steve.png")
    );
    // 在这里配置所有的皮肤与名字对应关系！
    static {
        registerSkin(0, "暮雨", "textures/entity/player_npc/skin_0.png");
        registerSkin(1, "4th", "textures/entity/player_npc/skin_1.png");
        registerSkin(2, "cilm", "textures/entity/player_npc/skin_2.png");
        registerSkin(3, "绿恐龙", "textures/entity/player_npc/skin_3.png");
        registerSkin(4, "suwk122", "textures/entity/player_npc/skin_4.png");
    }

    private static void registerSkin(int id, String name, String texturePath) {
        SKIN_MAP.put(id, new SkinEntry(name, new ResourceLocation(KlMain.MOD_ID, texturePath)));
    }

    /**
     * 获取总皮肤数量（用于实体生成时在 0 ~ 数量-1 之间取随机数）
     */
    public static int getSkinCount() {
        return Math.max(1, SKIN_MAP.size());
    }

    /**
     * 根据索引获取对应的贴图路径
     */
    public static ResourceLocation getTexture(int index) {
        SkinEntry entry = SKIN_MAP.get(index);
        return entry != null ? entry.texture() : DEFAULT_ENTRY.texture();
    }

    /**
     * 根据索引获取对应的名称组件 Component
     */
    public static Component getName(int index) {
        SkinEntry entry = SKIN_MAP.get(index);
        String nameStr = entry != null ? entry.name() : DEFAULT_ENTRY.name();
        return Component.literal(nameStr);
    }
}