package com.bailoteca.service;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.AsistenciaEventoRepo;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio que gestiona la lógica de las asistencias a eventos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenciaEventoService {

    private final AsistenciaEventoRepo asistenciaRepo;
    private final EventoRepo eventoRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Permite que un usuario marque asistencia a un evento, si tiene permiso.
     */
    @Transactional
    public void marcarAsistencia(Long eventoId, Usuario usuario) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!puedeVerEvento(evento, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asistir a este evento");
        }

        if (asistenciaRepo.existsByUsuarioIdAndEventoId(usuario.getId(), eventoId)) {
            log.warn("Usuario {} ya está inscrito al evento {}", usuario.getCorreo(), eventoId);
            return; // Evita duplicados
        }

        AsistenciaEvento asistencia = AsistenciaEvento.builder()
                .evento(evento)
                .usuario(usuario)
                .build();

        asistenciaRepo.save(asistencia);
        log.info("🟢 Usuario {} marcado como asistente al evento {}", usuario.getCorreo(), evento.getNombre());
    }

    /**
     * Permite que un usuario cancele su asistencia.
     */
    @Transactional
    public void cancelarAsistencia(Long eventoId, Usuario usuario) {
        if (!asistenciaRepo.existsByUsuarioIdAndEventoId(usuario.getId(), eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No estás inscrito en este evento");
        }

        asistenciaRepo.deleteByUsuarioIdAndEventoId(usuario.getId(), eventoId);
        log.info("🟡 Usuario {} canceló asistencia al evento {}", usuario.getCorreo(), eventoId);
    }

    /**
     * Devuelve la lista de asistentes de un evento (solo visible por admin o profesor creador).
     */
    public List<AsistenciaEvento> obtenerAsistentes(Long eventoId, Usuario usuarioActual) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!usuarioActual.getRol().name().equals("ADMIN") &&
                !evento.getOrganizador().getId().equals(usuarioActual.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver asistentes");
        }

        return asistenciaRepo.findByEventoId(eventoId);
    }

    /**
     * Valida si un usuario puede ver (y por tanto asistir) a un evento.
     */
    public boolean puedeVerEvento(Evento evento, Usuario usuario) {
        if (evento.isPublico()) return true;

        if (usuario.getRol().name().equals("ADMIN")) return true;

        if (usuario.getRol().name().equals("PROFESOR") &&
                evento.getOrganizador().getId().equals(usuario.getId())) return true;

        return usuario.getInscripciones().stream()
                .anyMatch(insc -> insc.getClase().getProfesor().getId().equals(evento.getOrganizador().getId()));
    }
}