package com.sanchit.payment_service.service;

import com.sanchit.payment_service.dto.OrderEvent;
import com.sanchit.payment_service.dto.PaymentEvent;
import com.sanchit.payment_service.entity.Payment;
import com.sanchit.payment_service.entity.PaymentStatus;
import com.sanchit.payment_service.kafka.PaymentKafkaProducer;
import com.sanchit.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;

    @Transactional
    public void processPayment(OrderEvent event) {

//        // TEMPORARY - remove after testing DLQ
//        throw new RuntimeException("Simulating payment processing failure");

//         idempotency check
        if (paymentRepository.existsByIdempotencyKey(event.idempotencyKey())) {
            log.warn("Duplicate payment event detected for idempotencyKey: {}", event.idempotencyKey());
            return;
        }

        log.info("Processing payment for orderId: {} | amount: {}",
                event.orderId(), event.amount());

        // simulate payment processing
        // in real world this would call a payment gateway like Razorpay or Stripe
        boolean paymentSuccess = simulatePayment(event.amount());

        PaymentStatus status = paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        // save payment record to DB
        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .customerEmail(event.customerEmail())
                .amount(event.amount())
                .status(status)
                .idempotencyKey(event.idempotencyKey())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved | paymentId: {} | status: {}", savedPayment.getId(), status);

        // publish result back to Kafka
        PaymentEvent paymentEvent = new PaymentEvent(
                savedPayment.getId(),
                event.orderId(),
                event.customerEmail(),
                event.amount(),
                status.name(),
                event.idempotencyKey(),
                status == PaymentStatus.SUCCESS ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED"
        );

        kafkaProducer.publishPaymentProcessed(paymentEvent);
    }

    private boolean simulatePayment(java.math.BigDecimal amount) {
        // simulate 80% success rate for testing
        // amounts ending in 0 fail, rest succeed
        return amount.remainder(new java.math.BigDecimal("10"))
                .compareTo(java.math.BigDecimal.ZERO) != 0;
    }
}
