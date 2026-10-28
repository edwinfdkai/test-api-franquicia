package com.edwin.testapi.integration;

import com.edwin.testapi.AbstractIntegrationTest;
import com.edwin.testapi.dto.request.CreateBranchRequest;
import com.edwin.testapi.dto.request.RenameRequest;
import com.edwin.testapi.entity.Franchise;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BranchApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FranchiseRepository franchiseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        branchRepository.deleteAll();
        franchiseRepository.deleteAll();

        franchise = new Franchise();
        franchise.setName("Franquicia Test");
        franchise = franchiseRepository.save(franchise);
    }

    @Test
    @DisplayName("Branch Integration: Create branch under franchise and rename branch in database")
    void createAndRenameBranch_ShouldPersistInDatabase() throws Exception {
        // 1. Create branch under franchise
        CreateBranchRequest createRequest = new CreateBranchRequest("Sucursal Bogota");
        String createResponseJson = mockMvc.perform(post("/api/v1/franchises/{franchiseId}/branches", franchise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Sucursal Bogota"))
                .andExpect(jsonPath("$.franchiseId").value(franchise.getId()))
                .andReturn().getResponse().getContentAsString();

        Long branchId = objectMapper.readTree(createResponseJson).get("id").asLong();
        assertThat(branchRepository.findById(branchId)).isPresent();
        assertThat(branchRepository.findById(branchId).orElseThrow().getFranchise().getId())
                .isEqualTo(franchise.getId());

        // 2. Rename branch
        RenameRequest renameRequest = new RenameRequest("Sucursal Bogota Norte");
        mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId))
                .andExpect(jsonPath("$.name").value("Sucursal Bogota Norte"))
                .andExpect(jsonPath("$.franchiseId").value(franchise.getId()));

        assertThat(branchRepository.findById(branchId).orElseThrow().getName())
                .isEqualTo("Sucursal Bogota Norte");

        // 3. Create branch for non-existent franchise -> 404
        mockMvc.perform(post("/api/v1/franchises/{franchiseId}/branches", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // 4. Rename non-existent branch -> 404
        mockMvc.perform(patch("/api/v1/branches/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
