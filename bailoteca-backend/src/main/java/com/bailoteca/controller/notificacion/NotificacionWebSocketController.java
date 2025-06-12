package com.bailoteca.controller.notificacion;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import com.bailoteca.models.notificacion.Notificacion;
import com.bailoteca.service.notificacion.NotificacionService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador que maneja las notificaciones a través de WebSocket.
 * Permite enviar notificaciones a todos los clientes suscritos al topic "/topic/notificaciones".
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Notificacion
 * @see NotificacionService
 */
@RestController
@RequiredArgsConstructor
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
