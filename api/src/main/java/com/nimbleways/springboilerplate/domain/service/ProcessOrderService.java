package com.nimbleways.springboilerplate.domain.service;

import com.nimbleways.springboilerplate.domain.port.inbound.ProcessOrderUseCase;
import com.nimbleways.springboilerplate.domain.port.outbound.OrderRepositoryPort;
import com.nimbleways.springboilerplate.domain.service.processor.ProductProcessor;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProcessOrderService implements ProcessOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final List<ProductProcessor> productProcessors;

    @Override
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Set<Product> products = order.getItems();
        if (products != null) {
            for (Product product : products) {
                processProduct(product);
            }
        }

        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        ProductProcessor processor = productProcessors.stream()
                .filter(p -> p.supports(product))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported product type: " + (product != null ? product.getType() : "null")));
        processor.process(product);
    }
}
