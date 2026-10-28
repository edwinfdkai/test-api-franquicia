package com.edwin.testapi.service;

import com.edwin.testapi.dto.request.CreateProductRequest;
import com.edwin.testapi.dto.response.HighestStockProductResponse;
import com.edwin.testapi.dto.response.ProductResponse;
import com.edwin.testapi.entity.Branch;
import com.edwin.testapi.entity.Product;
import com.edwin.testapi.exception.ResourceNotFoundException;
import com.edwin.testapi.repository.BranchRepository;
import com.edwin.testapi.repository.FranchiseRepository;
import com.edwin.testapi.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create and return product when branch exists")
        void create_WhenBranchExists_ShouldSaveAndReturnProductResponse() {
            Long branchId = 1L;
            CreateProductRequest request = new CreateProductRequest("Producto A", 50);

            Branch branch = new Branch();
            branch.setId(branchId);
            branch.setName("Sucursal Norte");

            Product savedProduct = new Product();
            savedProduct.setId(10L);
            savedProduct.setName("Producto A");
            savedProduct.setStock(50);
            savedProduct.setBranch(branch);

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            ProductResponse response = productService.create(branchId, request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.name()).isEqualTo("Producto A");
            assertThat(response.stock()).isEqualTo(50);
            assertThat(response.branchId()).isEqualTo(branchId);

            verify(branchRepository).findById(branchId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when branch does not exist")
        void create_WhenBranchDoesNotExist_ShouldThrowResourceNotFoundException() {
            Long branchId = 999L;
            CreateProductRequest request = new CreateProductRequest("Producto Inexistente", 10);

            when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(branchId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Sucursal no encontrada con id: " + branchId);

            verify(branchRepository).findById(branchId);
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("should delete product when it belongs to the branch")
        void delete_WhenProductBelongsToBranch_ShouldDeleteProduct() {
            Long branchId = 1L;
            Long productId = 10L;

            Branch branch = new Branch();
            branch.setId(branchId);

            Product product = new Product();
            product.setId(productId);
            product.setBranch(branch);

            when(productRepository.findByIdAndBranch_Id(productId, branchId))
                    .thenReturn(Optional.of(product));

            productService.delete(branchId, productId);

            verify(productRepository).findByIdAndBranch_Id(productId, branchId);
            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product does not belong to branch or does not exist")
        void delete_WhenProductDoesNotBelongToBranch_ShouldThrowResourceNotFoundException() {
            Long branchId = 1L;
            Long productId = 999L;

            when(productRepository.findByIdAndBranch_Id(productId, branchId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(branchId, productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Producto no encontrado en la sucursal indicada");

            verify(productRepository).findByIdAndBranch_Id(productId, branchId);
            verify(productRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateStock")
    class UpdateStockTests {

        @Test
        @DisplayName("should update stock when product exists and stock is valid")
        void updateStock_WhenProductExistsAndStockIsValid_ShouldUpdateAndReturnResponse() {
            Long productId = 10L;
            Integer newStock = 75;

            Branch branch = new Branch();
            branch.setId(1L);

            Product existing = new Product();
            existing.setId(productId);
            existing.setName("Producto A");
            existing.setStock(20);
            existing.setBranch(branch);

            Product saved = new Product();
            saved.setId(productId);
            saved.setName("Producto A");
            saved.setStock(newStock);
            saved.setBranch(branch);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
            when(productRepository.save(existing)).thenReturn(saved);

            ProductResponse response = productService.updateStock(productId, newStock);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(productId);
            assertThat(response.stock()).isEqualTo(newStock);
            verify(productRepository).findById(productId);
            verify(productRepository).save(existing);
        }

        @Test
        @DisplayName("should allow updating stock to zero")
        void updateStock_WhenStockIsZero_ShouldUpdateSuccessfully() {
            Long productId = 10L;
            Integer newStock = 0;

            Branch branch = new Branch();
            branch.setId(1L);

            Product existing = new Product();
            existing.setId(productId);
            existing.setStock(15);
            existing.setBranch(branch);

            Product saved = new Product();
            saved.setId(productId);
            saved.setStock(0);
            saved.setBranch(branch);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
            when(productRepository.save(existing)).thenReturn(saved);

            ProductResponse response = productService.updateStock(productId, newStock);

            assertThat(response.stock()).isEqualTo(0);
            verify(productRepository).save(existing);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when stock is negative")
        void updateStock_WhenStockIsNegative_ShouldThrowIllegalArgumentException() {
            Long productId = 10L;

            assertThatThrownBy(() -> productService.updateStock(productId, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El stock debe ser mayor o igual a cero");

            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when stock is null")
        void updateStock_WhenStockIsNull_ShouldThrowIllegalArgumentException() {
            Long productId = 10L;

            assertThatThrownBy(() -> productService.updateStock(productId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El stock debe ser mayor o igual a cero");

            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product does not exist")
        void updateStock_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateStock(productId, 10))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Producto no encontrado con id: " + productId);

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("rename")
    class RenameTests {

        @Test
        @DisplayName("should rename product when product exists")
        void rename_WhenProductExists_ShouldUpdateAndReturnResponse() {
            Long productId = 10L;
            String newName = "Nombre Actualizado";

            Branch branch = new Branch();
            branch.setId(1L);

            Product existing = new Product();
            existing.setId(productId);
            existing.setName("Nombre Viejo");
            existing.setStock(25);
            existing.setBranch(branch);

            Product saved = new Product();
            saved.setId(productId);
            saved.setName(newName);
            saved.setStock(25);
            saved.setBranch(branch);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
            when(productRepository.save(existing)).thenReturn(saved);

            ProductResponse response = productService.rename(productId, newName);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(productId);
            assertThat(response.name()).isEqualTo(newName);
            verify(productRepository).findById(productId);
            verify(productRepository).save(existing);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product does not exist")
        void rename_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.rename(productId, "Nuevo"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Producto no encontrado con id: " + productId);

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getHighestStockByBranch")
    class HighestStockTests {

        @Test
        @DisplayName("should throw ResourceNotFoundException when franchise does not exist")
        void getHighestStock_WhenFranchiseDoesNotExist_ShouldThrowResourceNotFoundException() {
            Long franchiseId = 999L;
            when(franchiseRepository.existsById(franchiseId)).thenReturn(false);

            assertThatThrownBy(() -> productService.getHighestStockByBranch(franchiseId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Franquicia no encontrada con id: " + franchiseId);

            verify(franchiseRepository).existsById(franchiseId);
            verify(productRepository, never()).findHighestStockProductsByFranchiseId(any());
        }

        @Test
        @DisplayName("should return empty list when franchise exists but has no products")
        void getHighestStock_WhenFranchiseHasNoProducts_ShouldReturnEmptyList() {
            Long franchiseId = 1L;
            when(franchiseRepository.existsById(franchiseId)).thenReturn(true);
            when(productRepository.findHighestStockProductsByFranchiseId(franchiseId))
                    .thenReturn(Collections.emptyList());

            List<HighestStockProductResponse> result = productService.getHighestStockByBranch(franchiseId);

            assertThat(result).isNotNull().isEmpty();
            verify(franchiseRepository).existsById(franchiseId);
            verify(productRepository).findHighestStockProductsByFranchiseId(franchiseId);
        }

        @Test
        @DisplayName("should delegate to repository and return highest stock product list")
        void getHighestStock_WhenProductsExist_ShouldReturnRepositoryResult() {
            Long franchiseId = 1L;
            List<HighestStockProductResponse> expected = List.of(
                    new HighestStockProductResponse(1L, "Sucursal A", 102L, "Mouse", 50),
                    new HighestStockProductResponse(2L, "Sucursal B", 103L, "Teclado", 30)
            );

            when(franchiseRepository.existsById(franchiseId)).thenReturn(true);
            when(productRepository.findHighestStockProductsByFranchiseId(franchiseId))
                    .thenReturn(expected);

            List<HighestStockProductResponse> result = productService.getHighestStockByBranch(franchiseId);

            assertThat(result).isNotNull().hasSize(2).isEqualTo(expected);
            verify(franchiseRepository).existsById(franchiseId);
            verify(productRepository).findHighestStockProductsByFranchiseId(franchiseId);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("should return product response when product exists")
        void getById_WhenProductExists_ShouldReturnProductResponse() {
            Long productId = 10L;
            Branch branch = new Branch();
            branch.setId(1L);

            Product product = new Product();
            product.setId(productId);
            product.setName("Tablet");
            product.setStock(15);
            product.setBranch(branch);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            ProductResponse response = productService.getById(productId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(productId);
            assertThat(response.name()).isEqualTo("Tablet");
            assertThat(response.stock()).isEqualTo(15);
            assertThat(response.branchId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product does not exist")
        void getById_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Producto no encontrado con id: " + productId);
        }
    }
}
