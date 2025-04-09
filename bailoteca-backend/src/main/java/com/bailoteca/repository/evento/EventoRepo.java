package com.bailoteca.repository.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bailoteca.models.evento.Evento;

public interface EventoRepo extends JpaRepository<Evento, Long> {
    
}
