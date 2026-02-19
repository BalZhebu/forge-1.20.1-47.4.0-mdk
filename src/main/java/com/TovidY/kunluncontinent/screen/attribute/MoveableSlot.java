package com.TovidY.kunluncontinent.screen.attribute;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;

public class MoveableSlot extends Slot {
    // 缓存反射字段，提升性能
    private static final Field X_FIELD;
    private static final Field Y_FIELD;

    static {
        try {
            X_FIELD = Slot.class.getDeclaredField("x");
            Y_FIELD = Slot.class.getDeclaredField("y");
            X_FIELD.setAccessible(true);
            Y_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("无法初始化槽位反射字段，请联系作者", e);
        }
    }

    public final int savedX;
    public final int savedY;

    public MoveableSlot(Container pContainer, int pIndex, int pX, int pY) {
        super(pContainer, pIndex, pX, pY);
        this.savedX = pX;
        this.savedY = pY;
    }

    public void setPosition(int x, int y) {
        try {
            X_FIELD.set(this, x);
            Y_FIELD.set(this, y);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}