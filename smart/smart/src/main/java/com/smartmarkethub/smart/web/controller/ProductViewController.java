package com.smartmarkethub.smart.web.controller;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.service.CategoryService;
import com.smartmarkethub.smart.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductViewController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductViewController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            Model model) {

        // Get products
        List<Product> products = productService.findProducts(categoryId, minPrice, maxPrice, sortBy);
        
        // Add to model
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());

        return "products/list";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);
        return "products/details";
    }
}


