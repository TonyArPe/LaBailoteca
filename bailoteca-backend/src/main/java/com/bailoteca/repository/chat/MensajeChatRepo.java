package com.bailoteca.repository.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.models.enums.EstadoMensaje;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeChatRepo extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByEmisorIdOrReceptorIdOrderByFechaEnvioAsc(Long emisorId, Long receptorId);
    List<Mensaje> findByEmisorIdAndReceptorIdOrderByFechaEnvioAsc(Long emisorId, Long receptorId);
    List<Mensaje> findByReceptorIdAndEstado(Long receptorId, EstadoMensaje estado);
}