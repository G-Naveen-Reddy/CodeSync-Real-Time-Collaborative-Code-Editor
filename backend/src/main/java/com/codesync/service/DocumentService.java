package com.codesync.service;

import com.codesync.dto.DocumentDTO;
import com.codesync.model.Document;
import com.codesync.model.User;
import com.codesync.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AuthService authService;

    public DocumentService(DocumentRepository documentRepository, AuthService authService) {
        this.documentRepository = documentRepository;
        this.authService = authService;
    }

    public DocumentDTO createDocument(String title, String content, String language, Long ownerId) {
        User owner = authService.getUserById(ownerId);

        Document document = new Document();
        document.setTitle(title);
        document.setContent(content != null ? content : "");
        document.setLanguage(language != null ? language : "plaintext");
        document.setOwner(owner);
        document.setVersion(0L);

        Document saved = documentRepository.save(document);
        return toDTO(saved);
    }

    public DocumentDTO getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toDTO(document);
    }

    public List<DocumentDTO> getUserDocuments(Long userId) {
        User user = authService.getUserById(userId);
        return documentRepository.findByOwner(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DocumentDTO updateDocument(Long documentId, String content, Long version) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setContent(content);
        document.setVersion(version);
        Document saved = documentRepository.save(document);
        return toDTO(saved);
    }

    public DocumentDTO updateDocumentTitle(Long documentId, String title) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        document.setTitle(title);
        Document saved = documentRepository.save(document);
        return toDTO(saved);
    }

    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }

    public boolean existsById(Long documentId) {
        return documentRepository.existsById(documentId);
    }

    private DocumentDTO toDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setContent(document.getContent());
        dto.setLanguage(document.getLanguage());
        dto.setOwnerId(document.getOwner().getId());
        dto.setOwnerName(document.getOwner().getUsername());
        dto.setVersion(document.getVersion());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }
}
