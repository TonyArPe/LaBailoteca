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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar Clases y sus horarios.
 * Incluye lógica de validación de propiedad, edición segura y control de
 * duplicados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaseService {

    private final ClaseRepo claseRepo;
    private final HorarioClaseRepo horarioClaseRepo;

    /**
     * Guarda una nueva clase con horarios.
     */
    @Transactional
    public Clase guardarClaseConHorarios(Clase clase, List<HorarioClase> horarios) {
        if (clase.getProfesor() == null || clase.getProfesor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clase debe tener un profesor asignado");
        }

        Clase claseGuardada = claseRepo.save(clase);
        horarios.forEach(h -> h.setClase(claseGuardada));
        horarioClaseRepo.saveAll(horarios);
        claseGuardada.setHorarioClases(horarios);
        return claseGuardada;
    }

    /**
     * Actualiza una clase y sincroniza sus horarios:
     * - Crea nuevos si no tienen ID.
     * - Actualiza existentes si coinciden por ID.
     * - Elimina los que no están incluidos en la petición.
     *
     * @param id      ID de la clase a actualizar
     * @param request Datos nuevos de la clase
     * @param usuario Usuario autenticado que intenta actualizar
     * @return Clase actualizada
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

        // Actualizar campos básicos
        if (request.getNombre() != null)
            clase.setNombre(request.getNombre());
        if (request.getDescripcion() != null)
            clase.setDescripcion(request.getDescripcion());
        if (request.getUbicacion() != null)
            clase.setUbicacion(request.getUbicacion());
        if (request.getVideoPresentacion() != null)
            clase.setVideoPresentacion(request.getVideoPresentacion());
        if (request.getDificultad() != null)
            clase.setDificultad(request.getDificultad());
        if (request.getPublica() != null)
            clase.setPublica(request.getPublica());

        // Actualizar horarios
        if (request.getHorarioClases() != null) {
            log.info("🔁 Sincronizando horarios...");

            // 1. Cargar horarios actuales
            List<HorarioClase> actuales = horarioClaseRepo.findByClaseId(clase.getId());

            log.info("📥 Recibidos {} horarios", request.getHorarioClases().size());
            for (HorarioClaseRequest h : request.getHorarioClases()) {
                log.info("📅 Horario -> id: {}, día: {}, inicio: {}, fin: {}",
                        h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin());
            }

            // 2. Mapear nuevas entradas
            List<HorarioClase> actualizados = request.getHorarioClases().stream()
                    .map(req -> {
                        if (req.getId() != null) {
                            return actuales.stream()
                                    .filter(h -> h.getId().equals(req.getId()))
                                    .findFirst()
                                    .map(hExistente -> {
                                        hExistente.setDiaSemana(req.getDiaSemana());
                                        hExistente.setHoraInicio(req.getHoraInicio());
                                        hExistente.setHoraFin(req.getHoraFin());
                                        return hExistente;
                                    }).orElseGet(() -> crearNuevoHorario(req, clase));
                        } else {
                            return crearNuevoHorario(req, clase);
                        }
                    })
                    .collect(Collectors.toList());

            // 3. Eliminar los que no estén en el request
            List<Long> idsEnRequest = request.getHorarioClases().stream()
                    .map(HorarioClaseRequest::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<HorarioClase> aEliminar = actuales.stream()
                    .filter(h -> !idsEnRequest.contains(h.getId()))
                    .toList();

            log.info("🧹 Eliminando horarios obsoletos: {}", aEliminar.size());
            actuales.removeAll(aEliminar);
            clase.getHorarioClases().clear();
            clase.getHorarioClases().addAll(actualizados);
        }

        return claseRepo.save(clase);
    }

    private HorarioClase crearNuevoHorario(HorarioClaseRequest req, Clase clase) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario nulo");
        }

        if (req.getDiaSemana() == null || req.getHoraInicio() == null || req.getHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horario incompleto (día, horaInicio o horaFin es null)");
        }

        HorarioClase nuevo = new HorarioClase();
        nuevo.setDiaSemana(req.getDiaSemana());
        nuevo.setHoraInicio(req.getHoraInicio());
        nuevo.setHoraFin(req.getHoraFin());
        nuevo.setClase(clase);
        return nuevo;
    }

    /**
     * Obtiene todas las clases públicas visibles por usuarios no autenticados.
     */
    public List<Clase> obtenerTodasLasClasesVisiblesParaInvitados() {
        return claseRepo.findByPublicaTrue();
    }
}