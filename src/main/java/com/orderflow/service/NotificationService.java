package com.orderflow.service;

import com.orderflow.entity.ProcessedEvent;
import com.orderflow.event.OrderCreatedEvent;
import com.orderflow.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String CONSUMER_GROUP =
            "orderflow-notification-group";

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void sendOrderCreatedNotification(
            OrderCreatedEvent event) {

        boolean alreadyProcessed =
                processedEventRepository
                        .findByEventIdAndConsumerGroup(
                                event.eventId(),
                                CONSUMER_GROUP
                        )
                        .isPresent();

        if (alreadyProcessed) {

            log.info(
                    "Duplicate event ignored: eventId={}, orderId={}",
                    event.eventId(),
                    event.orderId()
            );

            return;
        }

        log.info(
                "NOTIFICATION SERVICE: Order {} created for user {}. Total amount: {}",
                event.orderId(),
                event.userId(),
                event.totalAmount()
        );

        ProcessedEvent processedEvent =
                ProcessedEvent.builder()
                        .eventId(event.eventId())
                        .consumerGroup(CONSUMER_GROUP)
                        .processedAt(
                                java.time.LocalDateTime.now()
                        )
                        .build();

        processedEventRepository.save(processedEvent);
    }
}