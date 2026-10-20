package com.lokendra.springecom.repo;

import com.lokendra.springecom.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    String SEARCH_JPQL = """
            SELECT p FROM Product p
            WHERE LOWER(p.name)     LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR p.description     LIKE        CONCAT('%', :keyword, '%')
               OR LOWER(p.brand)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """;

    
    @Query(SEARCH_JPQL)
    List<Product> searchProducts(String keyword);

    
    @Query(SEARCH_JPQL)
    Page<Product> searchProducts(String keyword, Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("""
            SELECT p FROM Product p
            WHERE (:category IS NULL OR LOWER(p.category) = LOWER(:category))
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:inStock IS NULL OR :inStock = false OR p.stockQuantity > 0)
            """)
    List<Product> filterProducts(String category, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Boolean inStock);
}