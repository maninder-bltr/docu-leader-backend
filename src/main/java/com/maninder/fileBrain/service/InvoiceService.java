package com.maninder.fileBrain.service;

import com.maninder.fileBrain.entity.Document;
import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.InvoiceStatus;
import com.maninder.fileBrain.exception.InvoiceNotFoundException;
import com.maninder.fileBrain.repository.InvoiceRepository;
import com.maninder.fileBrain.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AIService aiService;
    private final DocumentService documentService;
    private final InvoiceExtractionService invoiceExtractionService;

    public InvoiceService(InvoiceRepository invoiceRepository, AIService aiService, DocumentService documentService, InvoiceExtractionService invoiceExtractionService) {
        this.invoiceRepository = invoiceRepository;
        this.aiService = aiService;
        this.documentService = documentService;
        this.invoiceExtractionService = invoiceExtractionService;
    }

    @Transactional
    public Invoice uploadInvoice(MultipartFile file, User user) throws IOException {
        // 1. Store file as document
        Document document = documentService.storeFile(file, user);

        // 2. Extract text using Tika (reuse existing method)
        Path filePath = Paths.get(document.getFilePath());
        String extractedText = documentService.extractTextFromFile(filePath);

        // 3. Extract and save invoice
        return invoiceExtractionService.extractAndSaveInvoice(document, extractedText, user);
    }

    // Helper methods for epoch conversion
    private long getStartOfDayEpoch(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private long getEndOfDayEpoch(LocalDate date) {
        return date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoice(User user, UUID invoiceId) {
        return invoiceRepository.findByIdAndUser(invoiceId, user)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    }

    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));
    }

    public List<Invoice> getUserInvoices(User user) {
        return invoiceRepository.findByUser(user);
    }

    @Transactional
    public void markAsPaid(User user, UUID invoiceId) {
        Invoice invoice = getInvoice(user, invoiceId);
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid by user {}", invoiceId, user.getEmail());
    }

    public String generateReminder(User user, UUID invoiceId) {
        Invoice invoice = getInvoice(user, invoiceId);

        String reminder = aiService.generateReminder(
                invoice.getVendorName(),
                invoice.getInvoiceNumber(),
                invoice.getDueDate()
        );

        log.info("Reminder generated for invoice {}", invoiceId);
        return reminder;
    }

    /**
     * Get invoices that are overdue (due date < current time and status UNPAID)
     */
    public List<Invoice> getOverdueInvoices() {
        long now = DateUtils.now();
        return invoiceRepository.findOverdueInvoices(now);
    }

    /**
     * Get invoices due today
     */
    public List<Invoice> getInvoicesDueToday() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long startOfDay = getStartOfDayEpoch(today);
        long endOfDay = getEndOfDayEpoch(today);

        return invoiceRepository.findInvoicesDueBetween(startOfDay, endOfDay);
    }

    /**
     * Get invoices due in the next N days
     */
    public List<Invoice> getInvoicesDueInNextDays(int days) {
        long now = DateUtils.now();
        long end = now + (days * 24L * 60 * 60 * 1000);
        return invoiceRepository.findInvoicesDueBetween(now, end);
    }

    public boolean isOverdue(UUID id) {
        Invoice invoice = getInvoice(id);
        if (invoice.getDueDate() == null || invoice.getStatus() != InvoiceStatus.UNPAID) {
            return false;
        }
        return invoice.getDueDate() < DateUtils.now();
    }

    public List<Invoice> getUnpaidInvoicesDueBetween(long start, long end) {
        return invoiceRepository.findUnpaidInvoicesDueBetween(start, end);
    }

}
