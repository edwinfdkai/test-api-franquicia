package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateBranchRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.response.BranchResponse;
import com.edwin.testapi.exception.GlobalExceptionHandler;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.service.BranchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BranchController.class)
@Import(GlobalExceptionHandler.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BranchService branchService;

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches - returns 201 Created on valid request")
    void create_WhenValidRequest_ShouldReturn201() throws Exception {
        Long franchiseId = 1L;
        CreateBranchRequest request = new CreateBranchRequest("Sucursal Norte");
        BranchResponse response = new BranchResponse(10L, "Sucursal Norte", franchiseId);

        when(branchService.create(eq(franchiseId), any(CreateBranchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Sucursal Norte"))
                .andExpect(jsonPath("$.franchiseId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches - returns 400 Bad Request when name is blank")
    void create_WhenNameIsBlank_ShouldReturn400() throws Exception {
        Long franchiseId = 1L;
        CreateBranchRequest request = new CreateBranchRequest("  ");

        mockMvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details.name").value("El nombre de la sucursal es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{franchiseId}/branches - returns 404 Not Found when franchise not found")
    void create_WhenFranchiseNotFound_ShouldReturn404() throws Exception {
        Long franchiseId = 999L;
        CreateBranchRequest request = new CreateBranchRequest("Sucursal Norte");

        when(branchService.create(eq(franchiseId), any(CreateBranchRequest.class)))
                .thenThrow(new ResourceNotFoundException("Franquicia no encontrada con id: " + franchiseId));

        mockMvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Franquicia no encontrada con id: " + franchiseId));
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{id} - returns 200 OK on valid request")
    void rename_WhenValidRequest_ShouldReturn200() throws Exception {
        Long branchId = 10L;
        RenameRequest request = new RenameRequest("Sucursal Renombrada");
        BranchResponse response = new BranchResponse(branchId, "Sucursal Renombrada", 1L);

        when(branchService.rename(eq(branchId), eq("Sucursal Renombrada"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId))
                .andExpect(jsonPath("$.name").value("Sucursal Renombrada"))
                .andExpect(jsonPath("$.franchiseId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{id} - returns 400 Bad Request when name is blank")
    void rename_WhenNameIsBlank_ShouldReturn400() throws Exception {
        Long branchId = 10L;
        RenameRequest request = new RenameRequest("");

        mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").value("El nombre es obligatorio"));
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{id} - returns 404 Not Found when branch not found")
    void rename_WhenNotFound_ShouldReturn404() throws Exception {
        Long branchId = 999L;
        RenameRequest request = new RenameRequest("Nuevo Nombre");

        when(branchService.rename(eq(branchId), eq("Nuevo Nombre")))
                .thenThrow(new ResourceNotFoundException("Sucursal no encontrada con id: " + branchId));

        mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sucursal no encontrada con id: " + branchId));
    }
}
