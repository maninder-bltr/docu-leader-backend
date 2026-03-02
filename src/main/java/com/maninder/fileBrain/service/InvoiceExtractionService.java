package com.maninder.fileBrain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.InvoiceStatus;
import com.maninder.fileBrain.repository.InvoiceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

@Service
@Slf4j
public class InvoiceExtractionService {

    private final AIService aiService;
    private final InvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvoiceExtractionService(AIService aiService, InvoiceRepository invoiceRepository) {
        this.aiService = aiService;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice extractAndSaveInvoice(Document document, String text, User user) {
        try {
            String jsonResponse = aiService.extractInvoiceData(text);
            JsonNode root = objectMapper.readTree(jsonResponse);

            Invoice invoice = new Invoice();
            invoice.setUser(user);
            invoice.setDocument(document);
            invoice.setVendorName(getText(root, "vendor_name"));
            invoice.setInvoiceNumber(getText(root, "invoice_number"));
            invoice.setIssueDate(parseEpochMillis(getText(root, "issue_date")));
            invoice.setDueDate(parseEpochMillis(getText(root, "due_date")));
            invoice.setTotalAmount(parseBigDecimal(getText(root, "total_amount")));
            invoice.setTaxAmount(parseBigDecimal(getText(root, "tax_amount")));
            invoice.setStatus(InvoiceStatus.UNPAID);

            invoiceRepository.save(invoice);
            log.info("Invoice extracted and saved for document {}", document.getId());
            return invoice;

        } catch (Exception e) {
            log.error("Failed to extract invoice from document {}", document.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private BigDecimal parseBigDecimal(String numStr) {
        if (numStr == null) return null;
        try {
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            log.warn("Could not parse number: {}", numStr);
            return null;
        }
    }

    private Long parseEpochMillis(String dateStr) {
        if (dateStr == null) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date: {}", dateStr);
            return null;
        }
    }
}
