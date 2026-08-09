package com.orderflow.service;

import com.orderflow.entity.OutboxEvent;
import com.orderflow.event.OrderCreatedEvent;
import com.orderflow.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String ORDER_CREATED_TOPIC = "order.created";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        var events =
                outboxEventRepository
                        .findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {

            try {

                if (!"OrderCreated".equals(event.getEventType())) {
                    continue;
                }

                OrderCreatedEvent originalEvent =
                        jsonMapper.readValue(
                                event.getPayload(),
                                OrderCreatedEvent.class
                        );

                OrderCreatedEvent orderCreatedEvent =
                        new OrderCreatedEvent(
                                event.getId(),
                                originalEvent.orderId(),
                                originalEvent.userId(),
                                originalEvent.totalAmount(),
                                originalEvent.createdAt(),
                                originalEvent.items()
                        );

                kafkaTemplate.send(
                        ORDER_CREATED_TOPIC,
                        event.getAggregateId(),
                        orderCreatedEvent
                ).whenComplete((result, exception) -> {

                    if (exception == null) {

                        event.setPublished(true);
                        event.setPublishedAt(
                                LocalDateTime.now()
                        );

                        outboxEventRepository.save(event);
                    }
                });

            } catch (Exception exception) {

                System.err.println(
                        "Failed to publish outbox event: "
                                + event.getId()
                                + " - "
                                + exception.getMessage()
                );
            }
        }
    }
}