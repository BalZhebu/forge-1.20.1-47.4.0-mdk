package com.TovidY.kunluncontinent.item.klitem; // 请确保包名与你项目一致

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DanYaoItem extends Item {

    private int tier = 1;

    // --- 属性字段 ---
    private float shengming, maxshengming, shengmingbaifenbi;
    private float wugong, wufang, baojishanghai, baojilv;
    private float zhenshang, kangbao, xixue, mingzhong, shanbi;
    private float jingyan, jingshenli, jingshenlibaifenbi, maxjingshenli;
    private float tupochenggonggailv;

    private int minLevel;
    private int maxused;

    public DanYaoItem(Properties properties) {
        super(properties);
    }

    public DanYaoItem setTier(int tier) {
        this.tier = tier;
        return this;
    }

    public int getTier() {
        return this.tier;
    }

    public enum Quality {
        damaged("丹渣", 0.0f, ChatFormatting.DARK_GRAY),
        green("药散", 0.3f, ChatFormatting.GREEN),
        blue("药丹", 0.5f, ChatFormatting.BLUE),
        purple("灵丹", 1.0f, ChatFormatting.DARK_PURPLE),
        gold("宝丹", 1.5f, ChatFormatting.GOLD),
        red("仙丹", 2.0f, ChatFormatting.RED);
        public final String label;
        public final float multiplier;
        public final ChatFormatting color;
        Quality(String label, float multiplier, ChatFormatting color) {
            this.label = label;
            this.multiplier = multiplier;
            this.color = color;
        }
        public static Quality get(ItemStack stack) {
            if (stack.hasTag() && stack.getTag().contains("DanYaoQuality")) {
                int index = stack.getTag().getInt("DanYaoQuality");
                return Quality.values()[Math.min(index, Quality.values().length - 1)];
            }
            return purple; // 默认紫色 100%
        }
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Quality q = Quality.get(itemstack);
        if (q == Quality.damaged) {
            if (!level.isClientSide) player.sendSystemMessage(Component.literal("这颗丹药已经碎成渣了，无法服用...").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(itemstack);
        }
        if (!player.getAbilities().instabuild) {
            int playerLevel = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(cap -> (int)cap.getDengji()).orElse(0);
            if (playerLevel < this.minLevel) {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.literal("您的境界不够（当前:" + playerLevel + "级，需求:" + this.minLevel + "级），无法服用此丹药").withStyle(ChatFormatting.RED));
                }
                return InteractionResultHolder.fail(itemstack);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            int usedCount = serverPlayer.getStats().getValue(Stats.ITEM_USED.get(this));
            if (this.maxused <= 0 || usedCount < this.maxused) {
                serverPlayer.awardStat(Stats.ITEM_USED.get(this));
                applyDanyaoAttribute(itemStack, serverPlayer);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("已达到该丹药服用上限！").withStyle(ChatFormatting.YELLOW));
            }
        }

        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return itemStack;
    }

    private void applyDanyaoAttribute(ItemStack stack, ServerPlayer player) {
        DanYaoQuality q = DanYaoQuality.getFromStack(stack);
        float m = q.multiplier;

        if (q == DanYaoQuality.PO_SUI) {
            return;
        }

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {

            if (this.wugong > 0) PlayerHunhuanAPI.addGongji(player, this.wugong * m);
            if (this.wufang > 0) PlayerHunhuanAPI.addFangyu(player, this.wufang * m);
            if (this.shengming > 0) PlayerHunhuanAPI.addShengming(player, this.shengming * m);
            if (this.maxshengming > 0) PlayerHunhuanAPI.addMaxshengming(player, this.maxshengming * m);
            if (this.jingyan > 0) PlayerHunhuanAPI.addJingyan(player, this.jingyan * m);
            if (this.jingshenli > 0) PlayerHunhuanAPI.addJingshenli(player, this.jingshenli * m);
            if (this.maxjingshenli > 0) PlayerHunhuanAPI.addMaxJingshenli(player, this.maxjingshenli * m);
            if (this.baojilv > 0) PlayerHunhuanAPI.addBaojilv(player, this.baojilv * m);
            if (this.baojishanghai > 0) PlayerHunhuanAPI.addBaojishanhai(player, this.baojishanghai * m);
            if (this.kangbao > 0) PlayerHunhuanAPI.addKangbao(player, this.kangbao * m);
            if (this.xixue > 0) PlayerHunhuanAPI.addXixue(player, this.xixue * m);
            if (this.mingzhong > 0) PlayerHunhuanAPI.addMingzhong(player, this.mingzhong * m);
            if (this.shanbi > 0) PlayerHunhuanAPI.addShanbi(player, this.shanbi * m);
            if (this.tupochenggonggailv > 0) PlayerHunhuanAPI.addTupochenggonggailv(player, this.tupochenggonggailv * m);

            if (this.shengmingbaifenbi > 0) {
                float addedHealth = player.getMaxHealth() * (this.shengmingbaifenbi * m) / 100f;
                PlayerHunhuanAPI.addShengming(player, addedHealth);
            }
            if (this.jingshenlibaifenbi > 0) {
                float addedMana = ModAttributeAPI.getMaxjingshenli(player) * (this.jingshenlibaifenbi * m) / 100f;
                PlayerHunhuanAPI.addJingshenli(player, addedMana);
            }

            SynsAPI.synsPlayerAttribute(player);
        });
    }

    // --- Setters (链式调用) ---
    public DanYaoItem setMinLevel(int l) { this.minLevel = l; return this; }
    public DanYaoItem setMaxused(int m) { this.maxused = m; return this; }
    public DanYaoItem setShengming(float v) { this.shengming = v; return this; }
    public DanYaoItem setMaxshengming(float v) { this.maxshengming = v; return this; }
    public DanYaoItem setWugong(float v) { this.wugong = v; return this; }
    public DanYaoItem setWufang(float v) { this.wufang = v; return this; }
    public DanYaoItem setBaojishanghai(float v) { this.baojishanghai = v; return this; }
    public DanYaoItem setBaojilv(float v) { this.baojilv = v; return this; }
    public DanYaoItem setKangbao(float v) { this.kangbao = v; return this; }
    public DanYaoItem setXixue(float v) { this.xixue = v; return this; }
    public DanYaoItem setMingzhong(float v) { this.mingzhong = v; return this; }
    public DanYaoItem setShanbi(float v) { this.shanbi = v; return this; }
    public DanYaoItem setJingyan(float v) { this.jingyan = v; return this; }
    public DanYaoItem setJingshenli(float v) { this.jingshenli = v; return this; }
    public DanYaoItem setMaxjingshenli(int v) { this.maxjingshenli = v; return this; }
    public DanYaoItem setTupochenggonggailv(float v) { this.tupochenggonggailv = v; return this; }
    public DanYaoItem setShengmingbaifenbi(float v) { this.shengmingbaifenbi = v; return this; }

    public DanYaoItem setJingshenlibaifenbi(float v) { this.jingshenlibaifenbi = v; return this; }

    // --- Tooltip 渲染 ---
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        Quality q = Quality.get(stack);
        float m = q.multiplier;

        if (q == Quality.damaged) {
            list.add(Component.literal("品级：" + q.label).withStyle(q.color).withStyle(ChatFormatting.BOLD));
            list.add(Component.literal("这只是一堆毫无药效的残渣。").withStyle(ChatFormatting.DARK_GRAY));
            list.add(Component.literal("可以被分解成丹渣").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        list.add(Component.literal("品级：" + q.label).withStyle(q.color).withStyle(ChatFormatting.BOLD));
        list.add(Component.literal("药效发挥：" + (int)(m * 100) + "%").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("------------------").withStyle(ChatFormatting.DARK_GRAY));

        if (this.wugong > 0) list.add(Component.literal("攻击力: +" + String.format("%.1f", this.wugong * m)).withStyle(ChatFormatting.YELLOW));
        if (this.wufang > 0) list.add(Component.literal("防御力: +" + String.format("%.1f", this.wufang * m)).withStyle(ChatFormatting.YELLOW));
        if (this.jingyan > 0) list.add(Component.literal("经验加成: +" + String.format("%.1f", this.jingyan * m)).withStyle(ChatFormatting.GOLD));
        if (this.shengming > 0) list.add(Component.literal("生命恢复: +" + String.format("%.1f", this.shengming * m)).withStyle(ChatFormatting.GREEN));
        if (this.maxshengming > 0) list.add(Component.literal("最大生命: +" + String.format("%.1f", this.maxshengming * m)).withStyle(ChatFormatting.DARK_GREEN));
        if (this.jingshenli > 0) list.add(Component.literal("精神力: +" + String.format("%.1f", this.jingshenli * m)).withStyle(ChatFormatting.AQUA));
        if (this.baojilv > 0) list.add(Component.literal("暴击率: +" + String.format("%.1f", this.baojilv * m) + "%").withStyle(ChatFormatting.RED));

        if (this.jingshenlibaifenbi > 0) list.add(Component.literal("增加精神力: +" + String.format("%.1f", this.jingshenlibaifenbi * m) + "%").withStyle(ChatFormatting.AQUA));

        if (this.maxjingshenli > 0) list.add(Component.literal("最大精神力: +" + String.format("%.1f", this.maxjingshenli * m)).withStyle(ChatFormatting.DARK_AQUA));

        // 分隔线
        if (this.minLevel > 0) {
            list.add(Component.literal(" "));
            list.add(Component.literal("服用要求：等级 " + this.minLevel).withStyle(ChatFormatting.DARK_RED));
        }
    }
}