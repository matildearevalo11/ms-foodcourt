package com.pragma.powerup.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.exception.ExternalServiceException;
import com.pragma.powerup.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.pragma.powerup.infrastructure.configuration.SecurityConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import java.util.List;

@WebMvcTest(RestaurantRestController.class)
@Import(SecurityConfiguration.class)
class RestaurantRestControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IRestaurantHandler handler;

    @Test
    void createRestaurant_WithValidRequest_ShouldReturnCreated() throws Exception {
        RestaurantRequestDto request = validRequest();
        RestaurantResponseDto response = new RestaurantResponseDto();
        response.setId(1L);
        response.setName(request.getName());
        when(handler.saveRestaurant(any())).thenReturn(response);

        mockMvc.perform(post("/restaurants").with(adminJwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("El Corral 2"));
    }

    @Test
    void createRestaurant_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/restaurants").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    void createRestaurant_WithNumericName_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDto request = validRequest();
        request.setName("12345");
        performBadRequest(request, "name", "Restaurant name cannot contain only numbers");
    }

    @Test
    void createRestaurant_WithInvalidNit_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDto request = validRequest();
        request.setNit("900A");
        performBadRequest(request, "nit", "NIT must contain only numbers");
    }

    @Test
    void createRestaurant_WithLongPhone_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDto request = validRequest();
        request.setPhone("+5730012345678");
        performBadRequest(request, "phone", "Restaurant phone must be at most 13 characters");
    }

    @Test
    void createRestaurant_WhenDomainRejectsOwner_ShouldReturnBadRequest() throws Exception {
        when(handler.saveRestaurant(any())).thenThrow(new ValidationException("Owner invalid"));
        performStatus(validRequest(), 400, "Owner invalid");
    }

    @Test
    void createRestaurant_WhenNitExists_ShouldReturnBadRequest() throws Exception {
        when(handler.saveRestaurant(any())).thenThrow(new ValidationException("NIT exists"));
        performStatus(validRequest(), 400, "NIT exists");
    }

    @Test
    void createRestaurant_WhenUsersServiceFails_ShouldReturnUnavailable() throws Exception {
        when(handler.saveRestaurant(any())).thenThrow(new ExternalServiceException("Users unavailable"));
        performStatus(validRequest(), 503, "Users unavailable");
    }

    @Test
    void createRestaurant_WhenUnexpectedFailure_ShouldHideDetails() throws Exception {
        when(handler.saveRestaurant(any())).thenThrow(new RuntimeException("secret"));
        performStatus(validRequest(), 500, "An unexpected error occurred");
    }

    @Test
    void createRestaurant_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/restaurants").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRestaurant_AsOwner_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRestaurants_AsCustomer_ShouldReturnOnlySummaryAndPagination() throws Exception {
        when(handler.getRestaurants(0, 2)).thenReturn(new PageResponseDto<>(
                List.of(new RestaurantSummaryResponseDto("Arepas", "arepas.png")),
                new PageMetadataDto(0, 2, 1, 1)));

        mockMvc.perform(get("/restaurants?page=0&size=2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Arepas"))
                .andExpect(jsonPath("$.data[0].urlLogo").value("arepas.png"))
                .andExpect(jsonPath("$.data[0].nit").doesNotExist())
                .andExpect(jsonPath("$.meta.totalElements").value(1));
    }

    @Test
    void getRestaurants_AsOwner_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/restaurants")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateOwnership_AsOwner_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(get("/restaurants/5/ownership")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
                .andExpect(status().isNoContent());

        verify(handler).validateLoggedOwner(5L);
    }

    private void performBadRequest(RestaurantRequestDto request, String field, String message) throws Exception {
        mockMvc.perform(post("/restaurants").with(adminJwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors." + field).value(message));
    }

    private void performStatus(RestaurantRequestDto request, int status, String message) throws Exception {
        mockMvc.perform(post("/restaurants").with(adminJwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(status)).andExpect(jsonPath("$.errors.message").value(message));
    }

    private RestaurantRequestDto validRequest() {
        RestaurantRequestDto request = new RestaurantRequestDto();
        request.setName("El Corral 2");
        request.setNit("900123456");
        request.setAddress("Local 15");
        request.setPhone("+573005698325");
        request.setUrlLogo("https://cdn.example.com/logo.png");
        request.setOwnerId(7L);
        return request;
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
        return jwt().jwt(token -> token.subject("1"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
