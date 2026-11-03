package com.nimbleways.springboilerplate.domain.service.processor;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class SeasonalProductProcessor implements ProductProcessor {

    private final ProductRepositoryPort productRepositoryPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    @Autowired
    public SeasonalProductProcessor(ProductRepositoryPort productRepositoryPort,
                                    NotificationPort notificationPort,
                                    Clock clock) {
        this.productRepositoryPort = productRepositoryPort;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    public SeasonalProductProcessor(ProductRepositoryPort productRepositoryPort,
                                    NotificationPort notificationPort) {
        this(productRepositoryPort, notificationPort, Clock.systemDefaultZone());
    }

    @Override
    public boolean supports(Product product) {
        return product != null && "SEASONAL".equalsIgnoreCase(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate now = LocalDate.now(clock);
        if (now.isAfter(product.getSeasonStartDate()) && now.isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepositoryPort.save(product);
        } else {
            handleSeasonalProduct(product, now);
        }
    }

    private void handleSeasonalProduct(Product product, LocalDate now) {
        int leadTime = product.getLeadTime() != null ? product.getLeadTime() : 0;
        if (now.plusDays(leadTime).isAfter(product.getSeasonEndDate())) {
            notificationPort.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepositoryPort.save(product);
        } else if (product.getSeasonStartDate().isAfter(now)) {
            notificationPort.sendOutOfStockNotification(product.getName());
            productRepositoryPort.save(product);
        } else {
            product.setLeadTime(leadTime);
            productRepositoryPort.save(product);
            notificationPort.sendDelayNotification(leadTime, product.getName());
        }
    }
}
