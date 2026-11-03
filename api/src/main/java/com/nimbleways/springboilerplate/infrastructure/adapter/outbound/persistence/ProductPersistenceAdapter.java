package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.persistence;

import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Optional<Product> findFirstByName(String name) {
        return productRepository.findFirstByName(name);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }
}
