package com.bailoteca.models.notificacion;

import com.bailoteca.models.enums.TipoNotificacion;
import com.bailoteca.models.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación enviada a un usuario.
 * Puede ser informativa (evento, clase, sistema, etc.).
 */
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_id", nullable = false)
    private Usuario receptor;

    @Column(length = 10000)
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private boolean leida;

    
    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;
}
