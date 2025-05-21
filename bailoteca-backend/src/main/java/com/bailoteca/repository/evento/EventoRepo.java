package com.bailoteca.repository.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.evento.Evento;

public interface EventoRepo extends JpaRepository<Evento, Long> {
    // Buscar eventos por nombre parcial
    List<Evento> findByNombreContainingIgnoreCase(String nombre);

    // Buscar eventos por estado (ACTIVO, CANCELADO, etc.)
    List<Evento> findByEstado(EstadoEvento estado);

    // Buscar eventos por organizador
    List<Evento> findByOrganizadorId(Long organizadorId);

    // Buscar eventos próximos
    List<Evento> findByFechaAfter(LocalDateTime fecha);
    
}
