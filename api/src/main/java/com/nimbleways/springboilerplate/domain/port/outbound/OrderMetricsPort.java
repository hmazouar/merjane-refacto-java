package com.nimbleways.springboilerplate.domain.port.outbound;

public interface OrderMetricsPort {

    void recordOrderProcessed(String status);

    void recordProductProcessed(String type);

    OrderMetricsPort NOOP = new OrderMetricsPort() {
        @Override
        public void recordOrderProcessed(String status) {
        }

        @Override
        public void recordProductProcessed(String type) {
        }
    };
}
