package com.bailoteca.repository.inscripcion;

import com.bailoteca.models.inscripcion.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepo extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByUsuarioId(Long usuarioId);
    List<Inscripcion> findByClaseId(Long claseId);
    boolean existsByUsuarioIdAndClaseId(Long usuarioId, Long claseId);
    List<Inscripcion> findByClaseProfesorId(Long profesorId);

}