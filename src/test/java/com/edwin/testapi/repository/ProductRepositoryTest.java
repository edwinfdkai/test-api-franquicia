package com.edwin.testapi.repository;

import com.edwin.testapi.AbstractIntegrationTest;
import com.edwin.testapi.dto.response.HighestStockProductResponse;
import com.edwin.testapi.entity.Branch;
import com.edwin.testapi.entity.Franchise;
import com.edwin.testapi.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Franchise franchiseA;
    private Franchise franchiseB;
    private Branch branchA1;
    private Branch branchA2;
    private Branch branchB1;

    @BeforeEach
    void setUp() {
        franchiseA = new Franchise();
        franchiseA.setName("Franquicia A");
        entityManager.persist(franchiseA);

        franchiseB = new Franchise();
        franchiseB.setName("Franquicia B");
        entityManager.persist(franchiseB);

        branchA1 = new Branch();
        branchA1.setName("Sucursal A1");
        branchA1.setFranchise(franchiseA);
        entityManager.persist(branchA1);

        branchA2 = new Branch();
        branchA2.setName("Sucursal A2");
        branchA2.setFranchise(franchiseA);
        entityManager.persist(branchA2);

        branchB1 = new Branch();
        branchB1.setName("Sucursal B1");
        branchB1.setFranchise(franchiseB);
        entityManager.persist(branchB1);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByIdAndBranch_Id - should return product when productId and branchId match")
    void findByIdAndBranch_Id_WhenMatch_ShouldReturnProduct() {
        Product product = new Product();
        product.setName("Teclado");
        product.setStock(20);
        product.setBranch(branchA1);
        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> found = productRepository.findByIdAndBranch_Id(product.getId(), branchA1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Teclado");
        assertThat(found.get().getBranch().getId()).isEqualTo(branchA1.getId());
    }

    @Test
    @DisplayName("findByIdAndBranch_Id - should return empty when product belongs to another branch")
    void findByIdAndBranch_Id_WhenBranchMismatch_ShouldReturnEmpty() {
        Product product = new Product();
        product.setName("Mouse");
        product.setStock(15);
        product.setBranch(branchA1);
        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> found = productRepository.findByIdAndBranch_Id(product.getId(), branchA2.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAllByBranch_Id - should return only products belonging to specific branch")
    void findAllByBranch_Id_ShouldReturnProductsForBranch() {
        Product p1 = new Product();
        p1.setName("P1");
        p1.setStock(10);
        p1.setBranch(branchA1);
        entityManager.persist(p1);

        Product p2 = new Product();
        p2.setName("P2");
        p2.setStock(30);
        p2.setBranch(branchA1);
        entityManager.persist(p2);

        Product p3 = new Product();
        p3.setName("P3");
        p3.setStock(40);
        p3.setBranch(branchA2);
        entityManager.persist(p3);

        entityManager.flush();

        List<Product> branchA1Products = productRepository.findAllByBranch_Id(branchA1.getId());

        assertThat(branchA1Products).hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    @DisplayName("findAllByBranch_Franchise_Id - should return all products belonging to branches of franchise")
    void findAllByBranch_Franchise_Id_ShouldReturnAllProductsOfFranchiseBranches() {
        Product p1 = new Product();
        p1.setName("P1");
        p1.setStock(10);
        p1.setBranch(branchA1);
        entityManager.persist(p1);

        Product p2 = new Product();
        p2.setName("P2");
        p2.setStock(20);
        p2.setBranch(branchA2);
        entityManager.persist(p2);

        Product pOther = new Product();
        pOther.setName("POther");
        pOther.setStock(50);
        pOther.setBranch(branchB1);
        entityManager.persist(pOther);

        entityManager.flush();

        List<Product> franchiseAProducts = productRepository.findAllByBranch_Franchise_Id(franchiseA.getId());

        assertThat(franchiseAProducts).hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    @DisplayName("findHighestStockProductsByFranchiseId - should return highest stock product per branch")
    void findHighestStock_ShouldReturnHighestStockPerBranch() {
        Product p1 = new Product();
        p1.setName("P1-Low");
        p1.setStock(10);
        p1.setBranch(branchA1);
        entityManager.persist(p1);

        Product p2 = new Product();
        p2.setName("P2-High");
        p2.setStock(45);
        p2.setBranch(branchA1);
        entityManager.persist(p2);

        Product p3 = new Product();
        p3.setName("P3-High");
        p3.setStock(30);
        p3.setBranch(branchA2);
        entityManager.persist(p3);

        entityManager.flush();

        List<HighestStockProductResponse> results =
                productRepository.findHighestStockProductsByFranchiseId(franchiseA.getId());

        assertThat(results).hasSize(2);
        assertThat(results.get(0).branchId()).isEqualTo(branchA1.getId());
        assertThat(results.get(0).productName()).isEqualTo("P2-High");
        assertThat(results.get(0).stock()).isEqualTo(45);

        assertThat(results.get(1).branchId()).isEqualTo(branchA2.getId());
        assertThat(results.get(1).productName()).isEqualTo("P3-High");
        assertThat(results.get(1).stock()).isEqualTo(30);
    }

    @Test
    @DisplayName("findHighestStockProductsByFranchiseId - should return all tied products when there is a tie")
    void findHighestStock_WhenTieInSameBranch_ShouldReturnAllTiedProductsOrdered() {
        Product p1 = new Product();
        p1.setName("Alpha");
        p1.setStock(50);
        p1.setBranch(branchA1);
        entityManager.persist(p1);

        Product p2 = new Product();
        p2.setName("Beta");
        p2.setStock(50); // Tie!
        p2.setBranch(branchA1);
        entityManager.persist(p2);

        Product p3 = new Product();
        p3.setName("Gamma");
        p3.setStock(10);
        p3.setBranch(branchA1);
        entityManager.persist(p3);

        entityManager.flush();

        List<HighestStockProductResponse> results =
                productRepository.findHighestStockProductsByFranchiseId(franchiseA.getId());

        assertThat(results).hasSize(2);
        assertThat(results.get(0).productId()).isEqualTo(p1.getId());
        assertThat(results.get(0).stock()).isEqualTo(50);
        assertThat(results.get(1).productId()).isEqualTo(p2.getId());
        assertThat(results.get(1).stock()).isEqualTo(50);
    }

    @Test
    @DisplayName("findHighestStockProductsByFranchiseId - should ignore branch without products")
    void findHighestStock_WhenBranchHasNoProducts_ShouldNotIncludeEmptyBranch() {
        // branchA1 has a product, branchA2 has NO products
        Product p1 = new Product();
        p1.setName("OnlyProduct");
        p1.setStock(25);
        p1.setBranch(branchA1);
        entityManager.persist(p1);

        entityManager.flush();

        List<HighestStockProductResponse> results =
                productRepository.findHighestStockProductsByFranchiseId(franchiseA.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).branchId()).isEqualTo(branchA1.getId());
    }

    @Test
    @DisplayName("findHighestStockProductsByFranchiseId - should return empty list when franchise has no products")
    void findHighestStock_WhenFranchiseHasNoProducts_ShouldReturnEmptyList() {
        List<HighestStockProductResponse> results =
                productRepository.findHighestStockProductsByFranchiseId(franchiseA.getId());

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("findHighestStockProductsByFranchiseId - should enforce isolation between franchises")
    void findHighestStock_IsolationBetweenFranchises() {
        Product pA = new Product();
        pA.setName("ProdA");
        pA.setStock(10);
        pA.setBranch(branchA1);
        entityManager.persist(pA);

        Product pB = new Product();
        pB.setName("ProdB-Massive");
        pB.setStock(1000);
        pB.setBranch(branchB1);
        entityManager.persist(pB);

        entityManager.flush();

        List<HighestStockProductResponse> resultsA =
                productRepository.findHighestStockProductsByFranchiseId(franchiseA.getId());

        assertThat(resultsA).hasSize(1);
        assertThat(resultsA.get(0).productName()).isEqualTo("ProdA");
        assertThat(resultsA.get(0).stock()).isEqualTo(10);
    }
}
