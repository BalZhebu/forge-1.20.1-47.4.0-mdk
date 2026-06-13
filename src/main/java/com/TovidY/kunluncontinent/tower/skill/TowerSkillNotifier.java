package com.TovidY.kunluncontinent.tower.skill;

import com.TovidY.kunluncontinent.network.server.S2CSkillNotifyPacket;
import net.minecraft.world.entity.Mob;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class TowerSkillNotifier {
    public static void popSkillText(Mob mob, String text) {
        if (mob == null || mob.level().isClientSide || !mob.isAlive()) return;
        Vec3 pos = new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() + 0.2, mob.getZ());
        int color = 0xFFFF55; // 默认亮黄色
        if (text.contains("§4")) color = 0xFF5555; // 红色（被动）
        if (text.contains("§6")) color = 0xFFAA00; // 金黄色（主动）
        String cleanText = text.replaceAll("§[0-9a-fk-orx]", "");
        NetworkHandler.INSTANCE.send(
                PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(mob.getX(), mob.getY(), mob.getZ(), 64.0D, mob.level().dimension())),
                new S2CSkillNotifyPacket(cleanText, color, pos)
        );
    }
}