package com.bookshop.controller;

import com.bookshop.dto.BookResponse;
import com.bookshop.dto.CategoryResponse;
import com.bookshop.service.BookService;
import com.bookshop.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Public endpoints — no authentication required")
@SecurityRequirement(name = "")
public class PublicController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;

    @Operation(summary = "List all categories", description = "Returns all available book categories.")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "List books (paginated)", description = "Returns a paginated list of books sorted by title.")
    @ApiResponse(responseCode = "200", description = "Books retrieved successfully")
    @GetMapping("/books")
    public ResponseEntity<Page<BookResponse>> getBooks(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size));
    }

    @Operation(summary = "Get book by ID", description = "Returns a single book with its category details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @GetMapping("/books/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "Book ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
