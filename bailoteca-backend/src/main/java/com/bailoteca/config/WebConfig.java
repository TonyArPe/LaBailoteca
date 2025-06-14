package com.bailoteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración global para servir archivos estáticos subidos por los usuarios.
 * 
 * Mapea la carpeta local "uploads/" a la URL pública "/media/**",
 * permitiendo que imágenes subidas se accedan desde el navegador o frontend móvil.
 *
 * Ejemplo:
 *   - Archivo guardado en: uploads/imagen.jpg
 *   - URL pública: http://host:puerto/media/imagen.jpg
 * 
 * Esta clase también puede definir caché para mejorar rendimiento.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600); // 1 hora
    }
}