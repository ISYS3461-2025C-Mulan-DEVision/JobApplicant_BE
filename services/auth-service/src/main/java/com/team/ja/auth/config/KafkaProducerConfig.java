package com.team.ja.auth.config;

import com.team.ja.common.event.UserRegisteredEvent;
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
 * Kafka producer configuration for auth-service.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // @Value("${spring.kafka.properties.security.protocol:SASL_PLAINTEXT}")
    // private String securityProtocol;

    // @Value("${spring.kafka.properties.sasl.mechanism:PLAIN}")
    // private String saslMechanism;

    // @Value("${spring.kafka.properties.sasl.jaas.config:}")
    // private String saslJaasConfig;

    @Bean
    public ProducerFactory<String, UserRegisteredEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Ensure reliability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Add SASL/SSL Configuration
        // configProps.put("security.protocol", securityProtocol);
        // configProps.put("sasl.mechanism", saslMechanism);
        // if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
        // configProps.put("sasl.jaas.config", saslJaasConfig);
        // }

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
