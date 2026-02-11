package com.TovidY.kunluncontinent.item;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DanYaoItem extends Item {

    private float shengming;
    private float maxshengming;
    private float shengmingbaifenbi;
    private float wugong;
    private float wufang;
    private float baojishanghai;
    private float baojilv;
    private float zhenshang;
    private float kangbao;
    private float xixue;
    private float mingzhong;
    private float shanbi;
    private float jingyan;
    private float jingshenli;
    private float jingshenlibaifenbi;
    private float maxjingshenli;
    private float tupochenggonggailv;


    private int minLevel;
    private int maxused;
    public DanYaoItem(Properties properties) {
        super(properties);
    }

    public DanYaoItem setMinLevel(int level) {
        this.minLevel = level;
        return this;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            AtomicInteger playerLevel = new AtomicInteger(0);
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                playerLevel.set((int) cap.getDengji());
            });
            if (playerLevel.get() < this.minLevel) {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.literal("您的境界不够（当前:" + playerLevel.get() + "级，需求:" + this.minLevel + "级），无法服用此丹药")
                            .withStyle(ChatFormatting.RED));
                }
                return InteractionResultHolder.fail(itemstack);
            }
        }
        return super.use(level, player, hand);
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {

        int i;
        if(livingEntity instanceof ServerPlayer serverPlayer){
            ServerStatsCounter stats = serverPlayer.getStats();
             i = stats.getValue(Stats.ITEM_USED.get(itemStack.getItem()));

//            stats.setValue(serverPlayer,Stats.ITEM_USED.get(itemStack.getItem()),99);

             if(this.getMaxused()<=0 || i<this.getMaxused()){
                 serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                 addDanyaoAttribute(itemStack,level,livingEntity);
             }else {
                 serverPlayer.sendSystemMessage(Component.translatable("已达到服用上限"));
             }
        }

        if (livingEntity instanceof Player && !((Player)livingEntity).getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return itemStack;
    }

    private void addDanyaoAttribute(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if(itemStack.getItem() instanceof DanYaoItem danYaoItem && livingEntity instanceof ServerPlayer player){
            livingEntity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if(danYaoItem.baojilv>0){
                    PlayerHunhuanAPI.addBaojilv(player,danYaoItem.baojilv);
                }
                if(danYaoItem.baojishanghai>0){
                    PlayerHunhuanAPI.addBaojishanhai(player,danYaoItem.baojishanghai);
                }
                if(danYaoItem.jingyan>0){
                    PlayerHunhuanAPI.addJingyan(player,danYaoItem.jingyan);
                }
                if(danYaoItem.kangbao>0){
                    PlayerHunhuanAPI.addKangbao(player,danYaoItem.kangbao);
                }
                if(danYaoItem.shanbi>0){
                    PlayerHunhuanAPI.addShanbi(player,danYaoItem.shanbi);
                }
                if(danYaoItem.jingshenli>0){
                    PlayerHunhuanAPI.addJingshenli(player,danYaoItem.jingshenli);
                }
                if(danYaoItem.maxjingshenli>0){
                    PlayerHunhuanAPI.addMaxJingshenli(player,danYaoItem.maxjingshenli);
                }
                if(danYaoItem.maxshengming>0){
                    PlayerHunhuanAPI.addMaxshengming(player,danYaoItem.maxshengming);
                }
                if(danYaoItem.mingzhong >0){
                    PlayerHunhuanAPI.addMingzhong(player,danYaoItem.mingzhong);
                }
                if(danYaoItem.shengming >0){
                    PlayerHunhuanAPI.addShengming(player,danYaoItem.shengming);
                }
                if(danYaoItem.tupochenggonggailv >0){
                    PlayerHunhuanAPI.addTupochenggonggailv(player,danYaoItem.tupochenggonggailv);
                }
                if(danYaoItem.wufang >0){
                    PlayerHunhuanAPI.addFangyu(player,danYaoItem.wufang);
                }
                if(danYaoItem.wugong >0){
                    PlayerHunhuanAPI.addGongji(player,danYaoItem.wugong);
                }
                if(danYaoItem.xixue >0){
                    PlayerHunhuanAPI.addXixue(player,danYaoItem.xixue);
                }
                if(danYaoItem.shengmingbaifenbi >0){
                    PlayerHunhuanAPI.addShengming(player,player.getMaxHealth()*danYaoItem.shengmingbaifenbi /100f);
                }
                if(danYaoItem.jingshenlibaifenbi >0){
                    PlayerHunhuanAPI.addJingshenli(player, ModAttributeAPI.getMaxjingshenli(player)* danYaoItem.jingshenlibaifenbi /100f);
                }

            });

            // 同步属性到客户端，确保属性变更即时生效
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute packet = 
                    new com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute(
                        capability.getShengming(), capability.getMaxshengming(), capability.getJingshenli(), capability.getMaxjingshenli(),
                        capability.getMingzhong(), capability.getFangyu(), capability.getGongji(), capability.getBaojilv(), capability.getBaojishanghai(),
                        capability.getXixue(), capability.getShanbi(), capability.getKangbao(), capability.getJingyan(), capability.getDengji(), capability.getMaxjingyan()
                    );
                com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            });
        }

    }

    public float getShengming() {
        return shengming;
    }

    public DanYaoItem setShengming(float shengming) {
        this.shengming = shengming;
        return this;
    }

    public float getMaxshengming() {
        return maxshengming;
    }

    public DanYaoItem setMaxshengming(float maxshengming) {
        this.maxshengming = maxshengming;
        return this;
    }

    public float getWugong() {
        return wugong;
    }

    public DanYaoItem setWugong(float wugong) {
        this.wugong = wugong;
        return this;
    }

    public float getWufang() {
        return wufang;
    }

    public DanYaoItem setWufang(float wufang) {
        this.wufang = wufang;
        return this;
    }

    public float getBaojishanghai() {
        return baojishanghai;
    }

    public DanYaoItem setBaojishanghai(float baojishanghai) {
        this.baojishanghai = baojishanghai;
        return this;
    }

    public float getBaojilv() {
        return baojilv;
    }

    public DanYaoItem setBaojilv(float baojilv) {
        this.baojilv = baojilv;
        return this;
    }

    public float getZhenshang() {
        return zhenshang;
    }

    public DanYaoItem setZhenshang(float zhenshang) {
        this.zhenshang = zhenshang;
        return this;
    }

    public float getKangbao() {
        return kangbao;
    }

    public DanYaoItem setKangbao(float kangbao) {
        this.kangbao = kangbao;
        return this;
    }

    public float getXixue() {
        return xixue;
    }

    public DanYaoItem setXixue(float xixue) {
        this.xixue = xixue;
        return this;
    }
    public float getMingzhong() {
        return mingzhong;
    }

    public DanYaoItem setMingzhong(float mingzhong) {
        this.mingzhong = mingzhong;
        return this;
    }

    public float getShanbi() {
        return shanbi;
    }

    public DanYaoItem setShanbi(float shanbi) {
        this.shanbi = shanbi;
        return this;
    }

    public float getJingyan() {
        return jingyan;
    }

    public DanYaoItem setJingyan(float jingyan) {
        this.jingyan = jingyan;
        return this;
    }

    public float getJingshenli() {
        return jingshenli;
    }

    public DanYaoItem setJingshenli(float jingshenli) {
        this.jingshenli = jingshenli;
        return this;
    }

    public float getTupochenggonggailv() {
        return tupochenggonggailv;
    }

    public DanYaoItem setTupochenggonggailv(float tupochenggonggailv) {
        this.tupochenggonggailv = tupochenggonggailv;
        return this;
    }

    public int getMaxused() {
        return maxused;
    }

    public DanYaoItem setMaxused(int maxused) {
        this.maxused = maxused;
        return this;
    }
    public float getMaxjingshenli() {
        return maxjingshenli;
    }

    public DanYaoItem setMaxjingshenli(int maxjingshenli) {
        this.maxjingshenli = maxjingshenli;
        return this;
    }


    public float getShengmingbaifenbi() {
        return shengmingbaifenbi;
    }

    public DanYaoItem setShengmingbaifenbi(float shengmingbaifenbi) {
        this.shengmingbaifenbi = shengmingbaifenbi;
        return this;
    }


    public float getJingshenlibaifenbi() {
        return jingshenlibaifenbi;
    }

    public DanYaoItem setJingshenlibaifenbi(float jingshenlibaifenbi) {
        this.jingshenlibaifenbi = jingshenlibaifenbi;
        return this;
    }


    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if(this.baojilv>0){
            list.add(Component.translatable("暴击率", (int)this.baojilv).withStyle(ChatFormatting.YELLOW));
        }
        if(this.baojishanghai>0){
            list.add(Component.translatable("暴击伤害", (int)this.baojishanghai).withStyle(ChatFormatting.YELLOW));
        }
        if(this.jingyan>0){
            list.add(Component.translatable("获得经验", (int)this.jingyan).withStyle(ChatFormatting.YELLOW));
        }
        if(this.kangbao>0){
            list.add(Component.translatable("抗暴", (int)this.kangbao).withStyle(ChatFormatting.YELLOW));
        }
        if(this.shanbi>0){
            list.add(Component.translatable("闪避", (int)this.shanbi).withStyle(ChatFormatting.YELLOW));
        }
        if(this.jingshenli>0){
            list.add(Component.translatable("获得精神力", (int)this.jingshenli).withStyle(ChatFormatting.YELLOW));
        }
        if(this.maxjingshenli>0){
            list.add(Component.translatable("获得最大精神力", (int)this.maxjingshenli).withStyle(ChatFormatting.YELLOW));
        }
        if(this.maxshengming>0){
            list.add(Component.translatable("最大生命", (int)this.maxshengming).withStyle(ChatFormatting.YELLOW));
        }
        if(this.mingzhong >0){
            list.add(Component.translatable("命中", (int)this.mingzhong).withStyle(ChatFormatting.YELLOW));
        }
        if(this.shengming >0){
            list.add(Component.translatable("获得生命", (int)this.shengming).withStyle(ChatFormatting.YELLOW));
        }
        if(this.tupochenggonggailv >0){
            list.add(Component.translatable("突破成功率", (int)this.tupochenggonggailv).withStyle(ChatFormatting.YELLOW));
        }
        if(this.wufang >0){
            list.add(Component.translatable("防御力", (int)this.wufang).withStyle(ChatFormatting.YELLOW));
        }
        if(this.wugong >0){
            list.add(Component.translatable("攻击力", (int)this.wugong).withStyle(ChatFormatting.YELLOW));
        }
        if(this.xixue >0){
            list.add(Component.translatable("吸血", (int)this.xixue).withStyle(ChatFormatting.YELLOW));
        }
        if(this.shengmingbaifenbi >0){
            list.add(Component.translatable("获得生命", (int)this.shengmingbaifenbi +"%").withStyle(ChatFormatting.YELLOW));
        }
        if(this.jingshenlibaifenbi >0){
            list.add(Component.translatable("获得精神力", (int)this.jingshenlibaifenbi+"%").withStyle(ChatFormatting.YELLOW));
        }
        if (this.minLevel > 0) {
            list.add(Component.literal("服用要求：")
                    .append(Component.literal("等级 " + this.minLevel))
                    .withStyle(ChatFormatting.RED));
        }
    }
    public int getMinLevel() {
        return minLevel;
    }
}