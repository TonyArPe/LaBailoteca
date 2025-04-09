package com.bailoteca.repository;

import com.bailoteca.models.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseRepo extends JpaRepository<Clase, Long> {
    List<Clase> findByProfesorId(Long profesorId);
}