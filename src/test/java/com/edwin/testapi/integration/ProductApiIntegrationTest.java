package com.edwin.testapi.integration;

import com.edwin.testapi.AbstractIntegrationTest;
import com.edwin.testapi.dto.request.CreateProductRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.dto.request.UpdateStockRequest;
import com.edwin.testapi.entity.Branch;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.entity.Product;
import com.edwin.testapi.repository.BranchRepository;
import com.edwin.testapi.repository.FranchiseRepository;
import com.edwin.testapi.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private FranchiseRepository franchiseRepository;

    private Franchise franchise;
    private Branch branchA;
    private Branch branchB;
    private Branch branchCWithoutProducts;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        branchRepository.deleteAll();
        franchiseRepository.deleteAll();

        franchise = new Franchise();
        franchise.setName("Franquicia Principal");
        franchise = franchiseRepository.save(franchise);

        branchA = new Branch();
        branchA.setName("Sucursal Norte");
        branchA.setFranchise(franchise);
        branchA = branchRepository.save(branchA);

        branchB = new Branch();
        branchB.setName("Sucursal Sur");
        branchB.setFranchise(franchise);
        branchB = branchRepository.save(branchB);

        branchCWithoutProducts = new Branch();
        branchCWithoutProducts.setName("Sucursal Vacia");
        branchCWithoutProducts.setFranchise(franchise);
        branchCWithoutProducts = branchRepository.save(branchCWithoutProducts);
    }

    @Test
    @DisplayName("Product Lifecycle: Create, Rename, Update Stock, and Delete belonging product")
    void productLifecycle_ShouldSucceedEndToEnd() throws Exception {
        // 1. Create product in branchA
        CreateProductRequest createRequest = new CreateProductRequest("Laptop Pro", 25);
        String createResponseJson = mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.branchId").value(branchA.getId()))
                .andReturn().getResponse().getContentAsString();

        Long createdProductId = objectMapper.readTree(createResponseJson).get("id").asLong();
        assertThat(productRepository.existsById(createdProductId)).isTrue();

        // 2. Rename product
        RenameRequest renameRequest = new RenameRequest("Laptop Pro M3");
        mockMvc.perform(patch("/api/v1/products/{id}", createdProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProductId))
                .andExpect(jsonPath("$.name").value("Laptop Pro M3"));

        assertThat(productRepository.findById(createdProductId).orElseThrow().getName())
                .isEqualTo("Laptop Pro M3");

        // 3. Update stock
        UpdateStockRequest stockRequest = new UpdateStockRequest(40);
        mockMvc.perform(patch("/api/v1/products/{id}/stock", createdProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProductId))
                .andExpect(jsonPath("$.stock").value(40));

        assertThat(productRepository.findById(createdProductId).orElseThrow().getStock())
                .isEqualTo(40);

        // 4. Update stock with invalid negative value -> should fail with 400
        UpdateStockRequest invalidStockRequest = new UpdateStockRequest(-5);
        mockMvc.perform(patch("/api/v1/products/{id}/stock", createdProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStockRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertThat(productRepository.findById(createdProductId).orElseThrow().getStock())
                .isEqualTo(40); // unchanged

        // 5. Delete product from its branch
        mockMvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchA.getId(), createdProductId))
                .andExpect(status().isNoContent());

        assertThat(productRepository.existsById(createdProductId)).isFalse();
    }

    @Test
    @DisplayName("Delete product that belongs to another branch should return 404 and NOT delete product")
    void delete_WhenProductBelongsToAnotherBranch_ShouldReturn404AndNotDelete() throws Exception {
        // Product belongs to branchB
        Product productInBranchB = new Product();
        productInBranchB.setName("Servidor");
        productInBranchB.setStock(5);
        productInBranchB.setBranch(branchB);
        productInBranchB = productRepository.save(productInBranchB);

        Long productId = productInBranchB.getId();

        // Attempt to delete passing branchA ID
        mockMvc.perform(delete("/api/v1/branches/{branchId}/products/{productId}", branchA.getId(), productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Producto no encontrado en la sucursal indicada"));

        // Verify product still exists in DB and still belongs to branchB
        Product untouched = productRepository.findById(productId).orElseThrow();
        assertThat(untouched.getBranch().getId()).isEqualTo(branchB.getId());
    }

    @Test
    @DisplayName("Highest Stock Query: includes ties, ignores empty branches, ignores non-existent franchise")
    void highestStock_ComprehensiveCoverage() throws Exception {
        // Branch A: Products with a TIE for highest stock
        Product pA1 = new Product();
        pA1.setName("Mouse Gamer");
        pA1.setStock(10);
        pA1.setBranch(branchA);
        productRepository.save(pA1);

        Product pA2 = new Product();
        pA2.setName("Teclado Mecanico");
        pA2.setStock(50); // Tie max
        pA2.setBranch(branchA);
        productRepository.save(pA2);

        Product pA3 = new Product();
        pA3.setName("Monitor 4K");
        pA3.setStock(50); // Tie max
        pA3.setBranch(branchA);
        productRepository.save(pA3);

        // Branch B: Single highest stock
        Product pB1 = new Product();
        pB1.setName("Cable HDMI");
        pB1.setStock(30); // Max in branch B
        pB1.setBranch(branchB);
        productRepository.save(pB1);

        Product pB2 = new Product();
        pB2.setName("Adaptador USB-C");
        pB2.setStock(5);
        pB2.setBranch(branchB);
        productRepository.save(pB2);

        // Branch C: Has NO products

        // Other franchise: Has higher stock (100), must not be returned
        Franchise otherFranchise = new Franchise();
        otherFranchise.setName("Otra Franquicia");
        otherFranchise = franchiseRepository.save(otherFranchise);

        Branch otherBranch = new Branch();
        otherBranch.setName("Otra Sucursal");
        otherBranch.setFranchise(otherFranchise);
        otherBranch = branchRepository.save(otherBranch);

        Product otherProduct = new Product();
        otherProduct.setName("Producto Aislado");
        otherProduct.setStock(100);
        otherProduct.setBranch(otherBranch);
        productRepository.save(otherProduct);

        // Query highest stock for primary franchise
        mockMvc.perform(get("/api/v1/franchises/{franchiseId}/products/highest-stock", franchise.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // 2 tied from branchA + 1 from branchB
                .andExpect(jsonPath("$[0].branchId").value(branchA.getId()))
                .andExpect(jsonPath("$[0].stock").value(50))
                .andExpect(jsonPath("$[1].branchId").value(branchA.getId()))
                .andExpect(jsonPath("$[1].stock").value(50))
                .andExpect(jsonPath("$[2].branchId").value(branchB.getId()))
                .andExpect(jsonPath("$[2].productName").value("Cable HDMI"))
                .andExpect(jsonPath("$[2].stock").value(30));

        // Query for other franchise with no products
        Franchise emptyFranchise = new Franchise();
        emptyFranchise.setName("Franquicia Vacia");
        emptyFranchise = franchiseRepository.save(emptyFranchise);

        mockMvc.perform(get("/api/v1/franchises/{franchiseId}/products/highest-stock", emptyFranchise.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Query for non-existent franchise -> 404
        mockMvc.perform(get("/api/v1/franchises/{franchiseId}/products/highest-stock", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Franquicia no encontrada con id: 999999"));
    }

    @Test
    @DisplayName("Integration: Malformed JSON request should return 400 with uniform error body")
    void malformedJsonRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/branches/{branchId}/products", branchA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Teclado\", \"stock\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la petición no es válido o está malformado"));
    }

    @Test
    @DisplayName("Integration: Invalid path parameter type should return 400 with uniform error body")
    void invalidPathVariableType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un formato o tipo inválido"));
    }
}
