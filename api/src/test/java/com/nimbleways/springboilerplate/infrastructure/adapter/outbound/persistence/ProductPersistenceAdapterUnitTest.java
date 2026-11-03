package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.persistence;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class ProductPersistenceAdapterUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductPersistenceAdapter adapter;

    @Test
    void findById_shouldDelegateToProductRepository() {
        Product product = new Product(1L, 10, 5, "NORMAL", "Keyboard", null, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Keyboard", result.get().getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void findFirstByName_shouldDelegateToProductRepository() {
        Product product = new Product(2L, 10, 5, "NORMAL", "Mouse", null, null, null);
        when(productRepository.findFirstByName("Mouse")).thenReturn(Optional.of(product));

        Optional<Product> result = adapter.findFirstByName("Mouse");

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
        verify(productRepository).findFirstByName("Mouse");
    }

    @Test
    void save_shouldDelegateToProductRepository() {
        Product product = new Product(3L, 10, 5, "NORMAL", "Screen", null, null, null);
        when(productRepository.save(product)).thenReturn(product);

        Product result = adapter.save(product);

        assertEquals(3L, result.getId());
        verify(productRepository).save(product);
    }
}
