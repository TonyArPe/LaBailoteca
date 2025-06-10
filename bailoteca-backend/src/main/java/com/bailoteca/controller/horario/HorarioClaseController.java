package com.bailoteca.controller.horario;

import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.repository.clase.HorarioClaseRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador que maneja las operaciones relacionadas con los horarios de clases.
 * Permite crear, listar y eliminar horarios de clases.
 * 
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
     * Obtiene todos los horarios de clases.
     *
     * @return Lista de todos los horarios de clases
     */
    @GetMapping
    public List<HorarioClase> getAll() {
        return horarioClaseRepo.findAll();
    }

    /**
     * Obtiene los horarios de una clase específica por su ID.
     *
     * @param claseId ID de la clase
     * @return Lista de horarios de la clase
     */
    @GetMapping("/clase/{claseId}")
    public List<HorarioClase> getByClase(@PathVariable Long claseId) {
        return horarioClaseRepo.findByClaseId(claseId);
    }

    /**
     * Crea un nuevo horario de clase.
     * SOLO ADMIN Y PROFESOR
     *
     * @param horarioClase Horario de clase a crear
     * @return El horario de clase creado
     */
    @PostMapping
    public HorarioClase create(@RequestBody HorarioClase horarioClase) {
        return horarioClaseRepo.save(horarioClase);
    }

    /**
     * Actualiza un horario de clase existente.
     * SOLO ADMIN Y PROFESOR
     *
     * @param id ID del horario de clase a actualizar
     * @param horarioClase Datos actualizados del horario de clase
     * @return El horario de clase actualizado
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        horarioClaseRepo.deleteById(id);
    }
}
