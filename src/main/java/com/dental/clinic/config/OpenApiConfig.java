package com.dental.clinic.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI dentalClinicOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Dental Clinic Management System API")
                        .description("""
                                ## Dental Clinic Management System

                                A comprehensive REST API for managing dental clinic operations including:
                                - **Authentication & Authorization** (JWT-based with RBAC)
                                - **Patient Management** (registration, profiles, medical history)
                                - **Appointment Booking** (scheduling, status tracking)
                                - **Treatment Plans** (multi-session plans, progress tracking)
                                - **Billing & Prescriptions**
                                - **Reports & Document Management**
                                - **Doctor Notes**
                                - **Notifications**

                                ### Authentication
                                Use the `/api/auth/login` endpoint to obtain a JWT token, then click **Authorize** above.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dental Clinic Dev Team")
                                .email("dev@dentalclinic.com")
                                .url("https://dentalclinic.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort + "/api").description("Local Development"),
                        new Server().url("https://api.dentalclinic.com/api").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from /api/auth/login")));
    }
}

/*
 * This file contains the OpenApiConfig class which manages system setup, external integration, security configurations, and bean mapping definitions.
 * Concepts used: Spring config annotations, @Bean initialization, and container lifecycle events.
 */
