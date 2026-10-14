package com.example.order_service.controller;

import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.kafka.OrderProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducer orderProducer;

    @PostMapping
    public ResponseEntity<OrderCreatedEvent> createOrder(@Valid @RequestBody OrderRequest request){
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                request.customerId(),
                request.product(),
                request.quantity(),
                LocalDateTime.now()
        );

        orderProducer.publish(event);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
