package com.bailoteca.service;

import com.bailoteca.dto.ClaseRequest;
import com.bailoteca.dto.HorarioClaseRequest;
import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.clase.HorarioClaseRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona las clases y sus horarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaseService {

    private final ClaseRepo claseRepo;
    private final HorarioClaseRepo horarioClaseRepo;

    /**
     * Guarda una nueva clase con los horarios asociados.
     */
    @Transactional
    public Clase guardarClaseConHorarios(Clase clase, List<HorarioClase> horarios) {
        if (clase.getProfesor() == null || clase.getProfesor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clase debe tener un profesor asignado");
        }

        Clase claseGuardada = claseRepo.save(clase);
        horarios.forEach(horario -> horario.setClase(claseGuardada));
        horarioClaseRepo.saveAll(horarios);
        claseGuardada.setHorarioClases(horarios);
        return claseGuardada;
    }

    /**
     * Actualiza una clase y sus horarios si el usuario tiene permisos.
     */
    @Transactional
    public Clase actualizarClaseYHorarios(Long id, ClaseRequest request, Usuario usuario) {
        Clase clase = claseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clase no encontrada"));

        Long profesorClaseId = clase.getProfesor() != null ? clase.getProfesor().getId() : null;
        log.debug("🔍 Validando propiedad: clase.profesor.id={} vs usuario.id={}", profesorClaseId, usuario.getId());

        boolean esPropietario = profesorClaseId != null && profesorClaseId.equals(usuario.getId());
        boolean esAdmin = usuario.getRol().name().equals("ADMIN");

        if (!(esPropietario || esAdmin)) {
            log.warn("🚫 Usuario no autorizado: id={} rol={}", usuario.getId(), usuario.getRol());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para editar esta clase");
        }

        log.info("✏️ Actualizando clase ID {} por usuario {}", id, usuario.getCorreo());

        if (request.getNombre() != null) clase.setNombre(request.getNombre());
        if (request.getDescripcion() != null) clase.setDescripcion(request.getDescripcion());
        if (request.getUbicacion() != null) clase.setUbicacion(request.getUbicacion());
        if (request.getVideoPresentacion() != null) clase.setVideoPresentacion(request.getVideoPresentacion());
        if (request.getDificultad() != null) clase.setDificultad(request.getDificultad());
        if (request.getPublica() != null) clase.setPublica(request.getPublica());

        if (request.getHorarioClases() != null && !request.getHorarioClases().isEmpty()) {
            log.info("🔁 Reemplazando horarios...");
            horarioClaseRepo.deleteByClaseId(clase.getId());

            List<HorarioClase> nuevos = request.getHorarioClases().stream()
                    .map(this::mapearHorario)
                    .peek(h -> h.setClase(clase))
                    .collect(Collectors.toList());

            horarioClaseRepo.saveAll(nuevos);
            clase.setHorarioClases(nuevos);
        }

        return claseRepo.save(clase);
    }

    private HorarioClase mapearHorario(HorarioClaseRequest h) {
        return HorarioClase.builder()
                .diaSemana(h.getDiaSemana())
                .horaInicio(h.getHoraInicio())
                .horaFin(h.getHoraFin())
                .build();
    }

    /**
     * Clases públicas visibles para invitados.
     */
    public List<Clase> obtenerTodasLasClasesVisiblesParaInvitados() {
        return claseRepo.findByPublicaTrue();
    }
}