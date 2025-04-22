package com.bailoteca.controller.pago;

import com.bailoteca.models.evento.Evento;
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

@RestController
@RequestMapping("/api/pagos-evento")
@RequiredArgsConstructor
public class PagoEventoController {

    private final PagoEventoRepo pagoEventoRepo;
    private final UsuarioRepo usuarioRepo;
    private final EventoRepo eventoRepo;

    /**
     * Devuelve todos los pagos registrados (solo ADMIN o PROFESOR).
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
     * Devuelve los pagos realizados por un usuario específico.
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
     * Devuelve todos los pagos realizados para un evento específico.
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
     * Crea un nuevo pago para un evento (el usuario autenticado debe ser el mismo
     * que paga).
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
     * Elimina un pago de evento (solo ADMIN o el propio usuario que realizó el
     * pago).
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
     * Método auxiliar para obtener el usuario autenticado desde el JWT.
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

    private boolean esAdmin(Usuario user) {
        return user.getRol().name().equals("ADMIN");
    }

    private boolean esProfesor(Usuario user) {
        return user.getRol().name().equals("PROFESOR");
    }
}