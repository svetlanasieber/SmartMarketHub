package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Category;
import com.smartmarkethub.smart.repository.CategoryRepository;
import com.smartmarkethub.smart.web.exception.DuplicateResourceException;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findNonEmptyCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> !category.getProducts().isEmpty())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Category> findAllWithProducts() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        validateNewCategory(category);
        
        Category savedCategory = categoryRepository.save(category);
        logger.info("Created new category: {}", savedCategory.getName());
        
        return savedCategory;
    }

    public Category updateCategory(Category category) {
        validateExistingCategory(category);
        
        Category updatedCategory = categoryRepository.save(category);
        logger.info("Updated category: {}", updatedCategory.getName());
        
        return updatedCategory;
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getProducts().isEmpty()) {
            throw new InvalidOperationException("Cannot delete category with existing products");
        }

        categoryRepository.delete(category);
        logger.info("Deleted category: {}", category.getName());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryStatistics() {
        List<Category> categories = categoryRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
       
        stats.put("totalCategories", categories.size());
        
      
        long categoriesWithProducts = categories.stream()
                .filter(c -> !c.getProducts().isEmpty())
                .count();
        stats.put("categoriesWithProducts", categoriesWithProducts);
        
      
        Map<String, Long> productsPerCategory = categories.stream()
                .collect(Collectors.toMap(
                    Category::getName,
                    category -> (long) category.getProducts().size()
                ));
        stats.put("productsPerCategory", productsPerCategory);
        
     
        Map<String, Long> activeProductsPerCategory = categories.stream()
                .collect(Collectors.toMap(
                    Category::getName,
                    category -> category.getProducts().stream()
                            .filter(p -> p.getQuantity() > 0)
                            .count()
                ));
        stats.put("activeProductsPerCategory", activeProductsPerCategory);
        
        return stats;
    }

    private void validateNewCategory(Category category) {
        categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(category.getName()))
                .findFirst()
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Category", "name", category.getName());
                });
    }

    private void validateExistingCategory(Category category) {
        categoryRepository.findAll().stream()
                .filter(c -> !c.getId().equals(category.getId()))
                .filter(c -> c.getName().equalsIgnoreCase(category.getName()))
                .findFirst()
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Category", "name", category.getName());
                });
    }
}
