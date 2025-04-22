package com.bailoteca.controller.chat;

import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.service.chat.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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