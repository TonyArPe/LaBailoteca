package com.bailoteca.repository.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.evento.Evento;

/**
 * Repositorio para gestionar los eventos en la aplicación.
 * Permite buscar eventos por nombre, estado, organizador y fecha.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Evento
 */
public interface EventoRepo extends JpaRepository<Evento, Long> {
    
    /**
     * Buscar eventos por nombre, ignorando mayúsculas y minúsculas.
     * @param nombre
     * @return
     */
    List<Evento> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar eventos por estado.
     * @param estado
     * @return
     */
    List<Evento> findByEstado(EstadoEvento estado);

    /**
     * Buscar eventos por organizador.
     * @param organizadorId
     * @return
     */
    List<Evento> findByOrganizadorId(Long organizadorId);

    /**
     * Buscar eventos que ocurren después de una fecha específica.
     * @param fecha
     * @return
     */
    List<Evento> findByFechaAfter(LocalDateTime fecha);
    
}