package com.team.ja.user.config;

import com.team.ja.common.event.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for user-service.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.properties.security.protocol:SASL_PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:PLAIN}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.team.ja.common.event");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Add SASL/SSL Configuration
        configProps.put("security.protocol", securityProtocol);
        configProps.put("sasl.mechanism", saslMechanism);
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

