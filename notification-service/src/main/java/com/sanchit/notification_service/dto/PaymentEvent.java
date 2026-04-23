package com.sanchit.notification_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(
        UUID paymentId,
        UUID orderId,
        String customerEmail,
        BigDecimal amount,
        String status,
        String idempotencyKey,
        String eventType
) {}