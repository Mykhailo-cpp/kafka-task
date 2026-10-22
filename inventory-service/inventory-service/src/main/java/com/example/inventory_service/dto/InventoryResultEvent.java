package com.example.inventory_service.dto;

import java.util.UUID;

public record InventoryResultEvent(UUID orderId, InventoryStatus status) {
}
