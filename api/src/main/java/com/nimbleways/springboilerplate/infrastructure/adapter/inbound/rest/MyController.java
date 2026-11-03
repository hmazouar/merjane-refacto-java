package com.nimbleways.springboilerplate.infrastructure.adapter.inbound.rest;

import com.nimbleways.springboilerplate.domain.port.inbound.ProcessOrderUseCase;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for order processing and inventory operations")
public class MyController {

    private final ProcessOrderUseCase processOrderUseCase;

    @Operation(
            summary = "Process an order",
            description = "Processes all product items belonging to the given order according to their type " +
                    "(NORMAL, SEASONAL, EXPIRABLE). Updates available stock, places replenishment orders with suppliers " +
                    "when inventory is depleted, and sends appropriate notifications (delay, out of stock, expiration)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order was processed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProcessOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found with the provided identifier",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during processing",
                    content = @Content
            )
    })
    @PostMapping(value = "{orderId}/processOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(
            @Parameter(description = "Unique identifier of the order to process", required = true, example = "1")
            @PathVariable Long orderId) {
        log.info("Received request to process order with ID: {}", orderId);
        ProcessOrderResponse response = processOrderUseCase.processOrder(orderId);
        log.info("Successfully processed order with ID: {}", orderId);
        return response;
    }
}
