package com.bailoteca.controller.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.service.chat.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que maneja mensajes entrantes en tiempo real
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