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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class ExpirableProductProcessorUnitTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private NotificationPort notificationPort;

    private Clock fixedClock;
    private ExpirableProductProcessor processor;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("UTC"));
        processor = new ExpirableProductProcessor(productRepositoryPort, notificationPort, fixedClock);
    }

    @Test
    void supports_shouldReturnTrueForExpirableAndFalseOtherwise() {
        Product expirable = new Product(1L, 0, 1, "EXPIRABLE", "Milk", null, null, null);
        Product expirableLower = new Product(2L, 0, 1, "expirable", "Milk", null, null, null);
        Product normal = new Product(3L, 0, 1, "NORMAL", "Milk", null, null, null);

        assertTrue(processor.supports(expirable));
        assertTrue(processor.supports(expirableLower));
        assertFalse(processor.supports(normal));
        assertFalse(processor.supports(null));
    }

    @Test
    void process_whenAvailableAndNotExpired_shouldDecrementAndSave() {
        // Today is 2026-06-15. Expiry is 2026-06-25.
        LocalDate expiryDate = LocalDate.of(2026, 6, 25);
        Product product = new Product(1L, 5, 8, "EXPIRABLE", "Fresh Milk", expiryDate, null, null);

        processor.process(product);

        assertEquals(7, product.getAvailable());
        verify(productRepositoryPort).save(product);
        verifyNoInteractions(notificationPort);
    }

    @Test
    void process_whenExpired_shouldNotifyExpirationSetAvailableToZeroAndSave() {
        // Today is 2026-06-15. Expiry was 2026-06-10.
        LocalDate expiryDate = LocalDate.of(2026, 6, 10);
        Product product = new Product(2L, 5, 4, "EXPIRABLE", "Old Milk", expiryDate, null, null);

        processor.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationPort).sendExpirationNotification("Old Milk", expiryDate);
        verify(productRepositoryPort).save(product);
    }

    @Test
    void process_whenZeroAvailableEvenIfNotExpired_shouldNotifyExpirationSetAvailableToZeroAndSave() {
        // Today is 2026-06-15. Expiry is 2026-06-25, but 0 available.
        LocalDate expiryDate = LocalDate.of(2026, 6, 25);
        Product product = new Product(3L, 5, 0, "EXPIRABLE", "Yogurt", expiryDate, null, null);

        processor.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationPort).sendExpirationNotification("Yogurt", expiryDate);
        verify(productRepositoryPort).save(product);
    }
}
