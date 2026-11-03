package com.nimbleways.springboilerplate.infrastructure.adapter.inbound.rest;

import com.nimbleways.springboilerplate.domain.port.inbound.ProcessOrderUseCase;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class MyControllerUnitTest {

    @Mock
    private ProcessOrderUseCase processOrderUseCase;

    @InjectMocks
    private MyController myController;

    @Test
    void processOrder_shouldDelegateToProcessOrderUseCaseAndReturnResponse() {
        // GIVEN
        Long orderId = 1L;
        ProcessOrderResponse expectedResponse = new ProcessOrderResponse(orderId);
        when(processOrderUseCase.processOrder(orderId)).thenReturn(expectedResponse);

        // WHEN
        ProcessOrderResponse actualResponse = myController.processOrder(orderId);

        // THEN
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(processOrderUseCase, times(1)).processOrder(orderId);
    }
}
