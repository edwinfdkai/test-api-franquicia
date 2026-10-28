package com.edwin.testapi.controller;

import com.edwin.testapi.dto.request.CreateProductRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.request.UpdateStockRequest;
import com.edwin.testapi.dto.response.HighestStockProductResponse;
import com.edwin.testapi.dto.response.ProductResponse;
import com.edwin.testapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    // Inyecta el servicio encargado de los productos.
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Añade un producto a una sucursal.
    @PostMapping("/branches/{branchId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @PathVariable Long branchId,
            @Valid @RequestBody CreateProductRequest request) {
        return productService.create(branchId, request);
    }

    // Elimina un producto de la sucursal indicada.
    @DeleteMapping("/branches/{branchId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long branchId,
            @PathVariable Long productId) {
        productService.delete(branchId, productId);
    }

    // Actualiza las unidades disponibles de un producto.
    @PatchMapping("/products/{id}/stock")
    public ProductResponse updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        return productService.updateStock(id, request.stock());
    }

    // Cambia el nombre de un producto existente.
    @PatchMapping("/products/{id}")
    public ProductResponse rename(
            @PathVariable Long id,
            @Valid @RequestBody RenameRequest request) {
        return productService.rename(id, request.name());
    }

    // Consulta el producto con mayor stock de cada sucursal.
    @GetMapping("/franchises/{franchiseId}/products/highest-stock")
    public List<HighestStockProductResponse> getHighestStock(
            @PathVariable Long franchiseId) {
        return productService.getHighestStockByBranch(franchiseId);
    }

    // Consulta un producto por su identificador.
    @GetMapping("/products/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }
}
