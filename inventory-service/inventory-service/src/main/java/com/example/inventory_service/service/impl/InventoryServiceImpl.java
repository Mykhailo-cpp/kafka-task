package com.example.inventory_service.service.impl;

import com.example.inventory_service.dto.InventoryResultEvent;
import com.example.inventory_service.dto.InventoryStatus;
import com.example.inventory_service.dto.OrderCreatedEvent;
import com.example.inventory_service.kafka.InventoryResultProducer;
import com.example.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private static final int MAX_AVAILABLE_QUANTITY = 5;

    private final InventoryResultProducer producer;

    @Override
    public InventoryResultEvent processOrder(OrderCreatedEvent order) {
        InventoryStatus status;

        if(order.quantity() <= MAX_AVAILABLE_QUANTITY){
            status = InventoryStatus.AVAILABLE;
        }
        else{
            status = InventoryStatus.OUT_OF_STOCK;
        }

        InventoryResultEvent result = new InventoryResultEvent(order.orderId(), status);

        producer.publish(result);

        return result;
    }
}
