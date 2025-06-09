package com.bailoteca.repository.evento;

import com.bailoteca.models.evento.AsistenciaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaEventoRepo extends JpaRepository<AsistenciaEvento, Long> {

    Optional<AsistenciaEvento> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    List<AsistenciaEvento> findByEventoId(Long eventoId);

    List<AsistenciaEvento> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    void deleteByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

}