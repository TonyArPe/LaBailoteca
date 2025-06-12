package com.bailoteca.repository.inscripcion;

import com.bailoteca.models.inscripcion.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar las inscripciones en la aplicación.
 * Permite buscar inscripciones por usuario, clase y profesor,
 * así como verificar la existencia de una inscripción específica.
 *
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Inscripcion
 */
@Repository
public interface InscripcionRepo extends JpaRepository<Inscripcion, Long> {

    /**
     * Busca todas las inscripciones de un usuario específico.
     *
     * @param usuarioId ID del usuario cuyas inscripciones se buscan.
     * @return Lista de inscripciones del usuario.
     */
    List<Inscripcion> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas las inscripciones a una clase específica.
     *
     * @param claseId ID de la clase cuyas inscripciones se buscan.
     * @return Lista de inscripciones a la clase.
     */
    List<Inscripcion> findByClaseId(Long claseId);

    /**
     * Verifica si existe una inscripción de un usuario a una clase específica.
     *
     * @param usuarioId ID del usuario.
     * @param claseId ID de la clase.
     * @return true si existe la inscripción, false en caso contrario.
     */
    boolean existsByUsuarioIdAndClaseId(Long usuarioId, Long claseId);

    /**
     * Busca todas las inscripciones de un profesor específico.
     *
     * @param profesorId ID del profesor cuyas inscripciones se buscan.
     * @return Lista de inscripciones del profesor.
     */
    List<Inscripcion> findByClaseProfesorId(Long profesorId);
}