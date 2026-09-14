package com.pragma.powerup.infrastructure.out.rest.adapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TraceabilityRestAdapterTest {
    private MockRestServiceServer server;
    private TraceabilityRestAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://traceability.test/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new TraceabilityRestAdapter(builder.build());
    }

    @Test
    void registersPendingOrder() {
        server.expect(once(), requestTo("http://traceability.test/api/v1/traceability"))
                .andExpect(content().json("""
                        {"orderId":30,"customerId":20,"restaurantId":5,"newStatus":"PENDING"}
                        """))
                .andRespond(withSuccess());

        adapter.registerStatusChange(order(), null, null);

        server.verify();
    }

    @Test
    void translatesTraceabilityServiceFailure() {
        server.expect(once(), requestTo("http://traceability.test/api/v1/traceability"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.registerStatusChange(order(), null, null))
                .isInstanceOf(ExternalServiceException.class);
        server.verify();
    }

    @Test
    void registersEmployeeAssignment() {
        Order order = order();
        order.setAssignedEmployeeId(40L);
        order.setStatus(OrderStatus.IN_PREPARATION);
        server.expect(once(), requestTo("http://traceability.test/api/v1/traceability"))
                .andExpect(content().json("""
                        {"orderId":30,"customerId":20,"restaurantId":5,"employeeId":40,
                        "previousStatus":"PENDING","newStatus":"IN_PREPARATION"}
                        """))
                .andRespond(withSuccess());

        adapter.registerStatusChange(order, OrderStatus.PENDING, 40L);

        server.verify();
    }

    private Order order() {
        return new Order(30L, 20L, 5L, null, OrderStatus.PENDING, null, null);
    }
}
