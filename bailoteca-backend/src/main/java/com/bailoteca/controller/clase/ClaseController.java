package com.bailoteca.controller.clase;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.repository.clase.ClaseRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseController {

    private final ClaseRepo claseRepo;

    /**
     * Obtener todas las clases.
     */
    @GetMapping
    public List<Clase> getAll() {
        return claseRepo.findAll();
    }

    /**
     * Obtener clases por ID del profesor.
     */
    @GetMapping("/profesor/{profesorId}")
    public List<Clase> getByProfesor(@PathVariable Long profesorId) {
        return claseRepo.findByProfesorId(profesorId);
    }

    /**
     * Obtener clase por ID.
     */
    @GetMapping("/{id}")
    public Clase getById(@PathVariable Long id) {
        return claseRepo.findById(id).orElse(null);
    }

    /**
     * Crear una nueva clase.
     */
    @PostMapping
    public Clase create(@RequestBody Clase clase) {
        return claseRepo.save(clase);
    }

    /**
     * Actualizar una clase existente.
     */
    @PutMapping("/{id}")
    public Clase update(@PathVariable Long id, @RequestBody Clase claseData) {
        return claseRepo.findById(id).map(clase -> {
            clase.setNombre(claseData.getNombre());
            clase.setDescripcion(claseData.getDescripcion());
            clase.setProfesor(claseData.getProfesor());
            clase.setVideoPresentacion(claseData.getVideoPresentacion());
            return claseRepo.save(clase);
        }).orElse(null);
    }

    /**
     * Eliminar una clase por ID.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        claseRepo.deleteById(id);
    }
}