package com.edwin.testapi.service;

import com.edwin.testapi.dto.request.CreateBranchRequest;
import com.edwin.testapi.dto.response.BranchResponse;
import com.edwin.testapi.entity.Branch;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.repository.BranchRepository;
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
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    @DisplayName("create - should save branch with franchise relation when franchise exists")
    void create_WhenFranchiseExists_ShouldSaveAndReturnBranchResponse() {
        // Arrange
        Long franchiseId = 1L;
        CreateBranchRequest request = new CreateBranchRequest("Sucursal Centro");

        Franchise franchise = new Franchise();
        franchise.setId(franchiseId);
        franchise.setName("Franquicia Principal");

        Branch savedBranch = new Branch();
        savedBranch.setId(10L);
        savedBranch.setName("Sucursal Centro");
        savedBranch.setFranchise(franchise);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.of(franchise));
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        // Act
        BranchResponse response = branchService.create(franchiseId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Sucursal Centro");
        assertThat(response.franchiseId()).isEqualTo(franchiseId);

        verify(franchiseRepository).findById(franchiseId);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when franchise does not exist")
    void create_WhenFranchiseDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long franchiseId = 999L;
        CreateBranchRequest request = new CreateBranchRequest("Sucursal Inexistente");

        when(franchiseRepository.findById(franchiseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.create(franchiseId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Franquicia no encontrada con id: " + franchiseId);

        verify(franchiseRepository).findById(franchiseId);
        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("rename - should update name when branch exists")
    void rename_WhenBranchExists_ShouldUpdateAndReturnBranchResponse() {
        // Arrange
        Long branchId = 10L;
        String newName = "Sucursal Nueva";

        Franchise franchise = new Franchise();
        franchise.setId(1L);

        Branch existingBranch = new Branch();
        existingBranch.setId(branchId);
        existingBranch.setName("Sucursal Antigua");
        existingBranch.setFranchise(franchise);

        Branch updatedBranch = new Branch();
        updatedBranch.setId(branchId);
        updatedBranch.setName(newName);
        updatedBranch.setFranchise(franchise);

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(existingBranch)).thenReturn(updatedBranch);

        // Act
        BranchResponse response = branchService.rename(branchId, newName);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(branchId);
        assertThat(response.name()).isEqualTo(newName);
        assertThat(response.franchiseId()).isEqualTo(1L);

        verify(branchRepository).findById(branchId);
        verify(branchRepository).save(existingBranch);
    }

    @Test
    @DisplayName("rename - should throw ResourceNotFoundException when branch does not exist")
    void rename_WhenBranchDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long branchId = 999L;
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.rename(branchId, "Nuevo Nombre"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sucursal no encontrada con id: " + branchId);

        verify(branchRepository).findById(branchId);
        verify(branchRepository, never()).save(any());
    }
}
