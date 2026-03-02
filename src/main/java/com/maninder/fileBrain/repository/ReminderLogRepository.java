package com.maninder.fileBrain.repository;

import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderLogRepository extends JpaRepository<ReminderLog, UUID> {

    List<ReminderLog> findByInvoice(Invoice invoice);

    List<ReminderLog> findByInvoiceOrderByCreatedAtDesc(Invoice invoice);
}
