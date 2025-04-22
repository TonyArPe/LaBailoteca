package com.bailoteca.models.chat;

import com.bailoteca.models.enums.EstadoMensaje;
import com.bailoteca.models.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que envía el mensaje
    @ManyToOne
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    // Usuario que recibe el mensaje
    @ManyToOne
    @JoinColumn(name = "receptor_id", nullable = false)
    private Usuario receptor;

    // Contenido del mensaje
    @Column(length = 5000)
    private String contenido;

    // Fecha y hora en que se envió el mensaje
    private LocalDateTime fechaEnvio;

    // Estado del mensaje (LEÍDO o NO_LEÍDO)
    @Enumerated(EnumType.STRING)
    private EstadoMensaje estado;
}