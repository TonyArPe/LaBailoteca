package com.bailoteca.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bailoteca.repository.usuario.UsuarioRepo;


/**
 * Configuración para migrar contraseñas existentes a formato encriptado.
 * Este proceso se ejecuta una sola vez al iniciar la aplicación.
 * Verifica si las contraseñas ya están encriptadas y las encripta si no lo están.
 */
@Configuration
public class DataMigrationConfig {

    @Bean
    public CommandLineRunner encryptExistingPasswords(UsuarioRepo usuarioRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            usuarioRepo.findAll().forEach(usuario -> {
                String actual = usuario.getContrasenna();
                if (!actual.startsWith("$2a$")) { // Verifica si NO está encriptada
                    usuario.setContrasenna(passwordEncoder.encode(actual));
                    usuarioRepo.save(usuario);
                    System.out.println("Contraseña encriptada para: " + usuario.getCorreo());
                }
            });
        };
    }
}