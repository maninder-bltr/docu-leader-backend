package com.maninder.fileBrain.controller;

import com.maninder.fileBrain.annotation.CurrentUser;
import com.maninder.fileBrain.dto.InvoiceDto;
import com.maninder.fileBrain.dto.ReminderResponse;
import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.service.InvoiceService;
import com.maninder.fileBrain.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<InvoiceDto> uploadInvoice(
            @CurrentUser User user,
            @RequestParam("file") MultipartFile file) throws IOException {

        Invoice invoice = invoiceService.uploadInvoice(file, user);
        return ResponseEntity.ok(mapToResponse(invoice));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getUserInvoices(@CurrentUser User user) {
        List<InvoiceDto> invoices = invoiceService.getUserInvoices(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(
            @CurrentUser User user,
            @PathVariable UUID id) {

        Invoice invoice = invoiceService.getInvoice(user, id);
        return ResponseEntity.ok(mapToResponse(invoice));
    }

    @GetMapping("/all")
    public List<InvoiceDto> getAllInvoices() {
        return invoiceService.getAllInvoices().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/overdue")
    public List<InvoiceDto> getOverdueInvoices() {
        return invoiceService.getOverdueInvoices().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/due-in-next-days")
    public List<InvoiceDto> getInvoicesDueInNextDays(@RequestParam(defaultValue = "7") int days) {
        return invoiceService.getInvoicesDueInNextDays(days).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<Void> markAsPaid(
            @CurrentUser User user,
            @PathVariable UUID id) {

        invoiceService.markAsPaid(user, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/remind")
    public ResponseEntity<ReminderResponse> generateReminder(
            @CurrentUser User user,
            @PathVariable UUID id) {

        String reminder = invoiceService.generateReminder(user, id);
        return ResponseEntity.ok(new ReminderResponse(reminder));
    }

    private InvoiceDto mapToResponse(Invoice invoice) {
        InvoiceDto response = new InvoiceDto();
        response.setId(invoice.getId());
        response.setDocumentId(invoice.getDocument().getId());
        response.setVendorName(invoice.getVendorName());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setIssueDate(DateUtils.formatDate(invoice.getIssueDate()));
        response.setDueDate(DateUtils.formatDate(invoice.getDueDate()));
        response.setTotalAmount(invoice.getTotalAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setStatus(invoice.getStatus().name());
        response.setCreatedAt(invoice.getCreatedAt());
        return response;
    }

    @GetMapping("/{id}/is-overdue")
    public ResponseEntity<Boolean> isOverdue(@PathVariable UUID id) {
        boolean overdue = invoiceService.isOverdue(id);
        return ResponseEntity.ok(overdue);
    }

    private InvoiceDto toDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setDocumentId(invoice.getDocument().getId());
        dto.setVendorName(invoice.getVendorName());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(DateUtils.formatDate(invoice.getIssueDate()));
        dto.setDueDate(DateUtils.formatDate(invoice.getDueDate()));

        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setStatus(invoice.getStatus().name());
        return dto;
    }
}
