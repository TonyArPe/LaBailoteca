package com.bailoteca.repository.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.evento.Evento;

/**
 * Repositorio JPA para la entidad Evento.
 * Proporciona métodos para consultar eventos por nombre, estado, fecha y organizador.
 * 
 * Usado por el servicio EventoService para aplicar lógica de negocio según el rol del usuario.
 * 
 * @author
 */
public interface EventoRepo extends JpaRepository<Evento, Long> {

    /**
     * Busca eventos cuyo nombre contiene la cadena proporcionada (ignorando mayúsculas/minúsculas).
     *
     * @param nombre Parte del nombre a buscar
     * @return Lista de eventos coincidentes
     */
    List<Evento> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca eventos por estado específico (e.g., ACTIVO, CANCELADO).
     *
     * @param estado Estado del evento
     * @return Lista de eventos con ese estado
     */
    List<Evento> findByEstado(EstadoEvento estado);

    /**
     * Busca eventos organizados por un usuario específico.
     *
     * @param organizadorId ID del usuario organizador
     * @return Lista de eventos organizados por ese usuario
     */
    List<Evento> findByOrganizadorId(Long organizadorId);

    /**
     * Busca eventos cuya fecha sea posterior a una fecha específica.
     *
     * @param fecha Fecha de corte
     * @return Lista de eventos futuros
     */
    List<Evento> findByFechaAfter(LocalDateTime fecha);
}