package com.bailoteca.controller.evento;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bailoteca.models.evento.Evento;
import com.bailoteca.repository.evento.EventoRepo;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controlador REST para gestionar los eventos
 */
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepo eventoRepo;

    /**
     * Obtiene todos los eventos activos
     * @return Lista de eventos
     */
    @GetMapping
    public List<Evento> getEventos() {
        return eventoRepo.findAll();
    }

    /**
     * Obtiene un evento por su ID
     * @param id ID del evento a obtener
     */
    @GetMapping("/{id}")
    public Evento getEvento(
        @PathVariable Long id){
        return eventoRepo.findById(id).orElse(null);
    }

    /**
     * Crea un evento nuevo.
     * SOLO ACCESIBLE PARA ADMIN Y PROFESOR
     * 
     * @param evento Evento a crear
     * @return Evento creado
     */
    @PostMapping

    public Evento createEvento(
        @RequestBody Evento evento) {
        return eventoRepo.save(evento);
    }
    
    /**
     * Actualiza un evento existente
     * SOLO ACCESIBLE PARA ADMIN Y PROFESOR
     * 
     * @param id ID del evento a actualizar
     * @param evento Evento con los nuevos datos
     * @return Evento actualizado o null si no existe
     */
    @PutMapping("/{id}")
    public Evento updateEvento(
    @PathVariable Long id,
    @RequestBody Evento evento) {
        Evento existingEvento = eventoRepo.findById(id).orElse(null);
        if (existingEvento != null) {
            existingEvento.setNombre(evento.getNombre());
            existingEvento.setDescripcion(evento.getDescripcion());
            existingEvento.setFecha(evento.getFecha());
            existingEvento.setLugar(evento.getLugar());
            existingEvento.setEstado(evento.getEstado());
            return eventoRepo.save(existingEvento);
        }
        return null;
    }

    /**
     * Borra un evento por su ID
     * SOLO ACCESIBLE PARA ADMIN Y PROFESOR
     * 
     * @param id ID del evento a borrar
     */
    @DeleteMapping("/{id}")
    public void deleteEvento(
        @PathVariable Long id) {
        eventoRepo.deleteById(id);
    }
}
