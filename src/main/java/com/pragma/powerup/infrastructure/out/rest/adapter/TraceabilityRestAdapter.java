package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import com.pragma.powerup.infrastructure.out.rest.dto.TraceabilityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class TraceabilityRestAdapter implements ITraceabilityPort {
    private final RestClient restClient;

    @Override
    public void registerStatusChange(Order order, OrderStatus previousStatus, Long employeeId) {
        try {
            restClient.post()
                    .uri("/traceability")
                    .body(new TraceabilityRequest(order.getId(), order.getCustomerId(), order.getRestaurantId(),
                            employeeId, previousStatus, order.getStatus()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ExternalServiceException(ExceptionMessages.TRACEABILITY_SERVICE_UNAVAILABLE.getMessage());
        }
    }
}
