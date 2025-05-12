package com.bailoteca.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Inicializa Firebase con el archivo de credenciales JSON.
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(
                        new ClassPathResource("firebase-service-account.json").getInputStream()
                ))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("FIREBASE: inicializado correctamente.");
        }
    }
}