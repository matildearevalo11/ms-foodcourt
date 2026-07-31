package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserContactRestAdapterTest {
    @Test
    void getCellphone_ShouldReturnCustomerCellphone() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://users");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UserContactRestAdapter adapter = new UserContactRestAdapter(builder.build());
        server.expect(requestTo("http://users/users/20/contact"))
                .andRespond(withSuccess("{\"data\":{\"id\":20,\"cellphone\":\"+573001234567\"}}",
                        org.springframework.http.MediaType.APPLICATION_JSON));

        assertEquals("+573001234567", adapter.getCellphone(20L));
    }

    @Test
    void getCellphone_WhenUsersServiceFails_ShouldThrowExternalServiceException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://users");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UserContactRestAdapter adapter = new UserContactRestAdapter(builder.build());
        server.expect(requestTo("http://users/users/20/contact")).andRespond(withServerError());

        assertThrows(ExternalServiceException.class, () -> adapter.getCellphone(20L));
    }
}
