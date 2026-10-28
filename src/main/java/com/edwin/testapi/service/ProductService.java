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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public ProductService(
            ProductRepository productRepository,
            BranchRepository branchRepository,
            FranchiseRepository franchiseRepository) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    /** Gestiona productos, stock y consultas de inventario. */
    @Transactional
    public ProductResponse create(Long branchId, CreateProductRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada con id: " + branchId));

        Product product = new Product();
        product.setName(request.name());
        product.setStock(request.stock());
        product.setBranch(branch);

        Product saved = productRepository.save(product);

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long branchId, Long productId) {
        Product product = productRepository
                .findByIdAndBranch_Id(productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado en la sucursal indicada"));

        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateStock(Long productId, Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException(
                    "El stock debe ser mayor o igual a cero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + productId));

        product.setStock(stock);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse rename(Long productId, String name) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + productId));

        product.setName(name);

        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<HighestStockProductResponse> getHighestStockByBranch(
            Long franchiseId) {

        if (!franchiseRepository.existsById(franchiseId)) {
            throw new ResourceNotFoundException(
                    "Franquicia no encontrada con id: " + franchiseId);
        }

        return productRepository.findHighestStockProductsByFranchiseId(franchiseId);
    }

    // Busca un producto por su identificador.
    @Transactional(readOnly = true)
    public ProductResponse getById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado con id: " + productId
                        )
                );

        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getStock(),
                product.getBranch().getId()
        );
    }
}
