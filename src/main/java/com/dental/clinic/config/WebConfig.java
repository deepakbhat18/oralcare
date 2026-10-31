package com.dental.clinic.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

/*
 * This file contains the WebConfig class which manages system setup, external integration, security configurations, and bean mapping definitions.
 * Concepts used: Spring config annotations, @Bean initialization, and container lifecycle events.
 */
