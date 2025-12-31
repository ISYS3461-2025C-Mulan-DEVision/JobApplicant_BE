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

    private UUID invoiceId;

    private UUID userId;

    private PayerType payerType;

    private String payerEmail;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private PaymentProcessor paymentProcessor;

    private UUID transactionId;

}
