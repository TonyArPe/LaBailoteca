package com.bailoteca.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Representa un pago realizado por un usuario para un evento específico.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pagos_evento")
public class PagoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que ha realizado el pago.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Evento al que está asociado el pago.
     */
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    private LocalDate fechaPago;
    private double cantidad;
    private boolean pagado;
}

