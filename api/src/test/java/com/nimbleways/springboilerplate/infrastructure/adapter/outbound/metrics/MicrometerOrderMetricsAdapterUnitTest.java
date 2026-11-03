package com.nimbleways.springboilerplate.infrastructure.adapter.outbound.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MicrometerOrderMetricsAdapterUnitTest {

    private MeterRegistry meterRegistry;
    private MicrometerOrderMetricsAdapter adapter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        adapter = new MicrometerOrderMetricsAdapter(meterRegistry);
    }

    @Test
    void recordOrderProcessed_incrementsCounter() {
        adapter.recordOrderProcessed("SUCCESS");
        adapter.recordOrderProcessed("SUCCESS");

        Counter counter = meterRegistry.find("orders.processed.total")
                .tag("status", "SUCCESS")
                .counter();

        assertNotNull(counter);
        assertEquals(2.0, counter.count());
    }

    @Test
    void recordProductProcessed_incrementsCounter() {
        adapter.recordProductProcessed("NORMAL");
        adapter.recordProductProcessed("NORMAL");
        adapter.recordProductProcessed("SEASONAL");

        Counter normalCounter = meterRegistry.find("products.processed.total")
                .tag("type", "NORMAL")
                .counter();
        Counter seasonalCounter = meterRegistry.find("products.processed.total")
                .tag("type", "SEASONAL")
                .counter();

        assertNotNull(normalCounter);
        assertEquals(2.0, normalCounter.count());
        assertNotNull(seasonalCounter);
        assertEquals(1.0, seasonalCounter.count());
    }

    @Test
    void recordProductProcessed_handlesNullType() {
        adapter.recordProductProcessed(null);

        Counter unknownCounter = meterRegistry.find("products.processed.total")
                .tag("type", "UNKNOWN")
                .counter();

        assertNotNull(unknownCounter);
        assertEquals(1.0, unknownCounter.count());
    }
}
