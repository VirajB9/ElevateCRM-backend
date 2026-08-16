package com.viraj.dmabackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .servers(List.of(
                        new Server().url("/")
                ))

                .info(
                        new Info()
                                .title("Digital Marketing Agency API")
                                .version("1.0")
                                .description("Backend APIs")
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "Bearer Authentication",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )

                .tags(List.of(
                        new Tag().name("1. Authentication"),
                        new Tag().name("2. User Management"),
                        new Tag().name("3. Role Management"),
                        new Tag().name("4. Permission Management"),
                        new Tag().name("5. Lead Management"),
                        new Tag().name("6. Client Management"),
                        new Tag().name("7. Project Management"),
                        new Tag().name("8. Dynamic Menu Management"),
                        new Tag().name("9. Invoice Management")
                ));
    }
}