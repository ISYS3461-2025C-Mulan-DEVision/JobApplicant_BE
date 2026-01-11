package com.team.ja.subscription.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.team.ja.common.enumeration.PayerType;
import com.team.ja.common.enumeration.PaymentProcessor;
import com.team.ja.common.enumeration.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * The unique identifier for the invoice payment
     */
    private UUID paymentId;

    /**
     * Secret identifier for the payment
     */
    private String clientSecret;

    /**
     * The Stripe payment intent ID
     */
    private String paymentIntentId;

}
