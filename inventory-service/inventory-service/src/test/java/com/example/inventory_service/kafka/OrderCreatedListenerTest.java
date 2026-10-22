package com.example.inventory_service.kafka;

import com.example.inventory_service.dto.OrderCreatedEvent;
import com.example.inventory_service.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderCreatedListener listener;

    @Test
    void handleOrderCreated_delegatesToInventoryService() {
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), 1L, "phone", 2, LocalDateTime.now());

        listener.handleOrderCreated(event);

        verify(inventoryService).processOrder(event);
    }
}