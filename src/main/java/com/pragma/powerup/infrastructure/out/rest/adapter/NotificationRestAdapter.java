package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.infrastructure.out.rest.dto.OrderReadyNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class NotificationRestAdapter implements INotificationPort {
    private final RestClient messagingRestClient;

    @Override
    public void notifyOrderReady(String cellphone, String securityPin) {
        try {
            messagingRestClient.post()
                    .uri("/notifications/order-ready")
                    .body(new OrderReadyNotificationRequest(cellphone, securityPin))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ExternalServiceException(ExceptionMessages.MESSAGING_SERVICE_UNAVAILABLE.getMessage());
        }
    }
}
