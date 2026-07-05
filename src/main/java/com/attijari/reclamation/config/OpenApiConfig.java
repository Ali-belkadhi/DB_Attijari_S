package com.attijari.reclamation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiConfig.class);

    @Bean
    public OpenAPI bankReclamationOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:3000")
                .description("Serveur local");

        Contact contact = new Contact()
                .name("Attijari Reclamation Team");

        Info info = new Info()
                .title("API de gestion des réclamations Attijari")
                .description("API REST pour tester l'authentification et les opérations CRUD des utilisateurs et des réclamations.")
                .version("1.0.0")
                .contact(contact)
                .license(new License().name("Usage interne"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }

    @Bean
    public ApplicationRunner swaggerUiLogger(
            @Value("${server.port:8080}") String serverPort,
            @Value("${springdoc.swagger-ui.path:/swagger-ui.html}") String swaggerPath) {
        return args -> LOGGER.info("Swagger UI disponible sur : http://localhost:{}{}", serverPort, swaggerPath);
    }
}