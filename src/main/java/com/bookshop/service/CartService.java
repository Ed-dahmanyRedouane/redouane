package com.bookshop.service;

import com.bookshop.dto.*;
import com.bookshop.entity.Book;
import com.bookshop.entity.CartItem;
import com.bookshop.entity.UserAccount;
import com.bookshop.repository.BookRepository;
import com.bookshop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public CartResponse getUserCart(UserAccount user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    @Transactional
    public CartItemResponse addToCart(UserAccount user, CartItemRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'ID: " + request.getBookId()));

        if (book.getStock() < request.getQuantity()) {
            throw new RuntimeException("Stock insuffisant pour le livre: " + book.getTitle());
        }

        CartItem existingItem = cartItemRepository.findByUserAndBook_Id(user, request.getBookId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (book.getStock() < newQuantity) {
                throw new RuntimeException("Stock insuffisant pour le livre: " + book.getTitle());
            }
            existingItem.setQuantity(newQuantity);
            CartItem updatedItem = cartItemRepository.save(existingItem);
            return convertToResponse(updatedItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .book(book)
                    .quantity(request.getQuantity())
                    .unitPrice(book.getPrice())
                    .build();
            CartItem savedItem = cartItemRepository.save(newItem);
            return convertToResponse(savedItem);
        }
    }

    @Transactional
    public CartItemResponse updateCartItem(UserAccount user, Long itemId, CartItemUpdateRequest request) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article du panier non trouvé avec l'ID: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cet article n'appartient pas à votre panier");
        }

        if (cartItem.getBook().getStock() < request.getQuantity()) {
            throw new RuntimeException("Stock insuffisant pour le livre: " + cartItem.getBook().getTitle());
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem updatedItem = cartItemRepository.save(cartItem);
        return convertToResponse(updatedItem);
    }

    @Transactional
    public void deleteCartItem(UserAccount user, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article du panier non trouvé avec l'ID: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cet article n'appartient pas à votre panier");
        }

        cartItemRepository.delete(cartItem);
    }

    private CartItemResponse convertToResponse(CartItem cartItem) {
        BigDecimal totalPrice = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .book(convertBookToResponse(cartItem.getBook()))
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(totalPrice)
                .build();
    }

    private BookResponse convertBookToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .stock(book.getStock())
                .description(book.getDescription())
                .category(convertCategoryToResponse(book.getCategory()))
                .build();
    }

    private CategoryResponse convertCategoryToResponse(com.bookshop.entity.Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
