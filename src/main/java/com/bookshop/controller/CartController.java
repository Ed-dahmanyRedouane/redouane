package com.bookshop.controller;

import com.bookshop.dto.CartItemRequest;
import com.bookshop.dto.CartItemResponse;
import com.bookshop.dto.CartItemUpdateRequest;
import com.bookshop.dto.CartResponse;
import com.bookshop.entity.UserAccount;
import com.bookshop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getUserCart(@AuthenticationPrincipal UserAccount user) {
        CartResponse cart = cartService.getUserCart(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserAccount user) {
        CartItemResponse cartItem = cartService.addToCart(user, request);
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable Long id,
            @Valid @RequestBody CartItemUpdateRequest request,
            @AuthenticationPrincipal UserAccount user) {
        CartItemResponse updatedItem = cartService.updateCartItem(user, id, request);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserAccount user) {
        cartService.deleteCartItem(user, id);
        return ResponseEntity.noContent().build();
    }
}
