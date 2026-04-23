package com.sanchit.payment_service.kafka;

import com.sanchit.payment_service.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${kafka.topics.payment-processed}")
    private String paymentProcessedTopic;

    public void publishPaymentProcessed(PaymentEvent event){
        kafkaTemplate.send(paymentProcessedTopic, event.idempotencyKey(), event)
                .whenComplete((result,ex) ->{
                    if (ex != null){
                        log.error("Failed to publish PaymentEvent for orderId: {} | error: {}",
                                event.orderId(), ex.getMessage());
                    }else {
                        log.info("PaymentEvent published | topic: {} | orderId: {} | status: {}",
                                result.getRecordMetadata().topic(),
                                event.orderId(),
                                event.status());
                    }
                });
    }
}
