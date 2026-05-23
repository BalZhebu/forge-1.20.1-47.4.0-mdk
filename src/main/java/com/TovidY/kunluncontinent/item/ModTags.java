package com.TovidY.kunluncontinent.item;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {
        // 如果以后有需要方块标签，写在这里
        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(KlMain.MOD_ID, name));
        }
    }

    public static class Items {
        // 这就是你刚才报错缺少的那个常量
        public static final TagKey<Item> DANYAO_DROSS = tag("danyao_dross");
        public static final TagKey<Item> ENGRAVING_KNIFE = tag("engraving_knife");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(KlMain.MOD_ID, name));
        }
    }
}
