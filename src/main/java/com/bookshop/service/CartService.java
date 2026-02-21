package com.bookshop.service;

import com.bookshop.dto.*;
import com.bookshop.entity.Book;
import com.bookshop.entity.CartItem;
import com.bookshop.entity.User;
import com.bookshop.repository.BookRepository;
import com.bookshop.repository.CartItemRepository;
import com.bookshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Returns the full cart for the authenticated user.
     */
    public CartResponse getCart(String email) {
        User user = findUserByEmail(email);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    /**
     * Adds a book to the cart. If the book already exists, increments quantity.
     */
    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        User user = findUserByEmail(email);
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book not found with id: " + request.getBookId()));

        if (book.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Insufficient stock for book: " + book.getTitle());
        }

        // Upsert: if item already exists for this user+book, add quantity
        CartItem cartItem = cartItemRepository.findByUserAndBook_Id(user, request.getBookId())
                .map(existing -> {
                    int newQty = existing.getQuantity() + request.getQuantity();
                    if (book.getStock() < newQty) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Insufficient stock for book: " + book.getTitle());
                    }
                    existing.setQuantity(newQty);
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .book(book)
                        .quantity(request.getQuantity())
                        .unitPrice(book.getPrice())
                        .build());

        cartItemRepository.save(cartItem);
        return getCart(email);
    }

    /**
     * Updates the quantity of a specific cart item.
     */
    @Transactional
    public CartResponse updateItem(String email, Long itemId, CartItemRequest request) {
        User user = findUserByEmail(email);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart item not found with id: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This cart item does not belong to you");
        }

        if (cartItem.getBook().getStock() < request.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Insufficient stock for book: " + cartItem.getBook().getTitle());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return getCart(email);
    }

    /**
     * Removes a specific item from the cart.
     */
    @Transactional
    public void removeItem(String email, Long itemId) {
        User user = findUserByEmail(email);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart item not found with id: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This cart item does not belong to you");
        }

        cartItemRepository.delete(cartItem);
    }

    // ── Private helpers ──────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .book(toBookResponse(item.getBook()))
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getSubTotal())
                .build();
    }

    private BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .stock(book.getStock())
                .description(book.getDescription())
                .category(CategoryResponse.builder()
                        .id(book.getCategory().getId())
                        .name(book.getCategory().getName())
                        .build())
                .build();
    }
}
