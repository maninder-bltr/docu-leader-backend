package com.maninder.fileBrain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DocumentUploadResponse {
    private UUID documentId;
    private String fileName;
    private String status;
}
