package com.bailoteca.repository.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las asistencias a eventos.
 */
public interface AsistenciaEventoRepo extends JpaRepository<AsistenciaEvento, Long> {

    /**
     * Busca asistencia por evento y usuario (único).
     */
    Optional<AsistenciaEvento> findByEventoAndUsuario(Evento evento, Usuario usuario);

    /**
     * Lista todas las asistencias a un evento.
     */
    List<AsistenciaEvento> findByEvento(Evento evento);

    /**
     * Lista todas las asistencias de un usuario.
     */
    List<AsistenciaEvento> findByUsuario(Usuario usuario);
}