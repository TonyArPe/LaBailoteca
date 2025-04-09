package com.bailoteca.repository.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.pago.PagoEvento;

import java.util.List;

@Repository
public interface PagoEventoRepo extends JpaRepository<PagoEvento, Long> {
    List<PagoEvento> findByUsuarioId(Long usuarioId);
    List<PagoEvento> findByEventoId(Long eventoId);
}
