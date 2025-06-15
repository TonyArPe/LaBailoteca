package com.bailoteca.models.evento;

import java.time.LocalDateTime;

import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un evento.
 * Los eventos son actividades organizadas por administradores o profesores,
 * y pueden ser visualizados por usuarios registrados.
 */
@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    /**
     * Identificador único del evento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título o nombre del evento.
     */
    private String nombre;

    /**
     * Descripción detallada del evento.
     */
    private String descripcion;

    /**
     * Fecha y hora en que tendrá lugar el evento.
     */
    private LocalDateTime fecha;

    /**
     * Ubicación donde se celebrará el evento.
     */
    private String lugar;

    /**
     * Estado actual del evento (e.g. ACTIVO, CANCELADO).
     * Puede usarse para lógica adicional de visibilidad o gestión.
     */
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;

    /**
     * Usuario organizador del evento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id")
    private Usuario organizador;

    /**
     * Indica si el evento es visible para usuarios no autenticados.
     */
    @Column(nullable = false)
    private boolean publico;

    /**
     * URL relativa de la imagen asociada al evento (almacenada en servidor).
     */
    @Column(name = "url_imagen")
    private String urlImagen;
}