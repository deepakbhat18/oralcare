package com.dental.clinic.security.aspect;

import java.lang.annotation.*;








@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    String action();

    String entityType() default "";
}

/*
 * This file contains the Audit class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
