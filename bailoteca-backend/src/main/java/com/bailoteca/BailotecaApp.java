package com.bailoteca;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Bailoteca.
 * Esta clase actúa como punto de entrada para la aplicación Spring Boot.
 * Se encarga de inicializar el contexto de la aplicación y arrancar el servidor embebido.
 * 
 * @author TonyArPe
 * @version 1.0
 * @since 08/04/2025
 * 
 */
@SpringBootApplication
public class BailotecaApp {
    public static void main(String[] args) {
        SpringApplication.run(BailotecaApp.class, args);
    }
}
