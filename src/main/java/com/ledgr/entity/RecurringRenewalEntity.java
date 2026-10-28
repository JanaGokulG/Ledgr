package com.ledgr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_renewals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringRenewalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "service_name", nullable = false, length = 128)
    private String serviceName;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "funding_account_number", nullable = false, length = 64)
    private String fundingAccountNumber;

    @Column(name = "billing_cycle", length = 32)
    private String billingCycle; // MONTHLY, QUARTERLY, ANNUAL

    @Column(name = "mandate_type", length = 64)
    private String mandateType; // UPI_AUTOPAY, NACH_E_MANDATE, DEBIT_CARD, BANK_STANDING_INSTRUCTION

    @Column(name = "advisory_note", length = 512)
    private String advisoryNote;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
