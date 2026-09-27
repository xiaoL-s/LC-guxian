package com.guxian.mini.util;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> REAL_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> POST_TYPE = new ThreadLocal<>();

    public static void setUserId(Long id) { USER_ID.set(id); }
    public static Long getUserId() { return USER_ID.get(); }
    public static void setRealName(String n) { REAL_NAME.set(n); }
    public static String getRealName() { return REAL_NAME.get(); }
    public static void setPostType(String t) { POST_TYPE.set(t); }
    public static String getPostType() { return POST_TYPE.get(); }

    public static void clear() {
        USER_ID.remove(); REAL_NAME.remove(); POST_TYPE.remove();
    }
}
