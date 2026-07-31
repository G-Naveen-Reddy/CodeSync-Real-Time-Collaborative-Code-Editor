package com.codesync.websocket;

import com.codesync.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * Handles WebSocket connections for user presence (online/offline status).
 * Lightweight handler that tracks who is currently active.
 */
@Component
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(PresenceWebSocketHandler.class);

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public PresenceWebSocketHandler(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Presence connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("Presence message: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "HEARTBEAT":
                    // Client is still alive, send acknowledgment
                    webSocketService.sendToSession(session,
                            Map.of("type", "HEARTBEAT_ACK", "data", Map.of()));
                    break;
                case "TYPING":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) messageMap.get("data");
                    Long roomId = Long.valueOf(data.get("roomId").toString());
                    String username = (String) data.get("username");
                    // Broadcast typing indicator to room
                    webSocketService.broadcastToRoom(roomId,
                            Map.of("type", "USER_TYPING",
                                    "data", Map.of("username", username)),
                            session);
                    break;
                default:
                    logger.warn("Unknown presence message type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Error processing presence message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Presence connection closed: {} with status {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Presence transport error on session {}: {}", session.getId(), exception.getMessage());
    }
}
