package com.bailoteca.controller.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.service.chat.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los mensajes en el chat.
 * Permite obtener mensajes privados entre dos usuarios y mensajes de grupo en una clase.
 * 
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

    /**
     * Obtiene los mensajes privados entre dos usuarios.
     *
     * @param user1 ID del primer usuario
     * @param user2 ID del segundo usuario
     * @return Lista de mensajes privados entre los dos usuarios
     */
    @GetMapping("/privado/{user1}/{user2}")
    public List<Mensaje> obtenerMensajesPrivados(@PathVariable Long user1, @PathVariable Long user2) {
        return mensajeService.obtenerPrivados(user1, user2);
    }

    /**
     * Obtiene los mensajes de un grupo asociado a una clase.
     *
     * @param claseId ID de la clase
     * @return Lista de mensajes del grupo
     */
    @GetMapping("/grupo/{claseId}")
    public List<Mensaje> obtenerMensajesGrupo(@PathVariable Long claseId) {
        return mensajeService.obtenerMensajesGrupo(claseId);
    }
}