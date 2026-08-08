package com.orderflow.service;

import com.orderflow.entity.OutboxEvent;
import com.orderflow.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public void saveEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            Object event) {

        try {
            String payload = jsonMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .published(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event",
                    exception
            );
        }
    }
}