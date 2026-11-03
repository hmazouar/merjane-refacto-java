package com.nimbleways.springboilerplate.infrastructure.adapter.inbound.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsEndpoint_returnsOpenApiJsonWithDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Merjane Order Processing API")))
                .andExpect(content().string(containsString("/orders/{orderId}/processOrder")))
                .andExpect(content().string(containsString("Process an order")));
    }

    @Test
    void swaggerUiEndpoint_isAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
