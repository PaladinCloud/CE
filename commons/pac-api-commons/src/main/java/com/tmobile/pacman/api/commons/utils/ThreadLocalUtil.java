package com.tmobile.pacman.api.commons.utils;

import java.util.Set;

public class ThreadLocalUtil {
    public static final ThreadLocal<Integer> count = new ThreadLocal<>();
    public static InheritableThreadLocal<Set<String>> fieldSet = new InheritableThreadLocal<>();
}
