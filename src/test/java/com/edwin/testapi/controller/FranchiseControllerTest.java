package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateFranchiseRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.response.FranchiseResponse;
import com.edwin.testapi.exception.GlobalExceptionHandler;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.service.FranchiseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FranchiseController.class)
@Import(GlobalExceptionHandler.class)
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FranchiseService franchiseService;

    @Test
    @DisplayName("POST /api/v1/franchises - returns 201 Created on valid request")
    void create_WhenValidRequest_ShouldReturn201() throws Exception {
        CreateFranchiseRequest request = new CreateFranchiseRequest("Franquicia Expreso");
        FranchiseResponse response = new FranchiseResponse(1L, "Franquicia Expreso");

        when(franchiseService.create(any(CreateFranchiseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Franquicia Expreso"));
    }

    @Test
    @DisplayName("POST /api/v1/franchises - returns 400 Bad Request when name is blank")
    void create_WhenNameIsBlank_ShouldReturn400() throws Exception {
        CreateFranchiseRequest request = new CreateFranchiseRequest("   ");

        mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Hay campos inválidos"))
                .andExpect(jsonPath("$.details.name").value("El nombre de la franquicia es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/v1/franchises - returns 400 Bad Request when name exceeds 100 characters")
    void create_WhenNameExceedsLimit_ShouldReturn400() throws Exception {
        String longName = "a".repeat(101);
        CreateFranchiseRequest request = new CreateFranchiseRequest(longName);

        mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").value("El nombre no puede superar 100 caracteres"));
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{id} - returns 200 OK on valid request")
    void rename_WhenValidRequest_ShouldReturn200() throws Exception {
        Long franchiseId = 1L;
        RenameRequest request = new RenameRequest("Nuevo Nombre");
        FranchiseResponse response = new FranchiseResponse(franchiseId, "Nuevo Nombre");

        when(franchiseService.rename(eq(franchiseId), eq("Nuevo Nombre"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/franchises/{id}", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(franchiseId))
                .andExpect(jsonPath("$.name").value("Nuevo Nombre"));
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{id} - returns 400 Bad Request when name is blank")
    void rename_WhenNameIsBlank_ShouldReturn400() throws Exception {
        Long franchiseId = 1L;
        RenameRequest request = new RenameRequest("");

        mockMvc.perform(patch("/api/v1/franchises/{id}", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").value("El nombre es obligatorio"));
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{id} - returns 404 Not Found when franchise not found")
    void rename_WhenNotFound_ShouldReturn404() throws Exception {
        Long franchiseId = 999L;
        RenameRequest request = new RenameRequest("Nombre Valido");

        when(franchiseService.rename(eq(franchiseId), eq("Nombre Valido")))
                .thenThrow(new ResourceNotFoundException("Franquicia no encontrada con id: " + franchiseId));

        mockMvc.perform(patch("/api/v1/franchises/{id}", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Franquicia no encontrada con id: " + franchiseId));
    }
}
