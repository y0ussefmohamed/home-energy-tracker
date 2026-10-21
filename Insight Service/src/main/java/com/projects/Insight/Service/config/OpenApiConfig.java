package com.projects.Insight.Service.config;

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

@Configuration
public class OpenApiConfig {

    @Value("${gateway.url:http://localhost:9000}")
    private String gatewayUrl;

    @Bean
    public OpenAPI insightServiceApiDocs() {
        return new OpenAPI()
                .addServersItem(new Server().url(gatewayUrl).description("API Gateway"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Insight Service API")
                        .description("Insight service API for Home Energy Tracker Project")
                        .contact(getContact())
                        .license(getLicense())
                        .version("1.0.0"));
    }

    private static License getLicense() {
        License license = new License();
        license.setName("Creative Commons Attribution-NonCommercial 4.0 International License");
        license.setUrl("https://creativecommons.org/licenses/by-nc/4.0/");
        return license;
    }

    private static Contact getContact() {
        Contact contact = new Contact();
        contact.setName("Home Energy Tracker");
        contact.setUrl("https://github.com/y0ussefmohamed/home-energy-tracker");
        return contact;
    }
}
