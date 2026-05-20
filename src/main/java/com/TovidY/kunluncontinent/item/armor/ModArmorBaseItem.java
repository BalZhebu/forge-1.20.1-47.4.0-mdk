package com.TovidY.kunluncontinent.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//装备属性
public class ModArmorBaseItem extends ArmorItem {

    // 1. 定义一个配置类，把每种材质的所有数值集中管理
    private record ArmorSetProperty(
            int tier,               // 材质等级/代号
            float hpBonusPct,       // 生命加成比例
            float defBonusPct,      // 防御加成比例
            float regenValue,       // 生命恢复数值
            String hpShowText,      // 文本显示的生命百分比
            String defShowText,     // 文本显示的防御百分比
            boolean hasFireRes      // 是否有火抗
    ) {}

    // 2. 用一个只读的 Map 代替所有的 if-else 判断
    private static final Map<ArmorMaterial, ArmorSetProperty> SET_PROPERTIES = Map.of(
            ModArmorMaterials.GRAY_IRON,              new ArmorSetProperty(1, 0.10f, 0.20f, 4f,  "10%", "20%",  false),
            ModArmorMaterials.CLOUD_PATTERNED_BRONZE, new ArmorSetProperty(2, 0.15f, 0.30f, 8f,  "15%", "30%",  false),
            ModArmorMaterials.RED_FIRE,               new ArmorSetProperty(3, 0.20f, 0.40f, 12f, "20%", "40%",  true),
            ModArmorMaterials.SUNKEN_SILVER,          new ArmorSetProperty(4, 0.35f, 0.60f, 16f, "35%", "60%",  false),
            ModArmorMaterials.COLD_HEARTED_STEEL,     new ArmorSetProperty(5, 0.50f, 0.80f, 22f, "50%", "80%",  false),
            ModArmorMaterials.RINSEI,                 new ArmorSetProperty(6, 0.60f, 1.20f, 28f, "60%", "100%", false)
    );

    public ModArmorBaseItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    private ArmorSetProperty getProperty() {
        return SET_PROPERTIES.getOrDefault(this.material, new ArmorSetProperty(0, 0f, 0f, 0f, "0%", "0%", false));
    }

    public static int getMaterialNum(ArmorMaterial material) {
        ArmorSetProperty prop = SET_PROPERTIES.get(material);
        return prop != null ? prop.tier() : 0;
    }

    public static ItemStack getLowTaozhuang(Iterable<ItemStack> armorSlots) {
        ItemStack lowestStack = ItemStack.EMPTY;
        int minTier = 7;
        for (ItemStack slot : armorSlots) {
            if (slot.isEmpty() || !(slot.getItem() instanceof ModArmorBaseItem armorItem)) {
                return ItemStack.EMPTY;
            }
            int currentTier = getMaterialNum(armorItem.getMaterial());
            if (currentTier < minTier) {
                minTier = currentTier;
                lowestStack = slot;
            }
        }
        return lowestStack;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue() - 1;
        int tier = getMaterialNum(this.material);
        int threshold = (int) (stack.getMaxDamage() * tier * tier * 0.05f);

        int finalAmount = (amount > threshold) ? (int) Math.sqrt(amount) : amount;
        finalAmount = (int) Math.sqrt(finalAmount);

        return Math.min(Math.min(remainingDurability, finalAmount), 200);
    }

    private int getDurabilityValue(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public float setMaxshengming(ItemStack stack, float value) {
        return getDurabilityValue(stack) * 0.02f;
    }

    public float setWufang(ItemStack stack, float value) {
        return getDurabilityValue(stack) * 0.01f;
    }

    public float setMaxshengmingTaozhuang(ItemStack stack, float value) {
        return value * getProperty().hpBonusPct();
    }

    public float setWufangTaozhuang(ItemStack stack, float value) {
        return value * getProperty().defBonusPct();
    }

    public float setShengminghuifuTaozhuang(ItemStack stack, float value) {
        return getProperty().regenValue();
    }

    // 5. 药水套装效果触发
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;
        Map<ArmorMaterial, Integer> materialCount = new HashMap<>();
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof ModArmorBaseItem armorItem) {
                materialCount.merge(armorItem.getMaterial(), 1, Integer::sum);
            }
        }
        materialCount.forEach((material, count) -> {
            if (count == 4) {
                ArmorSetProperty prop = SET_PROPERTIES.get(material);
                if (prop != null && prop.hasFireRes()) {
                    player.removeEffect(MobEffects.FIRE_RESISTANCE);
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
                }
            }
        });
    }

    // 6. 极致精简的物品信息文本显示（通过查表让代码量缩减了 80%）
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("套装描述").withStyle(ChatFormatting.DARK_AQUA));

        int currentDurability = getDurabilityValue(stack);
        if (currentDurability <= 1) {
            list.add(Component.translatable("已破损").withStyle(ChatFormatting.DARK_RED));
            return;
        }

        list.add(Component.translatable("最大生命", (int)(currentDurability * 0.02f)).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("防御力", (int)(currentDurability * 0.01f)).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("套装效果").withStyle(ChatFormatting.LIGHT_PURPLE));

        ArmorSetProperty prop = getProperty();
        if (prop.tier() > 0) {
            list.add(Component.translatable("最大生命", prop.hpShowText()).withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力", prop.defShowText()).withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复", String.valueOf((int)prop.regenValue())).withStyle(ChatFormatting.AQUA));
            if (prop.hasFireRes()) {
                list.add(Component.translatable("火焰抗性").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    static {
        MinecraftForge.EVENT_BUS.register(ModArmorBaseItem.class);
    }
}