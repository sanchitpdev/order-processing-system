package com.sanchit.notification_service.service;

import com.sanchit.notification_service.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(PaymentEvent event) {

        if ("PAYMENT_SUCCESS".equals(event.eventType())) {
            sendSuccessEmail(event);
        } else if ("PAYMENT_FAILED".equals(event.eventType())) {
            sendFailureEmail(event);
        } else {
            log.warn("Unknown event type received: {}", event.eventType());
        }
    }

    private void sendSuccessEmail(PaymentEvent event) {
        // in real world — integrate Twilio, SendGrid, AWS SES here
        log.info("SUCCESS EMAIL SENT");
        log.info("  To      : {}", event.customerEmail());
        log.info("  Subject : Your order has been confirmed");
        log.info("  Body    : Hi, your payment of Rs.{} for order {} was successful.",
                event.amount(), event.orderId());
    }

    private void sendFailureEmail(PaymentEvent event) {
        log.info("FAILURE EMAIL SENT");
        log.info("  To      : {}", event.customerEmail());
        log.info("  Subject : Payment failed for your order");
        log.info("  Body    : Hi, unfortunately your payment of Rs.{} for order {} failed. Please retry.",
                event.amount(), event.orderId());
    }
}