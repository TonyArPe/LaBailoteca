package com.bailoteca.repository.chat;

import com.bailoteca.models.chat.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepo extends JpaRepository<Mensaje, Long> {

    // Mensajes entre dos usuarios (privado)
    List<Mensaje> findByEmisorIdAndReceptorIdOrReceptorIdAndEmisorIdOrderByFechaEnvioAsc(
        Long emisorId, Long receptorId, Long receptorId2, Long emisorId2
    );

    // Mensajes por clase (grupal)
    List<Mensaje> findByClaseIdOrderByFechaEnvioAsc(Long claseId);
}