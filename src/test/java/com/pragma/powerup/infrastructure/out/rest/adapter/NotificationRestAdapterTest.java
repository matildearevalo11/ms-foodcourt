package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NotificationRestAdapterTest {
    @Test
    void notifyOrderReady_ShouldCallMessagingService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://messaging");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NotificationRestAdapter adapter = new NotificationRestAdapter(builder.build());
        server.expect(requestTo("http://messaging/notifications/order-ready"))
                .andExpect(content().json("""
                        {"phoneNumber":"+573001234567","securityPin":"482913"}
                        """))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        adapter.notifyOrderReady("+573001234567", "482913");

        server.verify();
    }

    @Test
    void notifyOrderReady_WhenMessagingFails_ShouldThrowExternalServiceException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://messaging");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NotificationRestAdapter adapter = new NotificationRestAdapter(builder.build());
        server.expect(requestTo("http://messaging/notifications/order-ready"))
                .andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () ->
                adapter.notifyOrderReady("+573001234567", "482913"));
    }
}
