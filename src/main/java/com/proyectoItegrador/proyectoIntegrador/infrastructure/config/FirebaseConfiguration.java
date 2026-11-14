package com.proyectoItegrador.proyectoIntegrador.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfiguration {

    @Bean
    FirebaseApp firebaseApp(
            @Value("${firebase.credentials-path}") String credentialsPath,
            @Value("${firebase.project-id:}") String projectId,
            @Value("${firebase.database-url}") String databaseUrl) throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException("Define FIREBASE_CREDENTIALS_PATH con la ruta al JSON de cuenta de servicio.");
        }
        GoogleCredentials credentials;
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(credentialsStream);
        }
        FirebaseOptions.Builder builder = FirebaseOptions.builder().setCredentials(credentials);
        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }
        builder.setDatabaseUrl(databaseUrl);
        FirebaseOptions options = builder.build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
