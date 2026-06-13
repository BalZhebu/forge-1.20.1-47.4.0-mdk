package com.TovidY.kunluncontinent.tower.floor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTimerManager {
    private static volatile int remainingSeconds = 0;
    private static volatile boolean shouldShow = false;

    public static void setTimer(int seconds, boolean show) {
        remainingSeconds = seconds;
        shouldShow = show;
    }

    public static int getRemainingSeconds() { return remainingSeconds; }
    public static boolean shouldShow() { return shouldShow; }
}