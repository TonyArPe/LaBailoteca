package com.bailoteca.controller;

import com.bailoteca.repository.PagoEventoRepo;

import lombok.RequiredArgsConstructor;

import com.bailoteca.models.PagoEvento;
import com.bailoteca.repository.PagoEventoRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/pagos-evento")
@RequiredArgsConstructor
public class PagoEventoController {
    private final PagoEventoRepo pagoEventoRepo;

    /**
     * Obtener la lista de pagos de los eventos
     * @return Lista de pagos de los eventos
     */
    @GetMapping("path")
    public List<PagoEvento> getAllPagosEvento() {
        return pagoEventoRepo.findAll();
    }

    /**
     * Obtener la lista de pagos de los eventos por id del usuario
     * @param usuarioId id del usuario
     * @return Lista de pagos del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<PagoEvento> getPagosByUsuario(@PathVariable Long usuarioId) {
        return pagoEventoRepo.findByUsuarioId(usuarioId);
    }

    /**
     * Obtener la lista de pagos de los eventos por id del evento
     * @param eventoId id del evento
     * @return Lista de pagos del evento
     */
    @GetMapping("/evento/{eventoId}")
        public List<PagoEvento> getPagosByEvento(@PathVariable Long eventoId) {
            return pagoEventoRepo.findByEventoId(eventoId);
        }

        @PostMapping
    public PagoEvento create(@RequestBody PagoEvento pago) {
        return pagoEventoRepo.save(pago);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        pagoEventoRepo.deleteById(id);
    }
}