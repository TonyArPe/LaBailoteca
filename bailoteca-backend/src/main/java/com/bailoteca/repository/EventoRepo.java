package com.bailoteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bailoteca.models.Evento;

public interface EventoRepo extends JpaRepository<Evento, Long> {
    
}
