package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.persistence;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
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
public class OrderPersistenceAdapterUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderPersistenceAdapter adapter;

    @Test
    void findById_shouldDelegateToOrderRepository() {
        Order order = new Order(1L, null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void save_shouldDelegateToOrderRepository() {
        Order order = new Order(2L, null);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = adapter.save(order);

        assertEquals(2L, result.getId());
        verify(orderRepository).save(order);
    }
}
