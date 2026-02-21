package com.bookshop.controller;

import com.bookshop.dto.BookResponse;
import com.bookshop.service.BookService;
import com.bookshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicController {

    private final CategoryService categoryService;
    private final BookService bookService;

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            return ResponseEntity.ok(categoryService.getAllCategories());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des catégories: " + e.getMessage());
        }
    }

    @GetMapping("/books")
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookResponse> books = bookService.getAllBooks(page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des livres: " + e.getMessage());
        }
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        try {
            BookResponse book = bookService.getBookById(id);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération du livre: " + e.getMessage());
        }
    }
}
