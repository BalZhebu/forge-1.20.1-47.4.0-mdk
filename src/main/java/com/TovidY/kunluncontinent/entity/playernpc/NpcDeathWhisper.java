package com.TovidY.kunluncontinent.entity.playernpc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

/**
 * NPC 亡语：<b>只有被玩家亲手击杀时</b>才会喊一句。
 *
 * <p>刻意用原版 ActionBar（{@code displayClientMessage(comp, true)}）而不是自绘 HUD——
 * 一行文字、一行代码、原版自动发包（见 MEMORY 的「UI 提示优先用原版」）。</p>
 */
public final class NpcDeathWhisper {

    private NpcDeathWhisper() {
    }

    private static final Random RANDOM = new Random();

    /**
     * 亡语文案池。<b>按等级分段</b>——高等级 NPC 死得更"不甘"，低等级的更普通。
     * 用户给了 "不，我不甘心！" 和 "怎么可能！！" 两条，其余按风格补齐。
     */
    private static final List<String> LOW = List.of(
            "呵……不过如此。",
            "下次……再来。",
            "我还……没发挥全力。",
            "记住我的名字。",
            "这一局不算。"
    );

    private static final List<String> MID = List.of(
            "不，我不甘心！",
            "怎么可能！！",
            "这种力量……不该属于你！",
            "凭什么！",
            "我不……甘心啊！"
    );

    private static final List<String> HIGH = List.of(
            "不可能……我已登临绝巅！",
            "哈哈哈……连我的魂骨都留不住吗！",
            "千年道行，一朝葬送！可恨！",
            "天不佑我！",
            "此仇……来世必报！"
    );

    /**
     * 播报一次亡语。
     *
     * @param npc     被击杀的 NPC
     * @param killer  击杀者（必须是玩家才会进来）
     */
    public static void speak(LivingEntity npc, ServerPlayer killer) {
        int level = npc instanceof PlayerNpcEntity p
                ? p.getSoulCapability().getDengji()
                : 0;

        List<String> pool = level >= 85 ? HIGH : (level >= 50 ? MID : LOW);
        String line = pool.get(RANDOM.nextInt(pool.size()));

        String npcName = npc.getName().getString();
        Component text = Component.literal("§8[§c亡语§8] §7\"" + npcName + "\" §f" + line);

        // ActionBar：击杀者屏幕上显示一行
        killer.displayClientMessage(text, true);

        // 同时给全场附近的玩家看一眼（亡语是"世界的声音"）
        if (npc.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.players().forEach(p -> {
                if (p != killer && p.distanceToSqr(npc) < 256.0) {
                    p.displayClientMessage(text, true);
                }
            });
        }

        // 亡灵尖啸（1.20.1 没有 WRAITH_* 常量，用 ENDERMAN_SCREAM 代替）+ 死亡音
        Level level0 = npc.level();
        level0.playSound(null, npc.getX(), npc.getY(), npc.getZ(),
                SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 0.6F, 1.5F);
        level0.playSound(null, npc.getX(), npc.getY(), npc.getZ(),
                SoundEvents.PLAYER_DEATH, SoundSource.HOSTILE, 0.4F, 1.2F);
    }
}