package cn.edu.fudan.se.multidependency.utils;

import java.text.DecimalFormat;

public class DataUtil {
    public static double toFixed(double value) {
        return Double.parseDouble(String.format("%.2f", value));
    }
    public static float toFixed(float value) {
        return Float.parseFloat(String.format("%.2f", value));
    }
}
