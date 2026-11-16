package com.viraj.dmabackend.config;

import com.viraj.dmabackend.common.response.ApiError;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                );
    }

    @Bean
    public OpenApiCustomizer globalErrorResponseCustomizer() {
        return openApi -> {
            if (openApi.getComponents() != null) {
                ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                        .resolveAsResolvedSchema(new AnnotatedType(ApiError.class));
                if (resolvedSchema != null && resolvedSchema.schema != null) {
                    openApi.getComponents().addSchemas("ApiError", resolvedSchema.schema);
                }
            }

            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) ->
                        pathItem.readOperations().forEach(operation -> {
                            ApiResponses responses = operation.getResponses();
                            if (responses == null) {
                                responses = new ApiResponses();
                                operation.setResponses(responses);
                            }

                            Content errorContent = new Content().addMediaType(
                                    org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiError"))
                            );

                            if (!responses.containsKey("400")) {
                                responses.addApiResponse("400", new ApiResponse()
                                        .description("Invalid request payload or malformed syntax")
                                        .content(errorContent));
                            }
                            if (!responses.containsKey("401") && !"/api/v1/auth/login".equals(path) && !"/api/v1/auth/logout".equals(path)) {
                                responses.addApiResponse("401", new ApiResponse()
                                        .description("Unauthorized - missing, invalid, or expired authentication token")
                                        .content(errorContent));
                            }
                            if (!responses.containsKey("403") && !"/api/v1/auth/login".equals(path) && !"/api/v1/auth/logout".equals(path)) {
                                responses.addApiResponse("403", new ApiResponse()
                                        .description("Forbidden - insufficient role or missing permission")
                                        .content(errorContent));
                            }
                            if (!responses.containsKey("500")) {
                                responses.addApiResponse("500", new ApiResponse()
                                        .description("Internal Server Error - an unexpected server error occurred")
                                        .content(errorContent));
                            }
                        })
                );
            }
        };
    }
}
