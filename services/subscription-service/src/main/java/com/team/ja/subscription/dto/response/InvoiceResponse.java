package com.team.ja.subscription.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.team.ja.common.enumeration.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    /**
     * The unique identifier for the invoice
     */
    private UUID invoiceId;

    /**
     * The status of the payment
     */
    private PaymentStatus paymentStatus;

    /**
     * The amount paid
     */
    private BigDecimal amount;

    /**
     * The currency of the payment
     */
    private String currency;

    /**
     * The payer email
     */
    private String payerEmail;

}
