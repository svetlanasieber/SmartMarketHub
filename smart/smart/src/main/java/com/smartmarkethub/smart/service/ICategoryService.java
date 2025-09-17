package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing product categories.
 */
public interface ICategoryService {

    /**
     * Retrieves all categories.
     *
     * @return List of all categories
     */
    List<Category> findAll();

    /**
     * Finds a category by its ID.
     *
     * @param id The ID of the category to find
     * @return Optional containing the category if found, empty otherwise
     */
    Optional<Category> findById(Long id);

    /**
     * Retrieves all categories that have products.
     *
     * @return List of non-empty categories
     */
    List<Category> findNonEmptyCategories();

    /**
     * Retrieves all categories with their associated products.
     *
     * @return List of categories with products
     */
    List<Category> findAllWithProducts();

    /**
     * Creates a new category.
     *
     * @param category The category to create
     * @return The created category
     * @throws DuplicateResourceException if a category with the same name already exists
     */
    Category createCategory(Category category);

    /**
     * Updates an existing category.
     *
     * @param category The category to update
     * @return The updated category
     * @throws ResourceNotFoundException if the category is not found
     * @throws DuplicateResourceException if the new name conflicts with an existing category
     */
    Category updateCategory(Category category);

    /**
     * Deletes a category by its ID.
     *
     * @param id The ID of the category to delete
     * @throws ResourceNotFoundException if the category is not found
     * @throws InvalidOperationException if the category contains products
     */
    void deleteCategory(Long id);

    /**
     * Retrieves statistics about categories.
     *
     * @return Map containing various statistics about categories
     */
    Map<String, Object> getCategoryStatistics();
}
