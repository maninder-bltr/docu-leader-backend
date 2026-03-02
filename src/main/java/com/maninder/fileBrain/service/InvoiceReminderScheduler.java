package com.maninder.fileBrain.service;

import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class InvoiceReminderScheduler {

    private final InvoiceService invoiceService;
    private final AIService aiService;
    private final ReminderLogService reminderLogService;

    @Scheduled(cron = "0 0 9 * * ?") // 9 AM every day
    public void sendInvoiceReminders() {
        log.info("Starting invoice reminder scheduler");

        long now = DateUtils.now();
        long threeDaysFromNow = now + (3 * 24 * 60 * 60 * 1000L);

        // Find unpaid invoices due in next 3 days
        List<Invoice> upcomingInvoices = invoiceService.getUnpaidInvoicesDueBetween(now, threeDaysFromNow);

        log.info("Found {} unpaid invoices due in next 3 days", upcomingInvoices.size());

        for (Invoice invoice : upcomingInvoices) {
            try {
                // Generate reminder message
                String reminder = aiService.generateReminder(
                        invoice.getVendorName(),
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate()
                );
                reminderLogService.logReminder(invoice, reminder);

                log.info("Generated reminder for invoice {}: {}",
                        invoice.getInvoiceNumber(), reminder.substring(0, Math.min(50, reminder.length())));

            } catch (Exception e) {
                log.error("Failed to generate reminder for invoice {}", invoice.getId(), e);
            }
        }

        log.info("Completed invoice reminder scheduler");
    }
}
