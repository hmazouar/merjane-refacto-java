package com.nimbleways.springboilerplate.domain.service.processor;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NormalProductProcessor implements ProductProcessor {

    private final ProductRepositoryPort productRepositoryPort;
    private final NotificationPort notificationPort;

    @Override
    public boolean supports(Product product) {
        return product != null && "NORMAL".equalsIgnoreCase(product.getType());
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0) {
            log.info("Decreasing stock for normal product '{}' (new stock: {})", product.getName(), product.getAvailable() - 1);
            product.setAvailable(product.getAvailable() - 1);
            productRepositoryPort.save(product);
        } else {
            int leadTime = product.getLeadTime() != null ? product.getLeadTime() : 0;
            if (leadTime > 0) {
                log.info("Normal product '{}' out of stock. Setting lead time: {} days and sending notification", product.getName(), leadTime);
                product.setLeadTime(leadTime);
                productRepositoryPort.save(product);
                notificationPort.sendDelayNotification(leadTime, product.getName());
            } else {
                log.warn("Normal product '{}' out of stock with 0 lead time. No notification sent.", product.getName());
            }
        }
    }
}
