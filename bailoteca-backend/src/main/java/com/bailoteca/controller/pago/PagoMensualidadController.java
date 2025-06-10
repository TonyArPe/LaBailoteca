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
 * Controlador REST para la gestión de pagos mensuales.
 * Permite a los usuarios registrar pagos mensuales, consultar pagos realizados
 * por un usuario específico, y obtener todos los pagos mensuales registrados.
 * Este controlador proporciona endpoints para crear, listar y eliminar pagos mensuales,
 * con restricciones de acceso basadas en el rol del usuario autenticado.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see PagoMensualidad
 * @see Usuario
 * @see UsuarioRepo
 * @see PagoMensualidadRepo
 */
@RestController
@RequestMapping("/api/pagos-mensualidad")
@RequiredArgsConstructor
public class PagoMensualidadController {

    private final PagoMensualidadRepo pagoMensualidadRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Obtener todos los pagos mensuales
     */
    @GetMapping
    public List<PagoMensualidad> getAll() {
        return pagoMensualidadRepo.findAll();
    }

    /**
     * Obtener pagos por ID de usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<PagoMensualidad> getByUsuario(@PathVariable Long usuarioId) {
        return pagoMensualidadRepo.findByUsuarioId(usuarioId);
    }

    /**
     * Obtener pagos por mes
     */
    @GetMapping("/mes/{mes}")
    public List<PagoMensualidad> getByMes(@PathVariable String mes) {
        return pagoMensualidadRepo.findByMes(mes);
    }

    /**
     * Crear un nuevo pago mensual (solo accesible para ADMIN o PROFESOR)
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
     * Eliminar un pago mensual (solo ADMIN o el mismo usuario)
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
     * Método auxiliar para obtener el usuario autenticado
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
     * Verifica si el usuario tiene rol ADMIN
     */
    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol().name().equals("ADMIN");
    }
}