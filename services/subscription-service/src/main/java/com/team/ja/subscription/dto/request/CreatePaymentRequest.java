package com.team.ja.subscription.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.team.ja.common.enumeration.PayerType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a payment.
 * This would be sent to JM Kafka topic as a Stripe payment intent creation
 * request.
 * The data required to process a payment.(e.g. amount, currency, payment
 * method, etc.)
 * 
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create payment request")
public class CreatePaymentRequest {

    /**
     * The Payer Type
     */
    @Schema(description = "The Payer Type", example = "APPLICANT", required = true)
    private PayerType payerType;

    /**
     * The ID of the payer
     */
    @Schema(description = "The ID of the payer", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private UUID payerId;

    /**
     * The email of the payer
     */
    @Schema(description = "The email of the payer", example = "payer@example.com", required = true)
    private String payerEmail;

    /**
     * The amount to be paid
     */
    @Schema(description = "The amount to be paid", example = "49.99", required = true)
    private BigDecimal amount;

    /**
     * The currency of the payment
     */
    @Schema(description = "The currency of the payment", example = "USD", required = true)
    private String currency;

    /**
     * The description of the payment
     */
    @Schema(description = "The description of the payment", example = "Subscription payment for June 2024")
    private String description;

}
