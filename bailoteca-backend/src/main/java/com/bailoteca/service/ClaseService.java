package com.bailoteca.service;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.repository.clase.ClaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaseService {

    private final ClaseRepo claseRepo;

    /**
     * Obtiene todas las clases visibles públicamente.
     * Esto incluye clases que son públicas y aquellas que son privadas pero visibles para el usuario autenticado.
     * @return Lista de clases visibles públicamente.
     */
    public List<Clase> obtenerTodasLasClasesVisiblesParaInvitados() {
        return claseRepo.findByPublicaTrue();
    }
}