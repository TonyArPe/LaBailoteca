package com.bailoteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP.
 * Esta clase configura los puntos finales de WebSocket
 * y el broker de mensajes para la aplicación.
 * Permite la comunicación en tiempo real entre el cliente y el servidor
 * a través de STOMP (Simple Text Oriented Messaging Protocol).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Permitir mensajes a destinos tipo /topic (grupal) y /queue (privado)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para los destinos enviados por el cliente (HTML JS → backend)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para enrutar mensajes a usuarios específicos (privado)
        registry.setUserDestinationPrefix("/user");
    }
}