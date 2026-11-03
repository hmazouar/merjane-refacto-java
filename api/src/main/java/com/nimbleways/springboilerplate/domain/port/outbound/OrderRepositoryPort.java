package com.nimbleways.springboilerplate.domain.port.outbound;

import com.nimbleways.springboilerplate.entities.Order;

import java.util.Optional;

public interface OrderRepositoryPort {
    Optional<Order> findById(Long orderId);
    Order save(Order order);
}
