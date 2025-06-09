package com.bailoteca.repository.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bailoteca.dto.UsuarioDTO;
import com.bailoteca.models.usuario.Usuario;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona métodos para realizar operaciones CRUD y consultas
 * personalizadas.
 * Extiende JpaRepository para heredar funcionalidades básicas de acceso a
 * datos.
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    Usuario findByDni(String dni);

    Usuario findByTelefono(String telefono);

    Usuario findByNombre(String nombre);

    @Query("SELECT new com.bailoteca.dto.UsuarioDTO(u.id, u.nombre, u.apellido, u.correo) FROM Inscripcion i JOIN i.usuario u WHERE i.clase.profesor.id = :idProfesor")
    List<UsuarioDTO> findAlumnosPorProfesorId(@Param("idProfesor") Long idProfesor);

    /**
     * Verifica si un usuario está inscrito en alguna clase de un profesor
     * específico.
     *
     * @param usuarioId  ID del usuario a verificar.
     * @param profesorId ID del profesor autenticado.
     * @return true si el usuario está inscrito en alguna clase del profesor.
     */
    @Query("""
                SELECT COUNT(i) > 0 FROM Inscripcion i
                WHERE i.usuario.id = :usuarioId
                  AND i.clase.profesor.id = :profesorId
            """)
    boolean estaInscritoEnClaseDeProfesor(@Param("usuarioId") Long usuarioId, @Param("profesorId") Long profesorId);

}