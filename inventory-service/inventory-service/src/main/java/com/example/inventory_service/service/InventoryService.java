package com.example.inventory_service.service;

import com.example.inventory_service.dto.InventoryResultEvent;
import com.example.inventory_service.dto.OrderCreatedEvent;

public interface InventoryService {
    InventoryResultEvent processOrder(OrderCreatedEvent event);
}
