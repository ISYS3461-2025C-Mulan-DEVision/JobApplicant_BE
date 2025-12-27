package com.team.ja.subscription.dto.request;

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
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Schema(description = "Create payment request")
public class CreatePaymentRequest {

}
