package com.pragma.powerup.infrastructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.dto.response.DishSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.infrastructure.configuration.SecurityConfiguration;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

@WebMvcTest(DishRestController.class)
@Import({ControllerAdvisor.class, SecurityConfiguration.class})
class DishRestControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    IDishHandler handler;

    @Test
    void createsDishAsActive() throws Exception {
        when(handler.createDish(eq(5L), any())).thenReturn(new DishResponseDto(
                1L, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, true));

        mvc.perform(post("/restaurants/5/dishes")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void rejectsMissingFieldsAndInvalidPrice() throws Exception {
        mvc.perform(post("/restaurants/5/dishes")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());

        mvc.perform(post("/restaurants/5/dishes")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().replace("25000", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void updatesPriceAndDescription() throws Exception {
        when(handler.updateDish(eq(5L), eq(10L), any())).thenReturn(new DishResponseDto(
                10L, "Hamburguesa", 30000L, "Nueva descripción",
                "https://cdn.example.com/dish.png", 2L, 5L, true));

        mvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":30000,\"description\":\"Nueva descripción\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(30000))
                .andExpect(jsonPath("$.data.description").value("Nueva descripción"));
    }

    @Test
    void rejectsInvalidUpdateOrFieldsOutsideContract() throws Exception {
        mvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":0,\"description\":\"\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":30000,\"description\":\"Nueva\",\"name\":\"Otro\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/restaurants/0/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":30000,\"description\":\"Nueva\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAuthenticatedOwner() throws Exception {
        mvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/restaurants/5/dishes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatesDishStatusAsRestaurantOwner() throws Exception {
        when(handler.updateDishStatus(eq(5L), eq(10L), any())).thenReturn(new DishResponseDto(
                10L, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false));

        mvc.perform(patch("/restaurants/5/dishes/10/status")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void validatesDishStatusRequestAndIdentifiers() throws Exception {
        mvc.perform(patch("/restaurants/5/dishes/10/status")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.active").exists());

        mvc.perform(patch("/restaurants/0/dishes/10/status")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAuthenticatedOwnerToUpdateDishStatus() throws Exception {
        mvc.perform(patch("/restaurants/5/dishes/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(patch("/restaurants/5/dishes/10/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsPaginatedRestaurantMenuAsCustomer() throws Exception {
        DishSummaryResponseDto dish = new DishSummaryResponseDto(
                10L, "Hamburguesa", 25000L, "Carne y queso", "https://cdn.example.com/dish.png", 2L);
        when(handler.getDishes(5L, 2L, 1, 5)).thenReturn(
                new PageResponseDto<>(List.of(dish), new PageMetadataDto(1, 5, 6, 2)));

        mvc.perform(get("/restaurants/5/dishes")
                        .with(customerJwt())
                        .param("categoryId", "2")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Hamburguesa"))
                .andExpect(jsonPath("$.data[0].restaurantId").doesNotExist())
                .andExpect(jsonPath("$.data[0].active").doesNotExist())
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(5))
                .andExpect(jsonPath("$.meta.totalElements").value(6));
    }

    @Test
    void requiresAuthenticatedCustomerToListMenu() throws Exception {
        mvc.perform(get("/restaurants/5/dishes"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/restaurants/5/dishes").with(ownerJwt()))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor ownerJwt() {
        return jwt().jwt(token -> token.subject("7").claim("role", "OWNER"))
                .authorities(new SimpleGrantedAuthority("ROLE_OWNER"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt().jwt(token -> token.subject("20").claim("role", "CUSTOMER"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private String validBody() {
        return """
                {"name":"Hamburguesa","price":25000,"description":"Carne y queso",
                 "urlImage":"https://cdn.example.com/dish.png","categoryId":2}
                """;
    }
}
