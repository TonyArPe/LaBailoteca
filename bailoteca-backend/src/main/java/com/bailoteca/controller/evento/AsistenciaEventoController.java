package com.bailoteca.controller.evento;

import com.bailoteca.dto.AsistenciaEventoRequest;
import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.security.UsuarioDetails;
import com.bailoteca.service.evento.AsistenciaEventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con las asistencias a eventos.
 * Permite registrar, listar y eliminar asistencias de los usuarios a eventos específicos.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see AsistenciaEvento
 * @see AsistenciaEventoService
 */
@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
@Slf4j
public class AsistenciaEventoController {

    private final AsistenciaEventoService asistenciaEventoService;
    private final UsuarioRepo usuarioRepo;

    private Usuario getUsuario(Authentication auth) {
        return usuarioRepo.findByCorreo(auth.getName())
                .orElseThrow(() -> {
                    log.error("Usuario con correo {} no encontrado", auth.getName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
    }

    /**
     * Registra la asistencia de un usuario a un evento.
     * Si ya existe una asistencia, la actualiza con los nuevos datos.
     *
     * @param eventoId ID del evento
     * @param request  datos de la asistencia
     * @param auth     autenticación del usuario
     * @return la asistencia registrada o actualizada
     */
    @PostMapping("/{eventoId}")
    public AsistenciaEvento registrarAsistencia(
            @PathVariable Long eventoId,
            @RequestBody AsistenciaEventoRequest request,
            Authentication auth) {

        Usuario usuario = getUsuario(auth);
        log.info("Registrando asistencia al evento {} para usuario {}", eventoId, usuario.getCorreo());

        return asistenciaEventoService.registrarAsistencia(eventoId, usuario, request.isAsistira(), request.isPagado());
    }

    /**
     * Lista todas las asistencias a un evento específico.
     *
     * @param eventoId ID del evento
     * @return lista de asistencias al evento
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AsistenciaEvento>> listarAsistenciasEvento(@PathVariable Long eventoId) {
        log.info("Listando asistencias para el evento {}", eventoId);
        return ResponseEntity.ok(asistenciaEventoService.listarAsistenciasPorEvento(eventoId));
    }

    /**
     * Lista las asistencias del usuario autenticado.
     *
     * @param userDetails detalles del usuario autenticado
     * @return lista de asistencias del usuario
     */
    @GetMapping("/mias")
    public ResponseEntity<List<AsistenciaEvento>> listarMisAsistencias(
            @AuthenticationPrincipal UsuarioDetails userDetails) {
        Usuario usuario = userDetails.getUsuario();
        log.info("Listando asistencias del usuario {}", usuario.getId());
        return ResponseEntity.ok(asistenciaEventoService.listarAsistenciasPorUsuario(usuario));
    }

    /**
     * Elimina la asistencia de un usuario a un evento.
     *
     * @param eventoId ID del evento
     * @param userDetails detalles del usuario autenticado
     * @return respuesta vacía con código 204 No Content
     */
    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Void> eliminarAsistencia(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioDetails userDetails) {
        Usuario usuario = userDetails.getUsuario();
        log.info("Usuario {} eliminando asistencia al evento {}", usuario.getId(), eventoId);
        asistenciaEventoService.eliminarAsistencia(eventoId, usuario);
        return ResponseEntity.noContent().build();
    }
}