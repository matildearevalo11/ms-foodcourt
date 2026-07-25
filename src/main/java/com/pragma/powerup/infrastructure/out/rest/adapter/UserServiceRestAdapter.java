package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.infrastructure.out.rest.dto.UserRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class UserServiceRestAdapter implements IOwnerValidationPort {
    private static final String OWNER_ROLE = "OWNER";
    private final RestClient usersRestClient;

    @Override
    public boolean isOwner(Long userId) {
        try {
            UserRoleResponse response = usersRestClient.get()
                    .uri("/users/{userId}/role", userId)
                    .retrieve()
                    .body(UserRoleResponse.class);
            return response != null && response.data() != null
                    && OWNER_ROLE.equalsIgnoreCase(response.data().role());
        } catch (HttpClientErrorException.NotFound ignored) {
            return false;
        } catch (RestClientException exception) {
            throw new ExternalServiceException(ExceptionMessages.USERS_SERVICE_UNAVAILABLE.getMessage());
        }
    }
}
