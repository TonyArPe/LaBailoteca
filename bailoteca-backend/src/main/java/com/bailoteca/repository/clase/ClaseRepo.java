package com.bailoteca.repository.clase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.clase.Clase;

import java.util.List;

/**
 * Repositorio para gestionar las clases en la aplicación.
 * Permite buscar clases por profesor, nombre y estado de publicación.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Clase
 */
@Repository
public interface ClaseRepo extends JpaRepository<Clase, Long> {
    List<Clase> findByProfesorId(Long profesorId);
    List<Clase> findByNombreContainingIgnoreCase(String nombre);
    List<Clase> findByPublicaTrue();
}