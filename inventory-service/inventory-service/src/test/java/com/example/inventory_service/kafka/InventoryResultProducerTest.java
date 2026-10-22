package com.example.inventory_service.kafka;

import com.example.inventory_service.config.KafkaTopicConfig;
import com.example.inventory_service.dto.InventoryResultEvent;
import com.example.inventory_service.dto.InventoryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryResultProducerTest {

    @Mock
    private KafkaTemplate<String, InventoryResultEvent> kafkaTemplate;

    @InjectMocks
    private InventoryResultProducer producer;

    private final InventoryResultEvent event =
            new InventoryResultEvent(UUID.randomUUID(), InventoryStatus.AVAILABLE);

    @Test
    void publish_sendsToInventoryResultTopicKeyedByOrderId() {
        when(kafkaTemplate.send(KafkaTopicConfig.INVENTORY_RESULT_TOPIC, event.orderId().toString(), event))
                .thenReturn(new CompletableFuture<>());

        producer.publish(event);

        verify(kafkaTemplate).send(KafkaTopicConfig.INVENTORY_RESULT_TOPIC, event.orderId().toString(), event);
    }

    @Test
    void publish_whenBrokerSendFails_doesNotThrow() {
        CompletableFuture<SendResult<String, InventoryResultEvent>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(KafkaTopicConfig.INVENTORY_RESULT_TOPIC, event.orderId().toString(), event))
                .thenReturn(failed);

        assertThatCode(() -> producer.publish(event)).doesNotThrowAnyException();
    }
}