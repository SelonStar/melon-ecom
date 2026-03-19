package com.melonecom.util;

import java.util.Map;

import com.melonecom.constant.JwtClaimsConstant;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {

    // 提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    // 根据键获取值
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    // 存储键值对
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    // 清除ThreadLocal 防止内存泄漏
    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static Map<String, Object> getClaims() {
    return (Map<String, Object>) THREAD_LOCAL.get();
}

    public static Long getUserId() {
    Map<String, Object> claims = getClaims();
    if (claims == null) return null;
    Object idObj = claims.get(JwtClaimsConstant.USER_ID);
    if (idObj == null) return null;
    if (idObj instanceof Number n) return n.longValue();
    return Long.valueOf(idObj.toString());


}
    public static Long getAdminId() {
    Map<String, Object> claims = getClaims();
    if (claims == null) return null;
    Object idObj = claims.get(JwtClaimsConstant.ADMIN_ID);
    if (idObj == null) return null;
    if (idObj instanceof Number n) return n.longValue();
    return Long.valueOf(idObj.toString());
}
}
