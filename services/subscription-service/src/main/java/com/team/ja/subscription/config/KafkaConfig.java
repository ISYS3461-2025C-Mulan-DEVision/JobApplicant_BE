package com.team.ja.subscription.config;

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
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for user-service.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.team.ja.common.event");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    /**
     * Generic consumer factory for Object type messages.
     * 
     * @return
     */
    @Bean
    public ConsumerFactory<String, Object> genericConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Reply container for handling replies from Kafka.
     * 
     * @param genericConsumerFactory
     * @return
     */
    @Bean
    public KafkaMessageListenerContainer<String, Object> replyContainer(
            ConsumerFactory<String, Object> genericConsumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties("reply-topic");
        return new KafkaMessageListenerContainer<>(genericConsumerFactory, containerProperties);
    }

    /**
     * Replying Kafka template for request-reply semantics.
     * 
     * @param producerFactory
     * @param replyContainer
     * @return
     */
    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            KafkaMessageListenerContainer<String, Object> replyContainer) {
        ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate = new ReplyingKafkaTemplate<>(
                producerFactory, replyContainer);
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return replyingKafkaTemplate;
    }

    /**
     * Producer factory for UserSearchProfileUpdateEvent messages.
     */
    @Bean
    public ProducerFactory<String, com.team.ja.common.event.UserSearchProfileUpdateEvent> userSearchProfileProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate specialized for UserSearchProfileUpdateEvent so producers can
     * autowire the typed template.
     */
    @Bean
    public KafkaTemplate<String, com.team.ja.common.event.UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate(
            ProducerFactory<String, com.team.ja.common.event.UserSearchProfileUpdateEvent> userSearchProfileProducerFactory) {
        return new KafkaTemplate<>(userSearchProfileProducerFactory);
    }
}
