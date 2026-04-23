package com.sanchit.orders_service.kafka;

import com.sanchit.orders_service.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.order-placed}")
    private String orderPlacedTopic;

    public void publicOrderPlaced(OrderEvent event){
        kafkaTemplate.send(orderPlacedTopic, event.idempotencyKey(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null){
                        log.error("Failed to publish OrderEvent for idempotencyKey: {} | error: {}",
                                event.idempotencyKey(), ex.getMessage());
                    } else {
                      log.info("OrderEvent published successfully | topic: {} | partition: {} | offset: {}",
                              result.getRecordMetadata().topic(),
                              result.getRecordMetadata().partition(),
                              result.getRecordMetadata().offset());
                    }
                });
    }

}
