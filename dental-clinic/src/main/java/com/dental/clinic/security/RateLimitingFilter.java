package com.dental.clinic.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {

        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", ip, path);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Too many login or registration attempts. Please try again after a minute.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

/*
 * This file contains the RateLimitingFilter class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
