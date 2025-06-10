package com.bailoteca.repository.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.pago.PagoEvento;

import java.util.List;

/**
 * Repositorio para gestionar los pagos de eventos en la aplicación.
 * Permite obtener pagos por usuario y por evento.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see PagoEvento
 */
@Repository
public interface PagoEventoRepo extends JpaRepository<PagoEvento, Long> {

    /**
     * Obtener todos los pagos realizados por un usuario específico.
     * @param usuarioId ID del usuario
     * @return Lista de pagos realizados por el usuario
     */
    List<PagoEvento> findByUsuarioId(Long usuarioId);

    /**
     * Obtener todos los pagos asociados a un evento específico.
     * @param eventoId ID del evento
     * @return Lista de pagos asociados al evento
     */
    List<PagoEvento> findByEventoId(Long eventoId);
}
