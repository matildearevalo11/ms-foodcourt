package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Employee;

public interface ILoggedUserPort {
    Long getLoggedUserId();
    Long getLoggedRestaurantId();
    Employee getLoggedEmployee();
}
