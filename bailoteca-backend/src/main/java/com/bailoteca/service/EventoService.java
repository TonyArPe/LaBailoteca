package com.bailoteca.service;

import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.evento.EventoRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepo eventoRepo;

    public List<Evento> obtenerEventosAutenticado(Usuario usuario) {
        switch (usuario.getRol()) {
            case ADMIN:
                return eventoRepo.findAll();

            case PROFESOR:
                return eventoRepo.findByOrganizadorId(usuario.getId());

            case USUARIO:
                // Obtener IDs de profesores de sus clases inscritas
                List<Long> profesorIds = usuario.getInscripciones().stream()
                        .filter(i -> i.getClase() != null && i.getClase().getProfesor() != null)
                        .map(i -> i.getClase().getProfesor().getId())
                        .distinct()
                        .toList();

                return eventoRepo.findAll().stream()
                        .filter(e -> profesorIds.contains(e.getOrganizador().getId()))
                        .toList();

            default:
                return List.of(); // Rol INVITADO no llega aquí
        }
    }

    public Evento obtenerPorId(Long id) {
        return eventoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    public List<Evento> obtenerPublicos() {
        return eventoRepo.findByEstado(EstadoEvento.ACTIVO);
    }

    public List<Evento> obtenerPorOrganizador(Long id) {
        return eventoRepo.findByOrganizadorId(id);
    }

    public Evento crearEvento(Evento evento, Usuario organizador) {
        evento.setOrganizador(organizador);
        evento.setEstado(EstadoEvento.ACTIVO);
        return eventoRepo.save(evento);
    }

    @Transactional
    public Evento actualizarEvento(Long id, Evento nuevosDatos, Usuario actual) {
        Evento evento = obtenerPorId(id);

        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esPropietario = evento.getOrganizador() != null &&
                evento.getOrganizador().getId().equals(actual.getId());

        if (!(esAdmin || esPropietario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para editar este evento");
        }

        evento.setNombre(nuevosDatos.getNombre());
        evento.setDescripcion(nuevosDatos.getDescripcion());
        evento.setFecha(nuevosDatos.getFecha());
        evento.setLugar(nuevosDatos.getLugar());
        evento.setEstado(nuevosDatos.getEstado());

        return evento;
    }

    public void eliminarEvento(Long id, Usuario actual) {
        Evento evento = obtenerPorId(id);
        boolean esAdmin = actual.getRol().name().equals("ADMIN");
        boolean esPropietario = evento.getOrganizador() != null &&
                evento.getOrganizador().getId().equals(actual.getId());

        if (!(esAdmin || esPropietario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este evento");
        }

        eventoRepo.delete(evento);
    }
}