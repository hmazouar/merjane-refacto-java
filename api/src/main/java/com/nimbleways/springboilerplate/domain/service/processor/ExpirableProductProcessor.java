package com.nimbleways.springboilerplate.domain.service.processor;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class ExpirableProductProcessor implements ProductProcessor {

    private final ProductRepositoryPort productRepositoryPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    @Autowired
    public ExpirableProductProcessor(ProductRepositoryPort productRepositoryPort,
                                     NotificationPort notificationPort,
                                     Clock clock) {
        this.productRepositoryPort = productRepositoryPort;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    public ExpirableProductProcessor(ProductRepositoryPort productRepositoryPort,
                                     NotificationPort notificationPort) {
        this(productRepositoryPort, notificationPort, Clock.systemDefaultZone());
    }

    @Override
    public boolean supports(Product product) {
        return product != null && "EXPIRABLE".equalsIgnoreCase(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate now = LocalDate.now(clock);
        if (product.getAvailable() > 0 && product.getExpiryDate() != null && product.getExpiryDate().isAfter(now)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepositoryPort.save(product);
        } else {
            notificationPort.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepositoryPort.save(product);
        }
    }
}
