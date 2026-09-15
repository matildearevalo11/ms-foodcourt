package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.spi.IUserContactPort;
import com.pragma.powerup.infrastructure.out.rest.dto.UserContactResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class UserContactRestAdapter implements IUserContactPort {
    private final RestClient restClient;

    @Override
    public String getCustomerCellphone(Long customerId) {
        try {
            UserContactResponse response = restClient.get()
                    .uri("/users/{userId}/contact", customerId)
                    .retrieve()
                    .body(UserContactResponse.class);
            if (response == null || response.data() == null || response.data().cellphone() == null) {
                throw new ExternalServiceException(ExceptionMessages.CUSTOMER_CONTACT_UNAVAILABLE.getMessage());
            }
            return response.data().cellphone();
        } catch (RestClientException exception) {
            throw new ExternalServiceException(ExceptionMessages.CUSTOMER_CONTACT_UNAVAILABLE.getMessage());
        }
    }
}
