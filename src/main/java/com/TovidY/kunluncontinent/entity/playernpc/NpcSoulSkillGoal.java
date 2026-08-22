package com.TovidY.kunluncontinent.entity.playernpc;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.SkillLibrary;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;
import java.util.List;

public class NpcSoulSkillGoal extends Goal {
    private final PlayerNpcEntity npc;
    private int cooldownTicks;

    public NpcSoulSkillGoal(PlayerNpcEntity npc) {
        this.npc = npc;
        this.cooldownTicks = 100 + this.npc.getRandom().nextInt(101);
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.npc.getTarget();
        if (target == null || !target.isAlive()) {
            this.cooldownTicks = 100 + this.npc.getRandom().nextInt(101);
            return false;
        }
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        boolean success = tryCastRandomSkill();
        if (success) {
            this.cooldownTicks = 160 + this.npc.getRandom().nextInt(141);
        } else {
            this.cooldownTicks = 40;
        }
    }

    private boolean tryCastRandomSkill() {
        if (!(this.npc.level() instanceof ServerLevel serverLevel)) { return false; }
        PlayerAttributeCapability cap = this.npc.getSoulCapability();
        if (cap == null) return false;
        List<String> wuhuns = cap.getWuhunListsname();
        if (wuhuns == null || wuhuns.isEmpty()) return false;
        String mainWuhun = wuhuns.get(0);
        List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(mainWuhun);
        if (rings == null || rings.isEmpty()) return false;
        int availableRings = Math.min(rings.size(), 9);
        int ringIndex = 1 + this.npc.getRandom().nextInt(availableRings);
        BaseSkillItem skill = SkillLibrary.getRandomSkill(mainWuhun, ringIndex, this.npc.getRandom());
        if (skill == null) return false;
        MobAttributeCapability ringCap = rings.get(ringIndex - 1);
        long nianxian = ringCap != null ? ringCap.getNianxian() : 1000L;
        float powerMultiplier = skill.getPowerMultiplier(nianxian);
        float npcGongji = cap.getGongji();
        float finalDamage = npcGongji * skill.getDamageMultiplier() * powerMultiplier;
        skill.executeEffectForNpc(serverLevel, this.npc, powerMultiplier, finalDamage);
        return true;
    }
}