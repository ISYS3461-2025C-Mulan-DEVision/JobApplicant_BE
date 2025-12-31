package com.team.ja.subscription.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.subscription.dto.request.CreatePaymentRequest;
import com.team.ja.subscription.dto.response.PaymentResponse;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Producer for handling payment requests via Kafka.
 * This class is responsible for sending payment creation requests to the
 * external payment system
 */
@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class PaymentProducer {

    private final ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResponse sendPaymentRequest(CreatePaymentRequest createPaymentRequest) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.APPLICANT_PAYMENT_REQUEST,
                createPaymentRequest);
        record.headers().add("reply-topic", KafkaTopics.APPLICANT_PAYMENT_RESPONSE.getBytes());
        RequestReplyFuture<String, Object, Object> future = replyingKafkaTemplate.sendAndReceive(record);

        try {
            ConsumerRecord<String, Object> responseRecord = future.get(10, TimeUnit.SECONDS);
            PaymentResponse paymentResponse = (PaymentResponse) responseRecord.value();

            // TODO: Save the payment response to db with the user id

            return paymentResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for payment response", e);
            throw new RuntimeException("Interrupted while waiting for payment response", e);
        } catch (ExecutionException e) {
            log.error("Error while processing payment response", e);
            throw new RuntimeException("Error while processing payment response", e.getCause());
        } catch (TimeoutException e) {
            log.error("Timed out waiting for payment response", e);
            throw new RuntimeException("Timed out waiting for payment response", e);
        }
    }

}
