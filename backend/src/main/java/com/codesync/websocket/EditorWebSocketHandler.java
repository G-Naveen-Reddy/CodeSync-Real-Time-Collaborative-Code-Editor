package com.codesync.websocket;

import com.codesync.dto.CursorPosition;
import com.codesync.model.Operation;
import com.codesync.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * Handles WebSocket connections for real-time code editing.
 * Manages edit operations, cursor positions, and room synchronization.
 */
@Component
public class EditorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(EditorWebSocketHandler.class);

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public EditorWebSocketHandler(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("Received message: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "JOIN_ROOM":
                    handleJoinRoom(session, messageMap);
                    break;
                case "LEAVE_ROOM":
                    webSocketService.leaveRoom(session);
                    break;
                case "EDIT_OPERATION":
                    handleEditOperation(session, messageMap);
                    break;
                case "CURSOR_UPDATE":
                    handleCursorUpdate(session, messageMap);
                    break;
                case "REQUEST_DOCUMENT":
                    handleRequestDocument(session, messageMap);
                    break;
                default:
                    logger.warn("Unknown message type: {}", type);
                    webSocketService.sendToSession(session,
                            Map.of("type", "ERROR", "data", Map.of("message", "Unknown message type: " + type)));
            }
        } catch (Exception e) {
            logger.error("Error processing message", e);
            webSocketService.sendToSession(session,
                    Map.of("type", "ERROR", "data", Map.of("message", "Error processing message: " + e.getMessage())));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleJoinRoom(WebSocketSession session, Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        Long roomId = Long.valueOf(data.get("roomId").toString());
        Long userId = Long.valueOf(data.get("userId").toString());
        String username = (String) data.get("username");

        webSocketService.joinRoom(session, roomId, userId, username);
    }

    @SuppressWarnings("unchecked")
    private void handleEditOperation(WebSocketSession session, Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        Operation operation = objectMapper.convertValue(data, Operation.class);
        webSocketService.handleEditOperation(session, operation);
    }

    @SuppressWarnings("unchecked")
    private void handleCursorUpdate(WebSocketSession session, Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        CursorPosition cursorPosition = objectMapper.convertValue(data, CursorPosition.class);
        webSocketService.updateCursorPosition(session, cursorPosition);
    }

    @SuppressWarnings("unchecked")
    private void handleRequestDocument(WebSocketSession session, Map<String, Object> message) {
        // This would be handled via REST API typically
        webSocketService.sendToSession(session,
                Map.of("type", "ERROR", "data", Map.of("message", "Use REST API to fetch documents")));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), status);
        webSocketService.leaveRoom(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error on session {}: {}", session.getId(), exception.getMessage());
        webSocketService.leaveRoom(session);
    }
}
