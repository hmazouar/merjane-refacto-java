package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.notification;

import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class NotificationAdapterUnitTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationAdapter adapter;

    @Test
    void sendDelayNotification_shouldDelegateToNotificationService() {
        adapter.sendDelayNotification(10, "Product A");
        verify(notificationService).sendDelayNotification(10, "Product A");
    }

    @Test
    void sendOutOfStockNotification_shouldDelegateToNotificationService() {
        adapter.sendOutOfStockNotification("Product B");
        verify(notificationService).sendOutOfStockNotification("Product B");
    }

    @Test
    void sendExpirationNotification_shouldDelegateToNotificationService() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        adapter.sendExpirationNotification("Product C", date);
        verify(notificationService).sendExpirationNotification("Product C", date);
    }
}
