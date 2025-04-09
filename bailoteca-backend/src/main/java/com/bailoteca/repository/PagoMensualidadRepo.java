package com.bailoteca.repository;

import com.bailoteca.models.PagoMensualidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoMensualidadRepo extends JpaRepository<PagoMensualidad, Long> {
    List<PagoMensualidad> findByUsuarioId(Long usuarioId);
    List<PagoMensualidad> findByMes(String mes);
}
