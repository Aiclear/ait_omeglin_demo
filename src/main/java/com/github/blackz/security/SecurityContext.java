package com.github.blackz.security;

/**
 * 上下文安全信息
 *
 * @author xinzheyu
 * @since 2025/12/20 11:03
 */
public final class SecurityContext {

    private static final ThreadLocal<UserInformation> CONTEXT = new InheritableThreadLocal<>();

    public static UserInformation getContext() {
        return CONTEXT.get();
    }

    public static void setContext(UserInformation user) {
        CONTEXT.set(user);
    }

    public static void removeContext() {
        CONTEXT.remove();
    }
}
