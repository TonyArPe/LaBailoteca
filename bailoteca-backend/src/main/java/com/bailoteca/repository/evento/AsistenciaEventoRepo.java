package com.bailoteca.repository.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las asistencias a eventos en la aplicación.
 * Permite buscar asistencias por evento y usuario, listar todas las asistencias
 * a un evento y listar todas las asistencias de un usuario.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see AsistenciaEvento
 */
public interface AsistenciaEventoRepo extends JpaRepository<AsistenciaEvento, Long> {

    /**
     * Busca una asistencia a un evento por el evento y el usuario.
     * 
     * @param evento El evento al que se asiste.
     * @param usuario El usuario que asiste al evento.
     * @return Una instancia de AsistenciaEvento si existe, o Optional.empty() si no.
     */
    Optional<AsistenciaEvento> findByEventoAndUsuario(Evento evento, Usuario usuario);

    /**
     * Lista todas las asistencias a un evento.
     * 
     * @param evento El evento del cual se quieren listar las asistencias.
     * @return Una lista de AsistenciaEvento asociadas al evento.
     */
    List<AsistenciaEvento> findByEvento(Evento evento);

    /**
     * Lista todas las asistencias de un usuario.
     * 
     * @param usuario El usuario del cual se quieren listar las asistencias.
     * @return Una lista de AsistenciaEvento asociadas al usuario.
     */
    List<AsistenciaEvento> findByUsuario(Usuario usuario);
}