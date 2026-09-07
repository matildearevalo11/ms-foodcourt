package com.pragma.powerup.infrastructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RestaurantRestController.class)
@Import(ControllerAdvisor.class)
class RestaurantRestControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    IRestaurantHandler handler;

    @Test
    void createsRestaurant() throws Exception {
        when(handler.createRestaurant(any())).thenReturn(new RestaurantResponseDto(
                1L, "Restaurante 123", "9001", "Local 1", "3001234567", "https://logo.test/a.png", 7L));

        mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void rejectsInvalidNameNitAndPhone() throws Exception {
        mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().replace("Restaurante 123", " 12345 ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().replace("\"9001\"", "\"ABC\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nit").exists());

        mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().replace("\"3001234567\"", "\"+5730056983259\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void reportsUnavailableUsersService() throws Exception {
        when(handler.createRestaurant(any())).thenThrow(new ExternalServiceException("User service is unavailable"));

        mvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errors.message").value("User service is unavailable"));
    }

    private String validBody() {
        return """
                {"name":"Restaurante 123","nit":"9001","address":"Local 1","phone":"3001234567",
                 "urlLogo":"https://logo.test/a.png","ownerId":7}
                """;
    }
}
