package com.bookshop.controller;

import com.bookshop.dto.CartItemRequest;
import com.bookshop.dto.CartResponse;
import com.bookshop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management — requires authentication (USER or ADMIN)")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "View cart", description = "Returns the authenticated user's shopping cart with all items and totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @Operation(summary = "Add item to cart", description = "Adds a book to the cart. If the book already exists, the quantity is incremented.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added to cart"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock", content = @Content),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(userDetails.getUsername(), request));
    }

    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of a specific item in the cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item updated"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock", content = @Content),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Item does not belong to you", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Cart item ID", example = "1") @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(
                cartService.updateItem(userDetails.getUsername(), itemId, request));
    }

    @Operation(summary = "Remove item from cart", description = "Deletes a specific item from the cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Item does not belong to you", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Cart item ID", example = "1") @PathVariable Long itemId) {
        cartService.removeItem(userDetails.getUsername(), itemId);
        return ResponseEntity.noContent().build();
    }
}
