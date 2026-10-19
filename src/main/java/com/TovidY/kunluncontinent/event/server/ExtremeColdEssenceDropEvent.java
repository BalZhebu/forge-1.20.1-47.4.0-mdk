package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 极寒冰域（{@code kunluncontinent:polar_ice_realm}）的<b>全局掉落</b>：
 * 该维度里任何生物死亡都有 {@link #DROP_CHANCE} 的概率额外掉一个「极寒精华」。
 *
 * <h3>为什么用事件而不是 Global Loot Modifier</h3>
 * <p>Forge 的 {@code GlobalLootModifier} 只在<b>实体战利品表被 roll</b> 的时候才会执行
 * （{@code LivingEntity#dropFromLootTable}）。也就是说：</p>
 * <ul>
 *   <li>原版生物（有 {@code entities/zombie} 之类的表）→ 能触发；</li>
 *   <li>本项目新加的、<b>没写战利品表</b>的生物（雪魔 / 冰晶 / 孤竹之外的那些）→ <b>永远不会触发</b>；</li>
 * </ul>
 * <p>而 {@code LivingDropsEvent} 是在 {@code dropAllDeathLoot} 的<b>最后无条件触发</b>的
 * （见 Forge 补丁：{@code ForgeHooks.onLivingDrops}），跟有没有战利品表无关。
 * 用户明确说"不断新增生物，不能一个一个写掉落表"，所以必须走这条。</p>
 *
 * <h3>细节</h3>
 * <ul>
 *   <li>概率固定 8%，<b>不吃抢夺</b>（要改吃抢夺就在 chance 上加 {@code event.getLootingLevel()}）；</li>
 *   <li>玩家死亡不触发（{@link Player} 排除），其余 {@link LivingEntity} 全部适用；</li>
 *   <li>掉落物走 {@code event.getDrops()}，和普通掉落一样有拾取延迟、能被 {@code /kill} 之外的清理逻辑处理。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExtremeColdEssenceDropEvent {

    /** 每次死亡额外掉落的概率。 */
    private static final float DROP_CHANCE = 0.08F;

    private ExtremeColdEssenceDropEvent() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        // 别的模组/机制取消了掉落就不插手
        if (event.isCanceled()) {
            return;
        }
        if (entity instanceof Player) {
            return;
        }
        // 只认极寒冰域这一个维度
        if (!entity.level().dimension().equals(ModDimensions.POLAR_ICE_REALM_LEVEL_KEY)) {
            return;
        }

        if (entity.getRandom().nextFloat() >= DROP_CHANCE) {
            return;
        }

        ItemStack stack = new ItemStack(ModItems.EXTREME_COLD_ESSENCE.get());
        ItemEntity drop = new ItemEntity(entity.level(),
                entity.getX(), entity.getY() + 0.5D, entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }
}
