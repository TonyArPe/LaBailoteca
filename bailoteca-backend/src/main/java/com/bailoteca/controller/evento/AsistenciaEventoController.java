package com.bailoteca.controller.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.service.AsistenciaEventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Marca asistencia del usuario al evento.
     */
    @PostMapping("/{id}/asistir")
    public void asistir(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        log.info("🔵 {} solicita asistir al evento {}", usuario.getCorreo(), id);
        asistenciaService.marcarAsistencia(id, usuario);
    }

    /**
     * Cancela asistencia del usuario al evento.
     */
    @DeleteMapping("/{id}/asistir")
    public void cancelar(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        log.info("🟠 {} solicita cancelar asistencia al evento {}", usuario.getCorreo(), id);
        asistenciaService.cancelarAsistencia(id, usuario);
    }

    /**
     * Devuelve la lista de asistentes si tiene permiso.
     */
    @GetMapping("/{id}/asistentes")
    public List<AsistenciaEvento> asistentes(@PathVariable Long id, Authentication auth) {
        Usuario usuario = getUsuario(auth);
        return asistenciaService.obtenerAsistentes(id, usuario);
    }

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}