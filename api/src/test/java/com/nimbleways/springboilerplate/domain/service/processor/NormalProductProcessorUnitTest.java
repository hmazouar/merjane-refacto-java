package com.nimbleways.springboilerplate.domain.service.processor;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class NormalProductProcessorUnitTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private NotificationPort notificationPort;

    private NormalProductProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new NormalProductProcessor(productRepositoryPort, notificationPort);
    }

    @Test
    void supports_shouldReturnTrueForNormalAndFalseOtherwise() {
        Product normal = new Product(1L, 0, 1, "NORMAL", "Item", null, null, null);
        Product normalLower = new Product(2L, 0, 1, "normal", "Item", null, null, null);
        Product seasonal = new Product(3L, 0, 1, "SEASONAL", "Item", null, null, null);

        assertTrue(processor.supports(normal));
        assertTrue(processor.supports(normalLower));
        assertFalse(processor.supports(seasonal));
        assertFalse(processor.supports(null));
    }

    @Test
    void process_whenAvailable_shouldDecrementAvailableAndSave() {
        Product product = new Product(1L, 10, 5, "NORMAL", "Keyboard", null, null, null);

        processor.process(product);

        assertEquals(4, product.getAvailable());
        verify(productRepositoryPort).save(product);
        verifyNoInteractions(notificationPort);
    }

    @Test
    void process_whenOutOfStockAndLeadTimePositive_shouldNotifyDelayAndSave() {
        Product product = new Product(2L, 7, 0, "NORMAL", "Cable", null, null, null);

        processor.process(product);

        assertEquals(0, product.getAvailable());
        assertEquals(7, product.getLeadTime());
        verify(productRepositoryPort).save(product);
        verify(notificationPort).sendDelayNotification(7, "Cable");
    }

    @Test
    void process_whenOutOfStockAndLeadTimeZero_shouldDoNothing() {
        Product product = new Product(3L, 0, 0, "NORMAL", "Mouse", null, null, null);

        processor.process(product);

        verifyNoInteractions(productRepositoryPort);
        verifyNoInteractions(notificationPort);
    }
}
