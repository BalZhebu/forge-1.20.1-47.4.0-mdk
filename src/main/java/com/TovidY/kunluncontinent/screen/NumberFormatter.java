package com.TovidY.kunluncontinent.screen;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

// 数值格式化工具类
public class NumberFormatter {
    private static boolean forceShowFullNumber = false;

    /**
     * 检查是否按住Shift键
     */
    private static boolean isShiftPressed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return false;

        long window = mc.getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    /**
     * 设置是否强制显示完整数值（用于测试或其他用途）
     */
    public static void setForceShowFullNumber(boolean force) {
        forceShowFullNumber = force;
    }

    /**
     * 格式化大数字，超过9999时使用万字单位
     * 按住Shift键显示完整数值
     */
    public static String formatNumber(double value) {
        return formatNumber(value, 1);
    }

    public static String formatRange(double current, double max) {
        return formatNumber(current) + "/" + formatNumber(max);
    }

    /**
     * 格式化大数字，超过9999时使用万字单位
     * 按住Shift键显示完整数值
     */
    public static String formatNumber(double value, int decimalPlaces) {
        // 检查是否按住Shift键或强制显示完整数值
        if (forceShowFullNumber || isShiftPressed()) {
            return formatFullNumber(value, decimalPlaces);
        }

        // 正常格式化逻辑
        return formatCompactNumber(value, decimalPlaces);
    }

    /**
     * 格式化完整数值（不缩写）
     */
    private static String formatFullNumber(double value, int decimalPlaces) {
        if (value == (int) value) {
            // 整数，直接返回
            return String.valueOf((int) value);
        } else {
            // 小数，保留指定位数
            return String.format("%." + decimalPlaces + "f", value);
        }
    }

    /**
     * 格式化紧凑数值（使用万字单位缩写）
     */
    private static String formatCompactNumber(double value, int decimalPlaces) {
        // 如果是整数且小于10000，直接返回
        if (value < 10000) {
            if (value == (int) value) {
                return String.valueOf((int) value);
            } else {
                return String.format("%." + decimalPlaces + "f", value);
            }
        }

        // 计算万字单位
        double wanValue = value / 10000.0;

        // 根据数值大小选择合适的小数位数
        if (wanValue >= 10000) {
            // 超过1亿，用亿字单位
            double yiValue = wanValue / 10000.0;
            return formatWithDecimal(yiValue, decimalPlaces) + "亿";
        } else if (wanValue >= 1000) {
            // 超过1000万，显示1位小数
            return String.format("%.1f万", wanValue);
        } else if (wanValue >= 100) {
            // 超过100万，显示1位小数
            return String.format("%.1f万", wanValue);
        } else {
            // 1万-99.9万，根据大小动态调整小数位数
            return formatWithDecimal(wanValue, decimalPlaces) + "万";
        }
    }

    /**
     * 格式化带小数位的数字
     */
    private static String formatWithDecimal(double value, int maxDecimalPlaces) {
        // 如果数值是整数，不显示小数
        if (value == (int) value) {
            return String.valueOf((int) value);
        }

        // 动态调整小数位数：数值越大，小数位数越少
        int actualDecimalPlaces = maxDecimalPlaces;
        if (value >= 1000) {
            actualDecimalPlaces = 0;
        } else if (value >= 100) {
            actualDecimalPlaces = Math.min(maxDecimalPlaces, 1);
        } else if (value >= 10) {
            actualDecimalPlaces = Math.min(maxDecimalPlaces, 2);
        }

        if (actualDecimalPlaces == 0) {
            return String.valueOf((int) Math.round(value));
        } else {
            return String.format("%." + actualDecimalPlaces + "f", value);
        }
    }

    /**
     * 格式化百分比数值（Shift不影响百分比显示）
     */
    public static String formatPercentage(double percentage) {
        // 百分比通常不需要万字单位
        if (percentage == (int) percentage) {
            return String.valueOf((int) percentage);
        } else {
            // 保留1位小数
            return String.format("%.1f", percentage);
        }
    }

    /**
     * 格式化生命值显示（当前/最大）
     */
    public static String formatHealth(double current, double max) {
        String currentStr = formatNumber(current);
        String maxStr = formatNumber(max);
        return currentStr + "/" + maxStr;
    }
}