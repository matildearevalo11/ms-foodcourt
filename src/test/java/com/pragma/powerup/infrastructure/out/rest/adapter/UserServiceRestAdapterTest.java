package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class UserServiceRestAdapterTest {
    private MockRestServiceServer server;
    private UserServiceRestAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://users/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new UserServiceRestAdapter(builder.build());
    }

    @Test
    void isOwner_WhenRoleIsOwner_ShouldReturnTrue() {
        server.expect(once(), requestTo("http://users/api/v1/users/7/role"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":{\"id\":7,\"role\":\"OWNER\"}}", MediaType.APPLICATION_JSON));
        assertTrue(adapter.isOwner(7L));
        server.verify();
    }

    @Test
    void isOwner_WhenRoleIsDifferent_ShouldReturnFalse() {
        server.expect(requestTo("http://users/api/v1/users/7/role"))
                .andRespond(withSuccess("{\"data\":{\"id\":7,\"role\":\"CLIENT\"}}", MediaType.APPLICATION_JSON));
        assertFalse(adapter.isOwner(7L));
    }

    @Test
    void isOwner_WhenBodyIsEmpty_ShouldReturnFalse() {
        server.expect(requestTo("http://users/api/v1/users/7/role"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        assertFalse(adapter.isOwner(7L));
    }

    @Test
    void isOwner_WhenUserDoesNotExist_ShouldReturnFalse() {
        server.expect(requestTo("http://users/api/v1/users/99/role"))
                .andRespond(withResourceNotFound());
        assertFalse(adapter.isOwner(99L));
    }

    @Test
    void isOwner_WhenServiceFails_ShouldThrowExternalServiceException() {
        server.expect(requestTo("http://users/api/v1/users/7/role"))
                .andRespond(withServerError());
        assertThrows(ExternalServiceException.class, () -> adapter.isOwner(7L));
    }
}
