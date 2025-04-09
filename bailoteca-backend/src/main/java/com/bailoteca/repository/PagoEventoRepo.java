package com.bailoteca.repository;

import com.bailoteca.models.PagoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoEventoRepo extends JpaRepository<PagoEvento, Long> {
    List<PagoEvento> findByUsuarioId(Long usuarioId);
    List<PagoEvento> findByEventoId(Long eventoId);
}
