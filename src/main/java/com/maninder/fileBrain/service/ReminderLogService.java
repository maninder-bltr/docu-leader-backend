package com.maninder.fileBrain.service;

import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.ReminderLog;
import com.maninder.fileBrain.repository.ReminderLogRepository;
import com.maninder.fileBrain.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderLogService {

    private final ReminderLogRepository reminderLogRepository;

    public void logReminder(Invoice invoice, String reminderMessage) {
        ReminderLog log = new ReminderLog();
        log.setInvoice(invoice);
        log.setReminderMessage(reminderMessage);
        log.setStatus("GENERATED");
        log.setSentAt(DateUtils.now());

        reminderLogRepository.save(log);
    }

    // For future use when email is integrated
    public void markAsSent(ReminderLog log) {
        log.setStatus("SENT");
        reminderLogRepository.save(log);
    }
}
