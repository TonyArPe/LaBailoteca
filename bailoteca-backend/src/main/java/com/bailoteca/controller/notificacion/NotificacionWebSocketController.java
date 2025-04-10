package com.bailoteca.controller.notificacion;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import com.bailoteca.models.notificacion.Notificacion;
import com.bailoteca.service.notificacion.NotificacionService;

/**
 * Controlador que maneja las notificaciones enviadas por WebSocket
 * Recibe mensajes de los clientes y los envía a los suscriptores
 * conectados
 */
public class NotificacionWebSocketController {

    private final NotificacionService notificacionService;

    /**
     * Maneja los mensajes enviados a "/app/notificar"
     * Guarda la notificacion y la envia a todos los clientes suscritos
     * al topic
     * 
     * @param notificacion la notificacion a enviar
     * @return la notificacion enviada
     */
    @MessageMapping("/notificar")
    @SendTo("/topic/notificaciones")
    public Notificacion enviarNotificacion(Notificacion notificacion){
        // Guardar la notificación en la base de datos
        notificacionService.crear(notificacion);
        // Devolver la notificación para que sea enviada a los clientes
        return notificacion;
    }
    
}
