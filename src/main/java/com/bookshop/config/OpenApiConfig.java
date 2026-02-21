package com.bookshop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookshopOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("BookShop API")
                        .description("""
                                RESTful API for an online bookshop application.

                                ## Authentication
                                This API uses **JWT Bearer tokens** for authentication.
                                1. Call `POST /api/auth/login` with your credentials
                                2. Copy the `token` from the response
                                3. Click **Authorize** above and paste: `Bearer <your-token>`

                                ## Pre-seeded accounts
                                | Role  | Email                | Password  |
                                |-------|----------------------|-----------|
                                | ADMIN | admin@bookshop.com   | admin123  |
                                | USER  | user@bookshop.com    | user123   |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Redouane")
                                .email("admin@bookshop.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
