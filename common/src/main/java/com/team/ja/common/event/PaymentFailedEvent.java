package com.team.ja.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {

    /**
     * The ID of the failed payment.
     */
    private UUID paymentId;

    /**
     * The type of the payer (e.g., APPLICANT, COMPANY).
     */
    private String payerType;

    /**
     * The ID of the payer (user or company).
     */
    private UUID payerId;

    /**
     * The email of the payer.
     */
    private String email;

    /**
     * The amount attempted to be paid.
     */
    private BigDecimal amount;

    /**
     * The currency of the payment (e.g., USD, EUR).
     */
    private String currency;

    /**
     * Reason for payment failure.
     */
    private String failureReason;

    /**
     * Payment failure timestamp.
     */
    private LocalDateTime failedAt;

}
