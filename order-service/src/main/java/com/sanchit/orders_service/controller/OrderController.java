package com.sanchit.orders_service.controller;

import com.sanchit.orders_service.entity.Order;
import com.sanchit.orders_service.service.OrderService;
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
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request){
        log.info("Received order request for customer: {}",request.customerEmail);

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
            String customerEmail,
            String productName,
            BigDecimal amount,
            String idempotencyKey
    ) {}

    public record OrderResponse(
            java.util.UUID orderId,
            String status,
            String message
    ) {}
}
