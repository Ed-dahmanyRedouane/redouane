package com.bookshop.controller;

import com.bookshop.dto.BookRequest;
import com.bookshop.dto.LoginRequest;
import com.bookshop.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin API Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken("admin@bookshop.com", "admin123");
        userToken = loginAndGetToken("user@bookshop.com", "user123");
    }

    @Test
    @DisplayName("POST /api/admin/books — admin can create a book")
    void createBook_AsAdmin_Success() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setAuthor("Test Author");
        request.setPrice(new BigDecimal("29.99"));
        request.setStock(10);
        request.setDescription("A test book");
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    @DisplayName("POST /api/admin/books — regular user gets 403 Forbidden")
    void createBook_AsUser_Forbidden() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Forbidden Book");
        request.setAuthor("Author");
        request.setPrice(new BigDecimal("10.00"));
        request.setStock(5);
        request.setDescription("Should be rejected");
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/books — unauthenticated gets 401")
    void createBook_NoAuth_Unauthorized() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("No Auth Book");
        request.setAuthor("Author");
        request.setPrice(new BigDecimal("10.00"));
        request.setStock(5);
        request.setDescription("No auth");
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/admin/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/admin/books/{id} — admin can delete a book")
    void deleteBook_AsAdmin_Success() throws Exception {
        // First create a book to delete
        BookRequest request = new BookRequest();
        request.setTitle("Book To Delete");
        request.setAuthor("Author");
        request.setPrice(new BigDecimal("15.00"));
        request.setStock(1);
        request.setDescription("Will be deleted");
        request.setCategoryId(1L);

        MvcResult result = mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long bookId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // Now delete it
        mockMvc.perform(delete("/api/admin/books/" + bookId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/public/books/" + bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/admin/books — validation error for missing title")
    void createBook_MissingTitle_BadRequest() throws Exception {
        String invalidJson = """
                {"author": "Author", "price": 10.00, "stock": 5, "categoryId": 1}
                """;

        mockMvc.perform(post("/api/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ── Helper ───────────────────────────────────────────────

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return response.getToken();
    }
}
