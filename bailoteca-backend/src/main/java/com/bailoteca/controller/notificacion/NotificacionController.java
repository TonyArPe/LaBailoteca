package com.bailoteca.controller.notificacion;

import com.bailoteca.models.notificacion.Notificacion;
import com.bailoteca.repository.notificacion.NotificacionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionRepo notificacionRepo;

    /**
     * Obtener todas las notificaciones de un usuario receptor.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> getByReceptor(@PathVariable Long usuarioId) {
        return notificacionRepo.findByReceptorIdOrderByFechaEnvioDesc(usuarioId);
    }

    /**
     * Crear una nueva notificación.
     * (Por ejemplo: al crear un evento).
     */
    @PostMapping
    public Notificacion crear(@RequestBody Notificacion notificacion) {
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacion.setLeida(false);
        return notificacionRepo.save(notificacion);
    }

    /**
     * Marcar una notificación como leída.
     */
    @PutMapping("/{id}/leida")
    public Notificacion marcarLeida(@PathVariable Long id) {
        Notificacion notificacion = notificacionRepo.findById(id).orElse(null);
        if (notificacion != null) {
            notificacion.setLeida(true);
            return notificacionRepo.save(notificacion);
        }
        return null;
    }

    /**
     * Eliminar una notificación por ID.
     */
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        notificacionRepo.deleteById(id);
    }
}
