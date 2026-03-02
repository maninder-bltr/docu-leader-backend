package com.maninder.fileBrain.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DocumentDto {
    private UUID id;
    private String fileName;
    private String documentType;
    private String status;
    private long createdAt;
}
