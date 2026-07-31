package com.codesync.service;

import com.codesync.dto.DocumentDTO;
import com.codesync.model.Room;
import com.codesync.model.User;
import com.codesync.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final AuthService authService;
    private final DocumentService documentService;

    public RoomService(RoomRepository roomRepository,
            AuthService authService,
            DocumentService documentService) {
        this.roomRepository = roomRepository;
        this.authService = authService;
        this.documentService = documentService;
    }

    public Room createRoom(String name, String description, String language, Long createdByUserId) {
        User createdBy = authService.getUserById(createdByUserId);

        // Create a default document for the room
        DocumentDTO document = documentService.createDocument(
                name + " - Code",
                "// Welcome to " + name + "\n// Start coding together!\n",
                language != null ? language : "javascript",
                createdByUserId);

        Room room = new Room();
        room.setName(name != null ? name : "room-" + UUID.randomUUID().toString().substring(0, 8));
        room.setDescription(description);
        room.setDocument(new com.codesync.model.Document());
        room.getDocument().setId(document.getId());
        room.setCreatedBy(createdBy);
        room.setIsActive(true);
        room.setCreatedBy(createdBy);

        return roomRepository.save(room);
    }

    public Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public Room getRoomByName(String name) {
        return roomRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    public List<Room> getUserRooms(Long userId) {
        User user = authService.getUserById(userId);
        return roomRepository.findByCreatedBy(user);
    }

    public void closeRoom(Long roomId) {
        Room room = getRoom(roomId);
        room.setIsActive(false);
        roomRepository.save(room);
    }

    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }
}
