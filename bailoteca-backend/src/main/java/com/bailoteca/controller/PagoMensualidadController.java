package com.bailoteca.controller;

import com.bailoteca.models.PagoMensualidad;
import com.bailoteca.repository.PagoMensualidadRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos-mensualidad")
@RequiredArgsConstructor
public class PagoMensualidadController {

    private final PagoMensualidadRepo pagoMensualidadRepo;

    @GetMapping
    public List<PagoMensualidad> getAll() {
        return pagoMensualidadRepo.findAll();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<PagoMensualidad> getByUsuario(@PathVariable Long usuarioId) {
        return pagoMensualidadRepo.findByUsuarioId(usuarioId);
    }

    @GetMapping("/mes/{mes}")
    public List<PagoMensualidad> getByMes(@PathVariable String mes) {
        return pagoMensualidadRepo.findByMes(mes);
    }

    @PostMapping
    public PagoMensualidad create(@RequestBody PagoMensualidad pago) {
        return pagoMensualidadRepo.save(pago);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        pagoMensualidadRepo.deleteById(id);
    }
}
