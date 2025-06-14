package com.bailoteca.controller.uploads;

import com.bailoteca.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST encargado de gestionar la subida de archivos multimedia.
 * Exposición pública desde /media/** configurada en WebConfig.
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final StorageService storageService;

    /**
     * Endpoint para subir un archivo al servidor.
     *
     * @param file archivo subido como multipart/form-data
     * @return nombre del archivo guardado o error HTTP 400/500
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("📩 Solicitud de subida de archivo recibida");

        if (file.isEmpty() || file.getOriginalFilename() == null) {
            log.warn("⚠️ Archivo inválido: vacío o sin nombre original");
            return ResponseEntity.badRequest().body("Archivo vacío o sin nombre");
        }

        try {
            String filename = storageService.saveFile(file);
            log.info("✅ Archivo subido correctamente: {}", filename);
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            log.error("❌ Error interno al guardar archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar archivo: " + e.getMessage());
        }
    }
}