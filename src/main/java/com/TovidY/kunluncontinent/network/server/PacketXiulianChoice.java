package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.capability.playerattributes.XiulianLogic;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// PacketXiulianChoice.java

public class PacketXiulianChoice {
    private final int action; // 0: 挂机打坐, 1: 一键跳过

    public PacketXiulianChoice(int action) {
        this.action = action;
    }

    public static void encode(PacketXiulianChoice msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.action);
    }

    public static PacketXiulianChoice decode(FriendlyByteBuf buffer) {
        return new PacketXiulianChoice(buffer.readInt());
    }

    public static void handle(PacketXiulianChoice msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (msg.action == 0) {
                    int remainMin = (int) Math.ceil(cap.getXiulianTime() / 60.0f);
                    player.sendSystemMessage(Component.literal("§e你选择静心打坐，预计剩余时间：" + remainMin + " 分钟"));
                } else if (msg.action == 1) {
                    // 防作弊校验：选择界面只会在坐上蒲团后打开，但这个包本身可以被任意客户端伪造 ——
                    // 不校验的话，玩家站在副本里/刷怪时发包就能凭空白嫖经验 + 瞬间回满精神力。
                    if (player.getVehicle() == null
                            || !player.getVehicle().getTags().contains("putuan_seat")) {
                        player.sendSystemMessage(Component.literal("§c请先坐上蒲团再进行闭关！"));
                        return;
                    }
                    processSkipXiulian(player, cap);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    /** 安全上限，防止极端数值下 while 失控。 */
    private static final int MAX_UPGRADE_LOOP = 200;

    private static void processSkipXiulian(ServerPlayer player, PlayerAttributeCapability cap) {
        int remainingSeconds = cap.getXiulianTime();
        if (remainingSeconds <= 0) return;

        int startLevel = cap.getDengji();

        // ⚠️ 精神力上限必须走 ModAttributeAPI（**含加点加成**）。
        //    旧代码用的是 cap.getMaxjingshenli() —— 那是裸字段，不含「凝神」加点。
        //    玩家加过点时裸上限 < 面板上限，于是「跳过修炼」把精神力写成裸上限，
        //    表现为**精神力反而被扣**（例：面板上限 6000、裸上限 3000，
        //    当前 4800 点满 → 跳过后变成 3000，凭空掉 30%）。
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
        if (maxJingshenli <= 0.0f) maxJingshenli = cap.getMaxjingshenli();

        // ⭐ 经验公式与蒲团挂机**完全共用** XiulianLogic。
        //    旧代码在这里另写了一份 `lvl<=30?5 : lvl<=89?7 : 10`（89 还是笔误，
        //    原分界是 70），导致「跳过」比「站着挂」多给 27%~70% 经验。
        //
        // ⭐⭐ 逐级模拟「真的挂完这段秒数」：
        //    旧实现是 `expFor(startLevel, remainingSeconds, maxExp(startLevel))` ——
        //    全程按【起始等级】的费率折算。但挂机的费率是每秒重算的：
        //    升级后 minutesPerLevel 变长、maxExp 变大 → 每秒经验变少。
        //    起始等级的费率是全程最高的，等于闭关把整段时间按"最贵汇率"折算，
        //    低等级升级快时，「一键闭关」比「真挂同样时长」凭空多拿 30%+ 经验。
        //    现在改成：每一级先算"升满这一级还要几秒"，时间够就升级、用新等级的新费率继续；
        //    时间不够就把剩余秒数按当前费率折成经验填进条里 —— 与真挂机严格等价。
        float secondsLeft = remainingSeconds;

        for (int loop = 0; loop < MAX_UPGRADE_LOOP && secondsLeft > 0.0f; loop++) {
            float maxExp = cap.getMaxjingyan();
            if (maxExp <= 0.0f) break;

            float expPerSecond = maxExp / (XiulianLogic.minutesPerLevel(cap.getDengji()) * 60f);
            float needExp = maxExp - cap.getJingyan();
            if (expPerSecond <= 0.0f) break;

            float secondsNeeded = needExp / expPerSecond;
            float jingyanBefore = cap.getJingyan();
            int levelBefore = cap.getDengji();

            if (secondsLeft >= secondsNeeded) {
                // 这一级的时间够了：填满经验条并尝试升级
                secondsLeft -= secondsNeeded;
                cap.setJingyan(maxExp);
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);

                if (cap.getDengji() != levelBefore) {
                    // 升级成功 → 新等级的费率/门槛变大，循环继续
                    continue;
                }
                if (cap.getJingyan() == jingyanBefore) {
                    // 经验与等级都没动 = 被挡住（99 级满级 / 需封神）→ 剩余时间没地方花，收手
                    break;
                }
                // 突破失败：经验被罚掉 80%、成功率 +1% —— 剩余秒数继续按新状态模拟
                //（真实挂机里也是每秒重试一次，行为一致）
            } else {
                // 剩下的时间不够升一级，按当前费率折成经验填进条里
                cap.setJingyan(Math.min(maxExp, cap.getJingyan() + expPerSecond * secondsLeft));
                secondsLeft = 0.0f;
                break;
            }
        }

        // 修炼时间直接扣光
        cap.setXiulianTime(0);
        // 精神力充满（用面板口径，见上面的注释）
        cap.setJingshenli(maxJingshenli);

        // ⚠️ 与自然耗尽对齐：一键闭关同样进入 3 分钟冷却。
        //    旧实现漏了这一行 —— 自然耗尽的玩家冷却期间时间恢复慢 3 倍，
        //    一键闭关的玩家却全速恢复，「跳过」反而比「挂到结束」更划算。
        player.getPersistentData().putInt("XiulianCooldown", XiulianLogic.COOLDOWN_TICKS);

        cap.setUsingAll(true);
        SynsAPI.synsPlayerAttribute(player);
        player.stopRiding();

        int endLevel = cap.getDengji();
        player.sendSystemMessage(Component.literal("§a你消耗了所有修炼时间，修仙等级从 §e"
                + startLevel + "§a 级提升至 §e" + endLevel + "§a 级！精神力已完全充盈！"));
    }
}