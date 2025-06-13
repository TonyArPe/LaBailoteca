package com.bailoteca.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
@Slf4j
public class StorageService {

    @Value("${upload.dir}")
    private String uploadDir;

    /**
     * Guarda un archivo en el servidor con un nombre único.
     *
     * @param file archivo a guardar.
     * @return ruta relativa del archivo guardado.
     * @throws IOException si falla la escritura.
     */
    public String saveFile(MultipartFile file) throws IOException {
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Archivo guardado en: {}", filePath.toString());

        return filename;
    }

    /**
     * Devuelve la ruta completa del archivo.
     */
    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    /**
     * Elimina un archivo.
     */
    public boolean deleteFile(String filename) {
        try {
            return Files.deleteIfExists(getFilePath(filename));
        } catch (IOException e) {
            log.error("Error al eliminar archivo {}", filename, e);
            return false;
        }
    }
}