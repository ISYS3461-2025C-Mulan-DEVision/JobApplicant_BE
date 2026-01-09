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
public class PaymentCompletedEvent {

    /**
     * The ID of the completed payment.
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
     * The amount paid.
     */
    private BigDecimal amount;

    /**
     * The currency of the payment (e.g., USD, EUR).
     */
    private String currency;

    /**
     * Payment create timestamp.
     */
    private LocalDateTime completedAt;

}
