package com.codesync.controller;

import com.codesync.model.Room;
import com.codesync.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getCredentials();
            Room room = roomService.createRoom(
                    request.get("name"),
                    request.get("description"),
                    request.get("language"),
                    userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        try {
            Room room = roomService.getRoom(id);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getActiveRooms() {
        List<Room> rooms = roomService.getActiveRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyRooms(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getCredentials();
            List<Room> rooms = roomService.getUserRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeRoom(@PathVariable Long id) {
        try {
            roomService.closeRoom(id);
            return ResponseEntity.ok(Map.of("message", "Room closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
