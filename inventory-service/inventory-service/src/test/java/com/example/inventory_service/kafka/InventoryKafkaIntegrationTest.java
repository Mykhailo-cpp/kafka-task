package com.example.inventory_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"order-created", "inventory-result"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class InventoryKafkaIntegrationTest {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Test
    void smallOrder_isReportedAvailable() {
        UUID orderId = UUID.randomUUID();

        sendOrder(orderId, 3);

        assertThat(awaitResult(orderId))
                .contains(orderId.toString())
                .contains("\"status\":\"AVAILABLE\"");
    }

    @Test
    void largeOrder_isReportedOutOfStock() {
        UUID orderId = UUID.randomUUID();

        sendOrder(orderId, 10);

        assertThat(awaitResult(orderId))
                .contains(orderId.toString())
                .contains("\"status\":\"OUT_OF_STOCK\"");
    }

    private void sendOrder(UUID orderId, int quantity) {

        String json = """
                {"orderId":"%s","customerId":1,"product":"laptop","quantity":%d,"createdTime":"2026-01-01T12:00:00.000"}
                """.formatted(orderId, quantity).trim();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
            producer.send(new ProducerRecord<>("order-created", orderId.toString(), json));
            producer.flush();
        }
    }

    private String awaitResult(UUID orderId) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of("inventory-result"));

            long deadline = System.currentTimeMillis() + 15_000;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> r : consumer.poll(Duration.ofMillis(500))) {
                    if (orderId.toString().equals(r.key())) {
                        return r.value();
                    }
                }
            }
        }
        throw new AssertionError("No inventory result for order " + orderId + " within 15s");
    }
}