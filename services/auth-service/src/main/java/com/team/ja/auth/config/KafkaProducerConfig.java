package com.team.ja.auth.config;

import com.team.ja.common.event.UserRegisteredEvent;
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
 * Kafka producer configuration for auth-service.
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
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

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
    public ProducerFactory<String, UserRegisteredEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
