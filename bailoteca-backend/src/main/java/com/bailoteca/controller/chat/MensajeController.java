package com.bailoteca.controller.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.service.chat.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que gestiona los mensajes de chat.
 * Permite obtener mensajes privados entre dos usuarios o mensajes de un grupo.
 * Este controlador proporciona endpoints para acceder a los mensajes
 * de chat privados y grupales, facilitando la comunicación entre usuarios.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Mensaje
 * @see MensajeService
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    @GetMapping("/privado/{user1}/{user2}")
    public List<Mensaje> obtenerMensajesPrivados(@PathVariable Long user1, @PathVariable Long user2) {
        return mensajeService.obtenerPrivados(user1, user2);
    }

    @GetMapping("/grupo/{claseId}")
    public List<Mensaje> obtenerMensajesGrupo(@PathVariable Long claseId) {
        return mensajeService.obtenerMensajesGrupo(claseId);
    }
}