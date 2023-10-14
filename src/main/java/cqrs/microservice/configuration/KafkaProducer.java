package cqrs.microservice.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducer {
    @Value(value = "${kafka.bootstrapServers:localhost:9093}")
    private String bootstrapServers;

    private final KafkaConfigProperties kafkaConfigProperties;

    private Map<String, Object> senderProps(){
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        producerProps.put(ProducerConfig.ACKS_CONFIG, kafkaConfigProperties.getAcks());
        producerProps.put(ProducerConfig.RETRIES_CONFIG, kafkaConfigProperties.getRetries());
        producerProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, kafkaConfigProperties.getDeliveryTimeoutMs());
        producerProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, kafkaConfigProperties.getMaxRequestSize());
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfigProperties.getRequestTimeoutMs());

        return producerProps;
    }

    @Bean
    public ProducerFactory<String, byte[]> producerFactory (){
        return new DefaultKafkaProducerFactory<>(senderProps());
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }
}
