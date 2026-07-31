package com.codesync.controller;

import com.codesync.dto.DocumentDTO;
import com.codesync.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<?> createDocument(@RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getCredentials();
            DocumentDTO document = documentService.createDocument(
                    request.get("title"),
                    request.get("content"),
                    request.get("language"),
                    userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyDocuments(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getCredentials();
            List<DocumentDTO> documents = documentService.getUserDocuments(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long version = Long.valueOf(request.get("version").toString());
            DocumentDTO document = documentService.updateDocument(id, content, version);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
