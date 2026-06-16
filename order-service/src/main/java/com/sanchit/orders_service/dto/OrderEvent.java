package com.sanchit.orders_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderEvent(
        UUID orderId,
        String customerEmail,
        String productName,
        BigDecimal amount,
        String idempotencyKey,
        String eventType
) {}