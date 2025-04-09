package com.bailoteca.repository.notificacion;

import com.bailoteca.models.notificacion.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepo extends JpaRepository<Notificacion, Long> {

    /**
     * Obtener todas las notificaciones de un usuario específico.
     */
    List<Notificacion> findByReceptorIdOrderByFechaEnvioDesc(Long receptorId);
}
