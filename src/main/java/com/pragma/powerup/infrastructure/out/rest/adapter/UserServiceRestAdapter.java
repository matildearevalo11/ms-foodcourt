package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.infrastructure.out.rest.dto.UserRoleResponse;
import com.pragma.powerup.infrastructure.out.rest.dto.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class UserServiceRestAdapter implements IOwnerValidationPort {
    private final RestClient usersRestClient;

    @Override
    public boolean isOwner(Long userId) {
        try {
            UserRoleResponse response = usersRestClient.get()
                    .uri("/users/{userId}/role", userId)
                    .retrieve()
                    .body(UserRoleResponse.class);
            return response != null && response.data() != null
                    && UserRoleEnum.OWNER == response.data().role();
        } catch (HttpClientErrorException.NotFound ignoredException) {
            return false;
        } catch (RestClientException exception) {
            throw new ExternalServiceException(ExceptionMessages.USERS_SERVICE_UNAVAILABLE.getMessage());
        }
    }
}
