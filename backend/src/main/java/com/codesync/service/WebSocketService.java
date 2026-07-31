package com.codesync.service;

import com.codesync.dto.CursorPosition;
import com.codesync.model.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages WebSocket sessions and message broadcasting for real-time
 * collaboration.
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final OTService otService;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Track sessions per room: roomId -> Set of WebSocketSession
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // Track user session info: sessionId -> {userId, username, roomId}
    private final Map<String, Map<String, Object>> sessionInfo = new ConcurrentHashMap<>();

    // Track cursor positions per room: roomId -> Map<userId, CursorPosition>
    private final Map<Long, Map<Long, CursorPosition>> cursorPositions = new ConcurrentHashMap<>();

    private static final String ROOM_USERS_KEY = "room_users:";

    public WebSocketService(OTService otService,
            DocumentService documentService,
            ObjectMapper objectMapper,
            RedisTemplate<String, Object> redisTemplate) {
        this.otService = otService;
        this.documentService = documentService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Handle a user joining a room.
     */
    public void joinRoom(WebSocketSession session, Long roomId, Long userId, String username) {
        // Track session
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);

        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("username", username);
        info.put("roomId", roomId);
        sessionInfo.put(session.getId(), info);

        // Add to cursor positions
        cursorPositions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        // Notify other users
        broadcastToRoom(roomId, createMessage("USER_JOINED",
                Map.of("userId", userId, "username", username)), session);

        // Send current users list to the joining user
        List<Map<String, Object>> usersInRoom = getUsersInRoom(roomId);
        sendToSession(session, createMessage("ROOM_USERS", Map.of("users", usersInRoom)));

        // Send current cursor positions to the joining user
        sendToSession(session, createMessage("CURSOR_POSITIONS",
                Map.of("cursors", new ArrayList<>(cursorPositions.get(roomId).values()))));

        logger.info("User {} joined room {}", username, roomId);
    }

    /**
     * Handle a user leaving a room.
     */
    public void leaveRoom(WebSocketSession session) {
        Map<String, Object> info = sessionInfo.get(session.getId());
        if (info == null)
            return;

        Long roomId = (Long) info.get("roomId");
        Long userId = (Long) info.get("userId");
        String username = (String) info.get("username");

        // Remove from room sessions
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }

        // Remove cursor position
        Map<Long, CursorPosition> cursors = cursorPositions.get(roomId);
        if (cursors != null) {
            cursors.remove(userId);
        }

        // Clean up session info
        sessionInfo.remove(session.getId());

        // Notify other users
        broadcastToRoom(roomId, createMessage("USER_LEFT",
                Map.of("userId", userId, "username", username)), null);

        logger.info("User {} left room {}", username, roomId);
    }

    /**
     * Handle an edit operation from a client.
     */
    public void handleEditOperation(WebSocketSession session, Operation operation) {
        Map<String, Object> info = sessionInfo.get(session.getId());
        if (info == null)
            return;

        Long roomId = (Long) info.get("roomId");
        Long userId = (Long) info.get("userId");
        String username = (String) info.get("username");

        operation.setUserId(userId);
        operation.setUsername(username);

        // Process through OT
        Operation transformedOp = otService.processOperation(operation);

        // Broadcast the transformed operation to all other users in the room
        broadcastToRoom(roomId, createMessage("EDIT_OPERATION", transformedOp), session);

        // Update document content in database periodically (version % 10 == 0)
        if (transformedOp.getVersion() % 10 == 0) {
            String currentContent = documentService.getDocument(operation.getDocumentId()).getContent();
            String newContent = com.codesync.util.OTAlgorithm.applyOperationStatic(currentContent, transformedOp);
            documentService.updateDocument(operation.getDocumentId(), newContent, transformedOp.getVersion());
        }
    }

    /**
     * Handle cursor position update.
     */
    public void updateCursorPosition(WebSocketSession session, CursorPosition cursorPosition) {
        Map<String, Object> info = sessionInfo.get(session.getId());
        if (info == null)
            return;

        Long roomId = (Long) info.get("roomId");
        Long userId = (Long) info.get("userId");

        cursorPosition.setUserId(userId);
        cursorPosition.setUsername((String) info.get("username"));

        // Store cursor position
        Map<Long, CursorPosition> cursors = cursorPositions.computeIfAbsent(roomId,
                k -> new ConcurrentHashMap<>());
        cursors.put(userId, cursorPosition);

        // Broadcast cursor position to other users
        broadcastToRoom(roomId, createMessage("CURSOR_UPDATE", cursorPosition), session);
    }

    /**
     * Broadcast a message to all sessions in a room except the sender.
     */
    public void broadcastToRoom(Long roomId, Map<String, Object> message, WebSocketSession excludeSession) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null)
            return;

        String messageStr;
        try {
            messageStr = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Error serializing message", e);
            return;
        }

        TextMessage textMessage = new TextMessage(messageStr);

        for (WebSocketSession session : sessions) {
            if (excludeSession != null && session.getId().equals(excludeSession.getId())) {
                continue;
            }
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    logger.error("Error sending message to session {}", session.getId(), e);
                }
            }
        }
    }

    /**
     * Send a message to a specific session.
     */
    public void sendToSession(WebSocketSession session, Map<String, Object> message) {
        try {
            String messageStr = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(messageStr));
        } catch (Exception e) {
            logger.error("Error sending message to session {}", session.getId(), e);
        }
    }

    /**
     * Get list of users currently in a room.
     */
    public List<Map<String, Object>> getUsersInRoom(Long roomId) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null)
            return Collections.emptyList();

        List<Map<String, Object>> users = new ArrayList<>();
        Set<Long> addedUserIds = new HashSet<>();

        for (WebSocketSession session : sessions) {
            Map<String, Object> info = sessionInfo.get(session.getId());
            if (info != null) {
                Long userId = (Long) info.get("userId");
                if (!addedUserIds.contains(userId)) {
                    users.add(Map.of(
                            "userId", userId,
                            "username", info.get("username")));
                    addedUserIds.add(userId);
                }
            }
        }

        return users;
    }

    private Map<String, Object> createMessage(String type, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", data);
        return message;
    }
}
