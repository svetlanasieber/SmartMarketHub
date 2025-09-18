package com.smartmarkethub.smart.web.controller;

import com.smartmarkethub.smart.model.Product;
import com.smartmarkethub.smart.service.CategoryService;
import com.smartmarkethub.smart.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        
        List<Product> featuredProducts = productService.findProducts(null, null, null, "name")
                .stream()
                .limit(4)
                .toList();
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categoryService.findAll());
        
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }
}


