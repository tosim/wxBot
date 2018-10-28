package com.tosim.wxbot.utils;

public class SystemUtil {

    public static final int OS_LINUX = 1;
    public static final int OS_MAC = 2;
    public static final int OS_WINDOWS = 3;

    public static boolean IS_OS_LINUX = false;
    public static boolean IS_OS_MAC = false;
    public static boolean IS_OS_WINDOWS = false;

    static {
        String currentOs = System.getProperty("os.name").toLowerCase();
        if (currentOs.indexOf("linux") >= 0) {
            IS_OS_LINUX = true;
        } else if (currentOs.indexOf("windows") >= 0) {
            IS_OS_WINDOWS = true;
        } else {
            IS_OS_MAC = true;
        }
    }
}
