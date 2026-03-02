package com.maninder.fileBrain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceDto {
    private UUID id;
    private UUID documentId;
    private String vendorName;
    private String invoiceNumber;
    private String issueDate;     // ISO date string for API
    private String dueDate;        // ISO date string for API
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String status;
    private long createdAt;
}
