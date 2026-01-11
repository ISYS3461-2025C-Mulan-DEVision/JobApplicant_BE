package com.team.ja.subscription.config;

import com.team.ja.common.event.PaymentCompletedEvent;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for subscription-service.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Consumer factory for PaymentCompletedEvent messages.
     * Uses ErrorHandlingDeserializer to properly handle deserialization errors
     * and prevent infinite retry loops.
     */
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Use ErrorHandlingDeserializer as wrapper to handle deserialization errors
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Configure the actual deserializers
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Set default value type for messages without type headers
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentCompletedEvent.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.team.ja.common.event");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // Add error handler with backoff: 2 seconds interval, max 3 retries
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));
        return factory;
    }

    /**
     * Producer factory for UserSearchProfileUpdateEvent messages.
     */
    @Bean
    public ProducerFactory<String, UserSearchProfileUpdateEvent> userSearchProfileProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Generic Producer factory for Object type messages.
     * 
     * @param producerFactory
     * @return
     */
    @Bean
    public ProducerFactory<String, Object> genericProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Producer factory for SubscriptionActivateEvent messages.
     * 
     * @param ProducerFactory
     * @return
     */
    @Bean
    public ProducerFactory<String, SubscriptionActivateEvent> subscriptionActivateProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Producer factory for SubscriptionDeactivateEvent messages.
     * 
     * @param subscriptionDeactivateProducerFactory
     * @return
     */
    @Bean
    public ProducerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Kafka template for SubscriptionDeactivateEvents messages.
     * 
     * @param subscriptionDeactivateProducerFactory
     * @return
     */
    @Bean
    public KafkaTemplate<String, SubscriptionDeactivateEvent> subscriptionDeactivateKafkaTemplate(
            ProducerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateProducerFactory) {
        return new KafkaTemplate<>(subscriptionDeactivateProducerFactory);
    }

    /**
     * Kafka template for SubscriptionActivateEvent messages.
     * 
     * @param genericProducerFactory
     * @return
     */
    @Bean
    public KafkaTemplate<String, SubscriptionActivateEvent> subscriptionActivateKafkaTemplate(
            ProducerFactory<String, SubscriptionActivateEvent> subscriptionActivateProducerFactory) {
        return new KafkaTemplate<>(subscriptionActivateProducerFactory);
    }

    /**
     * Kafka template for sending general messages.
     */
    @Bean
    public KafkaTemplate<String, Object> genericKafkaTemplate(
            ProducerFactory<String, Object> genericProducerFactory) {
        return new KafkaTemplate<>(genericProducerFactory);
    }
}
