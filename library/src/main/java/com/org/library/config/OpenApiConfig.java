package com.org.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appProperties.getName())
                        .version(appProperties.getVersion())
                        .description("REST API for a small library system — manage authors, books, members, and borrow records.")
                        .contact(new Contact()
                                .name("Library API Support")
                                .email("support@library.com")));
    }
}
