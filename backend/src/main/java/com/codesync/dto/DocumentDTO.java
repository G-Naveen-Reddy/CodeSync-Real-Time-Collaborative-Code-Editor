package com.codesync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String content;
    private String language;
    private Long ownerId;
    private String ownerName;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
