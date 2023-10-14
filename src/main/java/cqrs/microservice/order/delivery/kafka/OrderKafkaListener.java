package cqrs.microservice.order.delivery.kafka;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cqrs.microservice.configuration.OrderKafkaTopics;
import cqrs.microservice.order.domain.Order;
import cqrs.microservice.order.events.OrderCreatedEvent;
import cqrs.microservice.order.events.OrderDeliveryAddressChangedEvent;
import cqrs.microservice.order.events.OrderStatusUpdatedEvent;
import cqrs.microservice.shared.serializer.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaListener {
    private final ObjectMapper objectMapper;
    private final JsonSerializer jsonSerializer;
    private final OrderKafkaTopics orderKafkaTopics;

    @KafkaListener(topics = {"order.kafka.topics.order-address-changed"}, groupId = "order_microservice", concurrency = "10")
    public void changeDeliveryAddressListener(
            @Payload byte[] data,
            ConsumerRecordMetadata meta,
            Acknowledgment ack
            ) {
        log.info("(Listener) topic: {}, partition: {}, timestamp:{}, offset: {}, data: {}", meta.topic(), meta.partition(), meta.timestamp(), meta.offset(), new String(data));
        try {
            final var event = jsonSerializer.deserializeFromJsonBytes(data,
                    OrderDeliveryAddressChangedEvent.class);
            ack.acknowledge();
            log.info("ack event: {}", event);
        }  catch (Exception e) {
            ack.nack(Duration.ofMillis(1000));
            log.error("changeDeliveryAddressListener: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = {"order.kafka.topics.order-status-updated"}, groupId = "order_microservice", concurrency = "10")
    public void updateOrderStatusListener(@Payload byte[] data,ConsumerRecordMetadata meta, Acknowledgment ack){
        log.info("(updateOrderStatusListener) data: {}", new String(data));
        try {
            final var event = jsonSerializer.deserializeFromJsonBytes(data,
                    OrderStatusUpdatedEvent.class);
            ack.acknowledge();
            log.info("ack event: {}", event);
        }catch (Exception e){
            ack.nack(Duration.ofMillis(1000));
            log.error("updateOrderStatusListener: {}", e.getMessage());
        }
    }
    @KafkaListener(topics = {"order.kafka.topics.order-created"}, groupId = "order_microservice", concurrency = "10")
    public void createOrderListener(@Payload byte[] data,ConsumerRecordMetadata meta, Acknowledgment ack) {
        log.info("(createOrderListener) data: {}", new String(data));

        try {
            final var event = jsonSerializer.deserializeFromJsonBytes(data,
                    OrderCreatedEvent.class);
            ack.acknowledge();
            log.info("ack event: {}", event);
        } catch (Exception e) {
            ack.nack(Duration.ofMillis(1000));
            log.error("createOrderListener: {}", e.getMessage());
        }
    }
}
