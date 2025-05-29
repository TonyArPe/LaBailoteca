package com.bailoteca.service;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.clase.HorarioClaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de la lógica relacionada con Clases y sus horarios.
 * Aquí se maneja la creación y edición conjunta de Clases con sus Horarios asociados.
 */
@Service
@RequiredArgsConstructor
public class ClaseService {

    private final ClaseRepo claseRepo;
    private final HorarioClaseRepo horarioClaseRepo;

    /**
     * Crea una nueva clase con su lista de horarios y persiste ambos en la base de datos.
     *
     * @param clase Clase base sin horarios.
     * @param horarios Lista de horarios a asociar a la clase.
     * @return La clase guardada con los horarios ya persistidos.
     */
    @Transactional
    public Clase guardarClaseConHorarios(Clase clase, List<HorarioClase> horarios) {
        Clase claseGuardada = claseRepo.save(clase);

        for (HorarioClase horario : horarios) {
            horario.setClase(claseGuardada); // Asocia clase a cada horario
        }

        horarioClaseRepo.saveAll(horarios);
        claseGuardada.setHorarioClases(horarios);

        return claseGuardada;
    }

    /**
     * Actualiza una clase existente junto con su lista de horarios.
     * Se eliminan los horarios anteriores y se reemplazan por los nuevos.
     *
     * @param clase Clase con nuevos datos (ya contiene ID).
     * @param horariosNuevos Lista de nuevos horarios.
     * @return Clase actualizada con nuevos horarios.
     */
    @Transactional
    public Clase actualizarClaseYHorarios(Clase clase, List<HorarioClase> horariosNuevos) {
        // Eliminamos horarios anteriores
        horarioClaseRepo.deleteByClaseId(clase.getId());

        for (HorarioClase horario : horariosNuevos) {
            horario.setClase(clase); // Asocia clase a cada nuevo horario
        }

        horarioClaseRepo.saveAll(horariosNuevos);
        clase.setHorarioClases(horariosNuevos);

        return claseRepo.save(clase);
    }

    /**
     * Devuelve todas las clases marcadas como públicas.
     * Este método es utilizado por usuarios invitados.
     */
    public List<Clase> obtenerTodasLasClasesVisiblesParaInvitados() {
        return claseRepo.findByPublicaTrue();
    }
}