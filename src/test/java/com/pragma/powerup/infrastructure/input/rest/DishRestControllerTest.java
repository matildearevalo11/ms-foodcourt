package com.pragma.powerup.infrastructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DishRestController.class)
@Import(ControllerAdvisor.class)
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void rejectsMissingFieldsAndInvalidPrice() throws Exception {
        mvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());

        mvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().replace("25000", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    private String validBody() {
        return """
                {"name":"Hamburguesa","price":25000,"description":"Carne y queso",
                 "urlImage":"https://cdn.example.com/dish.png","categoryId":2}
                """;
    }
}
