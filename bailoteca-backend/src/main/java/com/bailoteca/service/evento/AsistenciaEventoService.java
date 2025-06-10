package com.bailoteca.service.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.AsistenciaEventoRepo;
import com.bailoteca.repository.evento.EventoRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Servicio encargado de gestionar las asistencias de los usuarios a eventos.
 * Permite registrar, listar y eliminar asistencias, así como actualizar el estado de asistencia y pago.
 * Este servicio asegura que las operaciones se realicen de forma transaccional y maneja
 * las validaciones necesarias para mantener la integridad de los datos.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see AsistenciaEvento
 * @see Evento
 * @see Usuario
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenciaEventoService {

    private final AsistenciaEventoRepo asistenciaRepo;
    private final EventoRepo eventoRepo;

    /**
     * Registra o actualiza la asistencia de un usuario a un evento.
     *
     * @param eventoId ID del evento
     * @param usuario  Usuario autenticado
     * @param asistira true si asistirá
     * @param pagado   true si ha pagado
     * @return la entidad actualizada o creada
     */
    @Transactional
    public AsistenciaEvento registrarAsistencia(Long eventoId, Usuario usuario, boolean asistira, boolean pagado) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        AsistenciaEvento asistencia = asistenciaRepo.findByEventoAndUsuario(evento, usuario)
                .orElseGet(() -> {
                    log.info("Creando nueva asistencia para evento {} y usuario {}", eventoId, usuario.getId());
                    return AsistenciaEvento.builder()
                            .evento(evento)
                            .usuario(usuario)
                            .build();
                });

        asistencia.setAsistira(asistira);
        asistencia.setPagado(pagado);

        AsistenciaEvento guardada = asistenciaRepo.save(asistencia);
        log.info("Asistencia actualizada: id={}, asistira={}, pagado={}", guardada.getId(), asistira, pagado);
        return guardada;
    }

    /**
     * Obtiene todas las asistencias a un evento dado.
     *
     * @param eventoId ID del evento
     * @return lista de asistencias
     */
    public List<AsistenciaEvento> listarAsistenciasPorEvento(Long eventoId) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        return asistenciaRepo.findByEvento(evento);
    }

    /**
     * Obtiene todas las asistencias de un usuario.
     *
     * @param usuario Usuario autenticado
     * @return lista de asistencias
     */
    public List<AsistenciaEvento> listarAsistenciasPorUsuario(Usuario usuario) {
        return asistenciaRepo.findByUsuario(usuario);
    }

    /**
     * Elimina la asistencia de un usuario a un evento.
     *
     * @param eventoId ID del evento
     * @param usuario  Usuario autenticado
     */
    @Transactional
    public void eliminarAsistencia(Long eventoId, Usuario usuario) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        AsistenciaEvento asistencia = asistenciaRepo.findByEventoAndUsuario(evento, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asistencia no encontrada"));

        log.info("Eliminando asistencia del usuario {} al evento {}", usuario.getId(), eventoId);
        asistenciaRepo.delete(asistencia);
    }
}