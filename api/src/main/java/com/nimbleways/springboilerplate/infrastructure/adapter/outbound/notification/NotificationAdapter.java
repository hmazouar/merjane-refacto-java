package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.notification;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

    private final NotificationService notificationService;

    @Override
    public void sendDelayNotification(int leadTime, String productName) {
        notificationService.sendDelayNotification(leadTime, productName);
    }

    @Override
    public void sendOutOfStockNotification(String productName) {
        notificationService.sendOutOfStockNotification(productName);
    }

    @Override
    public void sendExpirationNotification(String productName, LocalDate expiryDate) {
        notificationService.sendExpirationNotification(productName, expiryDate);
    }
}
