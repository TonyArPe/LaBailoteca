package com.bailoteca.models.evento;

import com.bailoteca.models.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

/**
*
* Entidad que representa la asistencia de un usuario a un evento.
*
* Solo puede haber una asistencia por usuario-evento.
*/
@Entity
@Table(name = "asistencias_eventos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"evento_id", "usuario_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que ha marcado asistencia al evento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Evento al que asiste el usuario.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
}