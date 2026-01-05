package com.team.ja.user.config;

import com.team.ja.common.event.SkillCreateEvent;
import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.common.event.UserProfileUpdatedEvent;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for user-service.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.security.protocol:SASL_PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:PLAIN}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Bean
    public ProducerFactory<String, UserProfileUpdatedEvent> userProfileUpdatedProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // Add SASL/SSL Configuration
        configProps.put("security.protocol", securityProtocol);
        configProps.put("sasl.mechanism", saslMechanism);
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserProfileUpdatedEvent> userProfileUpdatedKafkaTemplate() {
        return new KafkaTemplate<>(userProfileUpdatedProducerFactory());
    }

    @Bean
    public ProducerFactory<String, SkillCreateEvent> skillCreateEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // Add SASL/SSL Configuration
        configProps.put("security.protocol", securityProtocol);
        configProps.put("sasl.mechanism", saslMechanism);
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, SkillCreateEvent> skillCreateEventKafkaTemplate() {
        return new KafkaTemplate<>(skillCreateEventProducerFactory());

    }

    @Bean
    public ProducerFactory<String, UserMigrationEvent> userMigrationEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // Add SASL/SSL Configuration
        configProps.put("security.protocol", securityProtocol);
        configProps.put("sasl.mechanism", saslMechanism);
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserMigrationEvent> userMigrationEventKafkaTemplate() {
        return new KafkaTemplate<>(userMigrationEventProducerFactory());
    }
}
