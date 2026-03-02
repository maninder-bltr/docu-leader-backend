package com.maninder.fileBrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reminder_logs")
@Getter
@Setter
public class ReminderLog extends BaseEntity {

    @ManyToOne
    private Invoice invoice;

    @Column(columnDefinition = "TEXT")
    private String reminderMessage;

    private String status; // SENT, FAILED

    @Column(name = "sent_at")
    private Long sentAt;
}
