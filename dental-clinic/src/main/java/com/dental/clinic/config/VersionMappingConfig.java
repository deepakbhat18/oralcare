package com.dental.clinic.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public class VersionMappingConfig implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new VersionRequestMappingHandlerMapping();
    }

    private static class VersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

        @Override
        protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
            RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
            if (info != null && AnnotationUtils.findAnnotation(handlerType, RestController.class) != null) {
                Set<String> patterns = info.getDirectPaths();
                if (patterns.isEmpty()) {
                    patterns = info.getPatternValues();
                }

                List<String> newPatterns = new ArrayList<>();
                for (String pattern : patterns) {
                    if (!pattern.startsWith("/v1") && !pattern.startsWith("/v2")) {
                        newPatterns.add(pattern);
                        newPatterns.add("/v1" + pattern);
                        newPatterns.add("/v2" + pattern);
                    } else {
                        newPatterns.add(pattern);
                    }
                }

                if (!newPatterns.isEmpty()) {
                    info = info.mutate()
                            .paths(newPatterns.toArray(new String[0]))
                            .build();
                }
            }
            return info;
        }
    }
}

/*
 * This file contains the VersionMappingConfig class which manages system setup, external integration, security configurations, and bean mapping definitions.
 * Concepts used: Spring config annotations, @Bean initialization, and container lifecycle events.
 */
