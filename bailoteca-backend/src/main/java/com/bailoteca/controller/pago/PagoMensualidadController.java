package com.bailoteca.controller.pago;

import com.bailoteca.models.pago.PagoMensualidad;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.pago.PagoMensualidadRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los pagos de mensualidades.
 * Permite crear, consultar y eliminar pagos, así como obtener los pagos de un usuario o mes específico.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see PagoMensualidad
 */
@RestController
@RequestMapping("/api/pagos-mensualidad")
@RequiredArgsConstructor
public class PagoMensualidadController {

    private final PagoMensualidadRepo pagoMensualidadRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
     */
    @GetMapping
    public List<PagoMensualidad> getAll() {
        return pagoMensualidadRepo.findAll();
    }
    /**
     * Obtener un pago mensual específico por su ID.
     * Solo puede acceder el ADMIN o el mismo usuario autenticado.
     * @param id ID del pago mensual a consultar.
     * Si el usuario no tiene permisos, se devuelve un error 403.
     * @return Pago mensual o un error 403 si no tiene permisos.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<PagoMensualidad> getByUsuario(@PathVariable Long usuarioId) {
        return pagoMensualidadRepo.findByUsuarioId(usuarioId);
    }

    /**
     * Obtener los pagos mensuales realizados en un mes específico.
     * Solo puede acceder el ADMIN o un usuario con rol PROFESOR.
     * @param mes Mes a consultar (formato: "YYYY-MM").
     * @return Lista de pagos mensuales del mes especificado.
     */
    @GetMapping("/mes/{mes}")
    public List<PagoMensualidad> getByMes(@PathVariable String mes) {
        return pagoMensualidadRepo.findByMes(mes);
    }

    /**
     * Crear un nuevo pago mensual.
     * Solo pueden crear pagos los usuarios con rol ADMIN o PROFESOR.
     * @param pago Pago mensual a crear.
     * @return Pago mensual creado o error 403 si no tiene permisos.
     */
    @PostMapping
    public ResponseEntity<PagoMensualidad> create(@RequestBody PagoMensualidad pago) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (esAdmin(actual) || actual.getRol().name().equals("PROFESOR")) {
            return ResponseEntity.ok(pagoMensualidadRepo.save(pago));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Eliminar un pago mensual por su ID.
     * Solo puede eliminar el ADMIN o el usuario que realizó el pago.
     * @param id ID del pago mensual a eliminar.
     * @return Respuesta HTTP 200 OK si se eliminó correctamente, 403 Forbidden si no tiene permisos,
     * o 404 Not Found si el pago no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return pagoMensualidadRepo.findById(id).map(pago -> {
            if (esAdmin(actual) || pago.getUsuario().getId().equals(actual.getId())) {
                pagoMensualidadRepo.deleteById(id);
                return ResponseEntity.ok().<Void>build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
        }).orElse(ResponseEntity.notFound().<Void>build());
    }

    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
     */
    private Usuario getUsuarioAutenticado() {
        try {
            String correo = ((UserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal()).getUsername();
            return usuarioRepo.findByCorreo(correo).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si el usuario tiene rol ADMIN.
     *
     * @param usuario Usuario a verificar
     * @return true si es ADMIN, false en caso contrario
     */
    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol().name().equals("ADMIN");
    }
}