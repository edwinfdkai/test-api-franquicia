package com.edwin.testapi.service;

import com.edwin.testapi.dto.request.CreateFranchiseRequest;
import com.edwin.testapi.dto.response.FranchiseResponse;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.repository.FranchiseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseService franchiseService;

    @Test
    @DisplayName("create - should save franchise and return response")
    void create_ShouldSaveAndReturnFranchiseResponse() {
        // Arrange
        CreateFranchiseRequest request = new CreateFranchiseRequest("Franquicia Norte");

        Franchise savedFranchise = new Franchise();
        savedFranchise.setId(1L);
        savedFranchise.setName("Franquicia Norte");

        when(franchiseRepository.save(any(Franchise.class))).thenReturn(savedFranchise);

        // Act
        FranchiseResponse response = franchiseService.create(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Franquicia Norte");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }

    @Test
    @DisplayName("rename - should update name when franchise exists")
    void rename_WhenFranchiseExists_ShouldUpdateAndReturnFranchiseResponse() {
        // Arrange
        Long franchiseId = 1L;
        String newName = "Franquicia Actualizada";

        Franchise existingFranchise = new Franchise();
        existingFranchise.setId(franchiseId);
        existingFranchise.setName("Nombre Original");

        Franchise updatedFranchise = new Franchise();
        updatedFranchise.setId(franchiseId);
        updatedFranchise.setName(newName);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.of(existingFranchise));
        when(franchiseRepository.save(existingFranchise)).thenReturn(updatedFranchise);

        // Act
        FranchiseResponse response = franchiseService.rename(franchiseId, newName);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(franchiseId);
        assertThat(response.name()).isEqualTo(newName);
        verify(franchiseRepository).findById(franchiseId);
        verify(franchiseRepository).save(existingFranchise);
    }

    @Test
    @DisplayName("rename - should throw ResourceNotFoundException when franchise does not exist")
    void rename_WhenFranchiseDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long franchiseId = 999L;
        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> franchiseService.rename(franchiseId, "Nuevo Nombre"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Franquicia no encontrada con id: " + franchiseId);

        verify(franchiseRepository).findById(franchiseId);
        verify(franchiseRepository, never()).save(any());
    }
}
