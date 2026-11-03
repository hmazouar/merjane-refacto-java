package com.nimbleways.springboilerplate.domain.port.inbound;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;

public interface ProcessOrderUseCase {
    ProcessOrderResponse processOrder(Long orderId);
}
