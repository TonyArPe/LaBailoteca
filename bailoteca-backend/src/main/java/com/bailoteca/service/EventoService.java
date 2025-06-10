package com.bailoteca.service;

import com.bailoteca.dto.EventoDTO;
import com.bailoteca.dto.EventoRequest;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona la lógica de negocio relacionada con los eventos.
 * Controla los permisos de acceso y transformación entre entidades y DTOs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepo eventoRepo;
    private final UsuarioRepo usuarioRepo;

    // Crear evento desde request
    public Evento crearEventoDesdeRequest(EventoRequest request, Usuario usuario) {
        Evento evento = new Evento();
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        evento.setLugar(request.getLugar());
        evento.setEstado(request.getEstado());
        evento.setPublico(request.isPublico());
        evento.setOrganizador(usuario);
        return eventoRepo.save(evento);
    }

    // Actualizar evento desde request
    public Evento actualizarEventoDesdeRequest(Long id, EventoRequest datos, Usuario usuario) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (evento.getOrganizador().getId().equals(usuario.getId()) || usuario.getRol().name().equals("ADMIN")) {
            evento.setNombre(datos.getNombre());
            evento.setDescripcion(datos.getDescripcion());
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            evento.setLugar(datos.getLugar());
            evento.setEstado(datos.getEstado());
            evento.setPublico(datos.isPublico());
            return eventoRepo.save(evento);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para actualizar este evento");
        }
    }

    // Eliminar evento
    public void eliminarEvento(Long id, Usuario usuario) {
        Evento evento = eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (evento.getOrganizador().getId().equals(usuario.getId()) || usuario.getRol().name().equals("ADMIN")) {
            eventoRepo.delete(evento);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este evento");
        }
    }

    // Obtener eventos según el rol
    public List<EventoDTO> obtenerEventosAutenticado(Usuario usuario) {
        List<Evento> eventos;
        switch (usuario.getRol()) {
            case ADMIN:
                eventos = eventoRepo.findAll();
                break;
            case PROFESOR:
                eventos = eventoRepo.findByOrganizadorId(usuario.getId());
                break;
            case USUARIO:
                eventos = eventoRepo.findAll().stream()
                        .filter(e -> e.getOrganizador().getId().equals(usuario.getId()))
                        .collect(Collectors.toList());
                break;
            default:
                eventos = List.of();
                break;
        }
        return eventos.stream().map(EventoDTO::from).collect(Collectors.toList());
    }
}