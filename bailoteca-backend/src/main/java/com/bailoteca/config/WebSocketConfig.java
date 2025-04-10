package com.bailoteca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configurado WebSocket con STOMP(Simple Text Oriented Messaging Protocol)
 * es un protocolo de mensajería que opera sobre WebSockets, facilita la comunicación
 * entre el cliente y el servidor.
 * Habilita la mensajería basada en WebSocket y configura los endpoints y el broker de mensajes.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Registra el endpoint para que los clientes se conecten al servidor WebSocket
     * Se configura con SockJS para proporcionar compatibilidad con navegadores
     * que no soportan WebSocket nativamente.
     * 
     * @param registry el registro de los endpoints de WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    /**
     * Configura el broker de mensajes.
     * Define los prefijos para las rutas de los mensajes entrantes y salientes
     * 
     * @param registry el registro del broker de mensajes
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Para enviar desde el servidor
        registry.setApplicationDestinationPrefixes("/app"); // Para recibir desde cliente
}
}