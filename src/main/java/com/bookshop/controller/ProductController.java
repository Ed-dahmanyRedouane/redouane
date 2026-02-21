package com.bookshop.controller;

import com.bookshop.entity.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Product> getAllProducts() {
        return List.of(
                new Product(1, "Product 1", 10.0, "Description 1", "Category 1", "Image 1"),
                new Product(2, "Product 2", 20.0, "Description 2", "Category 2", "Image 2"),
                new Product(3, "Product 3", 30.0, "Description 3", "Category 3", "Image 3")
        );
    }
}
