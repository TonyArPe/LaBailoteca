package com.bailoteca.service.notificacion;

import com.bailoteca.models.notificacion.Notificacion;
import com.bailoteca.repository.notificacion.NotificacionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar las operaciones relacionadas con las notificaciones.
 * Encapsula la lógica de negocio asociada a las notificaciones.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepo notificacionRepo;

    /**
     * Obtiene todas las notificaciones de un usuario receptor, ordenadas por fecha de envío descendente.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Lista de notificaciones del usuario.
     */
    public List<Notificacion> obtenerPorReceptor(Long usuarioId) {
        return notificacionRepo.findByReceptorIdOrderByFechaEnvioDesc(usuarioId);
    }

    /**
     * Crea y guarda una nueva notificación en la base de datos.
     * Establece la fecha de envío al momento actual y marca la notificación como no leída por defecto.
     *
     * @param notificacion Objeto Notificacion a crear.
     * @return La notificación creada y guardada.
     */
    public Notificacion crear(Notificacion notificacion) {
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacion.setLeida(false);
        return notificacionRepo.save(notificacion);
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param id ID de la notificación a marcar como leída.
     * @return La notificación actualizada o null si no se encuentra.
     */
    public Notificacion marcarComoLeida(Long id) {
        return notificacionRepo.findById(id)
                .map(notificacion -> {
                    notificacion.setLeida(true);
                    return notificacionRepo.save(notificacion);
                })
                .orElse(null);
    }

    /**
     * Elimina una notificación específica por su ID.
     *
     * @param id ID de la notificación a eliminar.
     */
    public void eliminar(Long id) {
        notificacionRepo.deleteById(id);
    }

    /**
     * Cuenta el número de notificaciones no leídas de un usuario receptor.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Número de notificaciones no leídas.
     */
    public int contarNoLeidas(Long usuarioId) {
        return notificacionRepo.countByReceptorIdAndLeidaFalse(usuarioId);
    }

    /**
     * Obtiene todas las notificaciones no leídas de un usuario receptor, ordenadas por fecha de envío descendente.
     *
     * @param usuarioId ID del usuario receptor.
     * @return Lista de notificaciones no leídas.
     */
    public List<Notificacion> obtenerNoLeidas(Long usuarioId) {
        return notificacionRepo.findByReceptorIdAndLeidaFalseOrderByFechaEnvioDesc(usuarioId);
    }

    /**
     * Marca todas las notificaciones de un usuario receptor como leídas.
     *
     * @param usuarioId ID del usuario receptor.
     */
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> noLeidas = notificacionRepo.findByReceptorIdAndLeidaFalseOrderByFechaEnvioDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepo.saveAll(noLeidas);
    }
}