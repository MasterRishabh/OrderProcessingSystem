package com.ecommerce.ordersystem.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("REST API for Item Catalog Management, Concurrency Control, and Stock Reservation/Release")
                        .version("1.0.0")
                        .contact(new Contact().name("E-Commerce Architecture Team"))
                        .license(new License().name("Apache 2.0")));
    }
}
