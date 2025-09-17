package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface ICategoryService {


    List<Category> findAll();


    Optional<Category> findById(Long id);


    List<Category> findNonEmptyCategories();


    List<Category> findAllWithProducts();


    Category createCategory(Category category);


    Category updateCategory(Category category);


    void deleteCategory(Long id);

 
    Map<String, Object> getCategoryStatistics();
}
