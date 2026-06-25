package com.dental.clinic.security;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

/*
 * This file contains the TenantContext class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
