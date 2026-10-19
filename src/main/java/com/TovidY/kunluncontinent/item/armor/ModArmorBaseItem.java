package com.TovidY.kunluncontinent.item.armor;

import com.TovidY.kunluncontinent.KlMain;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//装备属性

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModArmorBaseItem extends ArmorItem {

    // ==================== 单件装备的固定属性 ====================

    /**
     * 一件装备提供的固定加成（最大生命 / 防御力）。
     *
     * <p><b>这是唯一需要改单件数值的地方</b>：数值不随剩余耐久打折，
     * 只有装备<b>破损</b>（剩余耐久 ≤ 1）时才整体归零。</p>
     */
    private record PieceStat(float hp, float def) {}

    // ==================== 材质配置 ====================

    /**
     * 一种材质的全部配置：四件套百分比 + 四个部位的固定单件数值。
     *
     * @param tier          材质等级（越高越耐磨，同时用于「最低档」排序）
     * @param hpBonusPct    四件套：生命加成比例（乘在总生命上）
     * @param defBonusPct   四件套：防御加成比例（乘在总防御上）
     * @param regenValue    四件套：生命恢复数值
     * @param hpShowText    tooltip 显示的四件套生命百分比
     * @param defShowText   tooltip 显示的四件套防御百分比
     * @param hasFireRes    四件套是否附带火焰抗性
     * @param helmet        单件：头盔
     * @param chest         单件：胸甲
     * @param leggings      单件：护腿
     * @param boots         单件：靴子
     */
    private record ArmorSetProperty(
            int tier,
            float hpBonusPct,
            float defBonusPct,
            float regenValue,
            String hpShowText,
            String defShowText,
            boolean hasFireRes,
            PieceStat helmet,
            PieceStat chest,
            PieceStat leggings,
            PieceStat boots
    ) {
        /** 按槽位取单件数值。 */
        PieceStat piece(ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> this.helmet;
                case CHESTPLATE -> this.chest;
                case LEGGINGS -> this.leggings;
                case BOOTS -> this.boots;
                default -> new PieceStat(0f, 0f);
            };
        }
    }

    private static final Map<ArmorMaterial, ArmorSetProperty> SET_PROPERTIES = Map.of(

            ModArmorMaterials.GRAY_IRON,
            new ArmorSetProperty(1, 0.10f, 0.20f, 4f, "10%", "20%", false,
                    new PieceStat(22, 8),      // 头盔
                    new PieceStat(35, 13),     // 胸甲
                    new PieceStat(30, 10),      // 护腿
                    new PieceStat(25, 9)),     // 靴子

            ModArmorMaterials.CLOUD_PATTERNED_BRONZE,
            new ArmorSetProperty(2, 0.15f, 0.30f, 8f, "15%", "30%", false,
                    new PieceStat(40, 15),
                    new PieceStat(55, 23),
                    new PieceStat(48, 19),
                    new PieceStat(45, 14)),

            ModArmorMaterials.RED_FIRE,
            new ArmorSetProperty(3, 0.20f, 0.40f, 12f, "20%", "40%", true,
                    new PieceStat(88, 45),
                    new PieceStat(110, 55),
                    new PieceStat(100, 48),
                    new PieceStat(90, 45)),

            ModArmorMaterials.SUNKEN_SILVER,
            new ArmorSetProperty(4, 0.35f, 0.60f, 16f, "35%", "60%", false,
                    new PieceStat(400, 123),
                    new PieceStat(600, 150),
                    new PieceStat(500, 140),
                    new PieceStat(400, 135)),

            ModArmorMaterials.COLD_HEARTED_STEEL,
            new ArmorSetProperty(5, 0.50f, 0.80f, 22f, "50%", "80%", false,
                    new PieceStat(1142, 850),
                    new PieceStat(2478, 1186),
                    new PieceStat(1987, 985),
                    new PieceStat(1575, 888)),

            ModArmorMaterials.RINSEI,
            new ArmorSetProperty(6, 0.60f, 1.20f, 28f, "60%", "120%", false,
                    new PieceStat(5888, 3245),
                    new PieceStat(6475, 5464),
                    new PieceStat(7045, 4557),
                    new PieceStat(5789, 4122))
    );

    public ModArmorBaseItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    private ArmorSetProperty getProperty() {
        return SET_PROPERTIES.getOrDefault(this.material,
                new ArmorSetProperty(0, 0f, 0f, 0f, "0%", "0%", false,
                        new PieceStat(0f, 0f), new PieceStat(0f, 0f),
                        new PieceStat(0f, 0f), new PieceStat(0f, 0f)));
    }

    public static int getMaterialNum(ArmorMaterial material) {
        ArmorSetProperty prop = SET_PROPERTIES.get(material);
        return prop != null ? prop.tier() : 0;
    }

    /**
     * 判断装备是否已破损（耐久 <= 1）。
     *
     * <p>单件属性只在这里归零 —— 破损前无论耐久剩多少，数值都是 {@code SET_PROPERTIES} 里的固定值。</p>
     */
    public static boolean isBroken(ItemStack stack) {
        if (stack.isEmpty()) return true;
        return (stack.getMaxDamage() - stack.getDamageValue()) <= 1;
    }


    /**
     * 耐久损耗。
     *
     * <h3>旧实现的两个问题</h3>
     * <pre>
     *   int finalAmount = (amount > threshold) ? (int) Math.sqrt(amount) : amount;
     *   finalAmount = (int) Math.sqrt(finalAmount);        // ← 无条件再开方一次
     * </pre>
     * <ol>
     *   <li><b>双重开方</b>：所有磨损都被多压了一次，护甲几乎不会掉耐久；</li>
     *   <li><b>磨损不单调</b>：灰铁（tier 1，胸甲 threshold≈26）时
     *       20 点伤害扣 4、30 点伤害只扣 2 —— <b>挨打越重，甲越不磨损</b>。</li>
     * </ol>
     *
     * <h3>现在的规则</h3>
     * <p>前 {@code threshold} 点磨损照常扣；超出部分按平方根增长。
     * 这样既保留"高级甲能吃更大伤害不爆"的意图，又保证<b>磨损随伤害单调不减</b>。</p>
     */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (amount <= 0) return 0;
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue() - 1;
        if (remainingDurability <= 0) return 0;

        int tier = getMaterialNum(this.material);
        // 免罚额度：tier 越高越大（tier=1 约 5% 最大耐久，tier=6 约 180%）
        int threshold = (int) (stack.getMaxDamage() * tier * tier * 0.05f);

        int finalAmount = amount;
        if (amount > threshold) {
            finalAmount = threshold + (int) Math.sqrt(amount - threshold);
        }
        return Math.min(Math.min(remainingDurability, finalAmount), 200);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == this.getEquipmentSlot() && isBroken(stack)) {
            return HashMultimap.create();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    /**
     * 单件提供的最大生命。
     *
     * <p>读 {@link #SET_PROPERTIES} 里对应部位的固定值，<b>不再按剩余耐久折算</b>；
     * 只有破损（{@link #isBroken}）时归零。</p>
     */
    public float setMaxshengming(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return getProperty().piece(this.getType()).hp();
    }

    /** 单件提供的防御力。口径与 {@link #setMaxshengming} 完全一致。 */
    public float setWufang(ItemStack stack, float value) {
        if (isBroken(stack)) return 0f;
        return getProperty().piece(this.getType()).def();
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

    // ==================== 套装 Buff 触发（带破损过滤） ====================

    /** 套装防火的续期阈值：剩余时间低于该值才补，避免每 tick 刷新。 */
    private static final int FIRE_RES_REFRESH_BELOW = 40;
    /** 一次施加的时长（5 秒），脱下套装后最多 5 秒自然消失。 */
    private static final int FIRE_RES_DURATION = 100;
    /**
     * 四件套的<b>持续效果</b>（目前只有赤火套的火焰抗性）。
     *
     * <h3>旧实现为什么必须改</h3>
     * <p>旧代码每 tick 执行 {@code removeEffect(FIRE_RESISTANCE)} + {@code addEffect(60)}：</p>
     * <ul>
     *   <li><b>会吃掉玩家的长效药水</b>：自己喝的 8 分钟防火被强行压成 3 秒，
     *       只要穿着套装就再也存不住长效防火；</li>
     *   <li><b>每 tick 发包</b>：效果被反复删除/添加，客户端同步包每秒 20 次，且效果图标会闪烁。</li>
     * </ul>
     *
     * <p>现在的做法：<b>只续期、不删除</b>。已有更长/足够长的防火时直接跳过
     * （玩家自己的药水优先），剩余时间不足时才补到 5 秒。</p>
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player player = event.player;

        // 便宜的前置判断：已有的防火还够久就完全不遍历装备
        MobEffectInstance current = player.getEffect(MobEffects.FIRE_RESISTANCE);
        if (current != null && current.getDuration() > FIRE_RES_REFRESH_BELOW) return;

        Map<ArmorMaterial, Integer> materialCount = new HashMap<>();
        for (ItemStack stack : player.getArmorSlots()) {
            // 只有未破损的装备才计入套装件数
            if (stack.getItem() instanceof ModArmorBaseItem armorItem && !isBroken(stack)) {
                materialCount.merge(armorItem.getMaterial(), 1, Integer::sum);
            }
        }

        // 4 个槽位全同材质才算四件套
        if (!materialCount.containsValue(4)) return;

        materialCount.forEach((material, count) -> {
            if (count == 4) {
                ArmorSetProperty prop = SET_PROPERTIES.get(material);
                if (prop != null && prop.hasFireRes()) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE, FIRE_RES_DURATION, 0, false, false, true));
                }
            }
        });
    }

    // ==================== Hover Tooltip 文本显示 ====================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (isBroken(stack)) {
            list.add(Component.literal("§4已破损（全部属性失效）"));
            return;
        }

        // 单件加成：固定值，只有破损才归零（数值见 SET_PROPERTIES 的 PieceStat）
        PieceStat piece = getProperty().piece(this.getType());
        list.add(Component.literal("§3单件加成"));
        list.add(Component.translatable("最大生命", (int) piece.hp()).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("防御力", (int) piece.def()).withStyle(ChatFormatting.YELLOW));

        // 套装加成：必须四件同材质才生效，且按「最低档那一件」的材质结算
        ArmorSetProperty prop = getProperty();
        if (prop.tier() > 0) {
            list.add(Component.literal("§d四件套"));
            list.add(Component.translatable("最大生命", prop.hpShowText()).withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("防御力", prop.defShowText()).withStyle(ChatFormatting.AQUA));
            list.add(Component.translatable("生命恢复", String.valueOf((int) prop.regenValue())).withStyle(ChatFormatting.AQUA));
            if (prop.hasFireRes()) {
                list.add(Component.literal("火焰抗性").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }
}