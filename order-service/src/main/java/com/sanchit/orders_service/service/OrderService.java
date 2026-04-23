package com.sanchit.orders_service.service;

import com.sanchit.orders_service.dto.OrderEvent;
import com.sanchit.orders_service.entity.Order;
import com.sanchit.orders_service.entity.OrderStatus;
import com.sanchit.orders_service.kafka.OrderKafkaProducer;
import com.sanchit.orders_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaProducer;

    @Transactional
    public Order placeOrder(String customerEmail,
                            String productName,
                            java.math.BigDecimal amount,
                            String idempotencyKey){
        //idempotency check - reject duplicate requests
        if (orderRepository.existsByIdempotencyKey(idempotencyKey)){
            log.warn("Duplicate order request detected for idempotencyKey: {}", idempotencyKey);
            return orderRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        }

        //Save order to DB
        Order order = Order.builder()
                .customerEmail(customerEmail)
                .productName(productName)
                .amount(amount)
                .idempotencyKey(idempotencyKey)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved to DB | orderId: {}", savedOrder.getId());

        //Publish event to Kafka
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getProductName(),
                savedOrder.getAmount(),
                savedOrder.getIdempotencyKey(),
                "ORDER_PLACED"
        );

        kafkaProducer.publicOrderPlaced(event);

        return savedOrder;
    }

}
