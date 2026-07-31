package com.codesync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPosition {
    private Long userId;
    private String username;
    private String documentId;
    private int line;
    private int column;
    private String color;
}
