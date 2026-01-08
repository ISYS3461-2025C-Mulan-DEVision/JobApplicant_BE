package com.team.ja.subscription.model;

import java.math.BigDecimal;
import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.PayerType;
import com.team.ja.common.enumeration.PaymentProcessor;
import com.team.ja.common.enumeration.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Invoice payment entity.
 * Contains invoice payment information for user payments in subscription
 * service.
 */
@Entity
@Table(name = "invoice_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseEntity {

    /**
     * The ID of the user associated with this invoice payment
     */
    @Column(name = "user_id", nullable = false)
    @Schema(description = "The ID of the user associated with this invoice payment")
    private UUID userId;

    /**
     * The type of the payer
     * This will default to APPLICANT in job applicant system
     */
    @Column(name = "payer_type", nullable = false)
    @Schema(description = "The type of the payer")
    private PayerType payerType;

    /**
     * The email of the payer (applicant email in job applicant system)
     */
    @Column(name = "payer_email", nullable = false)
    @Schema(description = "The email of the payer")
    private String payerEmail;

    /**
     * The amount of the invoice payment
     * This will default to 10 USD for subscription service in job applicant system
     */
    @Column(name = "amount", nullable = false)
    @Schema(description = "The amount of the invoice payment")
    private BigDecimal amount;

    /**
     * The status of the payment
     */
    @Column(name = "payment_status", nullable = false)
    @Schema(description = "The status of the payment")
    private PaymentStatus paymentStatus;

    /**
     * The currency of the payment
     */
    @Column(name = "currency", nullable = true)
    @Schema(description = "The currency of the payment")
    private String currency;

    /**
     * The type of the payment processor
     * This will depends on the payment processor used in the job manager system
     */
    @Column(name = "processor_type", nullable = false)
    @Schema(description = "The type of the payment processor")
    private PaymentProcessor paymentProcessor;

    /**
     * The transaction ID of the payment
     * This will be the transaction ID returned by the payment processor
     * Used for tracking and reference purposes
     */
    @Column(name = "transaction_id", nullable = true)
    @Schema(description = "The transaction ID of the payment")
    private UUID transactionId;

}
