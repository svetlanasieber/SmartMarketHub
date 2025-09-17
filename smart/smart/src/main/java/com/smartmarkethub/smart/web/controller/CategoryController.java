package com.smartmarkethub.smart.web.controller;

import com.smartmarkethub.smart.model.Category;
import com.smartmarkethub.smart.service.CategoryService;
import com.smartmarkethub.smart.web.dto.CategoryCreateRequest;
import com.smartmarkethub.smart.web.dto.CategoryResponse;
import com.smartmarkethub.smart.web.dto.CategoryUpdateRequest;
import com.smartmarkethub.smart.web.exception.InvalidOperationException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import com.smartmarkethub.smart.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) Boolean includeEmpty) {
        
        logger.debug("Fetching all categories. Include empty: {}", includeEmpty);
        
        List<Category> categories;
        if (Boolean.TRUE.equals(includeEmpty)) {
            categories = categoryService.findAll();
        } else {
            categories = categoryService.findNonEmptyCategories();
        }
        
        return ResponseEntity.ok(DtoMapper.toCategoryResponseList(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        logger.debug("Fetching category with id: {}", id);
        
        return categoryService.findById(id)
                .map(DtoMapper::toCategoryResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        logger.debug("Creating new category: {}", request.getName());
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        Category savedCategory = categoryService.createCategory(category);
        CategoryResponse response = DtoMapper.toCategoryResponse(savedCategory);
        
        return ResponseEntity.created(URI.create("/api/categories/" + savedCategory.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        
        if (!id.equals(request.getId())) {
            throw new InvalidOperationException("Path variable id does not match request body id");
        }
        
        logger.debug("Updating category with id: {}", id);
        
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
                
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        Category updatedCategory = categoryService.updateCategory(category);
        return ResponseEntity.ok(DtoMapper.toCategoryResponse(updatedCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.debug("Deleting category with id: {}", id);
        
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
                
        if (!category.getProducts().isEmpty()) {
            throw new InvalidOperationException("Cannot delete category with existing products");
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-products")
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithProducts() {
        logger.debug("Fetching categories with products");
        
        List<Category> categories = categoryService.findAllWithProducts();
        return ResponseEntity.ok(DtoMapper.toCategoryResponseList(categories));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        logger.debug("Fetching category statistics");
        
        Map<String, Object> stats = categoryService.getCategoryStatistics();
        return ResponseEntity.ok(stats);
    }
}
