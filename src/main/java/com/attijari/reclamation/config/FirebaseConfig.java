package com.attijari.reclamation.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-json}")
    private String firebaseServiceAccountJson;

    @PostConstruct
    public void initialize() {
        if (firebaseServiceAccountJson == null || firebaseServiceAccountJson.trim().isEmpty()) {
            log.error("La variable d'environnement FIREBASE_SERVICE_ACCOUNT_JSON est absente ou vide. Firebase ne sera pas initialisé.");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Gestion des sauts de ligne potentiellement échappés dans la variable d'environnement
                String jsonContent = firebaseServiceAccountJson.replace("\\n", "\n");
                
                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase App initialisé avec succès depuis la variable d'environnement.");
            }
        } catch (IOException e) {
            log.error("Erreur critique lors de l'initialisation de Firebase avec le JSON fourni.", e);
        }
    }
}
