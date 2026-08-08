package com.orderflow.service;

import com.orderflow.dto.PaymentRequest;
import com.orderflow.dto.PaymentResponse;
import com.orderflow.entity.*;
import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse processPayment(
            Long orderId,
            PaymentRequest request,
            Authentication authentication) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        User user = (User) authentication.getPrincipal();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot make payment for a cancelled order"
            );
        }

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException(
                    "Payment already exists for this order"
            );
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod(request.paymentMethod())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        /*
         * Temporary payment simulation.
         *
         * Later this will be replaced by an actual
         * payment-provider integration.
         */
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID()
        );
        payment.setUpdatedAt(LocalDateTime.now());

        order.setStatus(OrderStatus.CONFIRMED);

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}