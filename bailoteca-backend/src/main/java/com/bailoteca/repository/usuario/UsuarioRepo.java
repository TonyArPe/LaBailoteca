package com.bailoteca.repository.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bailoteca.models.usuario.Usuario;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas.
 * Extiende JpaRepository para heredar funcionalidades básicas de acceso a datos.
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long >{
    Usuario findByCorreo(String correo);
    Usuario findByDni(String dni);
    Usuario findByTelefono(String telefono);
    Usuario findByNombre(String nombre);
}
