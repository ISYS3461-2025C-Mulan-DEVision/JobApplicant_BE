package com.team.ja.common.event;

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
public class PaymentCancelled {

    /**
     * The ID of the cancelled payment.
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
     * Payment cancellation timestamp.
     */
    private LocalDateTime cancelledAt;

}
