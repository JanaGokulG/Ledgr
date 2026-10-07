package com.ledgr.sandbox.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aa_fi_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiSessionEntity {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "consent_id", nullable = false, length = 64)
    private String consentId;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, READY, COMPLETED, FAILED

    @Column(name = "date_range_from")
    private String dateRangeFrom;

    @Column(name = "date_range_to")
    private String dateRangeTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
