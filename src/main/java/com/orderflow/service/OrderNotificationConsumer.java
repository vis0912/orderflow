package com.orderflow.service;

import com.orderflow.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderNotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order.created",
            groupId = "orderflow-notification-group"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        notificationService.sendOrderCreatedNotification(event);
    }
}