package com.SmartHire.shared.utils;

/**
 * 线程本地存储工具类
 *
 * @author SmartHire Team
 */
public class ThreadLocalUtil {
    // 线程本地存储
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    // 获取线程本地存储的值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }

    // 设置线程本地存储的值
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }

    // 移除线程本地存储的值，防止线程内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
