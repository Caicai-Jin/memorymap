package com.memorymap.memorymap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Powers the interactive docs at /swagger-ui/index.html. The "bearerAuth" scheme
// below adds an "Authorize" button to that page — paste a JWT from POST /login there
// and every subsequent request made through the docs UI carries it automatically.
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI memoryMapOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MemoryMap API")
                        .description("Personal moment/mood journal with a private map of everywhere you've been.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
