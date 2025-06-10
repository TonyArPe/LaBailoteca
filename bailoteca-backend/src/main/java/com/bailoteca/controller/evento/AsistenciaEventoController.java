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
 * Controlador que gestiona las asistencias de los usuarios a los eventos.
 * Permite registrar, listar y eliminar asistencias.
 * Este controlador proporciona endpoints para que los usuarios puedan indicar
 * su asistencia a eventos específicos, así como para listar todas las asistencias
 * registradas a un evento o las asistencias del usuario autenticado.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see AsistenciaEvento
 * @see AsistenciaEventoRequest
 * @see AsistenciaEventoService
 * @see Usuario
 * @see UsuarioRepo
 * @see UsuarioDetails
 * 
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
     * Registra o actualiza la asistencia del usuario autenticado a un evento.
     * Si el usuario ya tiene una asistencia registrada, se actualiza.
     * Si no, se crea una nueva asistencia.
     * Este endpoint permite a los usuarios indicar si asistirán al evento y si han pagado.
     * 
     * @param request     contiene los detalles de la asistencia (asistirá, pagado)
     * @param eventoId    ID del evento
     * @param asistira    true si asistirá
     * @param pagado      true si ha pagado
     * @param userDetails detalles del usuario autenticado
     * @return Asistencia actualizada o creada
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
     * Lista todas las asistencias registradas a un evento.
     *
     * @param eventoId ID del evento
     * @return lista de asistencias
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<AsistenciaEvento>> listarAsistenciasEvento(@PathVariable Long eventoId) {
        log.info("Listando asistencias para el evento {}", eventoId);
        return ResponseEntity.ok(asistenciaEventoService.listarAsistenciasPorEvento(eventoId));
    }

    /**
     * Lista todas las asistencias del usuario autenticado.
     *
     * @param userDetails detalles del usuario autenticado
     * @return lista de asistencias
     */
    @GetMapping("/mias")
    public ResponseEntity<List<AsistenciaEvento>> listarMisAsistencias(
            @AuthenticationPrincipal UsuarioDetails userDetails) {
        Usuario usuario = userDetails.getUsuario();
        log.info("Listando asistencias del usuario {}", usuario.getId());
        return ResponseEntity.ok(asistenciaEventoService.listarAsistenciasPorUsuario(usuario));
    }

    /**
     * Elimina la asistencia del usuario autenticado a un evento.
     *
     * @param eventoId    ID del evento
     * @param userDetails detalles del usuario autenticado
     * @return 204 No Content
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