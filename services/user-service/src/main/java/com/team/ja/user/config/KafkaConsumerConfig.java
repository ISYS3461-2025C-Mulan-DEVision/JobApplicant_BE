package com.team.ja.user.config;

import com.team.ja.common.event.JobPostingEvent;
import com.team.ja.common.event.PaymentCompletedEvent;
import com.team.ja.common.event.SkillCreateEvent;
import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.common.event.UserProfileCreateEvent;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.StringUtils;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for user-service.
 * Supports both local Kafka and Confluent Cloud (SASL/SSL).
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

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
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.team.ja.common.event,com.devision.job_manager_jobpost.event");
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

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // Add error handler with retry policy: 3 retries with 2 second delay
        factory.setCommonErrorHandler(userRegisteredErrorHandler());
        return factory;
    }

    @Bean
    public CommonErrorHandler userRegisteredErrorHandler() {
        // Retry 3 times with 2 second interval, then log and skip the message
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        // Don't retry for these exception types
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }

    @Bean
    public ConsumerFactory<String, SkillCreateEvent> skillCreateEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SkillCreateEvent> skillCreateEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SkillCreateEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(skillCreateEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserMigrationEvent> userMigrationEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // Use ErrorHandlingDeserializer as a wrapper so deserialization errors are
        // surfaced to the container's error handler instead of failing the consumer
        // thread.
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        // Trust both common events and the DTO package that appears in hosted messages
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.team.ja.common.event,com.team.ja.user.dto.request");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Add SASL/SSL properties for Confluent Cloud
        addSaslProperties(configProps);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserMigrationEvent> userMigrationEventKafkaListenerContainerFactory(
            CommonErrorHandler migrateErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, UserMigrationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userMigrationEventConsumerFactory());
        factory.setCommonErrorHandler(migrateErrorHandler);
        return factory;
    }

    @Bean
    public CommonErrorHandler migrateErrorHandler(KafkaTemplate<String, UserMigrationEvent> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }

    @Bean
    public ConsumerFactory<String, UserProfileCreateEvent> userProfileCreateEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(commonConsumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserProfileCreateEvent> userProfileCreateEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserProfileCreateEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userProfileCreateEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Object> jobPostingEventConsumerFactory() {
        Map<String, Object> configProps = commonConsumerConfig();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Thay vì dùng JsonDeserializer, ta dùng StringDeserializer cho cả Key và Value
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobPostingEvent> jobPostingEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobPostingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobPostingEventConsumerFactory());

        factory.setRecordMessageConverter(multiTypeConverter());

        return factory;
    }

    @Bean
    public RecordMessageConverter multiTypeConverter() {
        JsonMessageConverter converter = new JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);

        converter.setTypeMapper(typeMapper);
        return converter;
    }
}
