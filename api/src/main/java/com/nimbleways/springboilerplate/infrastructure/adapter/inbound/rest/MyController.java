package com.nimbleways.springboilerplate.infrastructure.adapter.inbound.rest;

import com.nimbleways.springboilerplate.domain.port.inbound.ProcessOrderUseCase;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class MyController {

    private final ProcessOrderUseCase processOrderUseCase;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        return processOrderUseCase.processOrder(orderId);
    }
}
