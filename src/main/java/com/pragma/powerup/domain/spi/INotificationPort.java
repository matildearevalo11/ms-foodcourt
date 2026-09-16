package com.pragma.powerup.domain.spi;

public interface INotificationPort {
    void notifyOrderReady(String cellphone, String securityPin);
}
