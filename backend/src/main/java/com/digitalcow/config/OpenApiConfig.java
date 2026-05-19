package com.digitalcow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura Swagger UI con esquema Bearer JWT. */
@Configuration
public class OpenApiConfig {

    /**
     * Construye el bean OpenAPI con metadatos y esquema Bearer JWT.
     *
     * @return OpenAPI con seguridad Bearer
     */
    @Bean
    public OpenAPI digitalCowOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Digital Cow API").version("v1"))
            .addSecurityItem(new SecurityRequirement().addList("bearer"))
            .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearer",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
