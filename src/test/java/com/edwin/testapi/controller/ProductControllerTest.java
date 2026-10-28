package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateProductRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.request.UpdateStockRequest;
import com.edwin.testapi.dto.response.HighestStockProductResponse;
import com.edwin.testapi.dto.response.ProductResponse;
import com.edwin.testapi.exception.GlobalExceptionHandler;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 201 Created on valid request")
    void create_WhenValidRequest_ShouldReturn201() throws Exception {
        Long branchId = 1L;
        CreateProductRequest request = new CreateProductRequest("Monitor 27", 15);
        ProductResponse response = new ProductResponse(100L, "Monitor 27", 15, branchId);

        when(productService.create(eq(branchId), any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Monitor 27"))
                .andExpect(jsonPath("$.stock").value(15))
                .andExpect(jsonPath("$.branchId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 400 Bad Request when name is blank")
    void create_WhenNameIsBlank_ShouldReturn400() throws Exception {
        Long branchId = 1L;
        CreateProductRequest request = new CreateProductRequest("", 10);

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").value("El nombre del producto es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 400 Bad Request when stock is negative")
    void create_WhenStockIsNegative_ShouldReturn400() throws Exception {
        Long branchId = 1L;
        CreateProductRequest request = new CreateProductRequest("Monitor", -5);

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.stock").value("El stock no puede ser negativo"));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 400 Bad Request when stock is null")
    void create_WhenStockIsNull_ShouldReturn400() throws Exception {
        Long branchId = 1L;
        String json = "{\"name\": \"Monitor\", \"stock\": null}";

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.stock").value("El stock es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 404 Not Found when branch does not exist")
    void create_WhenBranchNotFound_ShouldReturn404() throws Exception {
        Long branchId = 999L;
        CreateProductRequest request = new CreateProductRequest("Monitor", 10);

        when(productService.create(eq(branchId), any(CreateProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Sucursal no encontrada con id: " + branchId));

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sucursal no encontrada con id: " + branchId));
    }

    @Test
    @DisplayName("DELETE /api/v1/branches/{branchId}/products/{productId} - returns 204 No Content on success")
    void delete_WhenProductBelongsToBranch_ShouldReturn204() throws Exception {
        Long branchId = 1L;
        Long productId = 10L;

        doNothing().when(productService).delete(branchId, productId);

        mockMvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchId, productId))
                .andExpect(status().isNoContent());

        verify(productService).delete(branchId, productId);
    }

    @Test
    @DisplayName("DELETE /api/v1/branches/{branchId}/products/{productId} - returns 404 Not Found when product does not belong to branch")
    void delete_WhenProductDoesNotBelongToBranch_ShouldReturn404() throws Exception {
        Long branchId = 1L;
        Long productId = 999L;

        doThrow(new ResourceNotFoundException("Producto no encontrado en la sucursal indicada"))
                .when(productService).delete(branchId, productId);

        mockMvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchId, productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Producto no encontrado en la sucursal indicada"));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - returns 200 OK on valid stock update")
    void updateStock_WhenValid_ShouldReturn200() throws Exception {
        Long productId = 10L;
        UpdateStockRequest request = new UpdateStockRequest(50);
        ProductResponse response = new ProductResponse(productId, "Teclado", 50, 1L);

        when(productService.updateStock(eq(productId), eq(50))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - returns 400 Bad Request when stock is negative")
    void updateStock_WhenNegativeStock_ShouldReturn400() throws Exception {
        Long productId = 10L;
        UpdateStockRequest request = new UpdateStockRequest(-1);

        mockMvc.perform(patch("/api/v1/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.stock").value("El stock no puede ser negativo"));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - returns 404 Not Found when product not found")
    void updateStock_WhenProductNotFound_ShouldReturn404() throws Exception {
        Long productId = 999L;
        UpdateStockRequest request = new UpdateStockRequest(10);

        when(productService.updateStock(eq(productId), eq(10)))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        mockMvc.perform(patch("/api/v1/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Producto no encontrado con id: " + productId));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id} - returns 200 OK on valid rename")
    void rename_WhenValid_ShouldReturn200() throws Exception {
        Long productId = 10L;
        RenameRequest request = new RenameRequest("Teclado Inalámbrico");
        ProductResponse response = new ProductResponse(productId, "Teclado Inalámbrico", 20, 1L);

        when(productService.rename(eq(productId), eq("Teclado Inalámbrico"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Teclado Inalámbrico"));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id} - returns 400 Bad Request when name is blank")
    void rename_WhenNameIsBlank_ShouldReturn400() throws Exception {
        Long productId = 10L;
        RenameRequest request = new RenameRequest("");

        mockMvc.perform(patch("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").value("El nombre es obligatorio"));
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId}/products/highest-stock - returns 200 OK with list")
    void getHighestStock_WhenFranchiseExists_ShouldReturn200() throws Exception {
        Long franchiseId = 1L;
        List<HighestStockProductResponse> responseList = List.of(
                new HighestStockProductResponse(10L, "Sucursal Norte", 101L, "Mouse", 80),
                new HighestStockProductResponse(20L, "Sucursal Sur", 201L, "Monitor", 60)
        );

        when(productService.getHighestStockByBranch(franchiseId)).thenReturn(responseList);

        mockMvc.perform(get("/api/v1/franchises/{franchiseId}/products/highest-stock", franchiseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].branchId").value(10))
                .andExpect(jsonPath("$[0].productName").value("Mouse"))
                .andExpect(jsonPath("$[0].stock").value(80))
                .andExpect(jsonPath("$[1].branchId").value(20))
                .andExpect(jsonPath("$[1].productName").value("Monitor"))
                .andExpect(jsonPath("$[1].stock").value(60));
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId}/products/highest-stock - returns 404 when franchise not found")
    void getHighestStock_WhenFranchiseNotFound_ShouldReturn404() throws Exception {
        Long franchiseId = 999L;

        when(productService.getHighestStockByBranch(franchiseId))
                .thenThrow(new ResourceNotFoundException("Franquicia no encontrada con id: " + franchiseId));

        mockMvc.perform(get("/api/v1/franchises/{franchiseId}/products/highest-stock", franchiseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Franquicia no encontrada con id: " + franchiseId));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - returns 200 OK when product exists")
    void getById_WhenProductExists_ShouldReturn200() throws Exception {
        Long productId = 10L;
        ProductResponse response = new ProductResponse(productId, "Laptop", 5, 1L);

        when(productService.getById(productId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - returns 404 Not Found when product does not exist")
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        Long productId = 999L;

        when(productService.getById(productId))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Producto no encontrado con id: " + productId));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/products - returns 400 Bad Request when JSON is malformed")
    void create_WhenJsonIsMalformed_ShouldReturn400() throws Exception {
        Long branchId = 1L;
        String malformedJson = "{ name: 'Laptop', stock: ";

        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es válido o está malformado"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - returns 400 Bad Request when path variable type is invalid")
    void getById_WhenIdTypeMismatch_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un formato o tipo inválido"));
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/stock - returns 400 Bad Request when service throws IllegalArgumentException")
    void updateStock_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        Long productId = 10L;
        UpdateStockRequest request = new UpdateStockRequest(5);

        when(productService.updateStock(eq(productId), any()))
                .thenThrow(new IllegalArgumentException("El stock debe ser mayor o igual a cero"));

        mockMvc.perform(patch("/api/v1/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El stock debe ser mayor o igual a cero"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - returns 500 Internal Server Error without leaking internal stack trace")
    void getById_WhenUnexpectedExceptionOccurs_ShouldReturn500WithoutLeakingDetails() throws Exception {
        Long productId = 10L;

        when(productService.getById(productId))
                .thenThrow(new RuntimeException("Database connection timeout or internal secret error"));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error inesperado en el servidor"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Database connection timeout"))));
    }
}
