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
 * Controlador REST encargado de gestionar la subida de archivos multimedia
 * (imágenes de perfil, imágenes de eventos, etc.) en Bailoteca.
 *
 * La descarga y visualización pública de estos archivos se gestiona desde
 * WebMvcConfig, mapeando la carpeta local `uploads/` a `/media/**`.
 *
 * @author Tony Aragón
 * @version 1.0
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final StorageService storageService;

    /**
     * Endpoint para subir un archivo al servidor.
     * El archivo se guarda físicamente en el directorio `uploads/`.
     *
     * @param file archivo subido desde el cliente
     * @return nombre del archivo almacenado
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = storageService.saveFile(file);
            log.info("✅ Archivo subido correctamente: {}", filename);
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            log.error("❌ Error al subir archivo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir archivo");
        }
    }
}