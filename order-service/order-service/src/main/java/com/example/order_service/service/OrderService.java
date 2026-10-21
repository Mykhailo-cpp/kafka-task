package com.example.order_service.service;

import com.example.order_service.dto.OrderCreatedEvent;
import com.example.order_service.dto.OrderRequest;

public interface OrderService {
    OrderCreatedEvent create(OrderRequest orderRequest);
}
