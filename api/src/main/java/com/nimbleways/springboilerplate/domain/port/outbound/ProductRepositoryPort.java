package com.nimbleways.springboilerplate.domain.port.outbound;

import com.nimbleways.springboilerplate.entities.Product;

import java.util.Optional;

public interface ProductRepositoryPort {
    Optional<Product> findById(Long productId);
    Optional<Product> findFirstByName(String name);
    Product save(Product product);
}
