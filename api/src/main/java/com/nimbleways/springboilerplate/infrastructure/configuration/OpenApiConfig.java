package com.nimbleways.springboilerplate.infrastructure.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI merjaneOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Merjane Order Processing API")
                        .description("Cloud-native REST API for order processing and inventory management built with Hexagonal Architecture. " +
                                "Supports Normal, Seasonal, and Expirable products with automatic replenishment alerts and customer notifications.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Merjane Engineering Team")
                                .email("dev@merjane.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Merjane Git Repository")
                        .url("https://github.com/hmazouar/merjane-refacto-java"))
                .servers(List.of(
                        new Server().url("/api").description("Application Context Base URL")
                ));
    }
}
