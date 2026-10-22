package com.example.inventory_service.service;

import com.example.inventory_service.dto.InventoryResultEvent;
import com.example.inventory_service.dto.InventoryStatus;
import com.example.inventory_service.dto.OrderCreatedEvent;
import com.example.inventory_service.kafka.InventoryResultProducer;
import com.example.inventory_service.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryResultProducer producer;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @ParameterizedTest(name = "quantity {0} -> {1}")
    @CsvSource({
            "1,  AVAILABLE",
            "5,  AVAILABLE",
            "6,  OUT_OF_STOCK",
            "100, OUT_OF_STOCK"
    })
    void processOrder_decidesStatusByQuantityAndPublishesResult(int quantity, InventoryStatus expected) {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent order = new OrderCreatedEvent(orderId, 1L, "laptop", quantity, LocalDateTime.now());

        InventoryResultEvent result = inventoryService.processOrder(order);

        assertThat(result).isEqualTo(new InventoryResultEvent(orderId, expected));
        verify(producer).publish(new InventoryResultEvent(orderId, expected));
    }
}