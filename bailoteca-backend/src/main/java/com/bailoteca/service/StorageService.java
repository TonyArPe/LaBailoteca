package com.bailoteca.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Servicio encargado de guardar, eliminar y acceder a archivos subidos
 * (como imágenes de perfil, banners de eventos, etc.)
 */
@Service
@Slf4j
public class StorageService {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    /**
     * Guarda un archivo en el sistema de archivos, dentro de la carpeta `uploads/`.
     *
     * @param file archivo recibido desde el cliente
     * @return nombre generado del archivo (con UUID para evitar colisiones)
     * @throws IOException si ocurre un error al escribir
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (!Files.exists(UPLOAD_DIR)) {
            try {
                Files.createDirectories(UPLOAD_DIR);
                log.info("📁 Carpeta 'uploads' creada en {}", UPLOAD_DIR.toAbsolutePath());
            } catch (IOException e) {
                log.error("❌ No se pudo crear carpeta de uploads", e);
                throw e;
            }
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IOException("Nombre de archivo no válido");
        }

        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = UPLOAD_DIR.resolve(filename);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("✅ Archivo guardado en {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("❌ Error al copiar archivo a disco", e);
            throw e;
        }

        return filename;
    }

    /**
     * Obtiene la ruta absoluta de un archivo guardado.
     *
     * @param filename nombre del archivo
     * @return ruta completa al archivo dentro de `uploads/`
     */
    public Path getFilePath(String filename) {
        return UPLOAD_DIR.resolve(filename);
    }

    /**
     * Elimina físicamente un archivo de la carpeta `uploads/`.
     *
     * @param filename nombre del archivo a borrar
     * @return true si fue eliminado correctamente, false en caso contrario
     */
    public boolean deleteFile(String filename) {
        try {
            boolean deleted = Files.deleteIfExists(getFilePath(filename));
            if (deleted) {
                log.info("🗑️ Archivo eliminado: {}", filename);
            } else {
                log.warn("⚠️ No se encontró archivo para eliminar: {}", filename);
            }
            return deleted;
        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo {}", filename, e);
            return false;
        }
    }
}