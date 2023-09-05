package com.tmobile.pacman.api.commons.utils;

public class ThreadLocalUtil {
    public static final ThreadLocal<Integer> count = new ThreadLocal<>();
    public static final InheritableThreadLocal<String> accessToken = new InheritableThreadLocal<>();
}
