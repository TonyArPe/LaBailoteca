package com.bailoteca.models.chat;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Modelo que representa un mensaje en el chat.
 * Un mensaje puede ser privado entre dos usuarios o grupal en una clase.
 * 
 * @author Tony Aragón
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see Clase
 */
@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;

    private LocalDateTime fechaEnvio;

    // Chat privado: emisor y receptor
    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;

    // Chat grupal: relacionado con una clase
    @ManyToOne
    @JoinColumn(name = "clase_id")
    private Clase clase;

    private boolean esGrupal;
}