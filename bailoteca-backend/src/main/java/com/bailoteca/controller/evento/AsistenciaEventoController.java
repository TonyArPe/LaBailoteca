package com.bailoteca.controller.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.AsistenciaEventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para marcar y cancelar asistencia a eventos.
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class AsistenciaEventoController {
    private final AsistenciaEventoService asistenciaService;
    private final UsuarioRepo usuarioRepo;

    @PostMapping("/{id}/asistir")
    public ResponseEntity<Void> asistir(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        log.info("🔵 {} solicita asistir al evento {}", usuario.getCorreo(), id);
        asistenciaService.marcarAsistencia(id, usuario);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/asistir")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        log.info("🟠 {} solicita cancelar asistencia al evento {}", usuario.getCorreo(), id);
        asistenciaService.cancelarAsistencia(id, usuario);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/asistentes")
    public ResponseEntity<List<AsistenciaEvento>> asistentes(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        return ResponseEntity.ok(asistenciaService.obtenerAsistentes(id, usuario));
    }

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}