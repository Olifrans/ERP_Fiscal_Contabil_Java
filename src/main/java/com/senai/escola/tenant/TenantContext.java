package com.senai.escola.tenant;


public final class TenantContext {
    private static final ThreadLocal<Long> TENANT = new ThreadLocal<>();
    public static void set(Long id) { TENANT.set(id); }
    public static Long get() { return TENANT.get(); }
    public static void clear() { TENANT.remove(); }
}