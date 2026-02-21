package com.bookshop.repository;

import com.bookshop.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAllByOrderByIdDesc(Pageable pageable);
}