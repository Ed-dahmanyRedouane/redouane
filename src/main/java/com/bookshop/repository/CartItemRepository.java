package com.bookshop.repository;

import com.bookshop.entity.CartItem;
import com.bookshop.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(UserAccount user);

    Optional<CartItem> findByUserAndBook_Id(UserAccount user, Long bookId);
}