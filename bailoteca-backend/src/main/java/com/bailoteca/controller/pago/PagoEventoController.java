package com.bailoteca.controller.pago;

import com.bailoteca.models.pago.PagoEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.pago.PagoEventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los pagos de eventos.
 * Permite crear, consultar y eliminar pagos, así como obtener los pagos de un usuario o evento específico.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see PagoEvento
 */
@RestController
@RequestMapping("/api/pagos-evento")
@RequiredArgsConstructor
public class PagoEventoController {

    private final PagoEventoRepo pagoEventoRepo;
    private final UsuarioRepo usuarioRepo;
    private final EventoRepo eventoRepo;

    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
     */
    @GetMapping
    public ResponseEntity<List<PagoEvento>> getAllPagos() {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null || !(esAdmin(actual) || esProfesor(actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pagoEventoRepo.findAll());
    }

    
    /**
     * Obtiene los pagos realizados por un usuario específico.
     * Solo puede acceder el ADMIN o el propio usuario autenticado.
     * @param usuarioId ID del usuario cuyos pagos se desean consultar.
     * @return Lista de pagos del usuario o un error 403 si no tiene permisos.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PagoEvento>> getPagosByUsuario(@PathVariable Long usuarioId) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (esAdmin(actual) || actual.getId().equals(usuarioId)) {
            return ResponseEntity.ok(pagoEventoRepo.findByUsuarioId(usuarioId));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Obtiene los pagos realizados para un evento específico.
     * Solo puede acceder el ADMIN o un profesor que tenga clases con ese evento.
     * @param eventoId ID del evento cuyos pagos se desean consultar.
     * @return Lista de pagos del evento o un error 403 si no tiene permisos.
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<PagoEvento>> getPagosByEvento(@PathVariable Long eventoId) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null || !(esAdmin(actual) || esProfesor(actual))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pagoEventoRepo.findByEventoId(eventoId));
    }

    /**
     * Crea un nuevo pago de evento.
     * Solo puede acceder el ADMIN o el propio usuario que realizó el pago.
     * Valida la existencia del usuario y del evento antes de crear el pago.
     *
     * @param pago PagoEvento a crear
     * @return PagoEvento creado o error 403 si no tiene permisos
     */
    @PostMapping
    public ResponseEntity<PagoEvento> createPago(@RequestBody PagoEvento pago) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!actual.getId().equals(pago.getUsuario().getId()) && !esAdmin(actual)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validar existencia del usuario y evento
        if (!usuarioRepo.existsById(pago.getUsuario().getId()) || !eventoRepo.existsById(pago.getEvento().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        pago.setFechaPago(LocalDate.now());
        return ResponseEntity.ok(pagoEventoRepo.save(pago));
    }

    /**
     * Elimina un pago de evento por su ID.
     * Solo puede acceder el ADMIN o el propio usuario que realizó el pago.
     * @param id ID del pago a eliminar
     * @return Respuesta vacía con estado 200 OK si se eliminó correctamente, 403 Forbidden si no tiene permisos,
     *         o 404 Not Found si el pago no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePago(@PathVariable Long id) {
        Usuario actual = getUsuarioAutenticado();
        if (actual == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return pagoEventoRepo.findById(id)
                .map(pago -> {
                    if (esAdmin(actual) || pago.getUsuario().getId().equals(actual.getId())) {
                        pagoEventoRepo.deleteById(id);
                        // Indicamos manualmente el tipo genérico del ResponseEntity al compilador con <Void>
                        return ResponseEntity.ok().<Void>build();
                    }
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }

    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     * Si no hay usuario autenticado, devuelve null.
     *
     * @return Usuario autenticado o null si no hay sesión válida.
     */
    private Usuario getUsuarioAutenticado() {
        try {
            String correo = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .getUsername();
            return usuarioRepo.findByCorreo(correo).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si el usuario tiene rol ADMIN.
     *
     * @param user Usuario a verificar
     * @return true si es ADMIN, false en caso contrario
     */
    private boolean esAdmin(Usuario user) {
        return user.getRol().name().equals("ADMIN");
    }

    /**
     * Verifica si el usuario tiene rol PROFESOR.
     *
     * @param user Usuario a verificar
     * @return true si es PROFESOR, false en caso contrario
     */
    private boolean esProfesor(Usuario user) {
        return user.getRol().name().equals("PROFESOR");
    }
}