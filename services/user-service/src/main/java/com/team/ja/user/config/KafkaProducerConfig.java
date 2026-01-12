package com.team.ja.user.config;

import com.team.ja.common.event.JobMatchedEvent;
import com.team.ja.common.event.SkillCreateEvent;
import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.common.event.UserProfileCreateEvent;
import com.team.ja.common.event.UserProfileUpdatedEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for user-service.
 * Supports both local Kafka and Confluent Cloud (SASL/SSL).
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    /**
     * Creates common producer configuration with SASL/SSL support.
     */
    private Map<String, Object> commonProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

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

    @Bean
    public ProducerFactory<String, UserProfileUpdatedEvent> userProfileUpdatedProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, UserProfileUpdatedEvent> userProfileUpdatedKafkaTemplate(ProducerFactory<String, UserProfileUpdatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, SkillCreateEvent> skillCreateEventProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, SkillCreateEvent> skillCreateEventKafkaTemplate(ProducerFactory<String, SkillCreateEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, UserMigrationEvent> userMigrationEventProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, UserMigrationEvent> userMigrationEventKafkaTemplate(ProducerFactory<String, UserMigrationEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, UserProfileCreateEvent> userProfileCreateEventProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, UserProfileCreateEvent> userProfileCreateEventKafkaTemplate(ProducerFactory<String, UserProfileCreateEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, UserSearchProfileUpdateEvent> userProfileUpdatedEventProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, UserSearchProfileUpdateEvent> userProfileUpdatedEventKafkaTemplate(ProducerFactory<String, UserSearchProfileUpdateEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, JobMatchedEvent> jobMatchedEventProducerFactory(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        Map<String, Object> configProps = commonProducerConfig();
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, JobMatchedEvent> jobMatchedEventKafkaTemplate(ProducerFactory<String, JobMatchedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
