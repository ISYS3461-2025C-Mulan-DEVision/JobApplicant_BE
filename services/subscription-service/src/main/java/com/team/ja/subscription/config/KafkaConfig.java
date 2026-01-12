package com.team.ja.subscription.config;

import com.team.ja.common.event.PaymentCompletedEvent;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.StringUtils;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer and producer configuration for subscription-service.
 * Supports both local Kafka and Confluent Cloud (SASL/SSL).
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    /**
     * Creates common consumer configuration with SASL/SSL support.
     */
    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Add SASL/SSL properties for Confluent Cloud
        addSaslProperties(configProps);

        return configProps;
    }

    /**
     * Creates common producer configuration with SASL/SSL support.
     */
    private Map<String, Object> commonProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Add SASL/SSL properties for Confluent Cloud
        addSaslProperties(configProps);

        return configProps;
    }

    /**
     * Adds SASL/SSL properties if configured (for Confluent Cloud).
     */
    private void addSaslProperties(Map<String, Object> configProps) {
        if (StringUtils.hasText(securityProtocol) && !"PLAINTEXT".equals(securityProtocol)) {
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }
        if (StringUtils.hasText(saslMechanism)) {
            configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }
        if (StringUtils.hasText(saslJaasConfig)) {
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        }
    }

    /**
     * Consumer factory for PaymentCompletedEvent messages.
     * Uses ErrorHandlingDeserializer to properly handle deserialization errors
     * and prevent infinite retry loops.
     */
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> consumerFactory() {
        Map<String, Object> configProps = commonConsumerConfig();

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
        return new DefaultKafkaProducerFactory<>(commonProducerConfig());
    }

    /**
     * Generic Producer factory for Object type messages.
     */
    @Bean
    public ProducerFactory<String, Object> genericProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfig());
    }

    /**
     * Producer factory for SubscriptionActivateEvent messages.
     */
    @Bean
    public ProducerFactory<String, SubscriptionActivateEvent> subscriptionActivateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfig());
    }

    /**
     * Producer factory for SubscriptionDeactivateEvent messages.
     */
    @Bean
    public ProducerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfig());
    }

    /**
     * Kafka template for SubscriptionDeactivateEvents messages.
     */
    @Bean
    public KafkaTemplate<String, SubscriptionDeactivateEvent> subscriptionDeactivateKafkaTemplate(
            ProducerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateProducerFactory) {
        return new KafkaTemplate<>(subscriptionDeactivateProducerFactory);
    }

    /**
     * Kafka template for SubscriptionActivateEvent messages.
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
