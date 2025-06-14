package com.bailoteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.api.client.util.Value;

/**
 * Configura el mapeo de rutas estáticas para servir archivos subidos desde la carpeta 'uploads/'.
 * Esto permite que las imágenes sean accesibles públicamente desde URLs como /media/{nombreArchivo}.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadDir);
    }
}