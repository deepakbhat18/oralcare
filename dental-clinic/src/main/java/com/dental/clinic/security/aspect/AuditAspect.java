package com.dental.clinic.security.aspect;

import com.dental.clinic.model.AuditLog;
import com.dental.clinic.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;










@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(value = "@annotation(audit)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Audit audit, Object result) {
        try {
            String username = resolveUsername();
            String ipAddress = resolveIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(audit.action())
                    .entityType(audit.entityType())
                    .ipAddress(ipAddress)
                    .extraInfo(buildExtraInfo(joinPoint))
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit logged: action={} by={}", audit.action(), username);

        } catch (Exception e) {

            log.error("Audit logging failed (non-fatal): {}", e.getMessage());
        }
    }

    private String resolveUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : req.getRemoteAddr();
        } catch (IllegalStateException e) {
            return "UNKNOWN";
        }
    }

    private String buildExtraInfo(JoinPoint joinPoint) {
        return "method=" + joinPoint.getSignature().getName()
                + " args=" + Arrays.toString(joinPoint.getArgs());
    }
}

/*
 * This file contains the AuditAspect class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
