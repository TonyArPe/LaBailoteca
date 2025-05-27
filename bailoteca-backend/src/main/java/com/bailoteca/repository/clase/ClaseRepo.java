package com.bailoteca.repository.clase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.clase.Clase;

import java.util.List;

@Repository
public interface ClaseRepo extends JpaRepository<Clase, Long> {
    List<Clase> findByProfesorId(Long profesorId);
    List<Clase> findByNombreContainingIgnoreCase(String nombre);
    List<Clase> findByPublicaTrue();
}