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
    public ModArmorBaseItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static ItemStack getLowTaozhuang(Iterable<ItemStack> armorSlots) {
        ItemStack itemStack = ItemStack.EMPTY;
        int num = 7;
        for (ItemStack armorSlot : armorSlots) {
            if(!armorSlot.isEmpty() && armorSlot.getItem() instanceof ModArmorBaseItem modArmorBaseItem){
                int materialNum = ModArmorBaseItem.getMaterialNum(modArmorBaseItem.getMaterial());
                if(materialNum<num){
                    itemStack = armorSlot;
                    num = materialNum;
                }
            }else {
                return ItemStack.EMPTY;
            }
        }
        return itemStack;
    }

    public static int getMaterialNum(ArmorMaterial material){

        if(material == ModArmorMaterials.GRAY_IRON){
            return 1;
        }
        if(material == ModArmorMaterials.CLOUD_PATTERNED_BRONZE){
            return 2;
        }
        if(material == ModArmorMaterials.RED_FIRE){
            return 3;
        }
        if(material == ModArmorMaterials.SUNKEN_SILVER){
            return 4;
        }
        if(material == ModArmorMaterials.COLD_HEARTED_STEEL){
            return 5;
        }
        return 0;
    }

    @Override
    public  <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        int maxDamage = stack.getMaxDamage();
        int damageValue = stack.getDamageValue();

        int i = maxDamage - damageValue-1;
        int i1 = (int) (stack.getMaxDamage() * ModArmorBaseItem.getMaterialNum(this.material)* ModArmorBaseItem.getMaterialNum(this.material)*0.05f);
        if(i1 <amount){
            i = (int) Math.sqrt(amount);
        }
        amount = (int) Math.sqrt(amount);
        return Math.min(Math.min(i,amount),200);

    }

    public float setMaxshengming(ItemStack stack ,float value) {
        return (stack.getMaxDamage()-stack.getDamageValue())*0.02f;
    }

    public float setMaxshengmingTaozhuang(ItemStack stack, float value) {
        if(this.getMaterial() == ModArmorMaterials.GRAY_IRON){
            return value*0.1f;
        }
        if(this.getMaterial() == ModArmorMaterials.CLOUD_PATTERNED_BRONZE){
            return value*0.15f;
        }
        if(this.getMaterial() == ModArmorMaterials.RED_FIRE){
            return value*0.2f;
        }
        if(this.getMaterial() == ModArmorMaterials.SUNKEN_SILVER){
            return value*0.35f;
        }
        if(this.getMaterial() == ModArmorMaterials.COLD_HEARTED_STEEL){
            return value*0.5f;
        }
        return 0;
    }

    public float setWufang(ItemStack stack, float value) {
        return (stack.getMaxDamage()-stack.getDamageValue())*0.01f;
    }

    public float setWufangTaozhuang(ItemStack stack, float value) {

        if(this.getMaterial() == ModArmorMaterials.GRAY_IRON){
            return value*0.2f;
        }
        if(this.getMaterial() == ModArmorMaterials.CLOUD_PATTERNED_BRONZE){
            return value*0.3f;
        }
        if(this.getMaterial() == ModArmorMaterials.RED_FIRE){
            return value*0.4f;
        }
        if(this.getMaterial() == ModArmorMaterials.SUNKEN_SILVER){
            return value*0.6f;
        }
        if(this.getMaterial() == ModArmorMaterials.COLD_HEARTED_STEEL){
            return value*0.8f;
        }
        return 0;
    }

    public float setShengminghuifuTaozhuang(ItemStack stack, float value) {
        if(this.getMaterial() == ModArmorMaterials.GRAY_IRON){
            return 4;
        }
        if(this.getMaterial() == ModArmorMaterials.CLOUD_PATTERNED_BRONZE){
            return 8;
        }
        if(this.getMaterial() == ModArmorMaterials.RED_FIRE){
            return 12;
        }
        if(this.getMaterial() == ModArmorMaterials.SUNKEN_SILVER){
            return 16;
        }
        if(this.getMaterial() == ModArmorMaterials.COLD_HEARTED_STEEL){
            return 22;
        }
        return 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Iterable<ItemStack> armorSlots = player.getArmorSlots();
        Map<ArmorMaterial, Integer> materialCount = new HashMap<>();
        for (ItemStack stack : armorSlots) {
            if (stack.getItem() instanceof ModArmorBaseItem armorItem) {
                materialCount.merge(armorItem.getMaterial(), 1, Integer::sum);
            }
        }
        materialCount.forEach((material, count) -> {
            if (count == 4) applySetEffects(player, material);
        });
    }

    //备用：套装效果（药水）
    private static void applySetEffects(Player player, ArmorMaterial material) {
        if (material == ModArmorMaterials.RED_FIRE) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
        }
    }

    static {
        MinecraftForge.EVENT_BUS.register(ModArmorBaseItem.class);
    }

    //物品描述
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("套装描述").withStyle(ChatFormatting.DARK_AQUA));
        if(stack.getMaxDamage()-stack.getDamageValue()<=1){
            list.add(Component.translatable("已破损").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        list.add(Component.translatable("最大生命", (int)((stack.getMaxDamage()-stack.getDamageValue())*0.02f)).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("防御力", (int)((stack.getMaxDamage()-stack.getDamageValue())*0.01f)).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("套装效果").withStyle(ChatFormatting.LIGHT_PURPLE));

        if(this.getMaterial() == ModArmorMaterials.GRAY_IRON){
            list.add(Component.translatable("最大生命","10%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力","20%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复","4").withStyle(ChatFormatting.AQUA));
        }
        if(this.getMaterial() == ModArmorMaterials.COLD_HEARTED_STEEL){
            list.add(Component.translatable("最大生命","50%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力","80%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复","22").withStyle(ChatFormatting.AQUA));
        }
        if(this.getMaterial() == ModArmorMaterials.SUNKEN_SILVER){
            list.add(Component.translatable("最大生命","35%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力","60%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复","16").withStyle(ChatFormatting.AQUA));
        }
        if(this.getMaterial() == ModArmorMaterials.CLOUD_PATTERNED_BRONZE){
            list.add(Component.translatable("最大生命","15%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力","30%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复","8").withStyle(ChatFormatting.AQUA));
        }
        if(this.getMaterial() == ModArmorMaterials.RED_FIRE){
            list.add(Component.translatable("最大生命","20%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力","40%").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复","12").withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("火焰抗性").withStyle(ChatFormatting.DARK_RED));
        }
    }


}
