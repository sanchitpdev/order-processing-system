package com.sanchit.notification_service.kafka;

import com.sanchit.notification_service.dto.PaymentEvent;
import com.sanchit.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.payment-processed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePaymentProcessed(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received PaymentEvent | orderId: {} | status: {} | partition: {} | offset: {}",
                event.orderId(), event.status(), partition, offset);

        notificationService.sendNotification(event);
    }
}