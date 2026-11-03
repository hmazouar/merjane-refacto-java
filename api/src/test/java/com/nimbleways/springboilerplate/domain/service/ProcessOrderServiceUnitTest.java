package com.nimbleways.springboilerplate.domain.service;

import com.nimbleways.springboilerplate.domain.port.outbound.OrderMetricsPort;
import com.nimbleways.springboilerplate.domain.port.outbound.OrderRepositoryPort;
import com.nimbleways.springboilerplate.domain.service.processor.ProductProcessor;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class ProcessOrderServiceUnitTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private ProductProcessor processor1;

    @Mock
    private ProductProcessor processor2;

    @Mock
    private OrderMetricsPort orderMetricsPort;

    private ProcessOrderService processOrderService;

    @BeforeEach
    void setUp() {
        processOrderService = new ProcessOrderService(orderRepositoryPort, List.of(processor1, processor2), orderMetricsPort);
    }

    @Test
    void processOrder_whenOrderExists_shouldProcessEachProductAndReturnResponse() {
        // GIVEN
        Long orderId = 100L;
        Product productA = new Product(1L, 10, 5, "TYPE_A", "Item A", null, null, null);
        Product productB = new Product(2L, 5, 2, "TYPE_B", "Item B", null, null, null);

        Order order = new Order(orderId, Set.of(productA, productB));
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        when(processor1.supports(any())).thenAnswer(inv -> "TYPE_A".equals(((Product) inv.getArgument(0)).getType()));
        when(processor2.supports(any())).thenAnswer(inv -> "TYPE_B".equals(((Product) inv.getArgument(0)).getType()));

        // WHEN
        ProcessOrderResponse response = processOrderService.processOrder(orderId);

        // THEN
        assertNotNull(response);
        assertEquals(orderId, response.id());
        verify(processor1).process(productA);
        verify(processor2).process(productB);
        verify(orderMetricsPort).recordOrderProcessed("SUCCESS");
        verify(orderMetricsPort).recordProductProcessed("TYPE_A");
        verify(orderMetricsPort).recordProductProcessed("TYPE_B");
    }

    @Test
    void processOrder_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {
        // GIVEN
        Long orderId = 999L;
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(OrderNotFoundException.class, () -> processOrderService.processOrder(orderId));
        verify(orderMetricsPort).recordOrderProcessed("NOT_FOUND");
    }

    @Test
    void processOrder_whenNoProcessorSupportsProduct_shouldThrowIllegalArgumentException() {
        // GIVEN
        Long orderId = 101L;
        Product unknownProduct = new Product(3L, 0, 1, "UNKNOWN", "Mysterious Item", null, null, null);
        Order order = new Order(orderId, Set.of(unknownProduct));

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(processor1.supports(unknownProduct)).thenReturn(false);
        when(processor2.supports(unknownProduct)).thenReturn(false);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> processOrderService.processOrder(orderId));
    }

    @Test
    void processOrder_whenOrderHasNoItems_shouldReturnResponseWithoutProcessing() {
        // GIVEN
        Long orderId = 102L;
        Order order = new Order(orderId, null);
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        // WHEN
        ProcessOrderResponse response = processOrderService.processOrder(orderId);

        // THEN
        assertNotNull(response);
        assertEquals(orderId, response.id());
        verifyNoInteractions(processor1, processor2);
        verify(orderMetricsPort).recordOrderProcessed("SUCCESS");
    }

    @Test
    void processOrder_whenProductIsNull_shouldThrowIllegalArgumentException() {
        // GIVEN
        Long orderId = 103L;
        Set<Product> items = new java.util.HashSet<>();
        items.add(null);
        Order order = new Order(orderId, items);
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> processOrderService.processOrder(orderId));
    }

    @Test
    void constructorWithoutMetricsPort_usesNoopPort() {
        ProcessOrderService serviceWithoutMetrics = new ProcessOrderService(orderRepositoryPort, List.of(processor1));
        Long orderId = 104L;
        Order order = new Order(orderId, null);
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        ProcessOrderResponse response = serviceWithoutMetrics.processOrder(orderId);
        assertNotNull(response);
    }
}
