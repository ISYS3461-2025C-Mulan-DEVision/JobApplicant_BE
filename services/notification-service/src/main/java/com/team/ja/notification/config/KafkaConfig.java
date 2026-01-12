package com.team.ja.notification.config;

import com.team.ja.common.event.ApplicationCreatedEvent;
import com.team.ja.common.event.JobMatchedEvent;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.common.event.UserRegisteredEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for notification-service.
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
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.team.ja.common.event");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

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
     * Generic consumer factory for Object type messages.
     */
    @Bean
    public ConsumerFactory<String, Object> genericConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(genericConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory specifically for JobPostMatchEvent.
     */
    @Bean
    public ConsumerFactory<String, JobMatchedEvent> jobPostMatchConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobMatchedEvent> jobPostMatchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobMatchedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobPostMatchConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory specifically for ApplicationCreatedEvent.
     */
    @Bean
    public ConsumerFactory<String, ApplicationCreatedEvent> applicationCreatedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> applicationCreatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationCreatedConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory for SubscriptionActivateEvent.
     */
    @Bean
    public ConsumerFactory<String, SubscriptionActivateEvent> subscriptionActivateConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SubscriptionActivateEvent> subscriptionActivateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SubscriptionActivateEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(subscriptionActivateConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory for SubscriptionDeactivateEvent.
     */
    @Bean
    public ConsumerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SubscriptionDeactivateEvent> subscriptionDeactivateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SubscriptionDeactivateEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(subscriptionDeactivateConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory for UserRegisteredEvent.
     */
    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userRegisteredConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> userRegisteredKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userRegisteredConsumerFactory());
        return factory;
    }

    /**
     * Consumer factory for JobMatchedEvent.
     * Used for listening to job-matched topic from user-service.
     * When a job post matches a premium user's search profile, user-service publishes
     * JobMatchedEvent and this consumer creates a notification for the user.
     */
    @Bean
    public ConsumerFactory<String, JobMatchedEvent> jobMatchedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobMatchedEvent> jobMatchedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobMatchedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobMatchedConsumerFactory());
        return factory;
    }
}
