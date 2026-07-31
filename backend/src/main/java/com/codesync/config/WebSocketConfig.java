package com.codesync.config;

import com.codesync.websocket.EditorWebSocketHandler;
import com.codesync.websocket.PresenceWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EditorWebSocketHandler editorWebSocketHandler;
    private final PresenceWebSocketHandler presenceWebSocketHandler;

    public WebSocketConfig(EditorWebSocketHandler editorWebSocketHandler,
            PresenceWebSocketHandler presenceWebSocketHandler) {
        this.editorWebSocketHandler = editorWebSocketHandler;
        this.presenceWebSocketHandler = presenceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(editorWebSocketHandler, "/ws/editor")
                .setAllowedOrigins("*");
        registry.addHandler(presenceWebSocketHandler, "/ws/presence")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536);
        container.setMaxBinaryMessageBufferSize(65536);
        container.setMaxSessionIdleTimeout(600000L); // 10 minutes
        return container;
    }
}
