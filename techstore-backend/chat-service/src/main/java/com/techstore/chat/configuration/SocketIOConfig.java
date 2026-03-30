package com.techstore.chat.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;

@Configuration
public class SocketIOConfig {
    @Value("${socket.port}")
    private int port;

    @Value("${socket.origin}")
    private String origin;

    @Bean
    SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();

        configuration.setPort(port);
        configuration.setOrigin(origin);

        return new SocketIOServer(configuration);
    }
}
