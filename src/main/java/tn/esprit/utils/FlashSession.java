package tn.esprit.utils;

public class FlashSession {
    private static String message;
    private static String type;

    public static void setFlash(String msg, String t) {
        message = msg;
        type = t;
    }

    public static String getMessage() {
        return message;
    }

    public static String getType() {
        return type;
    }

    public static void clear() {
        message = null;
        type = null;
    }
}
