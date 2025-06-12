package com.bailoteca.repository.chat;

import com.bailoteca.models.chat.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar los mensajes de chat en la aplicación.
 * Permite buscar mensajes privados entre dos usuarios o mensajes de un grupo.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Mensaje
 */
@Repository
public interface MensajeRepo extends JpaRepository<Mensaje, Long> {

    // Mensajes entre dos usuarios (privado)
    List<Mensaje> findByEmisorIdAndReceptorIdOrReceptorIdAndEmisorIdOrderByFechaEnvioAsc(
        Long emisorId, Long receptorId, Long receptorId2, Long emisorId2
    );

    // Mensajes por clase (grupal)
    List<Mensaje> findByClaseIdOrderByFechaEnvioAsc(Long claseId);
}