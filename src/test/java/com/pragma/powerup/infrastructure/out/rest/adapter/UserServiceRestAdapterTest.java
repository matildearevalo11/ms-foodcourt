package com.pragma.powerup.infrastructure.out.rest.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class UserServiceRestAdapterTest {
    private MockRestServiceServer server;
    private UserServiceRestAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://users.test/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new UserServiceRestAdapter(builder.build());
    }

    @Test
    void recognizesOwnerRole() {
        server.expect(once(), requestTo("http://users.test/api/v1/users/7/role"))
                .andRespond(withSuccess("{\"data\":{\"id\":7,\"role\":\"OWNER\"}}", MediaType.APPLICATION_JSON));

        assertThat(adapter.isOwner(7L)).isTrue();
        server.verify();
    }

    @Test
    void rejectsNonOwnerAndUnknownUser() {
        server.expect(once(), requestTo("http://users.test/api/v1/users/8/role"))
                .andRespond(withSuccess("{\"data\":{\"id\":8,\"role\":\"CUSTOMER\"}}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://users.test/api/v1/users/9/role"))
                .andRespond(withResourceNotFound());

        assertThat(adapter.isOwner(8L)).isFalse();
        assertThat(adapter.isOwner(9L)).isFalse();
        server.verify();
    }

    @Test
    void mapsUsersServiceFailureToDomainException() {
        server.expect(once(), requestTo("http://users.test/api/v1/users/7/role"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.isOwner(7L)).isInstanceOf(ExternalServiceException.class);
        server.verify();
    }
}
