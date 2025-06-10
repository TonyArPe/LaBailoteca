package com.bailoteca.controller.horario;

import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.repository.clase.HorarioClaseRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador REST para la gestión de Horarios de Clases.
 * Permite obtener, crear y eliminar horarios de clases.
 * Este controlador proporciona endpoints para acceder a los horarios
 * de clases, tanto para todas las clases como para una clase específica.
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see HorarioClase
 * @see HorarioClaseRepo
 */
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
