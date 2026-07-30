package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TraceabilityRestAdapterTest {
    @Test
    void registerStatusChange_ShouldSendOrderTraceability() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://traceability");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TraceabilityRestAdapter adapter = new TraceabilityRestAdapter(builder.build());
        server.expect(once(), requestTo("http://traceability/traceability"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(jsonPath("$.orderId").value(25))
                .andExpect(jsonPath("$.newStatus").value("PENDING"))
                .andRespond(withSuccess());

        adapter.registerStatusChange(order(), null);

        server.verify();
    }

    @Test
    void registerStatusChange_WhenServiceFails_ShouldThrowExternalServiceException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://traceability");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TraceabilityRestAdapter adapter = new TraceabilityRestAdapter(builder.build());
        server.expect(requestTo("http://traceability/traceability"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(ExternalServiceException.class,
                () -> adapter.registerStatusChange(order(), null));
    }

    private Order order() {
        Order order = new Order();
        order.setId(25L);
        order.setCustomerId(20L);
        order.setRestaurantId(5L);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }
}
