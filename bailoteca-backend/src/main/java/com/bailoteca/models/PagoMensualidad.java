package com.bailoteca.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Representa el pago mensual que un usuario hace por sus clases regulares.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pagos_mensualidad")
public class PagoMensualidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que ha realizado el pago.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    private String mes;
    private LocalDate fechaPago;
    private double cantifdad;
    private boolean pagado;
}

