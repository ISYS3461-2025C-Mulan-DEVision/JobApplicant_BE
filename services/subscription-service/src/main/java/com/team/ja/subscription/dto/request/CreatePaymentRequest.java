package com.team.ja.subscription.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Builder.Default
    private PayerType payerType = PayerType.APPLICANT;

    /**
     * The ID of the payer
     */
    @Schema(description = "The ID of the payer", required = true)
    private UUID payerId;

    /**
     * The email of the payer
     */
    @Schema(description = "The email of the payer", example = "payer@example.com", required = true)
    @JsonProperty("email")
    @JsonAlias("payerEmail")
    private String payerEmail;

    /**
     * The amount to be paid
     */
    @Schema(description = "The amount to be paid", example = "49.99", required = true)
    @Builder.Default
    private BigDecimal amount = new BigDecimal("10.00");

    /**
     * The currency of the payment
     */
    @Schema(description = "The currency of the payment", example = "USD", required = true)
    @Builder.Default
    private String currency = "USD";

    /**
     * The description of the payment
     */
    @Schema(description = "The description of the payment", example = "Subscription payment for June 2024")
    @Builder.Default
    private String description = "Subscription payment";

}
