package com.example.order_service.service.impl;


import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.kafka.OrderProducer;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    @Override
    public OrderCreatedEvent create(OrderRequest orderRequest) {
        Order order = Order.builder()
                .customerId(orderRequest.customerId())
                .product(orderRequest.product())
                .quantity(orderRequest.quantity())
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getProduct(),
                savedOrder.getQuantity(),
                savedOrder.getCreatedAt()
        );

        orderProducer.publish(event);

        return event;
    }
}
