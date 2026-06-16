package com.sanchit.orders_service.controller;

import com.sanchit.orders_service.entity.Order;
import com.sanchit.orders_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        // FIX: records expose accessors as methods — use customerEmail(), not field access.
        log.info("Received order request for customer: {}", request.customerEmail());

        Order order = orderService.placeOrder(
                request.customerEmail(),
                request.productName(),
                request.amount(),
                request.idempotencyKey()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OrderResponse(
                        order.getId(),
                        order.getStatus().name(),
                        "Order placed successfully"
                ));
    }

    // ── Request and Response records ──────────────────────────────────────

    public record OrderRequest(
            @NotBlank(message = "customerEmail is required")
            @Email(message = "customerEmail must be a valid email address")
            String customerEmail,

            @NotBlank(message = "productName is required")
            String productName,

            @NotNull(message = "amount is required")
            @Positive(message = "amount must be greater than zero")
            BigDecimal amount,

            @NotBlank(message = "idempotencyKey is required")
            String idempotencyKey
    ) {}

    public record OrderResponse(
            java.util.UUID orderId,
            String status,
            String message
    ) {}
}
