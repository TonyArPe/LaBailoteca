// MensajeService.java — con canales corregidos para evitar duplicados

package com.bailoteca.service.chat;

import com.bailoteca.exceptions.RecursoNoEncontradoException;
import com.bailoteca.models.chat.Mensaje;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.chat.MensajeRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepo mensajeRepo;
    private final UsuarioRepo usuarioRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public Mensaje enviarMensaje(Mensaje mensaje) {
        mensaje.setFechaEnvio(LocalDateTime.now());

        if (mensaje.getEmisor() != null) {
            Usuario emisor = usuarioRepo.findById(mensaje.getEmisor().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Emisor no encontrado"));
            mensaje.setEmisor(emisor);
        }

        if (!mensaje.isEsGrupal() && mensaje.getReceptor() != null) {
            Usuario receptor = usuarioRepo.findById(mensaje.getReceptor().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Receptor no encontrado"));
            mensaje.setReceptor(receptor);
        }

        Mensaje guardado = mensajeRepo.save(mensaje);

        if (mensaje.isEsGrupal()) {
            messagingTemplate.convertAndSend("/topic/clase/" + mensaje.getClase().getId(), guardado);
        } else {
            Long id1 = mensaje.getEmisor().getId();
            Long id2 = mensaje.getReceptor().getId();
            String canal = "/topic/chat/" + Math.min(id1, id2) + "/" + Math.max(id1, id2);
        
            messagingTemplate.convertAndSend(canal, guardado);
        }        
        return guardado;
    }

    public List<Mensaje> obtenerPrivados(Long user1, Long user2) {
        return mensajeRepo.findByEmisorIdAndReceptorIdOrReceptorIdAndEmisorIdOrderByFechaEnvioAsc(
                user1, user2, user1, user2);
    }

    public List<Mensaje> obtenerMensajesGrupo(Long claseId) {
        return mensajeRepo.findByClaseIdOrderByFechaEnvioAsc(claseId);
    }
}