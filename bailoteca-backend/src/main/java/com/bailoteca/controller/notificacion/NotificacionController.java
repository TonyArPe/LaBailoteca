package com.bailoteca.controller.notificacion;

import com.bailoteca.models.notificacion.Notificacion;
import com.bailoteca.service.notificacion.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con las notificaciones.
 * Permite crear, obtener, marcar como leídas y eliminar notificaciones de los usuarios.
 *
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Notificacion
 * @see NotificacionService
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * Obtiene todas las notificaciones de un usuario receptor.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Lista de notificaciones del usuario.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> obtenerPorReceptor(@PathVariable Long usuarioId) {
        return notificacionService.obtenerPorReceptor(usuarioId);
    }

    /**
     * Crea una nueva notificación.
     *
     * @param notificacion Objeto Notificacion a crear.
     * @return La notificación creada.
     */
    @PostMapping
    public Notificacion crear(@RequestBody Notificacion notificacion) {
        return notificacionService.crear(notificacion);
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param id ID de la notificación a marcar como leída.
     * @return La notificación actualizada o null si no se encuentra.
     */
    @PutMapping("/{id}/leida")
    public Notificacion marcarComoLeida(@PathVariable Long id) {
        return notificacionService.marcarComoLeida(id);
    }

    /**
     * Elimina una notificación específica por su ID.
     *
     * @param id ID de la notificación a eliminar.
     */
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
    }

    /**
     * Cuenta el número de notificaciones no leídas de un usuario receptor.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Número de notificaciones no leídas.
     */
    @GetMapping("/usuario/{usuarioId}/contador-noleidas")
    public int contarNoLeidas(@PathVariable Long usuarioId) {
        return notificacionService.contarNoLeidas(usuarioId);
    }

    /**
     * Obtiene todas las notificaciones no leídas de un usuario receptor.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Lista de notificaciones no leídas.
     */
    @GetMapping("/usuario/{usuarioId}/noleidas")
    public List<Notificacion> obtenerNoLeidas(@PathVariable Long usuarioId) {
        return notificacionService.obtenerNoLeidas(usuarioId);
    }

    /**
     * Marca todas las notificaciones de un usuario receptor como leídas.
     *
     * @param usuarioId ID del usuario receptor.
     */
    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    public void marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        notificacionService.marcarTodasComoLeidas(usuarioId);
    }
}