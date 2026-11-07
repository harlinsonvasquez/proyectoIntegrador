package com.proyectoItegrador.proyectoIntegrador.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión Estudiantil")
                        .version("1.0.0")
                        .description("API para gestión académica, solicitudes de cancelación, actividades y reportes. "
                                + "Los endpoints protegidos usan un token Bearer de Firebase Authentication."))
                .components(new Components().addSecuritySchemes("firebaseBearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Token de acceso de Firebase Authentication.")));
    }
}
