package com.edwin.testapi.integration;

import com.edwin.testapi.AbstractIntegrationTest;
import com.edwin.testapi.dto.request.CreateFranchiseRequest;
import com.edwin.testapi.dto.request.RenameRequest;
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
class FranchiseApiIntegrationTest extends AbstractIntegrationTest {

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

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        branchRepository.deleteAll();
        franchiseRepository.deleteAll();
    }

    @Test
    @DisplayName("Franchise Integration: Create and rename franchise in database")
    void createAndRenameFranchise_ShouldPersistInDatabase() throws Exception {
        // 1. Create franchise
        CreateFranchiseRequest createRequest = new CreateFranchiseRequest("Franquicia Colombia");
        String createResponseJson = mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Franquicia Colombia"))
                .andReturn().getResponse().getContentAsString();

        Long franchiseId = objectMapper.readTree(createResponseJson).get("id").asLong();
        assertThat(franchiseRepository.findById(franchiseId)).isPresent();

        // 2. Rename franchise
        RenameRequest renameRequest = new RenameRequest("Franquicia Global Colombia");
        mockMvc.perform(patch("/api/v1/franchises/{id}", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(franchiseId))
                .andExpect(jsonPath("$.name").value("Franquicia Global Colombia"));

        assertThat(franchiseRepository.findById(franchiseId).orElseThrow().getName())
                .isEqualTo("Franquicia Global Colombia");

        // 3. Rename non-existent franchise -> 404
        mockMvc.perform(patch("/api/v1/franchises/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
