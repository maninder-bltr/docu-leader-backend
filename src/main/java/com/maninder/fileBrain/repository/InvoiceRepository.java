package com.maninder.fileBrain.repository;

import com.maninder.fileBrain.entity.Invoice;
import com.maninder.fileBrain.entity.User;
import com.maninder.fileBrain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByUser(User user);

    Optional<Invoice> findByIdAndUser(UUID id, User user);

    List<Invoice> findByUserAndStatus(User user, InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.dueDate < :currentTime AND i.status = 'UNPAID'")
    List<Invoice> findOverdueInvoicesByUser(@Param("user") User user, @Param("currentTime") long currentTime);

    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.dueDate BETWEEN :start AND :end AND i.status = 'UNPAID'")
    List<Invoice> findInvoicesDueBetweenByUser(
            @Param("user") User user,
            @Param("start") long start,
            @Param("end") long end
    );

    // For scheduler - across all users
    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :start AND :end AND i.status = 'UNPAID'")
    List<Invoice> findUnpaidInvoicesDueBetween(@Param("start") long start, @Param("end") long end);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :now AND :end AND i.status = 'UNPAID'")
    List<Invoice> findInvoicesDueBetween(long now, long end);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :now AND i.status = 'UNPAID'")
    List<Invoice> findOverdueInvoices(long now);
}
