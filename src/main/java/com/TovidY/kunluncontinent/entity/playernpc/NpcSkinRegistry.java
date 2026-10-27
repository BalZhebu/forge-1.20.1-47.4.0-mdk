package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * 获取总皮肤数量（用于循环遍历 / 命令补全）。
     *
     * <p>⚠️ 只有在 id 是 {@code 0 ~ count-1} 连续时，才能拿它当"随机上限"用 ——
     * 随机请用 {@link #getRandomSkinIndex(net.minecraft.util.RandomSource)}，
     * 否则以后中间删掉一个 id 就会随机到不存在的皮肤。</p>
     */
    public static int getSkinCount() {
        return Math.max(1, SKIN_MAP.size());
    }

    /** 这个 id 是否真的注册过（读档时用来挡脏数据）。 */
    public static boolean isValidIndex(int index) {
        return SKIN_MAP.containsKey(index);
    }

    /**
     * 从已注册的 id 里随机挑一个。
     *
     * <p>不依赖 id 连续：从 {@link #SKIN_MAP} 的 key 里取，以后增删皮肤不会踩空。</p>
     */
    public static int getRandomSkinIndex(net.minecraft.util.RandomSource random) {
        if (SKIN_MAP.isEmpty()) {
            return 0;
        }
        List<Integer> ids = new ArrayList<>(SKIN_MAP.keySet());
        return ids.get(random.nextInt(ids.size()));
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