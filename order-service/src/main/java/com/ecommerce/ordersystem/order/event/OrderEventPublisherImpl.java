package com.ecommerce.ordersystem.order.event;

import com.ecommerce.ordersystem.common.event.OrderCancelledEvent;
import com.ecommerce.ordersystem.common.event.OrderConfirmedEvent;
import com.ecommerce.ordersystem.common.event.OrderCreatedEvent;
import com.ecommerce.ordersystem.common.event.OrderPartiallyCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderEventPublisherImpl implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisherImpl.class);

    public static final String TOPIC_ORDER_CREATED = "orders.created";
    public static final String TOPIC_ORDER_CONFIRMED = "orders.confirmed";
    public static final String TOPIC_ORDER_CANCELLED = "orders.cancelled";
    public static final String TOPIC_ORDER_PARTIALLY_CANCELLED = "orders.partially-cancelled";

    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final boolean kafkaEnabled;

    public OrderEventPublisherImpl(
            @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate,
            ApplicationEventPublisher applicationEventPublisher,
            @Value("${app.kafka.enabled:false}") boolean kafkaEnabled) {
        this.kafkaTemplate = Optional.ofNullable(kafkaTemplate);
        this.applicationEventPublisher = applicationEventPublisher;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId={}", event.getOrderId());
        applicationEventPublisher.publishEvent(event);
        sendToKafka(TOPIC_ORDER_CREATED, event.getOrderId(), event);
    }

    @Override
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Publishing OrderConfirmedEvent for orderId={}", event.getOrderId());
        applicationEventPublisher.publishEvent(event);
        sendToKafka(TOPIC_ORDER_CONFIRMED, event.getOrderId(), event);
    }

    @Override
    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent for orderId={}", event.getOrderId());
        applicationEventPublisher.publishEvent(event);
        sendToKafka(TOPIC_ORDER_CANCELLED, event.getOrderId(), event);
    }

    @Override
    public void publishOrderPartiallyCancelled(OrderPartiallyCancelledEvent event) {
        log.info("Publishing OrderPartiallyCancelledEvent for orderId={}, itemId={}", event.getOrderId(), event.getOrderItemId());
        applicationEventPublisher.publishEvent(event);
        sendToKafka(TOPIC_ORDER_PARTIALLY_CANCELLED, event.getOrderId(), event);
    }

    private void sendToKafka(String topic, String key, Object payload) {
        if (kafkaEnabled && kafkaTemplate.isPresent()) {
            try {
                // Keyed by orderId to guarantee partition ordering in Kafka
                kafkaTemplate.get().send(topic, key, payload);
                log.info("Successfully dispatched message to Kafka topic '{}' with key '{}'", topic, key);
            } catch (Exception e) {
                log.warn("Failed to publish to Kafka topic '{}': {}. Fallback maintained.", topic, e.getMessage());
            }
        } else {
            log.debug("Kafka publishing skipped (kafkaEnabled={}, kafkaTemplatePresent={})",
                    kafkaEnabled, kafkaTemplate.isPresent());
        }
    }
}
