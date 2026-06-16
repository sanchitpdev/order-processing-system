package com.sanchit.payment_service.kafka;

import com.sanchit.payment_service.dto.OrderEvent;
import com.sanchit.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 2000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLQ"

    )
    @KafkaListener(
            topics = "${kafka.topics.order-placed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderPlaced(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received OrderEvent | orderId: {} | partition: {} | offset: {}",
                event.orderId(), partition, offset);

        paymentService.processPayment(event);
    }

    @DltHandler
    public void handleDlt(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.error("MESSAGE LANDED IN DLQ | topic: {} | orderId: {} | idempotencyKey: {}",
                topic, event.orderId(), event.idempotencyKey());
        log.error("Manual intervention required for orderId: {}", event.orderId());
    }
}