package com.bailoteca.repository.notificacion;

import com.bailoteca.models.notificacion.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar las notificaciones en la aplicación.
 * Permite obtener notificaciones por receptor, filtrar por estado de lectura,
 * y contar notificaciones no leídas.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Notificacion
 */
@Repository
public interface NotificacionRepo extends JpaRepository<Notificacion, Long> {

    /**
     * Obtener todas las notificaciones de un usuario específico, ordenadas por fecha de envío descendente.
     * @param receptorId
     * @return
     */
    List<Notificacion> findByReceptorIdOrderByFechaEnvioDesc(Long receptorId);

    /**
     * Obtener las notificaciones no leídas de un usuario específico, ordenadas por fecha de envío descendente.
     * @param receptorId
     * @return
     */
    List<Notificacion> findByReceptorIdAndLeidaFalseOrderByFechaEnvioDesc(Long receptorId);

    /**
     * Contar las notificaciones no leídas de un usuario específico.
     * @param receptorId
     * @return
     */
    int countByReceptorIdAndLeidaFalse(Long receptorId);

}