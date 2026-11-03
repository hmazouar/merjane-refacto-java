package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks 
    private ProductService productService;

    @Test
    public void notifyDelay_shouldUpdateLeadTimeSaveAndNotify() {
        // GIVEN
        Product product = new Product(1L, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product.getLeadTime(), product);

        // THEN
        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    public void handleSeasonalProduct_whenLeadTimeExceedsSeasonEndDate_shouldNotifyOutOfStockAndSetAvailableToZero() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(2L, 10, 5, "SEASONAL", "Watermelon", null, now.minusDays(10), now.plusDays(5));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification("Watermelon");
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void handleSeasonalProduct_whenSeasonHasNotStartedYet_shouldNotifyOutOfStockAndKeepAvailable() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(3L, 5, 12, "SEASONAL", "Grapes", null, now.plusDays(10), now.plusDays(60));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        assertEquals(12, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification("Grapes");
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void handleSeasonalProduct_whenInSeasonAndCanReorderInTime_shouldNotifyDelay() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(4L, 5, 0, "SEASONAL", "Strawberries", null, now.minusDays(5), now.plusDays(20));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(5, "Strawberries");
        Mockito.verify(notificationService, Mockito.never()).sendOutOfStockNotification(Mockito.anyString());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
    }

    @Test
    public void handleExpiredProduct_whenAvailableAndNotExpired_shouldDecrementAvailableAndSave() {
        // GIVEN
        LocalDate now = LocalDate.now();
        Product product = new Product(5L, 5, 10, "EXPIRABLE", "Fresh Milk", now.plusDays(5), null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        assertEquals(9, product.getAvailable());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.never()).sendExpirationNotification(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void handleExpiredProduct_whenExpired_shouldNotifyExpirationAndSetAvailableToZero() {
        // GIVEN
        LocalDate now = LocalDate.now();
        LocalDate expiryDate = now.minusDays(1);
        Product product = new Product(6L, 5, 8, "EXPIRABLE", "Old Yogurt", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification("Old Yogurt", expiryDate);
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
    }

    @Test
    public void handleExpiredProduct_whenZeroAvailableEvenIfNotExpired_shouldNotifyExpirationAndSetAvailableToZero() {
        // GIVEN
        LocalDate now = LocalDate.now();
        LocalDate expiryDate = now.plusDays(10);
        Product product = new Product(7L, 5, 0, "EXPIRABLE", "Cheese", expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification("Cheese", expiryDate);
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
    }
}