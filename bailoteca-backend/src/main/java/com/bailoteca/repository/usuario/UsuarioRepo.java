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
 * Repositorio para gestionar los usuarios en la aplicación.
 * Permite buscar usuarios por correo, DNI, teléfono y nombre,
 * así como obtener alumnos por ID de profesor y verificar inscripciones.
 *
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo Correo electrónico del usuario.
     * @return Usuario si se encuentra, o un Optional vacío si no existe.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Busca un usuario por su DNI.
     *
     * @param dni DNI del usuario.
     * @return Usuario si se encuentra, o null si no existe.
     */
    Usuario findByDni(String dni);

    /**
     * Busca un usuario por su número de teléfono.
     *
     * @param telefono Número de teléfono del usuario.
     * @return Usuario si se encuentra, o null si no existe.
     */
    Usuario findByTelefono(String telefono);

    /**
     * Busca un usuario por su nombre.
     *
     * @param nombre Nombre del usuario.
     * @return Usuario si se encuentra, o null si no existe.
     */
    Usuario findByNombre(String nombre);

    /**
     * Busca todos los usuarios cuyo nombre contenga una cadena específica.
     *
     * @param nombre Cadena a buscar en el nombre del usuario.
     * @return Lista de usuarios cuyos nombres contienen la cadena especificada.
     */
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