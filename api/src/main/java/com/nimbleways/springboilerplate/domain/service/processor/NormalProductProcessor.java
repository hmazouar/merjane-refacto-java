package com.nimbleways.springboilerplate.domain.service.processor;

import com.nimbleways.springboilerplate.domain.port.outbound.NotificationPort;
import com.nimbleways.springboilerplate.domain.port.outbound.ProductRepositoryPort;
import com.nimbleways.springboilerplate.entities.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            product.setAvailable(product.getAvailable() - 1);
            productRepositoryPort.save(product);
        } else {
            int leadTime = product.getLeadTime() != null ? product.getLeadTime() : 0;
            if (leadTime > 0) {
                product.setLeadTime(leadTime);
                productRepositoryPort.save(product);
                notificationPort.sendDelayNotification(leadTime, product.getName());
            }
        }
    }
}
