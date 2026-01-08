package com.team.ja.subscription.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.enumeration.PayerType;
import com.team.ja.common.enumeration.PaymentProcessor;
import com.team.ja.common.enumeration.PaymentStatus;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.PaymentCompletedEvent;
import com.team.ja.subscription.model.Invoice;
import com.team.ja.subscription.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceConsumer {

    private final InvoiceRepository invoiceRepository;

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_COMPLETED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Received payment completed event for invoice processing: {}", event);

        // TODO: Implement notification to user about invoice generation

        Invoice invoice = new Invoice();
        // The user ID associated with the payment
        invoice.setUserId(event.getPayerId());
        // The payer type default to APPLICANT
        invoice.setPayerType(PayerType.APPLICANT);
        // The payer email
        invoice.setPayerEmail(event.getEmail());
        // The amount
        invoice.setAmount(event.getAmount());
        // Payment status
        invoice.setPaymentStatus(PaymentStatus.SUCCEEDED);
        // The transaction id for reference
        invoice.setTransactionId(event.getPaymentId());
        // Save the invoice record
        invoiceRepository.save(invoice);
    }

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_FAILED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentFailedEvent(PaymentCompletedEvent event) {
        log.info("Received payment failed event for invoice processing: {}", event);

        // TODO: Implement notification to user about invoice generation for failed
        // payment

        Invoice invoice = new Invoice();
        // The user ID associated with the payment
        invoice.setUserId(event.getPayerId());
        // The payer type default to APPLICANT
        invoice.setPayerType(PayerType.APPLICANT);
        // The payer email
        invoice.setPayerEmail(event.getEmail());
        // The amount
        invoice.setAmount(event.getAmount());
        // Payment status
        invoice.setPaymentStatus(PaymentStatus.FAILED);
        // The currency of the payment
        invoice.setCurrency(event.getCurrency());
        // The processor of the payment, default to STRIPE
        invoice.setPaymentProcessor(PaymentProcessor.STRIPE);
        // The transaction id for reference
        invoice.setTransactionId(event.getPaymentId());
        // Save the invoice record
        invoiceRepository.save(invoice);
    }

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_CANCELLED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCancelledEvent(PaymentCompletedEvent event) {
        log.info("Received payment cancelled event for invoice processing: {}", event);

        // TODO: Implement notification to user about invoice generation for cancelled

        Invoice invoice = new Invoice();
        // The user ID associated with the payment
        invoice.setUserId(event.getPayerId());
        // The payer type default to APPLICANT
        invoice.setPayerType(PayerType.APPLICANT);
        // The payer email
        invoice.setPayerEmail(event.getEmail());
        // The amount
        invoice.setAmount(event.getAmount());
        // Payment status
        invoice.setPaymentStatus(PaymentStatus.CANCELLED);
        // The transaction id for reference
        invoice.setTransactionId(event.getPaymentId());
        // Save the invoice record
        invoiceRepository.save(invoice);
    }
}