package com.TovidY.kunluncontinent.entity.playernpc;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class LookAtDialogPlayerGoal extends Goal {
    private final PlayerNpcEntity npc;

    public LookAtDialogPlayerGoal(PlayerNpcEntity npc) {
        this.npc = npc;
        // 标记此 Goal 会占用 MOVE（移动）和 LOOK（视角），强行覆盖随机乱跑的 Goal
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * 当 NPC 存在对话玩家（且没处于切磋战斗状态）时激活
     */
    @Override
    public boolean canUse() {
        Player dialogPlayer = this.npc.getDialogPlayer();
        return dialogPlayer != null && dialogPlayer.isAlive() && !this.npc.isSparring();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.npc.getNavigation().stop();
    }

    @Override
    public void tick() {
        Player dialogPlayer = this.npc.getDialogPlayer();
        if (dialogPlayer != null) {
            this.npc.getLookControl().setLookAt(dialogPlayer, 30.0F, 30.0F);
            this.npc.getNavigation().stop();
        }
    }
}
