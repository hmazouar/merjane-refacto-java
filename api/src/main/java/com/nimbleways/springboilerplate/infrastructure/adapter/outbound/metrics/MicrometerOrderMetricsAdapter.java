package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.metrics;

import com.nimbleways.springboilerplate.domain.port.outbound.OrderMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerOrderMetricsAdapter implements OrderMetricsPort {

    private final MeterRegistry meterRegistry;

    public MicrometerOrderMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordOrderProcessed(String status) {
        Counter.builder("orders.processed.total")
                .description("Total number of orders processed")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordProductProcessed(String type) {
        Counter.builder("products.processed.total")
                .description("Total number of products processed by type")
                .tag("type", type != null ? type : "UNKNOWN")
                .register(meterRegistry)
                .increment();
    }
}
