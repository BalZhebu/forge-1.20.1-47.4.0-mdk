package com.TovidY.kunluncontinent.item.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

    // 1. 配置类：管理材质基础数值
    private record ArmorSetProperty(
            int tier,               // 材质等级
            float hpBonusPct,       // 生命加成比例
            float defBonusPct,      // 防御加成比例
            float regenValue,       // 生命恢复数值
            String hpShowText,      // 文本显示的生命百分比
            String defShowText,     // 文本显示的防御百分比
            boolean hasFireRes      // 是否有火抗
    ) {}

    // 2. 材质属性配置 Map
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

    /**
     * 判断装备是否已破损（耐久 <= 1）
     */
    public static boolean isBroken(ItemStack stack) {
        if (stack.isEmpty()) return true;
        return (stack.getMaxDamage() - stack.getDamageValue()) <= 1;
    }

    public int getDurabilityValue(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
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

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == this.getEquipmentSlot() && isBroken(stack)) {
            return HashMultimap.create();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    public float setMaxshengming(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return getDurabilityValue(stack) * 0.02f;
    }

    public float setWufang(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return getDurabilityValue(stack) * 0.01f;
    }

    public float setMaxshengmingTaozhuang(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return value * getProperty().hpBonusPct();
    }

    public float setWufangTaozhuang(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return value * getProperty().defBonusPct();
    }

    public float setShengminghuifuTaozhuang(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return getProperty().regenValue();
    }

    // ==================== 最低等级套装获取（带破损过滤） ====================

    public static ItemStack getLowTaozhuang(Iterable<ItemStack> armorSlots) {
        ItemStack lowestStack = ItemStack.EMPTY;
        int minTier = 7;
        for (ItemStack slot : armorSlots) {
            // 如果槽位为空，或者不是本模组防具，或者防具【已破损】，则判定套装不生效
            if (slot.isEmpty() || !(slot.getItem() instanceof ModArmorBaseItem armorItem) || isBroken(slot)) {
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

    // ==================== 药水套装 Buff 触发（带破损过滤） ====================

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;

        Map<ArmorMaterial, Integer> materialCount = new HashMap<>();
        for (ItemStack stack : player.getArmorSlots()) {
            // 只有未破损的装备才计入套装件数
            if (stack.getItem() instanceof ModArmorBaseItem armorItem && !isBroken(stack)) {
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

    // ==================== Hover Tooltip 文本显示 ====================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("套装描述").withStyle(ChatFormatting.DARK_AQUA));

        if (isBroken(stack)) {
            list.add(Component.translatable("已破损（属性失效）").withStyle(ChatFormatting.DARK_RED));
            return;
        }

        int currentDurability = getDurabilityValue(stack);
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