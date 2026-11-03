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
public class SeasonalProductProcessorUnitTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private NotificationPort notificationPort;

    private Clock fixedClock;
    private SeasonalProductProcessor processor;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("UTC"));
        processor = new SeasonalProductProcessor(productRepositoryPort, notificationPort, fixedClock);
    }

    @Test
    void supports_shouldReturnTrueForSeasonalAndFalseOtherwise() {
        Product seasonal = new Product(1L, 0, 1, "SEASONAL", "Item", null, null, null);
        Product seasonalLower = new Product(2L, 0, 1, "seasonal", "Item", null, null, null);
        Product normal = new Product(3L, 0, 1, "NORMAL", "Item", null, null, null);

        assertTrue(processor.supports(seasonal));
        assertTrue(processor.supports(seasonalLower));
        assertFalse(processor.supports(normal));
        assertFalse(processor.supports(null));
    }

    @Test
    void process_whenInSeasonAndAvailable_shouldDecrementAndSave() {
        // Today is 2026-06-15. In season: 2026-06-01 to 2026-06-30.
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        Product product = new Product(1L, 5, 10, "SEASONAL", "Watermelon", null, start, end);

        processor.process(product);

        assertEquals(9, product.getAvailable());
        verify(productRepositoryPort).save(product);
        verifyNoInteractions(notificationPort);
    }

    @Test
    void process_whenLeadTimeExceedsSeasonEndDate_shouldNotifyOutOfStockAndSetAvailableToZero() {
        // Today is 2026-06-15. Season ended: 2026-06-10. Lead time is 10 days. Available is 5.
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 6, 10);
        Product product = new Product(2L, 10, 5, "SEASONAL", "Watermelon", null, start, end);

        processor.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationPort).sendOutOfStockNotification("Watermelon");
        verify(productRepositoryPort).save(product);
        verify(notificationPort, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void process_whenSeasonHasNotStartedYet_shouldNotifyOutOfStockAndKeepAvailable() {
        // Today is 2026-06-15. Start is 2026-07-01.
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        Product product = new Product(3L, 5, 8, "SEASONAL", "Grapes", null, start, end);

        processor.process(product);

        assertEquals(8, product.getAvailable());
        verify(notificationPort).sendOutOfStockNotification("Grapes");
        verify(productRepositoryPort).save(product);
        verify(notificationPort, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void process_whenCanReorderWithinSeason_shouldNotifyDelay() {
        // Today is 2026-06-15. Start is 2026-06-01. End is 2026-07-31. Lead time 10 days (arrives 2026-06-25).
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        Product product = new Product(4L, 10, 0, "SEASONAL", "Cherries", null, start, end);

        processor.process(product);

        assertEquals(10, product.getLeadTime());
        verify(notificationPort).sendDelayNotification(10, "Cherries");
        verify(productRepositoryPort).save(product);
        verify(notificationPort, never()).sendOutOfStockNotification(anyString());
    }
}
