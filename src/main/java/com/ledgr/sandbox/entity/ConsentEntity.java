package com.ledgr.sandbox.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aa_consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentEntity {

    @Id
    @Column(name = "consent_id", length = 64)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, ACTIVE, REJECTED, REVOKED, EXPIRED

    @Column(name = "consent_types", length = 256)
    private String consentTypes;

    @Column(name = "fi_types", length = 256)
    private String fiTypes;

    @Column(name = "date_range_from")
    private String dateRangeFrom;

    @Column(name = "date_range_to")
    private String dateRangeTo;

    @Column(name = "redirect_url", length = 512)
    private String redirectUrl;

    @Column(name = "callback_url", length = 512)
    private String callbackUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
