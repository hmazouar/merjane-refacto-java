package com.nimbleways.springboilerplate.domain.service;

import com.nimbleways.springboilerplate.domain.port.inbound.ProcessOrderUseCase;
import com.nimbleways.springboilerplate.domain.port.outbound.OrderMetricsPort;
import com.nimbleways.springboilerplate.domain.port.outbound.OrderRepositoryPort;
import com.nimbleways.springboilerplate.domain.service.processor.ProductProcessor;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ProcessOrderService implements ProcessOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final List<ProductProcessor> productProcessors;
    private final OrderMetricsPort orderMetricsPort;

    @Autowired
    public ProcessOrderService(OrderRepositoryPort orderRepositoryPort,
                               List<ProductProcessor> productProcessors,
                               OrderMetricsPort orderMetricsPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.productProcessors = productProcessors;
        this.orderMetricsPort = orderMetricsPort != null ? orderMetricsPort : OrderMetricsPort.NOOP;
    }

    public ProcessOrderService(OrderRepositoryPort orderRepositoryPort,
                               List<ProductProcessor> productProcessors) {
        this(orderRepositoryPort, productProcessors, OrderMetricsPort.NOOP);
    }

    @Override
    public ProcessOrderResponse processOrder(Long orderId) {
        log.info("Processing order with ID: {}", orderId);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order with ID: {} not found", orderId);
                    orderMetricsPort.recordOrderProcessed("NOT_FOUND");
                    return new OrderNotFoundException(orderId);
                });

        Set<Product> products = order.getItems();
        int itemCount = products != null ? products.size() : 0;
        log.info("Order with ID: {} contains {} product(s)", orderId, itemCount);

        if (products != null) {
            for (Product product : products) {
                processProduct(product);
            }
        }

        orderMetricsPort.recordOrderProcessed("SUCCESS");
        log.info("Completed processing order with ID: {}", orderId);
        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        String productType = product != null ? product.getType() : "null";
        log.debug("Processing product ID: {}, type: '{}'", product != null ? product.getId() : null, productType);

        ProductProcessor processor = productProcessors.stream()
                .filter(p -> p.supports(product))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No processor found for product type: {}", productType);
                    return new IllegalArgumentException("Unsupported product type: " + productType);
                });

        processor.process(product);
        orderMetricsPort.recordProductProcessed(productType);
    }
}
