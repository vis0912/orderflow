package com.orderflow.controller;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.PaymentRequest;
import com.orderflow.dto.PaymentResponse;
import com.orderflow.service.OrderService;
import com.orderflow.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(authentication)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrder(id, authentication)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.cancelOrder(id, authentication)
        );
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        paymentService.processPayment(
                                id,
                                request,
                                authentication
                        )
                );
    }
}