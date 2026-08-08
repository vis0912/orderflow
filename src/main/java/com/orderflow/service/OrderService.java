package com.orderflow.service;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderItemRequest;
import com.orderflow.dto.OrderItemResponse;
import com.orderflow.dto.OrderResponse;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderItem;
import com.orderflow.entity.OrderStatus;
import com.orderflow.entity.Product;
import com.orderflow.entity.User;
import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.ProductNotFoundException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import com.orderflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        Map<Long, Product> products = new HashMap<>();
        Map<Long, Integer> requestedQuantities = new HashMap<>();

        // Phase 1: Validate all products and stock
        for (OrderItemRequest itemRequest : request.items()) {

            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() ->
                            new ProductNotFoundException("Product not found"));

            if (!product.getActive()) {
                throw new BadRequestException(
                        "Product is not available: " + product.getName()
                );
            }

            int totalRequested = requestedQuantities.merge(
                    itemRequest.productId(),
                    itemRequest.quantity(),
                    Integer::sum
            );

            if (product.getStockQuantity() < totalRequested) {
                throw new BadRequestException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            products.put(itemRequest.productId(), product);
        }

        // Phase 2: Create order items and deduct stock
        for (OrderItemRequest itemRequest : request.items()) {

            Product product = products.get(itemRequest.productId());

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.quantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            product.setStockQuantity(
                    product.getStockQuantity()
                            - itemRequest.quantity()
            );

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return orderRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(
            Long orderId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(
            Long orderId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending orders can be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }
}