package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
    
    @Query("SELECT c FROM Category c WHERE SIZE(c.products) > 0")
    List<Category> findNonEmptyCategories();
}