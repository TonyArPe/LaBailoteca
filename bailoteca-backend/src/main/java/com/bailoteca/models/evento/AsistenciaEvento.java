package com.bailoteca.models.evento;

import com.bailoteca.models.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa la asistencia de un usuario a un evento.
 * Guarda si el usuario asistirá y si ha pagado por dicho evento.
 */
@Entity
@Table(name = "asistencias_eventos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Evento al que se asiste.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    /**
     * Usuario que asiste al evento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Indica si el usuario tiene intención de asistir al evento.
     */
    @Column(nullable = false)
    private boolean asistira = false;

    /**
     * Indica si el usuario ya ha pagado por el evento.
     */
    @Column(nullable = false)
    private boolean pagado = false;
}