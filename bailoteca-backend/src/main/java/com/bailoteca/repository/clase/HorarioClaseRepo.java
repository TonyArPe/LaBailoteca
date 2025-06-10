package com.bailoteca.repository.clase;

import com.bailoteca.models.clase.HorarioClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar los horarios de las clases en la aplicación.
 * Permite buscar horarios por ID de clase y eliminar horarios asociados a una clase.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see HorarioClase
 */
@Repository
public interface HorarioClaseRepo extends JpaRepository<HorarioClase, Long> {

    List<HorarioClase> findByClaseId(Long claseId);

    @Modifying
    @Transactional
    void deleteByClaseId(Long claseId);
}
