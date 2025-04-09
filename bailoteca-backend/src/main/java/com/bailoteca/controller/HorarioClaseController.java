package com.bailoteca.controller;

import com.bailoteca.models.HorarioClase;
import com.bailoteca.repository.HorarioClaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios-clase")
@RequiredArgsConstructor
public class HorarioClaseController {

    private final HorarioClaseRepo horarioClaseRepo;

    /**
     * Te muestra todos los horarios.
     */
    @GetMapping
    public List<HorarioClase> getAll() {
        return horarioClaseRepo.findAll();
    }

    /**
     * Muestra todos los horarios de una clase.
     */
    @GetMapping("/clase/{claseId}")
    public List<HorarioClase> getByClase(@PathVariable Long claseId) {
        return horarioClaseRepo.findByClaseId(claseId);
    }

    /**
     * Crear un nuevo horario.
     * SOLO ADMIN Y PROFESOR
     */
    @PostMapping
    public HorarioClase create(@RequestBody HorarioClase horarioClase) {
        return horarioClaseRepo.save(horarioClase);
    }

    /**
     * Eliminar un horario por ID.
     * SOLO ADMIN Y PROFESOR
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        horarioClaseRepo.deleteById(id);
    }
}
