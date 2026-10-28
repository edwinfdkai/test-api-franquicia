package com.edwin.testapi.repository;

import com.edwin.testapi.dto.response.HighestStockProductResponse;
import com.edwin.testapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndBranch_Id(Long productId, Long branchId);

    List<Product> findAllByBranch_Id(Long branchId);

    List<Product> findAllByBranch_Franchise_Id(Long franchiseId);

    @Query("""
        SELECT new com.edwin.testapi.dto.response.HighestStockProductResponse(
            b.id,
            b.name,
            p.id,
            p.name,
            p.stock
        )
        FROM Product p
        JOIN p.branch b
        WHERE b.franchise.id = :franchiseId
          AND p.stock = (
              SELECT MAX(p2.stock)
              FROM Product p2
              WHERE p2.branch.id = b.id
          )
        ORDER BY b.id ASC, p.id ASC
    """)
    List<HighestStockProductResponse> findHighestStockProductsByFranchiseId(@Param("franchiseId") Long franchiseId);

}
