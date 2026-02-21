package com.bookshop.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Public API Tests")
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/public/categories — should return seeded categories")
    void getCategories_Success() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'Fiction')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Technology')]").exists());
    }

    @Test
    @DisplayName("GET /api/public/books — should return paginated books")
    void getBooks_Paginated() throws Exception {
        mockMvc.perform(get("/api/public/books")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()", Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalElements", Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].title").isNotEmpty())
                .andExpect(jsonPath("$.content[0].category.name").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/public/books — should respect page size")
    void getBooks_PageSize() throws Exception {
        mockMvc.perform(get("/api/public/books")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalPages", Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/public/books/{id} — should return a single book")
    void getBookById_Success() throws Exception {
        mockMvc.perform(get("/api/public/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.author").isNotEmpty())
                .andExpect(jsonPath("$.price").isNumber())
                .andExpect(jsonPath("$.category.id").isNumber())
                .andExpect(jsonPath("$.category.name").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/public/books/999 — should return 404 for non-existent book")
    void getBookById_NotFound() throws Exception {
        mockMvc.perform(get("/api/public/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/public/books — no auth required")
    void getBooks_NoAuthRequired() throws Exception {
        // Public endpoints should work without any JWT token
        mockMvc.perform(get("/api/public/books"))
                .andExpect(status().isOk());
    }
}
