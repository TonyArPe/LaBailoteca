package com.bailoteca.controller.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.service.chat.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que maneja mensajes entrantes en tiempo real
 * a través de WebSocket.
 * Este controlador recibe mensajes enviados por los clientes
 * y los procesa utilizando el servicio de mensajes.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Mensaje
 * @see MensajeService
 */
@RestController
@RequiredArgsConstructor
public class MensajeWebSocketController {

    private final MensajeService mensajeService;

    @MessageMapping("/mensaje")
    public void recibirMensaje(Mensaje mensaje) {
        mensajeService.enviarMensaje(mensaje);
    }
}