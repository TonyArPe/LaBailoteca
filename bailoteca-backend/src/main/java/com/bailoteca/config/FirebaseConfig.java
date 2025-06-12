package com.bailoteca.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;


/**
 * Configuración de Firebase para la aplicación.
 * Inicializa Firebase con las credenciales del archivo JSON
 * y proporciona un bean de FirebaseAuth para su uso en la aplicación.
 * Esta clase se ejecuta al iniciar la aplicación
 * y asegura que Firebase esté correctamente configurado
 * antes de que se utilice en otros componentes.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see FirebaseAuth
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase_admin_bailoteca.json");

            if (serviceAccount == null) {
                throw new IllegalStateException("No se encontró el archivo firebase_admin_bailoteca.json en resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("FIREBASE --> Inicializado correctamente");
            }

        } catch (IOException e) {
            System.err.println("FIREBASE --> Error al inicializar Firebase: " + e.getMessage());
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}